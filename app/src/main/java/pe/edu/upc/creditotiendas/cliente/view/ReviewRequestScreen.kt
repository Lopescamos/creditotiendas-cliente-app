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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AccountViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.PurchaseRequestViewModel

/** Pantalla final — revisar la oferta y enviarla (US-08 submit) + confirmación US-09. */
@Composable
fun ReviewRequestScreen(
    accountVm: AccountViewModel,
    requestVm: PurchaseRequestViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val profile by accountVm.selectedProfile.collectAsState()
    val cart by requestVm.cart.collectAsState()
    val loading by requestVm.loading.collectAsState()
    val error by requestVm.error.collectAsState()
    val created by requestVm.lastCreated.collectAsState()

    val total = cart.sumOf { it.subtotal }

    Scaffold(
        topBar = { CtTopBar(title = "Revisar oferta", showBack = true, onBack = onBack) },
        containerColor = CtBg,
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (created != null) {
                CtCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = CtSuccess,
                             modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Solicitud enviada", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CtText)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Tu comercio revisará la solicitud #${created!!.id}. Te avisaremos cuando reciba respuesta.",
                        color = CtTextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    CtBadge("Pendiente de revisión", BadgeType.Warning)
                }
                CtPrimaryButton("Volver a Mi cuenta", onClick = {
                    requestVm.clearLastCreated(); onDone()
                })
            } else {
                Text("Revisa tu solicitud", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CtText)
                Text("Al enviarla, tu comercio la aprobará o rechazará. Si la aprueba, se generará tu venta " +
                     "a crédito automáticamente con las condiciones de tu perfil.",
                    color = CtTextMuted, fontSize = 13.sp)

                CtCard {
                    Text("Productos solicitados", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                    Spacer(Modifier.height(10.dp))
                    if (cart.isEmpty()) Text("No hay productos.", color = CtTextMuted, fontSize = 13.sp)
                    cart.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontSize = 13.sp, color = CtText)
                                Text("${item.quantity} × ${formatSoles(item.product.creditPrice)}",
                                     color = CtTextMuted, fontSize = 12.sp)
                            }
                            Text(formatSoles(item.subtotal), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))
                    Row {
                        Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(formatSoles(total), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = CtPrimary)
                    }
                }

                if (error != null) InfoAlert(error!!)

                CtPrimaryButton(
                    text = "Enviar solicitud",
                    onClick = { val p = profile; if (p != null) requestVm.submit(p.id, context) },
                    enabled = cart.isNotEmpty(),
                    loading = loading,
                )
                CtSecondaryButton("Agregar más productos", onClick = onBack)
            }
        }
    }
}
