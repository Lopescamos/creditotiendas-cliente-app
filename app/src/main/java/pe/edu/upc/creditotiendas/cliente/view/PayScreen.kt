package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AccountViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.PaymentViewModel

/**
 * US-10 — Pagar cuenta.
 *
 * Reglas respetadas:
 *  - No se manda monto, solo installmentIds (backend calcula).
 *  - Pago íntegro obligatorio (no se permiten parciales).
 *  - El backend aplica prelación mora → interés → capital.
 */
@Composable
fun PayScreen(
    accountVm: AccountViewModel,
    payVm: PaymentViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val profile by accountVm.selectedProfile.collectAsState()
    val installments by accountVm.installments.collectAsState()
    val sales by accountVm.sales.collectAsState()
    val due by payVm.due.collectAsState()
    val result by payVm.result.collectAsState()
    val loading by payVm.loading.collectAsState()
    val error by payVm.error.collectAsState()

    // Para cargar las cuotas exigibles tomamos la venta ACTIVE/OVERDUE más reciente y sus cuotas pendientes/vencidas.
    val activeSale = sales.firstOrNull { it.status == "ACTIVE" || it.status == "OVERDUE" }

    LaunchedEffect(activeSale?.id) {
        if (activeSale != null) accountVm.loadInstallments(activeSale.id, context)
    }
    LaunchedEffect(installments, profile) {
        val pending = installments.filter { it.status == "PENDING" || it.status == "OVERDUE" }
        val p = profile
        if (p != null && pending.isNotEmpty()) {
            payVm.loadDue(p.id, pending.map { it.id }, context)
        }
    }

    Scaffold(
        topBar = { CtTopBar(title = "Mi cuenta", showBack = true, onBack = onBack) },
        bottomBar = {
            BottomTabBar(current = BottomTab.Credit) { tab ->
                when (tab) {
                    BottomTab.Account -> onBack()
                    BottomTab.Credit  -> Unit
                    BottomTab.Help    -> onBack()
                }
            }
        },
        containerColor = CtBg,
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Pagar cuenta", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = CtText)
            Text("Cliente ${profile?.clientFullName ?: ""} · Tienda ${profile?.storeId ?: ""}",
                color = CtTextMuted, fontSize = 13.sp)

            if (result != null) {
                // Pantalla de confirmación
                CtCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = CtSuccess,
                             modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Pago registrado", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CtText)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Total: ${formatSoles(result!!.totalAmount)}", fontSize = 15.sp, color = CtText)
                    Text("Fecha: ${result!!.paymentDate.take(16)}", fontSize = 13.sp, color = CtTextMuted)
                    Spacer(Modifier.height(14.dp))
                    Text("Imputación del pago (prelación mora → interés → capital):",
                         fontSize = 12.sp, color = CtTextMuted)
                    Spacer(Modifier.height(6.dp))
                    result!!.allocations.forEach { a ->
                        Text("Cuota ${a.installmentNumber ?: "?"}  →  mora ${formatSoles(a.lateFeeAmount)}, " +
                             "interés ${formatSoles(a.interestAmount)}, capital ${formatSoles(a.capitalAmount)}",
                             fontSize = 12.sp, color = CtText)
                    }
                }
                Spacer(Modifier.height(8.dp))
                CtPrimaryButton("Volver a Mi cuenta", onClick = onDone)
            } else {
                // Card verde con total exigible
                CtCard(dark = true) {
                    Text("TOTAL EXIGIBLE COMPLETO", color = Color.White.copy(alpha = 0.75f),
                         fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(formatSoles(due?.totalAmount ?: 0.0), color = Color.White,
                         fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Pendiente según estado de cuenta", color = Color.White.copy(alpha = 0.72f),
                         fontSize = 12.sp)
                }

                CtCard {
                    Text("Desglose de la deuda exigible", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                    Spacer(Modifier.height(10.dp))
                    if (due == null) {
                        Text("Cargando desglose…", color = CtTextMuted, fontSize = 13.sp)
                    } else {
                        val cap = due!!.items.sumOf { it.capital }
                        val intr = due!!.items.sumOf { it.interest }
                        val mora = due!!.items.sumOf { it.lateFee }
                        BreakdownLine("Capital", cap)
                        BreakdownLine("Interés ordinario", intr)
                        BreakdownLine("Mora · interés moratorio", mora, highlight = mora > 0)
                        Spacer(Modifier.height(10.dp))
                        Text("Fecha pactada de pago", color = CtTextMuted, fontSize = 12.sp)
                        val firstDue = due!!.items.firstOrNull()?.dueDate?.take(10) ?: "Según acuerdo"
                        Text(firstDue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                    }
                }

                InfoAlert(
                    "Debe pagarse el total exigible completo. No se permiten pagos parciales en la fecha pactada."
                )

                if (error != null) {
                    InfoAlert(error!!)
                }

                CtPrimaryButton(
                    text = "Revisar y confirmar pago íntegro",
                    onClick = {
                        val p = profile ?: return@CtPrimaryButton
                        val ids = installments.filter { it.status == "PENDING" || it.status == "OVERDUE" }.map { it.id }
                        if (ids.isNotEmpty()) payVm.confirmPayment(p.id, ids, context)
                    },
                    enabled = due != null && !loading && (due!!.items.isNotEmpty()),
                    loading = loading,
                )
            }
        }
    }
}

@Composable
private fun BreakdownLine(label: String, amount: Double, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp,
             color = if (highlight) CtDanger else CtTextMuted)
        Text(formatSoles(amount), fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
             color = if (highlight) CtDanger else CtText)
    }
}
