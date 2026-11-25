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

/** US-06 — Mis estados de cuenta mensuales. */
@Composable
fun StatementsScreen(
    accountVm: AccountViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val profile by accountVm.selectedProfile.collectAsState()
    val statements by accountVm.statements.collectAsState()
    val loading by accountVm.loading.collectAsState()

    LaunchedEffect(profile?.id) {
        val p = profile; if (p != null) accountVm.loadStatements(p.id, context)
    }

    Scaffold(
        topBar = { CtTopBar(title = "Estados de cuenta", showBack = true, onBack = onBack) },
        containerColor = CtBg,
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Tus estados de cuenta", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CtText)
            Text("Se generan automáticamente cada día de corte configurado en tu perfil.",
                color = CtTextMuted, fontSize = 13.sp)

            if (loading) {
                Text("Cargando…", color = CtTextMuted, fontSize = 13.sp)
            } else if (statements.isEmpty()) {
                Text("Aún no se ha generado un estado de cuenta.", color = CtTextMuted, fontSize = 13.sp)
            } else {
                statements.forEach { s ->
                    CtCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.period, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = CtText)
                                Text("Generado ${s.generatedAt.take(10)}", color = CtTextMuted, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatSoles(s.finalBalance), fontWeight = FontWeight.Bold,
                                     fontSize = 17.sp, color = CtPrimary)
                                Text("Saldo final", color = CtTextMuted, fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        StatRow("Saldo inicial", s.previousBalance)
                        StatRow("Compras del período", s.periodPurchases)
                        StatRow("Pagos", s.periodPayments)
                        StatRow("Interés ordinario", s.periodInterest)
                        if (s.periodLateFees > 0) StatRow("Mora", s.periodLateFees, danger = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, amount: Double, danger: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 12.5.sp,
             color = if (danger) CtDanger else CtTextMuted)
        Text(formatSoles(amount), fontSize = 12.5.sp, fontWeight = FontWeight.Medium,
             color = if (danger) CtDanger else CtText)
    }
}
