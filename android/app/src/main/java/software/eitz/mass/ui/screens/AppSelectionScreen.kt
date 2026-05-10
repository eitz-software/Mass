package software.eitz.mass.ui.screens

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import software.eitz.mass.MassFont
import software.eitz.mass.util.AppDiscovery

data class LaunchableApp(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    tileId: String,
    onAppSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAllApps by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<LaunchableApp>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(tileId, showAllApps) {
        isLoading = true
        apps = withContext(Dispatchers.IO) {
            if (showAllApps) {
                AppDiscovery.getAllLaunchableApps(context)
            } else {
                val filtered = AppDiscovery.getFilteredApps(context, tileId)
                if (filtered.isEmpty()) {
                    AppDiscovery.getAllLaunchableApps(context)
                } else {
                    filtered
                }
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select $tileId", fontFamily = MassFont) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF1EDE4)
                )
            )
        },
        containerColor = Color(0xFFF1EDE4)
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5F5A50))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(apps ?: emptyList()) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(app.packageName) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = app.name,
                            fontFamily = MassFont,
                            fontSize = 18.sp,
                            color = Color(0xFF1E1E1A)
                        )
                    }
                }

                if (!showAllApps && AppDiscovery.getFilteredApps(context, tileId).isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = { showAllApps = true }) {
                                Text(
                                    text = "Show all apps",
                                    fontFamily = MassFont,
                                    fontSize = 16.sp,
                                    color = Color(0xFF5F5A50),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
