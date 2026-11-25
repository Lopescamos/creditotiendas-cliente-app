package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AccountViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.PurchaseRequestViewModel

/** US-08 pre-check — las 4 condiciones de elegibilidad. */
@Composable
fun EligibilityScreen(
    accountVm: AccountViewModel,
    requestVm: PurchaseRequestViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val profile by accountVm.selectedProfile.collectAsState()
    val eligibility by requestVm.eligibility.collectAsState()
    val loading by requestVm.loading.collectAsState()
    val error by requestVm.error.collectAsState()

    LaunchedEffect(profile?.id) {
        val p = profile; if (p != null) requestVm.checkEligibility(p.id, context)
    }

    Scaffold(
        topBar = { CtTopBar(title = "Solicitar compra", showBack = true, onBack = onBack) },
        bottomBar = { BottomTabBar(current = BottomTab.Credit) { } },
        containerColor = CtBg,
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Solicitar compra a crédito", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CtText)
            Text("Antes de enviar tu pedido, verificamos que cumplas con las condiciones del comercio.",
                color = CtTextMuted, fontSize = 13.sp)

            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = CtPrimary)
                    Spacer(Modifier.width(10.dp)); Text("Verificando elegibilidad…", color = CtTextMuted, fontSize = 13.sp)
                }
            } else if (eligibility == null) {
                if (error != null) InfoAlert(error!!)
            } else {
                val e = eligibility!!
                CtCard(dark = true) {
                    Text("CRÉDITO DISPONIBLE",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(formatSoles(e.availableCredit),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 26.sp)
                }

                CtCard {
                    Text("Tu elegibilidad", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                    Spacer(Modifier.height(10.dp))
                    EligibilityRow("Tu perfil está activo", e.profileActive)
                    EligibilityRow("Pagaste tu primera compra a tiempo", e.firstSalePaidOnTime)
                    EligibilityRow("No tienes cuotas vencidas", e.noOverdueInstallments)
                    EligibilityRow("Tienes crédito disponible", e.withinAvailableCredit)
                }

                if (!e.eligible) {
                    InfoAlert(
                        "Aún no puedes solicitar compras desde la app. Acércate a tu comercio para resolverlo.",
                    )
                }

                CtPrimaryButton(
                    text = if (e.eligible) "Elegir productos" else "No disponible",
                    onClick = onContinue,
                    enabled = e.eligible,
                )
            }
        }
    }
}

@Composable
private fun EligibilityRow(label: String, ok: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(22.dp).clip(CircleShape).background(
                if (ok) CtSuccessSoft else CtDangerSoft,
            ), contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = if (ok) CtSuccess else CtDanger,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = if (ok) CtText else CtDanger, modifier = Modifier.weight(1f))
    }
}
