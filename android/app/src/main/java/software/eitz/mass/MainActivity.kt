package software.eitz.mass

import androidx.compose.ui.layout.boundsInWindow
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.text.font.Font
import android.app.ActivityOptions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.ui.graphics.Brush

import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.Settings

import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.PhotoCamera

import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import software.eitz.mass.data.AppRepository
import software.eitz.mass.ui.screens.AppSelectionScreen
import software.eitz.mass.ui.screens.SettingsScreen

val MassFont = FontFamily(
    Font(R.font.courier_prime_regular)
)

class MainActivity : ComponentActivity() {

    private lateinit var appRepository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        appRepository = AppRepository(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF1EDE4)
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith
                            fadeOut(animationSpec = tween(250))
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Home -> MassHomeScreen(
                                appRepository = appRepository,
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onNavigateToAppSelection = { tileId ->
                                    currentScreen = Screen.AppSelection(tileId)
                                }
                            )

                            is Screen.Settings -> SettingsScreen(
                                appRepository = appRepository,
                                allTiles = getAllTiles(),
                                onTileClick = { tileId ->
                                    currentScreen = Screen.AppSelection(tileId)
                                },
                                onBack = { currentScreen = Screen.Home }
                            )

                            is Screen.AppSelection -> {
                                val scope = rememberCoroutineScope()
                                AppSelectionScreen(
                                    tileId = screen.tileId,
                                    onAppSelected = { packageName ->
                                        scope.launch {
                                            appRepository.saveAssignment(screen.tileId, packageName)
                                            currentScreen = Screen.Home
                                        }
                                    },
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object Settings : Screen()
    data class AppSelection(val tileId: String) : Screen()
}

fun getAllTiles() = listOf(
    AppItem("email", "Email", Icons.Outlined.Email),
    AppItem("calendar", "Calendar", Icons.Outlined.CalendarMonth),
    AppItem("clock", "Clock", Icons.Outlined.AccessTime),
    AppItem("notes", "Notes", Icons.Outlined.NoteAlt),
    AppItem("banking", "Banking", Icons.Outlined.AccountBalance),
    AppItem("2fa", "2FA Auth", Icons.Outlined.Security),
    AppItem("weather", "Weather", Icons.Outlined.WbCloudy),
    AppItem("calls", "Calls", Icons.Outlined.Call),
    AppItem("messages", "Messages", Icons.AutoMirrored.Outlined.Message),
    AppItem("camera", "Camera", Icons.Outlined.PhotoCamera)
)

data class AppItem(
    val id: String,
    val name: String,
    val icon: ImageVector
)

@Composable
fun MassHomeScreen(
    appRepository: AppRepository,
    onNavigateToSettings: () -> Unit,
    onNavigateToAppSelection: (String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val currentDate by flow {
        while (true) {
            emit(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))
            delay(1000 * 60) // Update every minute
        }
    }.collectAsState(initial = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))

    val apps = listOf(
        AppItem("email", "Email", Icons.Outlined.Email),
        AppItem("calendar", "Calendar", Icons.Outlined.CalendarMonth),
        AppItem("clock", "Clock", Icons.Outlined.AccessTime),
        AppItem("notes", "Notes", Icons.Outlined.NoteAlt),
        AppItem("banking", "Banking", Icons.Outlined.AccountBalance),
        AppItem("2fa", "2FA Auth", Icons.Outlined.Security),
        AppItem("weather", "Weather", Icons.Outlined.WbCloudy),
        AppItem("settings", "Settings", Icons.Outlined.Settings)
    )

    BackHandler {
        // Do nothing to prevent app from closing on Home screen
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3EFE6),
                        Color(0xFFE8E2D6),
                        Color(0xFFEDE7DB)
                    )
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = statusBarPadding + 48.dp, bottom = navBarPadding + 20.dp)
    ) {

        Text(
            text = "Maß",
            fontSize = 34.sp,
            fontFamily = MassFont,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1A)
        )

        Spacer(modifier = Modifier.height(18.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFF5F5A50)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = currentDate,
            fontSize = 17.sp,
            fontFamily = MassFont,
            color = Color(0xFF1E1E1A)
        )

        Spacer(modifier = Modifier.height(36.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {

            items(apps) { app ->
                val assignment by appRepository.getAssignmentFlow(app.id).collectAsState(initial = null)
                var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

                Box(
                    modifier = Modifier
                        .height(86.dp)
                        .border(
                            width = 0.8.dp,
                            color = Color(0xFF8A857B)
                        )
                        .onGloballyPositioned { layoutCoordinates = it }
                        .clickable {
                            if (app.id == "settings") {
                                onNavigateToSettings()
                            } else {
                                if (assignment != null) {
                                    val bounds = layoutCoordinates?.boundsInWindow()
                                    if (bounds != null) {
                                        launchApp(
                                            context,
                                            assignment!!,
                                            view,
                                            Rect(
                                                bounds.left.toInt(),
                                                bounds.top.toInt(),
                                                bounds.right.toInt(),
                                                bounds.bottom.toInt()
                                            )
                                        )
                                    } else {
                                        launchApp(context, assignment!!)
                                    }
                                } else {
                                    onNavigateToAppSelection(app.id)
                                }
                            }
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp),
                    ) {

                        Icon(
                            imageVector = app.icon,
                            contentDescription = app.name,
                            tint = Color(0xFF2A2925),
                            modifier = Modifier.size(22.dp).padding(bottom = 2.dp)
                        )

                        Text(
                            text = app.name,
                            fontSize = 18.sp,
                            fontFamily = MassFont,
                            color = Color(0xFF1E1E1A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(
            thickness = 0.8.dp,
            color = Color(0xFF5F5A50)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val callsAssignment by appRepository.getAssignmentFlow("calls").collectAsState(initial = null)
            val messagesAssignment by appRepository.getAssignmentFlow("messages").collectAsState(initial = null)
            val cameraAssignment by appRepository.getAssignmentFlow("camera").collectAsState(initial = null)

            var callsCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
            var messagesCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
            var cameraCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

            BottomApp(
                "Calls",
                Icons.Outlined.Call,
                modifier = Modifier
                    .onGloballyPositioned { callsCoords = it }
                    .clickable {
                        if (callsAssignment != null) {
                            val bounds = callsCoords?.boundsInWindow()
                            if (bounds != null) {
                                launchApp(
                                    context,
                                    callsAssignment!!,
                                    view,
                                    Rect(
                                        bounds.left.toInt(),
                                        bounds.top.toInt(),
                                        bounds.right.toInt(),
                                        bounds.bottom.toInt()
                                    )
                                )
                            } else {
                                launchApp(context, callsAssignment!!)
                            }
                        } else onNavigateToAppSelection("calls")
                    }
            )

            BottomApp(
                "Messages",
                Icons.AutoMirrored.Outlined.Message,
                modifier = Modifier
                    .onGloballyPositioned { messagesCoords = it }
                    .clickable {
                        if (messagesAssignment != null) {
                            val bounds = messagesCoords?.boundsInWindow()
                            if (bounds != null) {
                                launchApp(
                                    context,
                                    messagesAssignment!!,
                                    view,
                                    Rect(
                                        bounds.left.toInt(),
                                        bounds.top.toInt(),
                                        bounds.right.toInt(),
                                        bounds.bottom.toInt()
                                    )
                                )
                            } else {
                                launchApp(context, messagesAssignment!!)
                            }
                        } else onNavigateToAppSelection("messages")
                    }
            )

            BottomApp(
                "Camera",
                Icons.Outlined.PhotoCamera,
                modifier = Modifier
                    .onGloballyPositioned { cameraCoords = it }
                    .clickable {
                        if (cameraAssignment != null) {
                            val bounds = cameraCoords?.boundsInWindow()
                            if (bounds != null) {
                                launchApp(
                                    context,
                                    cameraAssignment!!,
                                    view,
                                    Rect(
                                        bounds.left.toInt(),
                                        bounds.top.toInt(),
                                        bounds.right.toInt(),
                                        bounds.bottom.toInt()
                                    )
                                )
                            } else {
                                launchApp(context, cameraAssignment!!)
                            }
                        } else onNavigateToAppSelection("camera")
                    }
            )
        }
    }
}

private fun launchApp(
    context: android.content.Context,
    packageName: String,
    view: View? = null,
    sourceBounds: Rect? = null
) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: run {
        Toast.makeText(context, "Could not launch $packageName", Toast.LENGTH_SHORT).show()
        return
    }

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

    val options = if (view != null && sourceBounds != null) {
        intent.sourceBounds = sourceBounds
        ActivityOptions.makeScaleUpAnimation(
            view,
            sourceBounds.left,
            sourceBounds.top,
            sourceBounds.width(),
            sourceBounds.height()
        )
    } else {
        ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
    }

    context.startActivity(intent, options.toBundle())
}

@Composable
fun BottomApp(
    name: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {

        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = Color(0xFF1E1E1A),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            fontSize = 14.sp,
            fontFamily = MassFont,
            color = Color(0xFF1E1E1A)
        )
    }
}