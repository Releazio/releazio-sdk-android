package com.releazio.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.releazio.sdk.Releazio
import com.releazio.sdk.core.ReleazioError
import com.releazio.sdk.models.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for MainActivity
 */
class MainViewModel : ViewModel() {
    
    private val _updateState = MutableStateFlow<UpdateState?>(null)
    val updateState: StateFlow<UpdateState?> = _updateState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Check for updates
     */
    fun checkUpdates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val state = Releazio.checkUpdates()
                _updateState.value = state
            } catch (e: ReleazioError) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}


