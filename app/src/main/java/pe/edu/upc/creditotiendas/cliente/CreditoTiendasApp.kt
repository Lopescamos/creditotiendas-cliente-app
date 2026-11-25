package pe.edu.upc.creditotiendas.cliente

import android.app.Application

class CreditoTiendasApp : Application() {
    companion object { lateinit var instance: CreditoTiendasApp; private set }
    override fun onCreate() { super.onCreate(); instance = this }
}
