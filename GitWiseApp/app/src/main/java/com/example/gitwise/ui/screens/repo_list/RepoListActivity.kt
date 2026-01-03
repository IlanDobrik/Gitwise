package com.example.gitwise.ui.screens.repo_list

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gitwise.githubapi.GitHubRepo
import com.example.gitwise.ui.screens.login.LoginActivity

class RepoListActivity : ComponentActivity() {

    private val vm: RepoListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = intent.getStringExtra(EXTRA_TOKEN)
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        vm.load(token)

        setContent {
            val state by vm.state.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                RepoListScreen(
                    state = state,
                    onLogout = {
                        getSharedPreferences("GitWise", MODE_PRIVATE).edit().clear().apply()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_TOKEN = "extra_token"
        const val EXTRA_USERNAME = "extra_username"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepoListScreen(
    state: RepoListUiState,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Repositories") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.repos) { repo ->
                        RepoRow(repo)
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoRow(repo: GitHubRepo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* TODO: open repo details */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(repo.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(repo.html_url, style = MaterialTheme.typography.bodySmall)
        }
    }
}
