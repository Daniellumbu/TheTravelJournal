package com.daniellumbu.thetraveljournal.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniellumbu.thetraveljournal.data.MarkerDAO
import com.daniellumbu.thetraveljournal.data.MarkerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarkerViewModel @Inject constructor(
    private val markerDao: MarkerDAO
) : ViewModel() {

    private val _selectedMarker = MutableStateFlow<MarkerEntity?>(null)
    val selectedMarker: StateFlow<MarkerEntity?> = _selectedMarker

    fun selectMarker(marker: MarkerEntity) {
        _selectedMarker.value = marker
    }

    fun addImageToMarker(imageUrl: String) {
        _selectedMarker.value?.let { marker ->
            val updatedMarker = marker.copy(imageUrls = marker.imageUrls + imageUrl)
            viewModelScope.launch {
                markerDao.updateMarker(updatedMarker)
                _selectedMarker.value = updatedMarker
            }
        }
    }

    fun updateMarker(marker: MarkerEntity) {
        viewModelScope.launch {
            markerDao.updateMarker(marker)
        }
        _selectedMarker.value = marker
    }
}
