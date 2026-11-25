package pe.edu.upc.creditotiendas.cliente.viewmodel

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import pe.edu.upc.creditotiendas.cliente.model.beans.ProductResponse
import pe.edu.upc.creditotiendas.cliente.model.beans.PurchaseRequestEligibilityResponse
import pe.edu.upc.creditotiendas.cliente.model.beans.PurchaseRequestResponse
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseRequestViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun ctx(): Context = mock()

    private fun eligibility(
        profile: Boolean = true, firstSale: Boolean = true,
        noOverdue: Boolean = true, withinLimit: Boolean = true,
    ) = PurchaseRequestEligibilityResponse(
        eligible = profile && firstSale && noOverdue && withinLimit,
        profileActive = profile, firstSalePaidOnTime = firstSale,
        noOverdueInstallments = noOverdue, withinAvailableCredit = withinLimit,
        availableCredit = 500.0, failureReasons = emptyList()
    )

    private fun product(id: Long, active: Boolean = true) = ProductResponse(
        id = id, storeId = 10L, name = "Prod-$id", description = null,
        type = "PRODUCT", supplier = null, brand = null, measurementUnitId = null,
        cashPrice = 90.0, creditPrice = 100.0, imageUrl = null, active = active
    )

    private fun createdRequest() = PurchaseRequestResponse(
        id = 999L, creditProfileId = 1L, storeId = 10L,
        items = emptyList(), totalAmount = 200.0, status = "PENDING",
        requestedAt = "2026-11-25", respondedAt = null,
        rejectionReason = null, generatedSaleId = null
    )

    @Test fun `estado inicial vacio`() {
        val vm = PurchaseRequestViewModel()
        assertNull(vm.eligibility.value)
        assertTrue(vm.catalog.value.isEmpty())
        assertTrue(vm.cart.value.isEmpty())
        assertEquals(0.0, vm.total, 0.0)
        assertFalse(vm.loading.value)
    }

    // ===== Regla no trivial: las 4 condiciones de elegibilidad =====

    @Test fun `elegibilidad — las 4 condiciones cumplidas devuelve eligible=true`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { checkEligibility(any()) } doReturn Response.success(eligibility())
        }
        val vm = PurchaseRequestViewModel(service)

        vm.checkEligibility(profileId = 1L, context = ctx())
        advanceUntilIdle()

        val e = vm.eligibility.value!!
        assertTrue(e.eligible)
        assertTrue(e.profileActive)
        assertTrue(e.firstSalePaidOnTime)
        assertTrue(e.noOverdueInstallments)
        assertTrue(e.withinAvailableCredit)
    }

    @Test fun `elegibilidad — falta primera compra pagada a tiempo bloquea`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { checkEligibility(any()) } doReturn Response.success(eligibility(firstSale = false))
        }
        val vm = PurchaseRequestViewModel(service)

        vm.checkEligibility(1L, ctx())
        advanceUntilIdle()

        val e = vm.eligibility.value!!
        assertFalse(e.eligible)
        assertFalse(e.firstSalePaidOnTime)
    }

    @Test fun `elegibilidad — cuotas vencidas impagas bloquea`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { checkEligibility(any()) } doReturn Response.success(eligibility(noOverdue = false))
        }
        val vm = PurchaseRequestViewModel(service)
        vm.checkEligibility(1L, ctx()); advanceUntilIdle()
        assertFalse(vm.eligibility.value!!.eligible)
    }

    @Test fun `elegibilidad — fuera del limite bloquea`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { checkEligibility(any()) } doReturn Response.success(eligibility(withinLimit = false))
        }
        val vm = PurchaseRequestViewModel(service)
        vm.checkEligibility(1L, ctx()); advanceUntilIdle()
        assertFalse(vm.eligibility.value!!.eligible)
    }

    // ===== Catálogo: solo activos =====

    @Test fun `loadCatalog filtra productos inactivos`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { storeCatalog(any()) } doReturn Response.success(
                listOf(product(1), product(2, active = false), product(3))
            )
        }
        val vm = PurchaseRequestViewModel(service)
        vm.loadCatalog(storeId = 10L, context = ctx()); advanceUntilIdle()
        assertEquals(2, vm.catalog.value.size)
        assertTrue(vm.catalog.value.all { it.active })
    }

    // ===== Submit =====

    @Test fun `submit exitoso limpia carrito y expone lastCreated`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { createPurchaseRequest(any(), any()) } doReturn Response.success(createdRequest())
        }
        val vm = PurchaseRequestViewModel(service)
        vm.addToCart(product(1))
        vm.addToCart(product(1))
        assertEquals(2, vm.cart.value[0].quantity)

        vm.submit(profileId = 1L, context = ctx())
        advanceUntilIdle()

        assertTrue(vm.cart.value.isEmpty())
        assertNotNull(vm.lastCreated.value)
        assertEquals("PENDING", vm.lastCreated.value!!.status)
        assertFalse(vm.loading.value)
    }

    @Test fun `submit con 422 muestra mensaje y conserva el carrito`() = runTest(dispatcher) {
        val err = "".toResponseBody("application/json".toMediaTypeOrNull())
        val service: WebService = mock {
            onBlocking { createPurchaseRequest(any(), any()) } doReturn Response.error(422, err)
        }
        val vm = PurchaseRequestViewModel(service)
        vm.addToCart(product(1))

        vm.submit(1L, ctx()); advanceUntilIdle()

        assertEquals("No cumples los criterios de elegibilidad.", vm.error.value)
        assertEquals(1, vm.cart.value.size)
        assertNull(vm.lastCreated.value)
    }

    @Test fun `submit con excepcion de red no se queda en loading`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { createPurchaseRequest(any(), any()) } doThrow java.io.IOException("offline")
        }
        val vm = PurchaseRequestViewModel(service)
        vm.addToCart(product(1))

        vm.submit(1L, ctx()); advanceUntilIdle()

        assertEquals("Sin conexión al servidor.", vm.error.value)
        assertFalse(vm.loading.value)
    }
}
