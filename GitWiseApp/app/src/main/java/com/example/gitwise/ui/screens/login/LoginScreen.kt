package com.example.gitwise.ui.screens.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gitwise.ui.screens.repo_list.RepoListActivity

class LoginActivity : ComponentActivity() {

    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val savedToken = prefs.getString(KEY_TOKEN, null)

        if (!savedToken.isNullOrBlank()) {
            vm.setToken(savedToken)
        }

        setContent {
            val state by vm.state.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LoginScreen(
                    state = state,
                    onTokenChange = vm::setToken,
                    onLogin = {
                        vm.login { token, username ->
                            prefs.edit().putString(KEY_TOKEN, token).apply()

                            startActivity(
                                Intent(this, RepoListActivity::class.java)
                                    .putExtra(RepoListActivity.Companion.EXTRA_TOKEN, token)
                                    .putExtra(RepoListActivity.Companion.EXTRA_USERNAME, username)
                            )
                            finish()
                        }
                    }
                )
            }
        }
    }

    companion object {
        private const val PREFS = "GitWise"
        private const val KEY_TOKEN = "token"
    }
}

@Composable
private fun LoginScreen(
    state: LoginUiState,
    onTokenChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Connect to GitHub", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.token,
            onValueChange = { onTokenChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Personal Access Token") },
            placeholder = { Text("ghp_...") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = state.error != null
        )

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.token.isNotBlank() && !state.loading
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
    }
}
