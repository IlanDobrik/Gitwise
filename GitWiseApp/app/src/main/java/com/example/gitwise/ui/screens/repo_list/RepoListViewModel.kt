package com.example.gitwise.ui.screens.repo_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitwise.githubapi.GitHubAPI
import com.example.gitwise.githubapi.GitHubRepo
import com.example.gitwise.githubapi.fetchRepositoriesSuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RepoListUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val repos: List<GitHubRepo> = emptyList()
)

class RepoListViewModel : ViewModel() {

    private val _state = MutableStateFlow(RepoListUiState())
    val state: StateFlow<RepoListUiState> = _state

    fun load(token: String) {
        _state.value = RepoListUiState(loading = true)

        viewModelScope.launch {
            val api = GitHubAPI(token)
            val (repos, err) = api.fetchRepositoriesSuspend()

            _state.value =
                if (repos != null) RepoListUiState(loading = false, repos = repos)
                else RepoListUiState(loading = false, error = err ?: "Failed to fetch repositories")
        }
    }
}
