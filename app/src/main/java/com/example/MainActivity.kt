package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.service.FloatingWidgetService
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DutyViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: DutyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

// Local translation system dictionary
object Loc {
    fun get(key: String, lang: String): String {
        val en = mapOf(
            "app_title" to "DutySync",
            "dashboard" to "Dashboard",
            "profiles" to "Captain IDs",
            "earnings" to "Trips & Cash",
            "hotspots" to "Hyd Hotspots",
            "go_duty" to "GO ON SHIFT",
            "go_duty_desc" to "Start Ola, Uber, and Rapido sync",
            "off_duty" to "GO OFFLINE",
            "off_duty_desc" to "Complete your shift and stop all pings",
            "active_ride_alert" to "LOCKDOWN MODE ACTIVE",
            "active_ride_desc" to "Remaining apps have been automatically switched OFFLINE to protect your acceptance rating. Commencing active duty trip.",
            "add_profile" to "Register Captain ID",
            "no_profiles" to "No Captain Profiles Registered. Bind your IDs below to sync status!",
            "demand_multiplier" to "Demand Surge Rate",
            "helmet_warning" to "Safety Rule: Wearing helmet is mandatory for Captain & Passenger in Hyderabad. Heavy fines apply at Ameerpet & Secunderabad!",
            "overlay_permission" to "Floating HUD overlay",
            "overlay_desc" to "Draw a floating button on top of Uber, Ola, or Rapido to change status directly on-the-go!",
            "start_hud" to "Launch Floating HUD",
            "stop_hud" to "Hide Floating HUD",
            "status_online" to "ONLINE & READY",
            "status_offline" to "OFFLINE",
            "status_paused" to "PAUSED OFFLINE",
            "accept_on" to "Accept booking on",
            "complete_ride" to "Complete Ride",
            "select_lang" to "Select Language:"
        )
        val te = mapOf(
            "app_title" to "డ్యూటీసింక్",
            "dashboard" to "స్మార్ట్ డ్యూటీ",
            "profiles" to "క్యాప్టెన్ ఐడీలు",
            "earnings" to "సంపాదనలు & ట్రిప్పులు",
            "hotspots" to "హైదరాబాద్ జోన్లు",
            "go_duty" to "షిఫ్ట్ ప్రారంభించు",
            "go_duty_desc" to "ఓలా, ఉబెర్, రాపిడో ఆన్‌లైన్ జరుపుము",
            "off_duty" to "ఆఫ్‌లైన్ వెళ్లు",
            "off_duty_desc" to "రైడులను ఆపివేసి డ్యూటీ ముగించండి",
            "active_ride_alert" to "లాక్‌డౌన్ మోడ్ ఆన్ చేయబడింది",
            "active_ride_desc" to "ఇతర యాప్‌లు స్వయంచాలకంగా హోల్డ్‌లో ఉంచబడ్డాయి, తద్వారా ఇతర రైడుల నుండి ఎలాంటి ఇబ్బంది ఉండదు.",
            "add_profile" to "మరో క్యాప్టెన్ ఐడీ నమోదుచేయి",
            "no_profiles" to "క్యాప్టెన్ ప్రొఫైల్‌లు లేవు. డ్యూటీ సమకాలీకరించడానికి ఐడీ నమోదు చేయండి!",
            "demand_multiplier" to "డిమాండ్ సర్జ్ రేటు",
            "helmet_warning" to "హెల్మెట్టే ప్రాణ రక్ష! సికింద్రాబాద్ మినహా హైదరాబాద్ అంతటా జరిమానాలు పడవచ్చు, జాగ్రత్త!",
            "overlay_permission" to "స్క్రీన్ పైన ఫ్లోటింగ్ బటన్",
            "overlay_desc" to "ఉబెర్ లేదా ఓలా యాప్స్ పైన ఫ్లోటింగ్ కంట్రోలర్ ద్వారా నేరుగా డ్యూటీ మోడ్ మార్చండి!",
            "start_hud" to "ఫ్లోటింగ్ బటన్ ప్రారంభించు",
            "stop_hud" to "ఫ్లోటింగ్ బటన్ తీసివేయి",
            "status_online" to "ఆన్‌లైన్ - రైడ్స్ సిద్ధం",
            "status_offline" to "ఆఫ్‌లైన్",
            "status_paused" to "తాత్కాలికంగా ఆగినది",
            "accept_on" to "రైడ్ అంగీకరించు",
            "complete_ride" to "రైడ్ పూర్తిచేయి",
            "select_lang" to "భాషను ఎంచుకోండి:"
        )
        val hi = mapOf(
            "app_title" to "ड्यूटीसिंक",
            "dashboard" to "स्मार्ट ड्यूटी",
            "profiles" to "कैप्टन आईडी",
            "earnings" to "कमाई और ट्रिप्स",
            "hotspots" to "हॉटस्पॉट ज़ोन",
            "go_duty" to "ड्यूटी चालू करें",
            "go_duty_desc" to "ओला, उबर, रैपिडो ऑनलाइन सिंक करें",
            "off_duty" to "ऑफ़लाइन जाएं",
            "off_duty_desc" to "ड्यूटी बंद करें और बुकिंग बंद करें",
            "active_ride_alert" to "लॉकडाउन मोड सक्रिय",
            "active_ride_desc" to "अन्य दो ऐप्स को म्यूट/ऑफलाइन कर दिया गया है ताकि अन्य बुकिंग्स आपको परेशान न करें।",
            "add_profile" to "कैप्टन प्रोफाइल जोड़ें",
            "no_profiles" to "कोई कैप्टन आईडी पंजीकृत नहीं है। सिंक करने के लिए नीचे अपनी आईडी जोड़ें!",
            "demand_multiplier" to "डिमांड सर्ज रेट",
            "helmet_warning" to "सुरक्षा नियम: हैदराबाद में कैप्टन और पैसेंजर दोनों के लिए हेलमेट अनिवार्य है। अमीरपेट में सख्त चेकिंग!",
            "overlay_permission" to "फ्लोटिंग HUD ओवरले",
            "overlay_desc" to "ड्राइविंग करते समय आसानी से अन्य ऐप्स पर स्टेटस बदलने के लिए फ्लोटिंग बटन चालू करें!",
            "start_hud" to "फ्लोटिंग HUD शुरू करें",
            "stop_hud" to "फ्लोटिंग HUD बंद करें",
            "status_online" to "ऑनलाइन - तैयार",
            "status_offline" to "ऑफ़लाइन",
            "status_paused" to "रुका हुआ (ऑफ़लाइन)",
            "accept_on" to "बुकिंग स्वीकार करें",
            "complete_ride" to "सवारी पूरी करें",
            "select_lang" to "भाषा चुनें:"
        )
        val m = when (lang) {
            "te" -> te
            "hi" -> hi
            else -> en
        }
        return m[key] ?: en[key] ?: key
    }
}

