package org.infa252.project

sealed class CameraUiState {
    object Idle : CameraUiState()
    object Loading : CameraUiState()
    data class Success(val latex: String) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}