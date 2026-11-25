package pe.edu.upc.creditotiendas.cliente.view.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.creditotiendas.cliente.view.*
import pe.edu.upc.creditotiendas.cliente.viewmodel.AccountViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.AuthViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.PaymentViewModel
import pe.edu.upc.creditotiendas.cliente.viewmodel.PurchaseRequestViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"                     // Mi cuenta (US-03)
    const val INSTALLMENTS = "installments/{saleId}"
    const val REQUEST_ELIGIBILITY = "request/eligibility"
    const val REQUEST_CATALOG = "request/catalog"
    const val REQUEST_REVIEW = "request/review"   // US-09
    const val PAY = "pay"
    const val STATEMENTS = "statements"
    const val HELP = "help"
    fun installments(saleId: Long) = "installments/$saleId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val authVm: AuthViewModel = viewModel()
    val accountVm: AccountViewModel = viewModel()
    val payVm: PaymentViewModel = viewModel()
    val requestVm: PurchaseRequestViewModel = viewModel()

    LaunchedEffect(Unit) { authVm.hydrateFromStorage(context) }
    val signedIn by authVm.signedIn.collectAsState()
    val startDestination = if (signedIn) Routes.HOME else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authVm = authVm,
                onSignedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            MyAccountScreen(
                accountVm = accountVm,
                authVm = authVm,
                onOpenPlan = { saleId -> navController.navigate(Routes.installments(saleId)) },
                onPay = { navController.navigate(Routes.PAY) },
                onRequest = { navController.navigate(Routes.REQUEST_ELIGIBILITY) },
                onStatements = { navController.navigate(Routes.STATEMENTS) },
                onHelp = { navController.navigate(Routes.HELP) },
                onSignOut = {
                    authVm.signOut(context)
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
            )
        }

        composable(Routes.INSTALLMENTS) { back ->
            val saleId = back.arguments?.getString("saleId")?.toLongOrNull() ?: 0L
            InstallmentsScreen(
                accountVm = accountVm,
                saleId = saleId,
                onBack = { navController.popBackStack() },
                onHelp = { navController.navigate(Routes.HELP) },
            )
        }

        composable(Routes.REQUEST_ELIGIBILITY) {
            EligibilityScreen(
                accountVm = accountVm,
                requestVm = requestVm,
                onContinue = { navController.navigate(Routes.REQUEST_CATALOG) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.REQUEST_CATALOG) {
            RequestCatalogScreen(
                accountVm = accountVm,
                requestVm = requestVm,
                onReview = { navController.navigate(Routes.REQUEST_REVIEW) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.REQUEST_REVIEW) {
            ReviewRequestScreen(
                accountVm = accountVm,
                requestVm = requestVm,
                onDone = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PAY) {
            PayScreen(
                accountVm = accountVm,
                payVm = payVm,
                onDone = {
                    payVm.reset()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.STATEMENTS) {
            StatementsScreen(
                accountVm = accountVm,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
    }
}
