package com.yagubogu.ui.main

import androidx.lifecycle.ViewModel
import com.yagubogu.ui.navigation.BottomNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _selectedBottomNavKey = MutableStateFlow<BottomNavKey>(BottomNavKey.Home)
    val selectedBottomNavKey: StateFlow<BottomNavKey> = _selectedBottomNavKey.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun selectBottomNavKey(key: BottomNavKey) {
        _selectedBottomNavKey.value = key
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
}
