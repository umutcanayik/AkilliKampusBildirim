package com.tufanpirihan.akillikampusbildirim.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.ForgotPasswordRequest
import com.tufanpirihan.akillikampusbildirim.model.LoginRequest
import com.tufanpirihan.akillikampusbildirim.model.RegisterRequest
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Initial)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("E-posta ve şifre boş olamaz")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    RetrofitClient.setToken(loginResponse.token)
                    RetrofitClient.setUserId(loginResponse.uid)
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("E-posta veya şifre hatalı")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun register(fullName: String, email: String, password: String, department: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || department.isBlank()) {
            _registerState.value = RegisterState.Error("Tüm alanları doldurun")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        password = password,
                        department = department
                    )
                )

                if (response.isSuccessful) {
                    _registerState.value = RegisterState.Success
                } else {
                    _registerState.value = RegisterState.Error("Kayıt başarısız")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("E-posta adresi boş olamaz")
            return
        }

        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            try {
                val response = RetrofitClient.apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    _forgotPasswordState.value = ForgotPasswordState.Success
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.Error("İşlem başarısız")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.Success
            }
        }
    }

    fun logout() {
        RetrofitClient.clearSession()
        _loginState.value = LoginState.Initial
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Initial
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Initial
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Initial
    }

    sealed class LoginState {
        object Initial : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class RegisterState {
        object Initial : RegisterState()
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    sealed class ForgotPasswordState {
        object Initial : ForgotPasswordState()
        object Loading : ForgotPasswordState()
        object Success : ForgotPasswordState()
        data class Error(val message: String) : ForgotPasswordState()
    }
}