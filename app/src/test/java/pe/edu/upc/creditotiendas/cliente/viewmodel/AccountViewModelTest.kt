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
import pe.edu.upc.creditotiendas.cliente.model.beans.CreditProfileResponse
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun ctx(): Context = mock()

    private fun profile(id: Long, storeId: Long) = CreditProfileResponse(
        id = id, storeId = storeId, clientUserId = 1L,
        clientFullName = "Cliente", clientDocument = "12345678",
        currencyId = 1L, creditLimit = 1000.0, availableCredit = 500.0,
        interestRate = 0.02, interestRateType = "EFFECTIVE",
        capitalizationPeriod = "MONTHLY", lateInterestRate = 0.05,
        lateInterestRateType = "EFFECTIVE", maxTermDays = 90,
        cutoffDay = 15, paymentDay = 30, status = "ACTIVE"
    )

    @Test fun `estado inicial vacio`() {
        val vm = AccountViewModel()
        assertTrue(vm.profiles.value.isEmpty())
        assertNull(vm.selectedProfile.value)
        assertFalse(vm.loading.value)
        assertNull(vm.error.value)
    }

    @Test fun `loadProfiles exitoso expone lista y auto-selecciona el primero`() = runTest(dispatcher) {
        val p1 = profile(1L, 10L)
        val p2 = profile(2L, 20L)
        val service: WebService = mock {
            onBlocking { myCreditProfiles() } doReturn Response.success(listOf(p1, p2))
            onBlocking { creditSalesByProfile(any()) } doReturn Response.success(emptyList())
            onBlocking { paymentsByProfile(any()) } doReturn Response.success(emptyList())
        }
        val vm = AccountViewModel(service)

        vm.loadProfiles(ctx())
        advanceUntilIdle()

        assertEquals(2, vm.profiles.value.size)
        assertEquals(p1, vm.selectedProfile.value)
        assertFalse(vm.loading.value)
    }

    @Test fun `multi-perfil no mezcla saldos — selectProfile cambia el perfil activo sin fusionar`() = runTest(dispatcher) {
        val p1 = profile(1L, 10L)
        val p2 = profile(2L, 20L).copy(availableCredit = 999.0)
        val service: WebService = mock {
            onBlocking { myCreditProfiles() } doReturn Response.success(listOf(p1, p2))
            onBlocking { creditSalesByProfile(any()) } doReturn Response.success(emptyList())
            onBlocking { paymentsByProfile(any()) } doReturn Response.success(emptyList())
        }
        val vm = AccountViewModel(service)

        vm.loadProfiles(ctx())
        advanceUntilIdle()
        assertEquals(p1, vm.selectedProfile.value)

        vm.selectProfile(p2, ctx())
        advanceUntilIdle()
        assertEquals(p2, vm.selectedProfile.value)
        assertEquals(2, vm.profiles.value.size)
    }

    @Test fun `loadProfiles con error HTTP deja mensaje mostrable`() = runTest(dispatcher) {
        val err = "".toResponseBody("application/json".toMediaTypeOrNull())
        val service: WebService = mock {
            onBlocking { myCreditProfiles() } doReturn Response.error(500, err)
        }
        val vm = AccountViewModel(service)

        vm.loadProfiles(ctx())
        advanceUntilIdle()

        assertNotNull(vm.error.value)
        assertTrue(vm.error.value!!.contains("500"))
        assertFalse(vm.loading.value)
        assertTrue(vm.profiles.value.isEmpty())
    }

    @Test fun `loadProfiles con excepcion de red no se queda en loading`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { myCreditProfiles() } doThrow java.io.IOException("offline")
        }
        val vm = AccountViewModel(service)

        vm.loadProfiles(ctx())
        advanceUntilIdle()

        assertEquals("Sin conexión al servidor.", vm.error.value)
        assertFalse(vm.loading.value)
    }
}
