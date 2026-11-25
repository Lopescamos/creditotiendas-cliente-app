package pe.edu.upc.creditotiendas.cliente.viewmodel

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import pe.edu.upc.creditotiendas.cliente.model.beans.AccountStatementResponse
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Response

/**
 * US-06 (statements). Tests específicos del flujo de carga de
 * estados de cuenta que alimenta `StatementsScreen`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatementsLoadingTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun ctx(): Context = mock()

    private fun stmt(period: String, finalBalance: Double) = AccountStatementResponse(
        id = 1L, creditProfileId = 1L, period = period,
        previousBalance = 0.0, periodPurchases = 200.0, periodPayments = 100.0,
        periodInterest = 10.0, periodLateFees = 0.0,
        finalBalance = finalBalance, generatedAt = "2026-11-25"
    )

    @Test fun `loadStatements exitoso expone la lista por periodo`() = runTest(dispatcher) {
        val list = listOf(stmt("2026-10", 110.0), stmt("2026-11", 220.0))
        val service: WebService = mock {
            onBlocking { statementsByProfile(any()) } doReturn Response.success(list)
        }
        val vm = AccountViewModel(service)

        vm.loadStatements(profileId = 1L, context = ctx())
        advanceUntilIdle()

        assertEquals(2, vm.statements.value.size)
        assertEquals("2026-11", vm.statements.value[1].period)
        assertFalse(vm.loading.value)
    }

    @Test fun `loadStatements con excepcion de red no bloquea la pantalla`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { statementsByProfile(any()) } doThrow java.io.IOException("offline")
        }
        val vm = AccountViewModel(service)

        vm.loadStatements(profileId = 1L, context = ctx())
        advanceUntilIdle()

        assertTrue(vm.statements.value.isEmpty())
        assertFalse(vm.loading.value)
    }
}
