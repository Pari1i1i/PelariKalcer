package com.example.pelarikalcer.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pelarikalcer.data.local.entity.UserEntity
import com.example.pelarikalcer.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Int) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            repository.prepopulateMockUsers()
        }
    }

    fun login(username: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = repository.login(username)
                if (user != null && user.passwordHash == passwordHash) {
                    _authState.value = AuthState.Success(user.userId)
                } else {
                    _authState.value = AuthState.Error("Username atau password salah")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun register(username: String, email: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val existingUser = repository.login(username)
                if (existingUser != null) {
                    _authState.value = AuthState.Error("Username sudah terdaftar")
                    return@launch
                }
                
                val newUser = UserEntity(
                    username = username,
                    email = email,
                    passwordHash = passwordHash
                )
                val newId = repository.register(newUser)
                _authState.value = AuthState.Success(newId.toInt())
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Terjadi kesalahan saat registrasi")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
