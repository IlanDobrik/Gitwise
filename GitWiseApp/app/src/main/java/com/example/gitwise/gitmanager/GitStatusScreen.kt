package com.example.gitwise.gitmanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.util.Date

@Composable
fun GitStatusScreen(
    navController: NavController,
    viewModel: GitStatusViewModel = viewModel()
) {
    val context = LocalContext.current
    val commits by viewModel.commits.collectAsState()
    val selectedCommitFiles by viewModel.selectedCommitFiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // If files are selected, we are in "File View" mode for a specific commit
    val isFileView = selectedCommitFiles.isNotEmpty()

    LaunchedEffect(Unit) {
        viewModel.loadCommits(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isFileView) {
                TopAppBar(
                    title = { Text("Commit Files") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelectedCommit() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back to commits")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isFileView) {
                FileList(files = selectedCommitFiles)
            } else {
                CommitList(
                    commits = commits,
                    onCommitClick = { commit ->
                        viewModel.loadCommitFiles(context, commit.id)
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun CommitList(
    commits: List<GitCommit>,
    onCommitClick: (GitCommit) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Commits",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(commits) { commit ->
            CommitItem(commit = commit, onClick = { onCommitClick(commit) })
            HorizontalDivider()
        }
    }
}

@Composable
fun CommitItem(
    commit: GitCommit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = commit.shortMessage,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = commit.authorName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = commit.date.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileList(files: List<GitFile>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
             Text(
                text = "Files",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(files) { file ->
            FileItem(file = file)
            HorizontalDivider()
        }
    }
}

@Composable
fun FileItem(file: GitFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = file.path,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = file.mode,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit
) {
    CenterAlignedTopAppBar(
        title = title,
        navigationIcon = navigationIcon
    )
}


@Preview(showBackground = true)
@Composable
fun GitStatusScreenPreview() {
    com.example.gitwise.ui.theme.GitwiseTheme {
        // Mock data for preview
        val commits = listOf(
            GitCommit("1", "Initial commit", "Alice", Date()),
            GitCommit("2", "Added transaction", "Bob", Date())
        )
        
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                CommitList(commits = commits, onCommitClick = {})
            }
        }
    }
}
