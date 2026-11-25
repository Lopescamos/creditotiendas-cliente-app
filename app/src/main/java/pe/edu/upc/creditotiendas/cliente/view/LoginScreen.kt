package pe.edu.upc.creditotiendas.cliente.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.creditotiendas.cliente.ui.theme.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onSignedIn: () -> Unit,
) {
    val context = LocalContext.current
    val loading by authVm.loading.collectAsState()
    val error by authVm.error.collectAsState()
    val signedIn by authVm.signedIn.collectAsState()

    LaunchedEffect(signedIn) { if (signedIn) onSignedIn() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hide by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(CtPrimary, CtPrimaryDark))
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CtSurface)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(CtPrimary),
                    contentAlignment = Alignment.Center,
                ) { Text("CT", color = CtAccent, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
                Spacer(Modifier.height(10.dp))
                Text("CreditoTiendas", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = CtText)
                Text("Consulta tu cuenta y gestiona tus pagos", color = CtTextMuted, fontSize = 13.sp)

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; authVm.clearError() },
                    label = { Text("Correo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; authVm.clearError() },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (hide) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { hide = !hide }) {
                            Icon(
                                if (hide) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null, tint = CtTextMuted,
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(CtDangerSoft).padding(12.dp),
                    ) { Text(error ?: "", color = Color(0xFF9B1C1C), fontSize = 13.sp) }
                }

                Spacer(Modifier.height(20.dp))
                CtPrimaryButton(
                    text = "Ingresar",
                    onClick = { authVm.signIn(email, password, context) },
                    loading = loading,
                    enabled = email.isNotBlank() && password.length >= 8,
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    "CreditoTiendas · SI642 UPC 2026-20",
                    color = CtTextLight, fontSize = 11.sp,
                )
            }
        }
    }
}
