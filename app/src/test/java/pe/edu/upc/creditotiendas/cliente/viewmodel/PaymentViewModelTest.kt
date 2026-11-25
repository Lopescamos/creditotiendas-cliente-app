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
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import pe.edu.upc.creditotiendas.cliente.model.beans.PaymentAllocation
import pe.edu.upc.creditotiendas.cliente.model.beans.PaymentDueBreakdown
import pe.edu.upc.creditotiendas.cliente.model.beans.PaymentDueResponse
import pe.edu.upc.creditotiendas.cliente.model.beans.PaymentResponse
import pe.edu.upc.creditotiendas.cliente.model.beans.RegisterPaymentRequest
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun ctx(): Context = mock()

    private fun dueResponse() = PaymentDueResponse(
        creditProfileId = 1L, asOf = "2026-11-25",
        items = listOf(
            PaymentDueBreakdown(
                installmentId = 10L, installmentNumber = 1,
                dueDate = "2026-11-10", capital = 80.0, interest = 15.0,
                lateFee = 5.0, daysOverdue = 2, total = 100.0
            )
        ),
        totalAmount = 100.0
    )

    private fun paymentResponse() = PaymentResponse(
        id = 500L, creditProfileId = 1L, creditSaleId = 1L,
        paymentDate = "2026-11-25", totalAmount = 100.0,
        allocations = listOf(
            PaymentAllocation(
                installmentId = 10L, installmentNumber = 1,
                lateFeeAmount = 5.0, interestAmount = 15.0,
                capitalAmount = 80.0
            )
        )
    )

    @Test fun `estado inicial vacio`() {
        val vm = PaymentViewModel()
        assertNull(vm.due.value)
        assertNull(vm.result.value)
        assertFalse(vm.loading.value)
        assertNull(vm.error.value)
    }

    @Test fun `loadDue exitoso expone desglose`() = runTest(dispatcher) {
        val due = dueResponse()
        val service: WebService = mock {
            onBlocking { paymentDue(any(), any()) } doReturn Response.success(due)
        }
        val vm = PaymentViewModel(service)

        vm.loadDue(profileId = 1L, installmentIds = listOf(10L), context = ctx())
        advanceUntilIdle()

        assertEquals(due, vm.due.value)
        assertFalse(vm.loading.value)
    }

    @Test fun `confirmPayment envia solo installmentIds — nunca un monto editable`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { registerPayment(any(), any()) } doReturn Response.success(paymentResponse())
        }
        val vm = PaymentViewModel(service)

        vm.confirmPayment(profileId = 1L, installmentIds = listOf(10L, 11L), context = ctx())
        advanceUntilIdle()

        // Verifica que el body enviado al endpoint es exactamente RegisterPaymentRequest
        // con installmentIds — su forma de dato no tiene campo de monto.
        val captor = argumentCaptor<RegisterPaymentRequest>()
        verifyBlocking(service) { registerPayment(any(), captor.capture()) }
        val sent = captor.firstValue
        assertEquals(listOf(10L, 11L), sent.installmentIds)
        // Confirmación estructural: RegisterPaymentRequest solo tiene un campo (installmentIds).
        val props = RegisterPaymentRequest::class.java.declaredFields
            .filter { !it.isSynthetic }
            .map { it.name }
        assertEquals(listOf("installmentIds"), props)

        assertNotNull(vm.result.value)
        assertEquals(100.0, vm.result.value!!.totalAmount, 0.0)
        assertFalse(vm.loading.value)
    }

    @Test fun `confirmPayment con 422 informa que el pago no cubre el integro`() = runTest(dispatcher) {
        val err = "".toResponseBody("application/json".toMediaTypeOrNull())
        val service: WebService = mock {
            onBlocking { registerPayment(any(), any()) } doReturn Response.error(422, err)
        }
        val vm = PaymentViewModel(service)

        vm.confirmPayment(profileId = 1L, installmentIds = listOf(10L), context = ctx())
        advanceUntilIdle()

        assertEquals("El pago no cubre el monto íntegro exigible.", vm.error.value)
        assertNull(vm.result.value)
        assertFalse(vm.loading.value)
    }

    @Test fun `confirmPayment con excepcion de red no se queda en loading`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { registerPayment(any(), any()) } doThrow java.io.IOException("offline")
        }
        val vm = PaymentViewModel(service)

        vm.confirmPayment(profileId = 1L, installmentIds = listOf(10L), context = ctx())
        advanceUntilIdle()

        assertEquals("Sin conexión al servidor.", vm.error.value)
        assertFalse(vm.loading.value)
    }
}
