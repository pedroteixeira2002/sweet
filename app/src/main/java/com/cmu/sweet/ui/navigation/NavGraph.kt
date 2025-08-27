package com.cmu.sweet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cmu.sweet.ui.auth.LoginScreen
import com.cmu.sweet.ui.home.HomeScreen
import com.cmu.sweet.ui.auth.SignUpScreen
import com.cmu.sweet.ui.establishment.EstablishmentDetailsScreen
import com.cmu.sweet.ui.home.SplashScreen
import com.cmu.sweet.ui.home.WelcomeScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object EstablishmentDetails : Screen("establishmentDetails/{establishmentId}") {
        fun createRoute(establishmentId: String) = "establishmentDetails/$establishmentId"
    }
}

@Composable
fun AppNavGraph(startDestination: String = Screen.Splash.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onSignUpClick = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Signup.route) {
            SignUpScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetails = { establishmentId ->
                    navController.navigate(Screen.EstablishmentDetails.createRoute(establishmentId))
                }
                // Adicione outros parâmetros que HomeScreen possa precisar do NavController, se houver
                // ex: onLogout = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Home.route){inclusive = true} }}
            )
        }

        composable(
            route = Screen.EstablishmentDetails.route,
            arguments = listOf(navArgument("establishmentId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val establishmentId = backStackEntry.arguments?.getString("establishmentId")
            if (establishmentId != null) {
                EstablishmentDetailsScreen(
                    establishmentId = establishmentId,
                    onNavigateBack = { navController.popBackStack() }
                    // O ViewModel dentro de EstablishmentDetailsScreen
                    // usará o establishmentId (provavelmente via SavedStateHandle)
                )
            } else {
                // Tratar ID nulo, talvez navegar de volta ou logar um erro
                navController.popBackStack() // Exemplo simples
            }
        }
    }
}
