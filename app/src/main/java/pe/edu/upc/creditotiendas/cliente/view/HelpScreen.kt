package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*

/** US-15 — Ayuda financiera. Contenido estático. */
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { CtTopBar(title = "Ayuda", showBack = true, onBack = onBack) },
        bottomBar = { BottomTabBar(current = BottomTab.Help) { if (it != BottomTab.Help) onBack() } },
        containerColor = CtBg,
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Ayuda financiera", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CtText)
            Text("Entiende cómo trabaja tu crédito en CreditoTiendas.", color = CtTextMuted, fontSize = 13.sp)

            HelpItem(
                title = "¿Qué es la tasa de interés?",
                body = "Es el costo de usar el dinero del comercio hasta que lo devuelvas. En tu perfil puede ser " +
                       "nominal (una tasa anual que el comercio aplica por períodos) o efectiva (la que realmente " +
                       "pagas al año). Se congela al registrar cada venta y no cambia después.",
            )
            HelpItem(
                title = "¿Cómo se calculan mis cuotas? (método francés)",
                body = "Usamos el método francés: la cuota mensual es siempre igual, pero adentro varía. " +
                       "En las primeras cuotas pagas más interés que capital; en las últimas, al revés. " +
                       "La fórmula: R = C' × i / (1 − (1+i)^−n), donde C' incluye la capitalización de los días de gracia.",
            )
            HelpItem(
                title = "¿Qué es la gracia total?",
                body = "Son los días entre tu compra y el primer pago. Durante esos días no pagas pero el interés " +
                       "se capitaliza: tu deuda inicial se ajusta por (1 + TED)^días_gracia antes de armar el cronograma.",
            )
            HelpItem(
                title = "Día de corte vs día de pago",
                body = "El día de corte cierra tu mes y es cuando se genera tu estado de cuenta. El día de pago " +
                       "es cuando debe acreditarse la cuota del mes. Entre uno y otro tienes tiempo para pagar sin mora.",
            )
            HelpItem(
                title = "¿Cómo se calcula la mora?",
                body = "Solo se cobra mora sobre la cuota vencida impaga. Se calcula como " +
                       "saldoVencido × [(1+TED_mora)^días_atraso − 1]. Pagar el mismo día del vencimiento no genera mora.",
            )
            HelpItem(
                title = "Prelación del pago: mora → interés → capital",
                body = "Cuando pagas, el comercio aplica el dinero primero a la mora acumulada, luego al interés " +
                       "y recién al capital. Es la regla obligatoria del sistema.",
            )
            HelpItem(
                title = "¿Por qué no se permiten pagos parciales?",
                body = "El sistema exige que cubras el monto exigible completo en la fecha de pago. Si no puedes " +
                       "cubrirlo, acércate al comercio para renegociar antes del vencimiento.",
            )
        }
    }
}

@Composable
private fun HelpItem(title: String, body: String) {
    CtCard {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
        Spacer(Modifier.height(6.dp))
        Text(body, color = CtTextMuted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}
