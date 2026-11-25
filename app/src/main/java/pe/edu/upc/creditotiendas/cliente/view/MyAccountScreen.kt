package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.model.beans.InstallmentResponse
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AccountViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.AuthViewModel

@Composable
fun MyAccountScreen(
    accountVm: AccountViewModel,
    authVm: AuthViewModel,
    onOpenPlan: (Long) -> Unit,
    onPay: () -> Unit,
    onRequest: () -> Unit,
    onStatements: () -> Unit,
    onHelp: () -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val profiles by accountVm.profiles.collectAsState()
    val profile by accountVm.selectedProfile.collectAsState()
    val sales by accountVm.sales.collectAsState()
    val payments by accountVm.payments.collectAsState()
    val loading by accountVm.loading.collectAsState()

    val name = remember { AuthViewModel.savedUserName(context) ?: "Cliente" }

    LaunchedEffect(Unit) { accountVm.loadProfiles(context) }

    // Totales derivados de ventas activas: el backend ya expone availableCredit por perfil,
    // así que el saldo pendiente = creditLimit - availableCredit.
    val outstanding = profile?.let { it.creditLimit - it.availableCredit } ?: 0.0

    Scaffold(
        topBar = { CtTopBar(title = "Mi cuenta", initials = name.firstOrNull()?.toString()) },
        bottomBar = {
            BottomTabBar(current = BottomTab.Account, onSelect = { tab ->
                when (tab) {
                    BottomTab.Account -> Unit
                    BottomTab.Credit  -> onRequest()
                    BottomTab.Help    -> onHelp()
                }
            })
        },
        containerColor = CtBg,
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(6.dp))

            // Hola, nombre
            Column {
                Text("Hola, ${name.split(" ").firstOrNull() ?: name}",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CtText)
                Text("Consulta tus compras, saldos y fechas de pago.",
                    fontSize = 13.sp, color = CtTextMuted)
            }

            if (profiles.size > 1) {
                ProfileSelector(profiles, profile?.id) { selected ->
                    accountVm.selectProfile(selected, context)
                }
            }

            // Card verde: saldo pendiente
            CtCard(dark = true) {
                Text("Saldo pendiente", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(formatSoles(outstanding), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Revisa el detalle de tu cuenta.", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
            }

            // Próximo pago
            NextPaymentCard(sales = sales, onDetail = onPay)

            // Tu cuenta: acciones rápidas
            Text("Tu cuenta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = CtText,
                modifier = Modifier.padding(top = 4.dp))

            QuickActionButton(
                icon = Icons.Outlined.Receipt,
                label = "Ver estado de cuenta",
                onClick = onStatements,
            )
            QuickActionButton(
                icon = Icons.Outlined.Payments,
                label = "Pagar mi cuenta",
                onClick = onPay,
            )
            QuickActionButton(
                icon = Icons.Outlined.AddShoppingCart,
                label = "Solicitar compra a crédito",
                onClick = onRequest,
            )

            // Movimientos recientes
            Text("Movimientos recientes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = CtText, modifier = Modifier.padding(top = 4.dp))

            when {
                loading -> Text("Cargando…", color = CtTextMuted, fontSize = 13.sp)
                sales.isEmpty() && payments.isEmpty() ->
                    Text("Aún no tienes movimientos en tu cuenta.", color = CtTextMuted, fontSize = 13.sp)
                else -> {
                    val salesItems = sales.take(3).map { s ->
                        MovementItem("C", "Compra a crédito",
                            "${s.saleDate ?: ""} · ${s.items?.size ?: 0} productos",
                            formatSoles(s.totalAmount),
                            when (s.status) {
                                "ACTIVE" -> "Activa" to BadgeType.Warning
                                "OVERDUE" -> "Vencida" to BadgeType.Danger
                                "PAID_ON_TIME" -> "Pagada" to BadgeType.Success
                                "PAID_LATE" -> "Pagada tarde" to BadgeType.Warning
                                else -> s.status to BadgeType.Neutral
                            },
                        ) { onOpenPlan(s.id) }
                    }
                    val payItems = payments.take(3).map { p ->
                        MovementItem("P", "Pago registrado",
                            "${p.paymentDate.take(10)} · venta #${p.creditSaleId}",
                            formatSoles(p.totalAmount),
                            "Pagado" to BadgeType.Success, onClick = null)
                    }
                    (salesItems + payItems).sortedByDescending { it.subtitle }.take(5).forEach { it ->
                        MovementRow(it)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onSignOut, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Cerrar sesión", color = CtTextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ProfileSelector(
    profiles: List<pe.edu.upc.creditotiendas.cliente.model.beans.CreditProfileResponse>,
    currentId: Long?,
    onSelect: (pe.edu.upc.creditotiendas.cliente.model.beans.CreditProfileResponse) -> Unit,
) {
    CtCard {
        Text("Elige el comercio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CtText)
        Spacer(Modifier.height(8.dp))
        profiles.forEach { p ->
            val selected = p.id == currentId
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (selected) CtPrimarySoft else Color.Transparent)
                    .clickable { onSelect(p) }.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tienda ${p.storeId}", modifier = Modifier.weight(1f), fontSize = 13.sp)
                Text(formatSoles(p.availableCredit), color = CtPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun NextPaymentCard(
    sales: List<pe.edu.upc.creditotiendas.cliente.model.beans.CreditSaleResponse>,
    onDetail: () -> Unit,
) {
    val active = sales.firstOrNull { it.status == "ACTIVE" || it.status == "OVERDUE" }
    CtCard {
        Text("PRÓXIMO PAGO", color = CtTextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
             letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        if (active == null) {
            Text("No tienes próximos pagos pendientes.", color = CtTextMuted, fontSize = 13.sp)
        } else {
            Text(formatSoles(active.totalAmount / active.installmentCount.coerceAtLeast(1)),
                 fontWeight = FontWeight.Bold, fontSize = 24.sp, color = CtText)
            Spacer(Modifier.height(2.dp))
            Text("Venta #${active.id} — método ${if (active.modality == "SINGLE_PAYMENT") "pago único" else "cuotas"}",
                 color = CtTextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CtBadge(if (active.status == "OVERDUE") "Vencida" else "Por vencer",
                        if (active.status == "OVERDUE") BadgeType.Danger else BadgeType.Warning)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDetail) {
                    Text("Ver detalle del cálculo ›", color = CtPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CtAccent.copy(alpha = 0.22f))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(CtPrimary),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = CtAccent, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold,
             fontSize = 14.sp, color = CtPrimaryDark)
        Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = CtPrimary,
             modifier = Modifier.size(16.dp))
    }
}

private data class MovementItem(
    val initial: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val badge: Pair<String, BadgeType>,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun MovementRow(item: MovementItem) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CtSurface).padding(14.dp)
            .then(if (item.onClick != null) Modifier.clickable { item.onClick.invoke() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(CtPrimarySoft),
            contentAlignment = Alignment.Center,
        ) { Text(item.initial, color = CtPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = CtText)
            Text(item.subtitle, fontSize = 12.sp, color = CtTextMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.amount, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CtText)
            Spacer(Modifier.height(4.dp))
            CtBadge(item.badge.first, item.badge.second)
        }
    }
}
