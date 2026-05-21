package org.infa252.project

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CameraViewModel(
    private val repository: CameraRepository
) : ViewModel() {

    var uiState by mutableStateOf<CameraUiState>(CameraUiState.Idle)
        private set

    fun recognize(bitmap: Bitmap) {
        uiState = CameraUiState.Loading
        viewModelScope.launch {
            uiState = try {
                val latex = repository.recognize(bitmap)
                CameraUiState.Success(latex)
            } catch (e: Exception) {
                CameraUiState.Error(e.message ?: "Ошибка распознавания")
            }
        }
    }

    fun reset() {
        uiState = CameraUiState.Idle
    }
}