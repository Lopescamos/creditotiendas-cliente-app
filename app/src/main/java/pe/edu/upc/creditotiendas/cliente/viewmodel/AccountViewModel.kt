package pe.edu.upc.creditotiendas.cliente.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.creditotiendas.cliente.model.beans.*
import pe.edu.upc.creditotiendas.cliente.model.client.RetrofitClient
import pe.edu.upc.creditotiendas.cliente.model.response.WebService

class AccountViewModel(private val webService: WebService? = null) : ViewModel() {

    private fun service(context: Context): WebService =
        webService ?: RetrofitClient.create(context)

    private val _profiles = MutableStateFlow<List<CreditProfileResponse>>(emptyList())
    val profiles: StateFlow<List<CreditProfileResponse>> = _profiles

    private val _selectedProfile = MutableStateFlow<CreditProfileResponse?>(null)
    val selectedProfile: StateFlow<CreditProfileResponse?> = _selectedProfile

    private val _sales = MutableStateFlow<List<CreditSaleResponse>>(emptyList())
    val sales: StateFlow<List<CreditSaleResponse>> = _sales

    private val _installments = MutableStateFlow<List<InstallmentResponse>>(emptyList())
    val installments: StateFlow<List<InstallmentResponse>> = _installments

    private val _payments = MutableStateFlow<List<PaymentResponse>>(emptyList())
    val payments: StateFlow<List<PaymentResponse>> = _payments

    private val _statements = MutableStateFlow<List<AccountStatementResponse>>(emptyList())
    val statements: StateFlow<List<AccountStatementResponse>> = _statements

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfiles(context: Context) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val resp = service(context).myCreditProfiles()
                if (resp.isSuccessful) {
                    val list = resp.body().orEmpty()
                    _profiles.value = list
                    if (_selectedProfile.value == null && list.isNotEmpty()) {
                        selectProfile(list.first(), context)
                    }
                } else { _error.value = "Error ${resp.code()} al cargar tu cuenta." }
            } catch (e: Exception) { _error.value = "Sin conexión al servidor." }
            finally { _loading.value = false }
        }
    }

    fun selectProfile(profile: CreditProfileResponse, context: Context) {
        _selectedProfile.value = profile
        loadSales(profile.id, context)
        loadPayments(profile.id, context)
    }

    fun loadSales(profileId: Long, context: Context) {
        viewModelScope.launch {
            try {
                val resp = service(context).creditSalesByProfile(profileId)
                if (resp.isSuccessful) _sales.value = resp.body().orEmpty()
            } catch (_: Exception) {}
        }
    }

    fun loadInstallments(saleId: Long, context: Context) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = service(context).installmentsBySale(saleId)
                if (resp.isSuccessful) _installments.value = resp.body().orEmpty()
                else _error.value = "No pudimos cargar las cuotas."
            } catch (_: Exception) { _error.value = "Sin conexión al servidor." }
            finally { _loading.value = false }
        }
    }

    fun loadPayments(profileId: Long, context: Context) {
        viewModelScope.launch {
            try {
                val resp = service(context).paymentsByProfile(profileId)
                if (resp.isSuccessful) _payments.value = resp.body().orEmpty()
            } catch (_: Exception) {}
        }
    }

    fun loadStatements(profileId: Long, context: Context) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = service(context).statementsByProfile(profileId)
                if (resp.isSuccessful) _statements.value = resp.body().orEmpty()
            } catch (_: Exception) {}
            finally { _loading.value = false }
        }
    }

    fun clearError() { _error.value = null }
}
