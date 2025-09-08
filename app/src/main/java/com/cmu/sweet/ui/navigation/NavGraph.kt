package com.cmu.sweet.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cmu.sweet.data.local.SweetDatabase
import com.cmu.sweet.data.repository.EstablishmentRepository
import com.cmu.sweet.data.repository.UserRepository
import com.cmu.sweet.ui.screen.LoginScreen
import com.cmu.sweet.ui.screen.HomeScreen
import com.cmu.sweet.ui.screen.SignUpScreen
import com.cmu.sweet.ui.screen.AddEstablishmentScreen
import com.cmu.sweet.ui.screen.EstablishmentDetailsScreen
import com.cmu.sweet.ui.screen.SplashScreen
import com.cmu.sweet.ui.screen.WelcomeScreen
import com.cmu.sweet.ui.screen.EditProfileScreen
import com.cmu.sweet.ui.screen.AddReviewScreen
import com.cmu.sweet.ui.screen.SettingsScreen
import com.cmu.sweet.view_model.EstablishmentDetailsViewModel
import com.cmu.sweet.view_model.LightSensorViewModel
import com.cmu.sweet.view_model.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object EstablishmentDetails : Screen("establishmentDetails/{establishmentId}") {
        fun createRoute(establishmentId: String) = "establishmentDetails/$establishmentId"
    }

    object AddEstablishment : Screen("addEstablishment")
    object AddReview : Screen("addReview?establishmentId={establishmentId}") {
        fun createRoute(establishmentId: String? = null): String {
            return if (establishmentId != null) {
                "addReview?establishmentId=$establishmentId"
            } else {
                "addReview"
            }
        }
    }

    object EditProfile : Screen("editProfile/{userId}") {
        const val userIdArg = "userId"
        val routeWithArgs = "editProfile/{$userIdArg}"
        val arguments = listOf(
            navArgument(userIdArg) { type = NavType.StringType }
        )

        fun createRoute(userId: String) = "editProfile/$userId"
    }
}

@Composable
fun AppNavGraph(
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val userDao = SweetDatabase.getInstance(context).userDao()
    val establishmentDao = SweetDatabase.getInstance(context).establishmentDao()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userRepository = UserRepository(firestore, userDao, firebaseAuth)
    val establishmentRepository = EstablishmentRepository(firestore, establishmentDao)
    val lightSensorViewModel = remember { LightSensorViewModel(context) }



    NavHost(navController = navController, startDestination = Screen.Splash.route) {

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
                },
                onNavigateToAddEstablishment = { navController.navigate(Screen.AddEstablishment.route) },
                onNavigateToAddReview = { establishmentId ->
                    navController.navigate(Screen.AddReview.createRoute(establishmentId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToEditProfile = { userId ->
                    navController.navigate(Screen.EditProfile.createRoute(userId)) // Define this screen/route
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable(
            route = "establishmentDetails/{establishmentId}",
            arguments = listOf(navArgument("establishmentId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            EstablishmentDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel<EstablishmentDetailsViewModel>(
                    factory = EstablishmentDetailsViewModel.Factory(
                        context.applicationContext as Application,
                        establishmentRepository
                    )
                )
            )
        }

        composable(Screen.AddEstablishment.route) {
            AddEstablishmentScreen(navController)
        }

        composable(
            route = Screen.AddReview.route,
            arguments = listOf(navArgument("establishmentId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val establishmentId = backStackEntry.arguments?.getString("establishmentId")
            AddReviewScreen(navController = navController, establishmentId = "40tmGYETnTMtuXTHfVHj")
        }

        composable(
            route = Screen.EditProfile.routeWithArgs,
            arguments = Screen.EditProfile.arguments
        ) { backStackEntry ->
            EditProfileScreen(
                onNavigateBack = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("profile_updated", true)

                    navController.popBackStack()
                }
            )
        }
        composable(route = "settings") {
            SettingsScreen(navController, settingsViewModel, lightSensorViewModel)
        }
    }
}