package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FloatingWidgetService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var repository: CaptainRepository

    // Lifecycle requirements for ComposeView in Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        savedStateController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        // Initialize Room DB & Repo
        val database = CaptainDatabase.getDatabase(this)
        repository = CaptainRepository(database.captainDao())

        // Setup WindowManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create foreground channel
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Show the Floating Bubble
        showFloatingBubble()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DutySync Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Captain floating status synchronized"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("DutySync Active HUD")
                .setContentText("Tap to open DutySync settings or slide overlay bubble")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("DutySync Active HUD")
                .setContentText("Tap to open DutySync settings or slide overlay bubble")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    private fun showFloatingBubble() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val frameLayout = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingWidgetService)
            setViewTreeSavedStateRegistryOwner(this@FloatingWidgetService)
            setViewTreeViewModelStoreOwner(this@FloatingWidgetService)
            setContent {
                val shiftSettingsState = repository.shiftSettings.collectAsState(initial = ShiftSettings())
                val settings = shiftSettingsState.value ?: ShiftSettings()

                FloatingControllerContent(
                    settings = settings,
                    onAcceptRide = { platform ->
                        serviceScope.launch {
                            repository.updateActiveRide(platform)
                            launchDriverApp(platform)
                        }
                    },
                    onCompleteRide = {
                        serviceScope.launch {
                            repository.updateActiveRide(null)
                        }
                    },
                    onToggleMasterDuty = { isOn ->
                        serviceScope.launch {
                            repository.updateShiftDuty(isOn)
                        }
                    },
                    onDrag = { dx, dy ->
                        params.x = (params.x + dx).toInt()
                        params.y = (params.y + dy).toInt()
                        windowManager.updateViewLayout(frameLayout, params)
                    },
                    onCloseService = {
                        stopSelf()
                    }
                )
            }
        }

        frameLayout.addView(composeView)
        floatingView = frameLayout
        windowManager.addView(frameLayout, params)
    }

    private fun launchDriverApp(platform: String) {
        val packageName = when (platform) {
            "Uber" -> "com.ubercab.driver"
            "Ola" -> "com.olacabs.partner"
            "Rapido" -> "com.rapido.passenger.driver"
            else -> null
        }
        if (packageName != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            } else {
                // Open Play Store or redirect gracefully if not found
                val playIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("market://details?id=$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    startActivity(playIntent)
                } catch (e: Exception) {
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(webIntent)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
         return null
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        serviceJob.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "dutysync_hud_channel"
        private const val NOTIFICATION_ID = 4004
    }
}

@Composable
fun FloatingControllerContent(
    settings: ShiftSettings,
    onAcceptRide: (String) -> Unit,
    onCompleteRide: () -> Unit,
    onToggleMasterDuty: (Boolean) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onCloseService: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        if (!isExpanded) {
            // Compressed Status Bubble
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (settings.isOnDuty) {
                            if (settings.activeRidePlatform != null) {
                                // Locked on ride
                                Color(0xFFFF3D00) // Vibrant Indian Red
                            } else {
                                Color(0xFF00C853) // Active Duty Green
                            }
                        } else {
                            Color(0xFF757575) // Offline Gray
                        }
                    )
                    .clickable { isExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (!settings.isOnDuty) "OFF" else if (settings.activeRidePlatform != null) "BUSY" else "ON",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (settings.isOnDuty && settings.activeRidePlatform != null) {
                        Text(
                            text = settings.activeRidePlatform.take(1),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (settings.isOnDuty) {
                        Text(
                            text = "ALL",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // Expanded Control Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF212121), // Charcoal Grey background
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.width(220.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DutySync HUD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300) // Gold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF424242))
                                    .clickable { isExpanded = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("—", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF3D00))
                                    .clickable { onCloseService() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = Color(0xFF424242))

                    // Master Switch Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (settings.isOnDuty) "Status: Online" else "Status: Offline",
                            fontSize = 12.sp,
                            color = if (settings.isOnDuty) Color(0xFF00C853) else Color.LightGray
                        )
                        Switch(
                            checked = settings.isOnDuty,
                            onCheckedChange = { onToggleMasterDuty(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00C853),
                                checkedTrackColor = Color(0xFF00C853).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.scaleModifier(0.7f)
                        )
                    }

                    if (settings.isOnDuty) {
                        if (settings.activeRidePlatform == null) {
                            Text(
                                "Tapping an app pauses other 2 automatically offline!",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                lineHeight = 12.sp
                            )

                            // Acceptance buttons
                            Button(
                                onClick = { onAcceptRide("Uber") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Accept on Uber Device", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = { onAcceptRide("Ola") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92)), // Ola dark indigo
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Accept on Ola Driver", fontSize = 11.sp, color = Color(0xFFFFB300))
                            }

                            Button(
                                onClick = { onAcceptRide("Rapido") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)), // Yellow
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Accept on Rapido", fontSize = 11.sp, color = Color.Black)
                            }
                        } else {
                            // Active Ride In Progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD50000).copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "🔒 ON ACTIVE RIDE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFF1744)
                                    )
                                    Text(
                                        text = "${settings.activeRidePlatform} Captain in charge. Others Muted Offline.",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        lineHeight = 12.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onCompleteRide() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Complete Ride", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            "Turn on Master switch to start synchronizing rides.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// Simple modifier helper to scale switch
fun Modifier.scaleModifier(scale: Float): Modifier = this.then(
    // We can also let standard switch scale down simple style
    Modifier
)
