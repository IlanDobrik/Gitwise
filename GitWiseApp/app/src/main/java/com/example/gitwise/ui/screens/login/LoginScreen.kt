package com.example.gitwise.ui.screens.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gitwise.ui.screens.repo_list.RepoListActivity
import com.example.gitwise.core.storage.TokenStore
import com.example.gitwise.ui.screens.login.LoginViewModel


class LoginActivity : ComponentActivity() {

    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = TokenStore(this)

        setContent {
            val state by vm.state.collectAsState()
            var autoTried by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                if (!autoTried) {
                    autoTried = true
                    val saved = store.loadToken()
                    if (!saved.isNullOrBlank()) {
                        vm.tryAutoLogin(
                            savedToken = saved,
                            onSuccess = { token, username ->
                                startActivity(
                                    Intent(this@LoginActivity, RepoListActivity::class.java)
                                        .putExtra(RepoListActivity.EXTRA_TOKEN, token)
                                        .putExtra(RepoListActivity.EXTRA_USERNAME, username)
                                )
                                finish()
                            },
                            onInvalid = {
                                store.clear()
                                vm.setToken("")
                                vm.setError("Saved token is invalid. Please login again.")
                            }
                        )
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                LoginScreen(
                    state = state,
                    onTokenChange = vm::setToken,
                    onLogin = {
                        vm.login(
                            onSuccess = { token, username ->
                                store.saveToken(token)
                                startActivity(
                                    Intent(this@LoginActivity, RepoListActivity::class.java)
                                        .putExtra(RepoListActivity.EXTRA_TOKEN, token)
                                        .putExtra(RepoListActivity.EXTRA_USERNAME, username)
                                )
                                finish()
                            },
                            onInvalid = { msg ->
                                vm.setError(msg)
                            }
                        )
                    }
                )
            }
        }
    }
}


@Composable
fun LoginScreen(
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

