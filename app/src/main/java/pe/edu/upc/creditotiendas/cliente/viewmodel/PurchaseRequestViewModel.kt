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

data class CartItem(val product: ProductResponse, val quantity: Int) {
    val subtotal: Double get() = product.creditPrice * quantity
}

class PurchaseRequestViewModel(private val webService: WebService? = null) : ViewModel() {

    private fun service(context: Context): WebService =
        webService ?: RetrofitClient.create(context)

    private val _eligibility = MutableStateFlow<PurchaseRequestEligibilityResponse?>(null)
    val eligibility: StateFlow<PurchaseRequestEligibilityResponse?> = _eligibility

    private val _catalog = MutableStateFlow<List<ProductResponse>>(emptyList())
    val catalog: StateFlow<List<ProductResponse>> = _catalog

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _myRequests = MutableStateFlow<List<PurchaseRequestResponse>>(emptyList())
    val myRequests: StateFlow<List<PurchaseRequestResponse>> = _myRequests

    private val _lastCreated = MutableStateFlow<PurchaseRequestResponse?>(null)
    val lastCreated: StateFlow<PurchaseRequestResponse?> = _lastCreated

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val total: Double get() = _cart.value.sumOf { it.subtotal }

    fun checkEligibility(profileId: Long, context: Context) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val resp = service(context).checkEligibility(profileId)
                if (resp.isSuccessful) _eligibility.value = resp.body()
                else _error.value = "No pudimos verificar tu elegibilidad."
            } catch (_: Exception) { _error.value = "Sin conexión al servidor." }
            finally { _loading.value = false }
        }
    }

    fun loadCatalog(storeId: Long, context: Context) {
        viewModelScope.launch {
            try {
                val resp = service(context).storeCatalog(storeId)
                if (resp.isSuccessful) _catalog.value = resp.body().orEmpty().filter { it.active }
            } catch (_: Exception) {}
        }
    }

    fun loadMyRequests(profileId: Long, context: Context) {
        viewModelScope.launch {
            try {
                val resp = service(context).myPurchaseRequests(profileId)
                if (resp.isSuccessful) _myRequests.value = resp.body().orEmpty()
            } catch (_: Exception) {}
        }
    }

    fun addToCart(product: ProductResponse) {
        val cur = _cart.value.toMutableList()
        val idx = cur.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) cur[idx] = cur[idx].copy(quantity = cur[idx].quantity + 1)
        else cur.add(CartItem(product, 1))
        _cart.value = cur
    }

    fun updateQty(productId: Long, qty: Int) {
        _cart.value = if (qty <= 0) _cart.value.filter { it.product.id != productId }
                      else _cart.value.map { if (it.product.id == productId) it.copy(quantity = qty) else it }
    }

    fun clearCart() { _cart.value = emptyList() }

    fun submit(profileId: Long, context: Context) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try {
                val items = _cart.value.map {
                    SaleLineItem(it.product.id, it.product.name, it.quantity, it.product.creditPrice)
                }
                val resp = service(context)
                    .createPurchaseRequest(profileId, CreatePurchaseRequest(items))
                if (resp.isSuccessful) {
                    _lastCreated.value = resp.body()
                    clearCart()
                } else _error.value = when (resp.code()) {
                    422 -> "No cumples los criterios de elegibilidad."
                    else -> "No se pudo enviar la solicitud (${resp.code()})."
                }
            } catch (_: Exception) { _error.value = "Sin conexión al servidor." }
            finally { _loading.value = false }
        }
    }

    fun clearLastCreated() { _lastCreated.value = null }
    fun clearError() { _error.value = null }
}
