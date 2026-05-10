package software.eitz.mass.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import software.eitz.mass.AppItem
import software.eitz.mass.MassFont
import software.eitz.mass.data.AppRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appRepository: AppRepository,
    allTiles: List<AppItem>,
    onTileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontFamily = MassFont) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF1EDE4)
                )
            )
        },
        containerColor = Color(0xFFF1EDE4)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "App Assignments",
                    fontFamily = MassFont,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF1E1E1A)
                )
            }

            items(allTiles) { tile ->
                val assignment = appRepository.getAssignmentFlow(tile.id).collectAsState(initial = null)
                
                ListItem(
                    modifier = Modifier.clickable { onTileClick(tile.id) },
                    headlineContent = { Text(tile.name, fontFamily = MassFont) },
                    supportingContent = { 
                        Text(
                            assignment.value ?: "Not assigned",
                            fontFamily = MassFont,
                            color = if (assignment.value != null) Color.Gray else Color.Red
                        ) 
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