data class AvailableRide(
    val id: String,
    val platform: String,
    val fromLocation: String,
    val toLocation: String,
    val fare: Double,
    val durationMin: Int,
    val distanceKm: Double
)

fun generateRandomRide(platform: String? = null): AvailableRide {
    val platforms = listOf("Uber", "Ola", "Rapido")
    val selectedPlatform = platform ?: platforms.random()
    
    val locations = listOf(
        "Gachibowli DLF", "Hitech City Metro", "Secunderabad Station", 
        "Begumpet Airport", "Madhapur Cyber Towers", "Jubilee Hills Rd 36", 
        "Ameerpet Metro", "Charminar", "Kukatpally Forum Mall", "Mehdipatnam Circle"
    )
    
    val from = locations.random()
    var to = locations.random()
    while (from == to) {
        to = locations.random()
    }
    
    val distance = ((30..220).random() / 10.0) // 3.0 to 22.0 km
    val fare = (distance * 15 + (40..80).random()).coerceIn(60.0, 500.0)
    val finalFare = (java.lang.Math.round(fare / 5.0) * 5).toDouble()
    val duration = (distance * 2 + (5..12).random()).toInt()
    
    return AvailableRide(
        id = java.util.UUID.randomUUID().toString(),
        platform = selectedPlatform,
        fromLocation = from,
        toLocation = to,
        fare = finalFare,
        durationMin = duration,
        distanceKm = distance
    )
}

