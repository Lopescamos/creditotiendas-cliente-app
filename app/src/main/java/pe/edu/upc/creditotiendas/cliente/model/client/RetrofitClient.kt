package pe.edu.upc.creditotiendas.cliente.model.client

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.upc.creditotiendas.cliente.model.response.WebService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    /**
     * 10.0.2.2 es el loopback del host desde el emulador Android.
     * Para dispositivo físico, cambiar a la IP de tu PC en la LAN.
     */
    const val BASE_URL = "http://10.0.2.2:8080/"
    const val PREFS = "ct_prefs"
    const val KEY_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_NAME = "user_name"

    private fun client(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .build()
    }

    fun create(context: Context): WebService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client(context))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WebService::class.java)
}
