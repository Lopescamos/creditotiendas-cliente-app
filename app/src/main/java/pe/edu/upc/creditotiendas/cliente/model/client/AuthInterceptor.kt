package pe.edu.upc.creditotiendas.cliente.model.client

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

/** Inyecta Authorization: Bearer <token> automáticamente si hay sesión. */
class AuthInterceptor(private val appContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = appContext.getSharedPreferences(RetrofitClient.PREFS, Context.MODE_PRIVATE)
        val token = prefs.getString(RetrofitClient.KEY_TOKEN, null)
        val req = chain.request()
        return if (token.isNullOrBlank() || req.header("Authorization") != null) {
            chain.proceed(req)
        } else {
            chain.proceed(req.newBuilder().addHeader("Authorization", "Bearer $token").build())
        }
    }
}
