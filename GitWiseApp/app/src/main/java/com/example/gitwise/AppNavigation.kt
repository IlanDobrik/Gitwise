package com.example.gitwise

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gitwise.datatypes.Transaction
import com.example.gitwise.gitmanager.GitStatusScreen
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppDestinations {
    const val HOME = "home"
    const val CONFIG = "config"
    const val TRANSACTION_LIST = "transaction_list"
    const val TRANSACTION_EDITOR = "transaction_editor"
    const val GIT_STATUS = "git_status"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.HOME) {
        composable(AppDestinations.HOME) {
            MainScreen(
                navController = navController
            )
        }
        composable(AppDestinations.CONFIG) {
            ConfigScreen(navController = navController)
        }
        composable(AppDestinations.TRANSACTION_LIST) {
            TransactionListScreen(navController = navController)
        }
        composable(AppDestinations.GIT_STATUS) {
            GitStatusScreen(navController = navController)
        }
        composable(
            route = "${AppDestinations.TRANSACTION_EDITOR}?transaction={transaction}",
            arguments = listOf(
                navArgument("transaction") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val transactionJson = backStackEntry.arguments?.getString("transaction")
            val transaction = if (!transactionJson.isNullOrEmpty()) {
                try {
                    val decodedJson = URLDecoder.decode(transactionJson, StandardCharsets.UTF_8.toString())
                    Gson().fromJson(decodedJson, Transaction::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
            
            TransactionEditorScreen(
                navController = navController,
                initialTransaction = transaction
            )
        }
    }
}
