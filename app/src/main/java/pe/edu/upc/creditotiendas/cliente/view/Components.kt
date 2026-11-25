package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun formatSoles(amount: Double): String =
    "S/ " + NumberFormat.getNumberInstance(Locale("es","PE")).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }.format(amount)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CtTopBar(
    title: String,
    initials: String? = null,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(CtPrimary),
                    contentAlignment = Alignment.Center,
                ) { Text("MB", color = CtAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, color = CtText, fontSize = 17.sp)
            }
        },
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = CtText)
                }
            }
        },
        actions = {
            if (initials != null) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center,
                ) { Text(initials.take(1).uppercase(), color = Color(0xFFC2410C), fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.width(12.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CtSurface),
    )
}

@Composable
fun CtCard(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (dark) CtPrimary else CtSurface)
            .border(1.dp, if (dark) CtPrimary else CtBorder, RoundedCornerShape(14.dp))
            .padding(18.dp),
        content = content,
    )
}

@Composable
fun CtBadge(text: String, type: BadgeType) {
    val (bg, fg) = when (type) {
        BadgeType.Success -> CtSuccessSoft to Color(0xFF126A36)
        BadgeType.Warning -> CtWarningSoft to Color(0xFF92540C)
        BadgeType.Danger  -> CtDangerSoft  to Color(0xFF9B1C1C)
        BadgeType.Neutral -> Color(0xFFEEF0ED) to CtTextMuted
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
enum class BadgeType { Success, Warning, Danger, Neutral }

@Composable
fun CtPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CtAccent,
            contentColor = CtPrimaryDark,
            disabledContainerColor = Color(0xFFE3E7DE),
            disabledContentColor = CtTextLight,
        ),
    ) {
        if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CtPrimary, strokeWidth = 2.dp)
        else Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun CtSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CtPrimary),
    ) {
        Text(text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun BottomTabBar(
    current: BottomTab,
    onSelect: (BottomTab) -> Unit,
) {
    NavigationBar(containerColor = CtSurface, tonalElevation = 0.dp) {
        BottomTab.values().forEach { tab ->
            NavigationBarItem(
                selected = current == tab,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            BottomTab.Account -> Icons.Outlined.AccountBalanceWallet
                            BottomTab.Credit  -> Icons.Outlined.ShoppingCart
                            BottomTab.Help    -> Icons.Outlined.HelpOutline
                        },
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CtPrimary,
                    selectedTextColor = CtPrimary,
                    indicatorColor = CtPrimarySoft,
                    unselectedIconColor = CtTextMuted,
                    unselectedTextColor = CtTextMuted,
                ),
            )
        }
    }
}
enum class BottomTab(val label: String) { Account("Cuenta"), Credit("Crédito"), Help("Ayuda") }

@Composable
fun InfoAlert(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(CtWarningSoft).padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF92540C), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, color = Color(0xFF92540C), fontSize = 12.5.sp, lineHeight = 17.sp)
    }
}