fun launchDriverApp(context: Context, platform: String) {
    val packageName = when (platform) {
        "Uber" -> "com.ubercab.driver"
        "Ola" -> "com.olacabs.partner"
        "Rapido" -> "com.rapido.passenger.driver"
        else -> null
    }
    if (packageName != null) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val playIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(playIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }
    }
}

enum class HomeTab {
    DASHBOARD, PROFILES, EARNINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DutyViewModel) {
    val context = LocalContext.current
    val shiftSettings by viewModel.shiftSettings.collectAsStateWithLifecycle()
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val trips by viewModel.allTrips.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(HomeTab.DASHBOARD) }

    // Editorial theme background colors
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            EditorialBackground,
            Color(0xFFEFF1F9)
        )
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Loc.get("app_title", shiftSettings.selectedLanguage).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = Indigo950,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 24.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (shiftSettings.isOnDuty) Green500 else Red500)
                                    )
                                    Text(
                                        text = "HYDERABAD • MADHAPUR ZONE",
                                        fontSize = 10.sp,
                                        color = Slate500,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // User Profile Avatar PK
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Indigo100)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PK",
                                    fontSize = 12.sp,
                                    color = Indigo600,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = EditorialBackground,
                        titleContentColor = Indigo950
                    )
                )

                // High-fidelity integrated Language Bar underneath header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Slate100)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Loc.get("select_lang", shiftSettings.selectedLanguage).uppercase(),
                        fontSize = 9.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("en" to "EN", "te" to "తె", "hi" to "हि").forEach { (code, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (shiftSettings.selectedLanguage == code) Indigo600 else Slate100
                                    )
                                    .clickable { viewModel.selectLanguage(code) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (shiftSettings.selectedLanguage == code) Color.White else Slate900
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, Slate200)
            ) {
                NavigationBarItem(
                    selected = activeTab == HomeTab.DASHBOARD,
                    onClick = { activeTab = HomeTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { 
                        Text(
                            text = Loc.get("dashboard", shiftSettings.selectedLanguage).uppercase(), 
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Indigo600,
                        selectedTextColor = Indigo600,
                        indicatorColor = Indigo100,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    )
                )
                NavigationBarItem(
                    selected = activeTab == HomeTab.PROFILES,
                    onClick = { activeTab = HomeTab.PROFILES },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profiles") },
                    label = { 
                        Text(
                            text = Loc.get("profiles", shiftSettings.selectedLanguage).uppercase(), 
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Indigo600,
                        selectedTextColor = Indigo600,
                        indicatorColor = Indigo100,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    )
                )
                NavigationBarItem(
                    selected = activeTab == HomeTab.EARNINGS,
                    onClick = { activeTab = HomeTab.EARNINGS },
                    icon = { Icon(Icons.Default.List, contentDescription = "Earnings") },
                    label = { 
                        Text(
                            text = Loc.get("earnings", shiftSettings.selectedLanguage).uppercase(), 
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Indigo600,
                        selectedTextColor = Indigo600,
                        indicatorColor = Indigo100,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                HomeTab.DASHBOARD -> DashboardScreen(viewModel, shiftSettings, profiles, trips)
                HomeTab.PROFILES -> ProfilesScreen(viewModel, shiftSettings, profiles)
                HomeTab.EARNINGS -> EarningsScreen(viewModel, shiftSettings, trips)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DutyViewModel,
    settings: ShiftSettings,
    profiles: List<CaptainProfile>,
    trips: List<TripLog>
) {
    val context = LocalContext.current
    var isSimulateActiveRideDialogShow by remember { mutableStateOf(false) }

    var isOverlayPermGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }

    val totalEarnings = remember(trips) { trips.sumOf { it.fareAmount } }
    val totalRides = remember(trips) { trips.size }

    // Dynamic Live Offer bookings feed - entirely self-contained, lightweight state
    var availableRides by remember {
        mutableStateOf(
            listOf(
                generateRandomRide("Uber"),
                generateRandomRide("Ola"),
                generateRandomRide("Rapido")
            )
        )
    }

    var acceptedRideDetails by remember { mutableStateOf<AvailableRide?>(null) }

    // Periodically swaps unaccepted offers to feel realistic and alive, zero battery/lag overhead
    LaunchedEffect(settings.isOnDuty, settings.activeRidePlatform) {
        if (settings.isOnDuty && settings.activeRidePlatform == null) {
            while (true) {
                kotlinx.coroutines.delay(12000)
                if (availableRides.isNotEmpty()) {
                    val indexToReplace = (0 until availableRides.size).random()
                    val freshRide = generateRandomRide(availableRides[indexToReplace].platform)
                    availableRides = availableRides.toMutableList().apply {
                        set(indexToReplace, freshRide)
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Core Switch Hero Panel
        item {
            
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Indigo600),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "CURRENT STATUS",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (settings.isOnDuty) "ON DUTY" else "OFFLINE",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Earnings badging
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EARNINGS TODAY",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format("₹%.0f", totalEarnings),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Rides Done badging
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "RIDES DONE",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$totalRides",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Main Toggle switch
                    Button(
                        onClick = { viewModel.toggleMasterDuty(!settings.isOnDuty) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.isOnDuty) Color(0xFFEF4444) else Color(0xFF22C55E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.isOnDuty) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Switch status",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (settings.isOnDuty)
                                Loc.get("off_duty", settings.selectedLanguage).uppercase()
                            else
                                Loc.get("go_duty", settings.selectedLanguage).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (settings.isOnDuty && settings.dutyStartTimestamp > 0L) {
                        val duration = System.currentTimeMillis() - settings.dutyStartTimestamp
                        val hr = duration / (1000 * 60 * 60)
                        val min = (duration / (1000 * 60)) % 60
                        Text(
                            text = String.format("Active Shift: %02d hour %02d min", hr, min),
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Live App Status Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACTIVE INTEGRATIONS",
                    color = Slate500,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Uber Column
                    DashboardAppCell(
                        modifier = Modifier.weight(1f),
                        platform = "Uber",
                        themeColor = Color.Black,
                        accentColor = Color.White,
                        isDutyOn = settings.isOnDuty,
                        isActiveRide = settings.activeRidePlatform == "Uber",
                        isAnyActiveRide = settings.activeRidePlatform != null,
                        language = settings.selectedLanguage
                    )

                    // Ola Column
                    DashboardAppCell(
                        modifier = Modifier.weight(1f),
                        platform = "Ola",
                        themeColor = Color(0xFFA3E635), 
                        accentColor = Color.Black,
                        isDutyOn = settings.isOnDuty,
                        isActiveRide = settings.activeRidePlatform == "Ola",
                        isAnyActiveRide = settings.activeRidePlatform != null,
                        language = settings.selectedLanguage
                    )

                    // Rapido Column
                    DashboardAppCell(
                        modifier = Modifier.weight(1f),
                        platform = "Rapido",
                        themeColor = Color(0xFFFACC15),
                        accentColor = Color.Black,
                        isDutyOn = settings.isOnDuty,
                        isActiveRide = settings.activeRidePlatform == "Rapido",
                        isAnyActiveRide = settings.activeRidePlatform != null,
                        language = settings.selectedLanguage
                    )
                }
            }
        }

        // LockDown Alert Banner (if ride accepted)
        if (settings.isOnDuty && settings.activeRidePlatform != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "${Loc.get("active_ride_alert", settings.selectedLanguage)} (${settings.activeRidePlatform?.uppercase()})",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Loc.get("active_ride_desc", settings.selectedLanguage),
                                color = Slate900,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { isSimulateActiveRideDialogShow = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Loc.get("complete_ride", settings.selectedLanguage), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Sync Simulation Trigger Panel (When online and no ride)
        if (settings.isOnDuty && settings.activeRidePlatform == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📡",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "LIVE MULTI-APP RADAR",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Indigo950,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                    Text("SCANNING", color = Color(0xFF22C55E), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Text(
                            text = "Incoming live rides from Ola, Uber, and Rapido are synchronized below. Accepting a booking automatically switches other apps offline & mutes notifications.",
                            fontSize = 11.sp,
                            color = Slate500,
                            lineHeight = 15.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            availableRides.forEach { ride ->
                                LiveOfferCard(
                                    ride = ride,
                                    onAccept = {
                                        acceptedRideDetails = ride
                                        viewModel.triggerActiveRide(ride.platform)
                                        launchDriverApp(context, ride.platform)
                                        Toast.makeText(context, "Muting other apps! Redirecting to ${ride.platform} Driver...", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Overlay HUD Controls Configuration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Menu, contentDescription = "Overlay", tint = Indigo600)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Loc.get("overlay_permission", settings.selectedLanguage),
                                fontWeight = FontWeight.Bold,
                                color = Indigo950,
                                fontSize = 13.sp
                            )
                        }

                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isOverlayPermGranted) Green500.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOverlayPermGranted) "ACTIVE" else "REQUIRED",
                                color = if (isOverlayPermGranted) Green500 else Color(0xFFEF4444),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Text(
                        text = Loc.get("overlay_desc", settings.selectedLanguage),
                        fontSize = 11.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!isOverlayPermGranted) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Grant 'DutySync' overlay authority & return!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Permission", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, FloatingWidgetService::class.java)
                                    context.startService(intent)
                                    Toast.makeText(context, "DutySync HUD Floating Bubble Launched!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(Loc.get("start_hud", settings.selectedLanguage).uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    context.stopService(Intent(context, FloatingWidgetService::class.java))
                                    Toast.makeText(context, "HUD Floating Bubble Hidden", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate100),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(Loc.get("stop_hud", settings.selectedLanguage).uppercase(), color = Slate900, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Traffic Safety Reminder Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFACC15).copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, Color(0xFFFACC15)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Helmet Warning",
                        tint = Color(0xFFEAB308)
                    )
                    Text(
                        text = Loc.get("helmet_warning", settings.selectedLanguage),
                        fontSize = 11.sp,
                        color = Slate900,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (isSimulateActiveRideDialogShow) {
        DialogSimulateTripLog(
            platformName = settings.activeRidePlatform ?: "Uber",
            initialFrom = acceptedRideDetails?.fromLocation,
            initialTo = acceptedRideDetails?.toLocation,
            initialFare = acceptedRideDetails?.fare,
            initialDuration = acceptedRideDetails?.durationMin,
            onDismiss = { isSimulateActiveRideDialogShow = false },
            onConfirmLog = { from, to, fare, duration ->
                viewModel.completeActiveRide(from, to, fare, duration)
                acceptedRideDetails = null
                isSimulateActiveRideDialogShow = false
                Toast.makeText(context, "Ride completed & logged to Hyderabad ledger! Syncing others ONLINE.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun DashboardAppCell(
    modifier: Modifier = Modifier,
    platform: String,
    themeColor: Color,
    accentColor: Color,
    isDutyOn: Boolean,
    isActiveRide: Boolean,
    isAnyActiveRide: Boolean,
    language: String
) {
    val cellBg = if (!isDutyOn) {
        Color.White
    } else if (isActiveRide) {
        themeColor
    } else if (isAnyActiveRide) {
        Color.White
    } else {
        themeColor
    }

    val textColor = if (!isDutyOn) {
        Slate900
    } else if (isActiveRide) {
        accentColor
    } else if (isAnyActiveRide) {
        Slate500
    } else {
        accentColor
    }

    val statusText = if (!isDutyOn) {
        Loc.get("status_offline", language)
    } else if (isActiveRide) {
        "LOCK ACTIVE"
    } else if (isAnyActiveRide) {
        Loc.get("status_paused", language)
    } else {
        Loc.get("status_online", language)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cellBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActiveRide && isDutyOn) Indigo600 else Slate200
        ),
        modifier = modifier.height(130.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDutyOn && (isActiveRide || !isAnyActiveRide)) accentColor else themeColor
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = platform.first().toString(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (isDutyOn && (isActiveRide || !isAnyActiveRide)) themeColor else accentColor
                )
            }

            Text(
                text = platform.uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (!isDutyOn) Slate100
                        else if (isActiveRide) Color(0xFFEF4444)
                        else if (isAnyActiveRide) Color(0xFFF59E0B)
                        else Color(0xFF22C55E)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusText.uppercase(),
                    fontSize = 8.sp,
                    color = if (!isDutyOn || isAnyActiveRide) Slate900 else Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Dialog to log simulation ride details
@Composable
fun DialogSimulateTripLog(
    platformName: String,
    initialFrom: String? = null,
    initialTo: String? = null,
    initialFare: Double? = null,
    initialDuration: Int? = null,
    onDismiss: () -> Unit,
    onConfirmLog: (String, String, Double, Int) -> Unit
) {
    val fromPreset = listOf("Gachibowli", "Hitech City", "Secunderabad", "Begumpet", "Madhapur", "Jubilee Hills")
    val toPreset = listOf("Mehdipatnam", "Charminar", "Ameerpet", "Kukatpally", "HITEX Exhibition Center", "MGBS Terminal")

    var selectedFrom by remember { mutableStateOf(initialFrom ?: fromPreset.random()) }
    var selectedTo by remember { mutableStateOf(initialTo ?: toPreset.random()) }
    var inputFare by remember { mutableStateOf(initialFare ?: (120..420).random().toDouble()) }
    var inputDuration by remember { mutableStateOf(initialDuration ?: (15..45).random()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Complete Ride Ledger Entry",
                color = Indigo950,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        },
        containerColor = Color.White,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Record fare cash collection inside Hyderabad logs. This matches real-world completed platform ride syncing.",
                    fontSize = 11.sp,
                    color = Slate500
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Indigo100)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Platform: $platformName Captain Mode", color = Indigo600, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text("From: $selectedFrom (Hyderabad)", color = Slate900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("To: $selectedTo (Hyderabad)", color = Slate900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fare Charged (₹)", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = inputFare.toString(),
                            onValueChange = { inputFare = it.toDoubleOrNull() ?: 100.0 },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Slate900, unfocusedTextColor = Slate900, focusedBorderColor = Indigo600, unfocusedBorderColor = Slate200)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Duration (Min)", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = inputDuration.toString(),
                            onValueChange = { inputDuration = it.toIntOrNull() ?: 20 },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Slate900, unfocusedTextColor = Slate900, focusedBorderColor = Indigo600, unfocusedBorderColor = Slate200)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmLog(selectedFrom, selectedTo, inputFare, inputDuration) },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Confirm & Re-Sync Online", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate500)
            }
        }
    )
}

@Composable
fun ProfilesScreen(
    viewModel: DutyViewModel,
    settings: ShiftSettings,
    profiles: List<CaptainProfile>
) {
    var isAddDialogShow by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CAPTAIN REGISTRATION CABINET",
                color = Slate500,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp
            )

            Button(
                onClick = { isAddDialogShow = true },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(Loc.get("add_profile", settings.selectedLanguage).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
        }

        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(BorderStroke(1.dp, Slate200), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Loc.get("no_profiles", settings.selectedLanguage),
                    color = Slate500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    CaptainIdCard(profile = profile, onDelete = { viewModel.deleteProfile(profile.id) })
                }
            }
        }
    }

    if (isAddDialogShow) {
        DialogAddCaptainProfile(
            onDismiss = { isAddDialogShow = false },
            onSave = { platform, badge, plate, phone ->
                viewModel.addCaptainProfile(platform, badge, plate, phone)
                isAddDialogShow = false
            }
        )
    }
}

@Composable
fun CaptainIdCard(profile: CaptainProfile, onDelete: () -> Unit) {
    val platformColors = when (profile.platformName) {
        "Uber" -> Pair(Color.Black, Color.White)
        "Ola" -> Pair(Color(0xFFA3E635), Color.Black)
        else -> Pair(Color(0xFFFACC15), Color.Black) // Rapido
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(platformColors.first),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.platformName.substring(0, 3).uppercase(),
                        color = platformColors.second,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Column {
                    Text(
                        text = "${profile.platformName} ID Badge",
                        color = Slate900,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Captain ID: ${profile.captainIdStr}",
                        color = Slate500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (profile.vehicleNumber.isNotEmpty()) {
                        Text(
                            text = "Vehicle No: ${profile.vehicleNumber}",
                            color = Indigo600,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun DialogAddCaptainProfile(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf("Ola") }
    var captainId by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Register Captain Credentials",
                color = Indigo950,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        },
        containerColor = Color.White,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Captain System Platform:", color = Slate900, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Ola", "Uber", "Rapido").forEach { plat ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedPlatform == plat) Indigo600 else Slate100)
                                .clickable { selectedPlatform = plat }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = plat,
                                color = if (selectedPlatform == plat) Color.White else Slate900,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = captainId,
                    onValueChange = { captainId = it },
                    label = { Text("Captain ID Badge Code") },
                    placeholder = { Text("e.g. HYD-OLA-9204") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedLabelColor = Indigo600,
                        unfocusedLabelColor = Slate500,
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Associated Plate / Bike No.") },
                    placeholder = { Text("e.g. TS09-EA-4903") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedLabelColor = Indigo600,
                        unfocusedLabelColor = Slate500,
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    label = { Text("Captain Register Mobile No") },
                    placeholder = { Text("e.g. +91 98480 22338") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        focusedLabelColor = Indigo600,
                        unfocusedLabelColor = Slate500,
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (captainId.isNotEmpty()) {
                        onSave(selectedPlatform, captainId, vehicleNumber, phoneNumber)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Register Profile", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate500)
            }
        }
    )
}

@Composable
fun EarningsScreen(
    viewModel: DutyViewModel,
    settings: ShiftSettings,
    trips: List<TripLog>
) {
    val totalCashToday = remember(trips) { trips.sumOf { it.fareAmount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Earnings summary HUD panel
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HYDERABAD DAILY LEDGER",
                        fontWeight = FontWeight.Black,
                        color = Slate500,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )

                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Simulate",
                        tint = Indigo600,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                // Simulate one-click automatic trip creation for easily demoing logs
                                val hyderFrom = listOf("Ameerpet Metro", "Charminar Outer Gate", "Durgam Cheruvu Cafe", "Gachibowli DLF Phase 1")
                                val hyderTo = listOf("Hitech City Cyber Towers", "Secunderabad Junction Hub", "Begumpet Airport Terminal")
                                val rates = listOf(140.0, 220.0, 310.0, 180.0, 410.0)
                                viewModel.completeActiveRide(
                                    hyderFrom.random(),
                                    hyderTo.random(),
                                    rates.random(),
                                    (12..40).random()
                                )
                            }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${"%.2f".format(totalCashToday)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate900
                        )
                        Text(
                            text = "Collected today in cash/GPay",
                            color = Slate500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Shift stats target info
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Daily Target Met",
                            color = Slate500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%.0f".format((totalCashToday / 1500.0) * 100)}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Indigo600
                        )
                    }
                }

                // Simple linear progress bar representing target goal
                LinearProgressIndicator(
                    progress = (totalCashToday / 1500.0).toFloat().coerceIn(0f, 1f),
                    color = Indigo600,
                    trackColor = Slate100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Text(
                    text = "* Click 'Refresh' symbol above to quickly simulate and log random rides!",
                    fontSize = 10.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Ledger Trips Logs Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHIFT HISTORY",
                color = Slate500,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "${trips.size} Trips Completed",
                color = Slate900,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(BorderStroke(1.dp, Slate200), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No completed trips found. Earn today by accepting rides from the sync engine!",
                    color = Slate500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trips, key = { it.id }) { trip ->
                    TripRecordCard(trip = trip, onDelete = { viewModel.deleteTripLog(trip.id) })
                }
            }
        }
    }
}

@Composable
fun TripRecordCard(trip: TripLog, onDelete: () -> Unit) {
    val platformColors = when (trip.platformName) {
        "Uber" -> Pair(Color.Black, Color.White)
        "Ola" -> Pair(Color(0xFFA3E635), Color.Black)
        else -> Pair(Color(0xFFFACC15), Color.Black) // Rapido
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                spacing = 10.dp,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform initial badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(platformColors.first),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trip.platformName.substring(0, 1),
                        color = platformColors.second,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trip.platformName,
                            fontWeight = FontWeight.Black,
                            color = Slate900,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(trip.timestamp))
                        Text(
                            text = "@ $timeFormatted",
                            fontSize = 10.sp,
                            color = Slate500,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${trip.startLocation} → ${trip.endLocation}",
                        color = Slate900,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${trip.durationMinutes} mins elapsed",
                        color = Slate500,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, spacing = 8.dp) {
                Text(
                    text = "₹${"%.0f".format(trip.fareAmount)}",
                    fontWeight = FontWeight.Black,
                    color = Green500,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Log",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// High-fidelity card representing an active booking on the live multi-app radar
@Composable
fun LiveOfferCard(
    ride: AvailableRide,
    onAccept: () -> Unit
) {
    val platformColor = when (ride.platform) {
        "Uber" -> Color.Black
        "Ola" -> Color(0xFFA3E635)
        "Rapido" -> Color(0xFFFACC15)
        else -> Indigo600
    }
    val platformTextColor = when (ride.platform) {
        "Uber" -> Color.White
        "Ola" -> Color.Black
        "Rapido" -> Color.Black
        else -> Color.White
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EditorialBackground),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header showing platform badge and estimated fare
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(platformColor)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = ride.platform.uppercase(),
                            color = platformTextColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "⚡ Incoming Booking",
                        color = Indigo600,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = "₹${"%.0f".format(ride.fare)}",
                    fontWeight = FontWeight.Black,
                    color = Green500,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Route locations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "route",
                    tint = Slate500,
                    modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
                )
                Column {
                    Text(
                        text = "FROM: ${ride.fromLocation}",
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TO: ${ride.toLocation}",
                        fontSize = 12.sp,
                        color = Indigo950,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trip metadata (dist and duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📏 ${"%.1f".format(ride.distanceKm)} KM",
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⏱️ ${ride.durationMin} MINS",
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ACCEPT & GO",
                        color = platformTextColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// Simple row alternative support with spacing for some compose versions
@Composable
fun Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (horizontalArrangement == Arrangement.Start) Arrangement.spacedBy(spacing) else horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}
