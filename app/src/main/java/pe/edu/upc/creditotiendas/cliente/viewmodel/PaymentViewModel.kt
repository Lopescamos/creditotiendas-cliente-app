package pe.edu.upc.creditotiendas.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.creditotiendas.cliente.model.beans.PaymentDueResponse
import pe.edu.upc.creditotiendas.cliente.model.beans.PaymentResponse
import pe.edu.upc.creditotiendas.cliente.model.beans.RegisterPaymentRequest
import pe.edu.upc.creditotiendas.cliente.model.client.RetrofitClient
import pe.edu.upc.creditotiendas.cliente.model.response.WebService

class PaymentViewModel(private val webService: WebService? = null) : ViewModel() {

    private fun service(context: Context): WebService =
        webService ?: RetrofitClient.create(context)

    private val _due = MutableStateFlow<PaymentDueResponse?>(null)
    val due: StateFlow<PaymentDueResponse?> = _due

    private val _result = MutableStateFlow<PaymentResponse?>(null)
    val result: StateFlow<PaymentResponse?> = _result

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDue(profileId: Long, installmentIds: List<Long>, context: Context) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val ids = installmentIds.joinToString(",")
                val resp = service(context).paymentDue(profileId, ids)
                if (resp.isSuccessful) _due.value = resp.body()
                else _error.value = "No pudimos calcular el desglose exigible."
            } catch (_: Exception) { _error.value = "Sin conexión al servidor." }
            finally { _loading.value = false }
        }
    }

    fun confirmPayment(profileId: Long, installmentIds: List<Long>, context: Context) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val resp = service(context)
                    .registerPayment(profileId, RegisterPaymentRequest(installmentIds))
                if (resp.isSuccessful) _result.value = resp.body()
                else _error.value = when (resp.code()) {
                    422 -> "El pago no cubre el monto íntegro exigible."
                    else -> "No se pudo registrar el pago (${resp.code()})."
                }
            } catch (_: Exception) { _error.value = "Sin conexión al servidor." }
            finally { _loading.value = false }
        }
    }

    fun reset() { _due.value = null; _result.value = null; _error.value = null }
}
