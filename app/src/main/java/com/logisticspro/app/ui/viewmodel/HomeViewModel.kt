package com.logisticspro.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
