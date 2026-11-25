package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
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

/** US-08 — Elegir productos del catálogo activo de la tienda. */
@Composable
fun RequestCatalogScreen(
    accountVm: AccountViewModel,
    requestVm: PurchaseRequestViewModel,
    onReview: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val profile by accountVm.selectedProfile.collectAsState()
    val catalog by requestVm.catalog.collectAsState()
    val cart by requestVm.cart.collectAsState()

    LaunchedEffect(profile?.storeId) {
        val p = profile; if (p != null) requestVm.loadCatalog(p.storeId, context)
    }

    val total = cart.sumOf { it.subtotal }
    val availableCredit = requestVm.eligibility.collectAsState().value?.availableCredit ?: 0.0
    val withinLimit = total <= availableCredit

    Scaffold(
        topBar = { CtTopBar(title = "Catálogo", showBack = true, onBack = onBack) },
        containerColor = CtBg,
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(CtSurface).padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total de la solicitud", color = CtTextMuted, fontSize = 12.sp)
                        Text(formatSoles(total), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CtText)
                        if (!withinLimit)
                            Text("Excede tu crédito disponible", color = CtDanger, fontSize = 11.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    CtPrimaryButton(
                        text = "Revisar y enviar",
                        onClick = onReview,
                        modifier = Modifier.width(180.dp),
                        enabled = cart.isNotEmpty() && withinLimit,
                    )
                }
            }
        },
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (catalog.isEmpty()) {
                Text("No hay productos disponibles en el catálogo de tu comercio.",
                    color = CtTextMuted, fontSize = 13.sp)
            } else {
                catalog.forEach { p ->
                    val qty = cart.firstOrNull { it.product.id == p.id }?.quantity ?: 0
                    CtCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CtText)
                                if (!p.brand.isNullOrBlank())
                                    Text(p.brand, color = CtTextMuted, fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                CtBadge(if (p.type == "SERVICE") "Servicio" else "Producto", BadgeType.Neutral)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatSoles(p.creditPrice), fontWeight = FontWeight.Bold, color = CtText)
                                Text("Precio crédito", fontSize = 11.sp, color = CtTextMuted)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (qty == 0) {
                            CtSecondaryButton("Agregar a la solicitud", onClick = { requestVm.addToCart(p) })
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { requestVm.updateQty(p.id, qty - 1) },
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                        .background(CtPrimarySoft),
                                ) { Icon(Icons.Outlined.Remove, contentDescription = null, tint = CtPrimary) }
                                Text("$qty", modifier = Modifier.padding(horizontal = 16.dp),
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                IconButton(
                                    onClick = { requestVm.updateQty(p.id, qty + 1) },
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                        .background(CtPrimarySoft),
                                ) { Icon(Icons.Outlined.Add, contentDescription = null, tint = CtPrimary) }
                            }
                        }
                    }
                }
            }
        }
    }
}
