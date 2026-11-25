package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AccountViewModel

/** US-07 — Plan de cuotas con estado real + ayuda contextual. */
@Composable
fun InstallmentsScreen(
    accountVm: AccountViewModel,
    saleId: Long,
    onBack: () -> Unit,
    onHelp: () -> Unit,
) {
    val context = LocalContext.current
    val installments by accountVm.installments.collectAsState()
    val loading by accountVm.loading.collectAsState()

    LaunchedEffect(saleId) { accountVm.loadInstallments(saleId, context) }

    Scaffold(
        topBar = { CtTopBar(title = "Plan de cuotas", showBack = true, onBack = onBack) },
        bottomBar = {
            BottomTabBar(current = BottomTab.Credit) { tab ->
                when (tab) {
                    BottomTab.Account -> onBack()
                    BottomTab.Credit  -> Unit
                    BottomTab.Help    -> onHelp()
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
            Text("Venta #$saleId", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CtText)
            Text("Cronograma por método francés — los valores son del snapshot de la venta.",
                fontSize = 13.sp, color = CtTextMuted)

            CtCard(dark = true) {
                val remaining = installments.filter { it.status == "PENDING" || it.status == "OVERDUE" }
                    .sumOf { it.installmentAmount }
                Text("Saldo restante de esta venta", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
                     fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(formatSoles(remaining), color = androidx.compose.ui.graphics.Color.White,
                     fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("${installments.count { it.status == "PAID_ON_TIME" || it.status == "PAID_LATE" }} de ${installments.size} cuotas pagadas",
                     color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
            }

            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = CtPrimary)
                    Spacer(Modifier.width(10.dp)); Text("Cargando cuotas…", color = CtTextMuted, fontSize = 13.sp)
                }
            } else if (installments.isEmpty()) {
                Text("No hay cuotas registradas.", color = CtTextMuted, fontSize = 13.sp)
            } else {
                installments.forEach { inst ->
                    CtCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cuota ${inst.installmentNumber} / ${installments.size}",
                                     fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                                Text("Vence ${inst.dueDate.take(10)}", color = CtTextMuted, fontSize = 12.sp)
                            }
                            val (label, type) = when (inst.status) {
                                "PENDING" -> "Por vencer" to BadgeType.Warning
                                "OVERDUE" -> "Vencida" to BadgeType.Danger
                                "PAID_ON_TIME" -> "Pagada" to BadgeType.Success
                                "PAID_LATE" -> "Pagada tarde" to BadgeType.Warning
                                else -> inst.status to BadgeType.Neutral
                            }
                            CtBadge(label, type)
                        }
                        Spacer(Modifier.height(12.dp))
                        BreakdownRow("Capital", formatSoles(inst.capital))
                        BreakdownRow("Interés", formatSoles(inst.interest))
                        BreakdownRow("Cuota fija", formatSoles(inst.installmentAmount), bold = true)
                        BreakdownRow("Saldo remanente", formatSoles(inst.remainingBalance))
                    }
                }
            }

            CtCard {
                Text("¿Cómo se calcula tu cuota?",
                     fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Usamos el método francés: la cuota mensual es fija, pero adentro cambia la mezcla. " +
                    "Al inicio pagas más interés y menos capital; al final, lo contrario.",
                    color = CtTextMuted, fontSize = 13.sp,
                )
                Spacer(Modifier.height(10.dp))
                CtSecondaryButton("Entender método francés", onClick = onHelp)
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = CtTextMuted)
        Text(value, fontSize = 13.sp,
             fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
             color = CtText)
    }
}
