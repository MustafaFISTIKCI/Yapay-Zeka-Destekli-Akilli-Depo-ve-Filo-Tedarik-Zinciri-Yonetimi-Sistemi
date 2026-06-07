package com.logisticspro.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logisticspro.app.data.model.RoutePoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiEvent {
    data class ShowToast(val message: String) : HomeUiEvent()
    object ShowStockUpdateDialog : HomeUiEvent()
    object OpenBarcodeScanner : HomeUiEvent()
    data class ShowDeliveryConfirmation(val barcode: String) : HomeUiEvent()
}

class HomeViewModel : ViewModel() {
    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Seçili teslimat noktası index'i (-1 ise en yakın otomatik seçilir)
    private val _selectedPointIndex = MutableStateFlow(-1)
    val selectedPointIndex: StateFlow<Int> = _selectedPointIndex.asStateFlow()

    fun selectPoint(index: Int) {
        _selectedPointIndex.value = index
    }
    private val _routePoints = MutableStateFlow(
        optimizeRoute(
            listOf(
                RoutePoint(39.9334, 32.8597, "Ana Depo (Ulus)"),
                RoutePoint(39.9042, 32.8598, "Kızılay Şube"),
                RoutePoint(39.8912, 32.7820, "Çankaya Teslimat"),
                RoutePoint(39.9208, 32.8541, "Bahçelievler Noktası"),
                RoutePoint(39.9125, 32.8402, "Anıttepe Müşteri")
            )
        )
    )
    val routePoints: StateFlow<List<RoutePoint>> = _routePoints.asStateFlow()

    fun markDelivered(index: Int) {
        viewModelScope.launch {
            val currentList = _routePoints.value.toMutableList()
            if (index in currentList.indices && index != 0) {
                val pointToDeliver = currentList.removeAt(index)
                val deliveredPoint = pointToDeliver.copy(isDelivered = true)
                
                // Teslim edilenleri listenin başına (depodan hemen sonraya) ama teslim sırasına göre ekleyelim
                // Depo (0), Teslim Edilen 1, Teslim Edilen 2 ... Bekleyen 1, Bekleyen 2
                val lastDeliveredIndex = currentList.indexOfLast { it.isDelivered }
                val insertIndex = if (lastDeliveredIndex == -1) 1 else lastDeliveredIndex + 1
                
                currentList.add(insertIndex, deliveredPoint)
                
                _routePoints.value = optimizeRoute(currentList)
                _uiEvent.emit(HomeUiEvent.ShowToast("✅ ${deliveredPoint.name} teslim edildi! Bulunduğunuz yerden yeni rota hesaplandı."))
            }
        }
    }

    fun addRoutePoint(lat: Double, lng: Double) {
        viewModelScope.launch {
            val currentList = _routePoints.value.toMutableList()
            val newIndex = currentList.size
            currentList.add(RoutePoint(lat, lng, "Yeni Nokta $newIndex", false))
            _routePoints.value = optimizeRoute(currentList)
            _uiEvent.emit(HomeUiEvent.ShowToast("📍 Yeni nokta eklendi! Rota optimize edildi."))
        }
    }

    private fun optimizeRoute(points: List<RoutePoint>): List<RoutePoint> {
        if (points.isEmpty()) return points
        
        val depo = points.first()
        
        // Teslim edilmiş noktalar (zaten doğru sıradalar - markDelivered içinde sıraya soktuk)
        val delivered = points.filter { it.isDelivered && it != depo }
        
        // Henüz teslim edilmemiş noktalar
        val undelivered = points.filter { !it.isDelivered && it != depo }.toMutableList()
        
        val result = mutableListOf<RoutePoint>()
        result.add(depo)
        result.addAll(delivered)
        
        // Referans noktamız: Eğer hiç teslimat yoksa depo, varsa EN SON TESLİMAT YAPILAN yer.
        var currentLocation = delivered.lastOrNull() ?: depo
        
        // Kalan noktaları EN SON TESLİMAT YAPILAN YERDEN başlayarak en yakına göre diz
        while (undelivered.isNotEmpty()) {
            val nearest = undelivered.minByOrNull { calculateDistance(currentLocation, it) }!!
            result.add(nearest)
            undelivered.remove(nearest)
            currentLocation = nearest // Bir sonrakini bulmak için şu anki hedefi başlangıç yap
        }
        
        return result
    }

    private fun calculateDistance(p1: RoutePoint, p2: RoutePoint): Double {
        val latDiff = p1.latitude - p2.latitude
        val lngDiff = p1.longitude - p2.longitude
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
    }

    fun onScanBarcodeClick() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.OpenBarcodeScanner)
        }
    }

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.ShowDeliveryConfirmation(barcode))
        }
    }

    fun confirmDelivery(packageId: String) {
        viewModelScope.launch {
            // Simüle edilmiş teslimat onayı
            _uiEvent.emit(HomeUiEvent.ShowToast("Paket #$packageId teslimatı başarıyla onaylandı!"))
        }
    }

    fun onUpdateStockClick() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.ShowStockUpdateDialog)
        }
    }

    fun updateStock(productId: String, quantity: String) {
        viewModelScope.launch {
            if (productId.isBlank() || quantity.isBlank()) {
                _uiEvent.emit(HomeUiEvent.ShowToast("Lütfen tüm alanları doldurunuz"))
                return@launch
            }
            
            // Simüle edilmiş API çağrısı
            _uiEvent.emit(HomeUiEvent.ShowToast("Ürün ID: $productId için stok $quantity adet olarak güncellendi."))
        }
    }
}
