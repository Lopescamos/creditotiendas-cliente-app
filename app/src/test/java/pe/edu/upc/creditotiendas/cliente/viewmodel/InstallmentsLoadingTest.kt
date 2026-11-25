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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import pe.edu.upc.creditotiendas.cliente.model.beans.InstallmentResponse
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Response

/**
 * Tests específicos del flujo US-07 (`InstallmentsScreen`).
 * La pantalla reutiliza `AccountViewModel`, así que los tests ejercen
 * `loadInstallments` y validan que el snapshot se expone tal cual.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InstallmentsLoadingTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun ctx(): Context = mock()

    private fun inst(n: Int, status: String, amount: Double = 100.0) = InstallmentResponse(
        id = n.toLong(), creditSaleId = 1L, creditProfileId = 1L,
        installmentNumber = n, dueDate = "2026-11-${10 + n}",
        capital = 80.0, interest = 20.0, installmentAmount = amount,
        remainingBalance = 500.0 - n * 100.0, status = status, paidAt = null
    )

    @Test fun `loadInstallments exitoso expone snapshot tal cual`() = runTest(dispatcher) {
        val snapshot = listOf(
            inst(1, "PAID_ON_TIME"),
            inst(2, "PENDING"),
            inst(3, "OVERDUE"),
        )
        val service: WebService = mock {
            onBlocking { installmentsBySale(any()) } doReturn Response.success(snapshot)
        }
        val vm = AccountViewModel(service)

        vm.loadInstallments(saleId = 1L, context = ctx())
        advanceUntilIdle()

        assertEquals(3, vm.installments.value.size)
        assertEquals(snapshot, vm.installments.value)
        assertFalse(vm.loading.value)
    }

    @Test fun `loadInstallments con error HTTP expone mensaje y no loading infinito`() = runTest(dispatcher) {
        val err = "".toResponseBody("application/json".toMediaTypeOrNull())
        val service: WebService = mock {
            onBlocking { installmentsBySale(any()) } doReturn Response.error(404, err)
        }
        val vm = AccountViewModel(service)

        vm.loadInstallments(saleId = 99L, context = ctx())
        advanceUntilIdle()

        assertEquals("No pudimos cargar las cuotas.", vm.error.value)
        assertFalse(vm.loading.value)
        assertTrue(vm.installments.value.isEmpty())
    }

    @Test fun `loadInstallments con excepcion de red deja error mostrable`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { installmentsBySale(any()) } doThrow java.io.IOException("offline")
        }
        val vm = AccountViewModel(service)

        vm.loadInstallments(saleId = 1L, context = ctx())
        advanceUntilIdle()

        assertNotNull(vm.error.value)
        assertFalse(vm.loading.value)
    }
}
