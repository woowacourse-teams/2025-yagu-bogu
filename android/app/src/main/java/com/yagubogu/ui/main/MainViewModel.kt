package com.yagubogu.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _selectedBottomNavKey: MutableStateFlow<BottomNavKey> =
        MutableStateFlow(BottomNavKey.Home)
    val selectedBottomNavKey: StateFlow<BottomNavKey> = _selectedBottomNavKey.asStateFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun selectBottomNavKey(key: BottomNavKey) {
        _selectedBottomNavKey.value = key
    }

    fun showLoading() {
        _isLoading.value = true
    }

    fun hideLoading() {
        _isLoading.value = false
    }

    fun <T> withLoading(block: suspend () -> T): Flow<T> =
        flow {
            _isLoading.value = true
            try {
                emit(block())
            } finally {
                _isLoading.value = false
            }
        }
}
