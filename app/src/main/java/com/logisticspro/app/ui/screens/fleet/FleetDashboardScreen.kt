package com.logisticspro.app.ui.screens.fleet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.logisticspro.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDashboardScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val routePoints by viewModel.routePoints.collectAsState()
    val selectedIndex by viewModel.selectedPointIndex.collectAsState()
    
    // Aktif teslimat noktası belirleme mantığı:
    // 1. Eğer kullanıcı haritadan bir nokta seçtiyse (selectedIndex != -1) ve o nokta teslim edilmemişse onu göster.
    // 2. Yoksa teslim edilmemiş ilk noktayı göster.
    val activePointIndex = if (selectedIndex != -1 && selectedIndex < routePoints.size && !routePoints[selectedIndex].isDelivered && routePoints[selectedIndex].name != "Ana Depo (Ulus)") {
        selectedIndex
    } else {
        routePoints.indexOfFirst { !it.isDelivered && it.name != "Ana Depo (Ulus)" }
    }
    
    val activePoint = if (activePointIndex != -1) routePoints[activePointIndex] else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filo Takip Paneli") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Box(modifier = Modifier.weight(1f)) {
                FleetMapScreen(
                    route = routePoints,
                    onMarkerClick = { index ->
                        viewModel.selectPoint(index)
                    }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activePoint != null) {
                        if (selectedIndex == activePointIndex) MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    } else Color(0xFFE8F5E9)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (activePoint != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(
                                    "Aktif Teslimat: ${activePoint.name}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Durum: Yolda", color = MaterialTheme.colorScheme.primary)
                            }
                            if (selectedIndex != -1 && selectedIndex == activePointIndex) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("Seçili", color = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val gmmIntentUri = android.net.Uri.parse("google.navigation:q=${activePoint.latitude},${activePoint.longitude}")
                                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Yol Tarifi")
                            }
                            Button(
                                onClick = { 
                                    viewModel.markDelivered(activePointIndex)
                                    viewModel.selectPoint(-1) // Teslim edince seçimi sıfırla
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                            ) {
                                Text("Teslim Edildi")
                            }
                        }
                    } else {
                        Text(
                            "Tüm Teslimatlar Tamamlandı! 🎉",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tüm paketler varış noktalarına ulaştı.", color = Color.Gray)
                    }
                }
            }
        }
    }
}
