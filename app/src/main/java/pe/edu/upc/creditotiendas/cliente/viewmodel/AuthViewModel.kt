package pe.edu.upc.creditotiendas.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.creditotiendas.cliente.model.beans.SignInRequest
import pe.edu.upc.creditotiendas.cliente.model.beans.SignInResponse
import pe.edu.upc.creditotiendas.cliente.model.client.RetrofitClient
import pe.edu.upc.creditotiendas.cliente.model.response.WebService

class AuthViewModel(private val webService: WebService? = null) : ViewModel() {

    private fun service(context: Context): WebService =
        webService ?: RetrofitClient.create(context)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _signedIn = MutableStateFlow(false)
    val signedIn: StateFlow<Boolean> = _signedIn

    private val _user = MutableStateFlow<SignInResponse?>(null)
    val user: StateFlow<SignInResponse?> = _user

    fun hydrateFromStorage(context: Context) {
        val prefs = context.getSharedPreferences(RetrofitClient.PREFS, Context.MODE_PRIVATE)
        val token = prefs.getString(RetrofitClient.KEY_TOKEN, null)
        _signedIn.value = !token.isNullOrBlank()
    }

    fun signIn(email: String, password: String, context: Context) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resp = service(context).signIn(SignInRequest(email.trim(), password))
                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    if (body.role != "CLIENT") {
                        _error.value = "Esta app es solo para clientes. Usa el panel web si eres administrador."
                        _loading.value = false
                        return@launch
                    }
                    val prefs = context.getSharedPreferences(RetrofitClient.PREFS, Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString(RetrofitClient.KEY_TOKEN, body.token)
                        .putLong(RetrofitClient.KEY_USER_ID, body.id)
                        .putString(RetrofitClient.KEY_USER_EMAIL, body.email)
                        .putString(RetrofitClient.KEY_USER_NAME,
                            body.fullName ?: "${body.firstName.orEmpty()} ${body.lastName.orEmpty()}".trim())
                        .apply()
                    _user.value = body
                    _signedIn.value = true
                } else {
                    _error.value = when (resp.code()) {
                        401, 403 -> "Correo o contraseña incorrectos."
                        else     -> "No pudimos ingresar (${resp.code()}). Intenta de nuevo."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión con el servidor."
            } finally {
                _loading.value = false
            }
        }
    }

    fun signOut(context: Context) {
        val prefs = context.getSharedPreferences(RetrofitClient.PREFS, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _signedIn.value = false
        _user.value = null
    }

    fun clearError() { _error.value = null }

    companion object {
        fun savedUserName(context: Context): String? =
            context.getSharedPreferences(RetrofitClient.PREFS, Context.MODE_PRIVATE)
                .getString(RetrofitClient.KEY_USER_NAME, null)
    }
}
