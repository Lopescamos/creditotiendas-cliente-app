package pe.edu.upc.creditotiendas.cliente.viewmodel

import android.content.Context
import android.content.SharedPreferences
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import pe.edu.upc.creditotiendas.cliente.model.beans.SignInRequest
import pe.edu.upc.creditotiendas.cliente.model.beans.SignInResponse
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun fakeContext(): Context {
        val editor: SharedPreferences.Editor = mock {
            on { putString(any(), anyOrNull()) } doReturn it
            on { putLong(any(), any()) } doReturn it
        }
        val prefs: SharedPreferences = mock {
            on { edit() } doReturn editor
            on { getString(any(), anyOrNull()) } doReturn null
        }
        return mock {
            on { getSharedPreferences(any(), any()) } doReturn prefs
            on { applicationContext } doReturn it
        }
    }

    private fun clientBody() = SignInResponse(
        id = 1L, email = "c@ct.pe", fullName = "Cliente Demo",
        firstName = "Cliente", lastName = "Demo",
        role = "CLIENT", token = "jwt-token", storeIds = listOf(10L)
    )

    @Test fun `estado inicial es vacio y sin loading`() {
        val vm = AuthViewModel()
        assertFalse(vm.loading.value)
        assertNull(vm.error.value)
        assertFalse(vm.signedIn.value)
        assertNull(vm.user.value)
    }

    @Test fun `signIn exitoso con rol CLIENT marca signedIn y expone user`() = runTest(dispatcher) {
        val body = clientBody()
        val service: WebService = mock {
            onBlocking { signIn(any<SignInRequest>()) } doReturn Response.success(body)
        }
        val vm = AuthViewModel(service)

        vm.signIn("c@ct.pe", "x", fakeContext())
        advanceUntilIdle()

        assertTrue(vm.signedIn.value)
        assertEquals(body, vm.user.value)
        assertNull(vm.error.value)
        assertFalse(vm.loading.value)
    }

    @Test fun `signIn con rol distinto de CLIENT se rechaza con mensaje explicito`() = runTest(dispatcher) {
        val adminBody = clientBody().copy(role = "STORE_ADMIN")
        val service: WebService = mock {
            onBlocking { signIn(any<SignInRequest>()) } doReturn Response.success(adminBody)
        }
        val vm = AuthViewModel(service)

        vm.signIn("admin@ct.pe", "x", fakeContext())
        advanceUntilIdle()

        assertFalse(vm.signedIn.value)
        assertNull(vm.user.value)
        assertEquals(
            "Esta app es solo para clientes. Usa el panel web si eres administrador.",
            vm.error.value
        )
        assertFalse(vm.loading.value)
    }

    @Test fun `signIn con 401 deja mensaje de credenciales y no queda en loading`() = runTest(dispatcher) {
        val err = "".toResponseBody("application/json".toMediaTypeOrNull())
        val service: WebService = mock {
            onBlocking { signIn(any<SignInRequest>()) } doReturn Response.error(401, err)
        }
        val vm = AuthViewModel(service)

        vm.signIn("c@ct.pe", "mala", fakeContext())
        advanceUntilIdle()

        assertFalse(vm.signedIn.value)
        assertEquals("Correo o contraseña incorrectos.", vm.error.value)
        assertFalse(vm.loading.value)
    }

    @Test fun `signIn con excepcion de red deja error mostrable y no loading infinito`() = runTest(dispatcher) {
        val service: WebService = mock {
            onBlocking { signIn(any<SignInRequest>()) } doThrow java.io.IOException("offline")
        }
        val vm = AuthViewModel(service)

        vm.signIn("c@ct.pe", "x", fakeContext())
        advanceUntilIdle()

        assertFalse(vm.signedIn.value)
        assertEquals("Sin conexión con el servidor.", vm.error.value)
        assertFalse(vm.loading.value)
        assertNotNull(vm.error.value)
    }
}
