package com.example.gitwise.gitmanager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File
import java.util.Date

data class GitCommit(
    val id: String,
    val shortMessage: String,
    val authorName: String,
    val date: Date
)

data class GitFile(
    val path: String,
    val mode: String
)

class GitStatusViewModel : ViewModel() {
    private val _commits = MutableStateFlow<List<GitCommit>>(emptyList())
    val commits: StateFlow<List<GitCommit>> = _commits.asStateFlow()

    private val _selectedCommitFiles = MutableStateFlow<List<GitFile>>(emptyList())
    val selectedCommitFiles: StateFlow<List<GitFile>> = _selectedCommitFiles.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadCommits(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val repoBase = getRepoBase(context)
                if (repoBase.exists()) {
                    Git.open(repoBase).use { git ->
                        val log = git.log().call()
                        val commitList = mutableListOf<GitCommit>()
                        for (commit in log) {
                            commitList.add(
                                GitCommit(
                                    id = commit.name,
                                    shortMessage = commit.shortMessage,
                                    authorName = commit.authorIdent.name,
                                    date = commit.authorIdent.`when`
                                )
                            )
                        }
                        _commits.value = commitList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCommitFiles(context: Context, commitId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val repoBase = getRepoBase(context)
                if (repoBase.exists()) {
                    Git.open(repoBase).use { git ->
                        val repo = git.repository
                        val commitIdObj = ObjectId.fromString(commitId)
                        
                        // Resolve the commit
                        val walk = org.eclipse.jgit.revwalk.RevWalk(repo)
                        val commit: RevCommit = walk.parseCommit(commitIdObj)
                        val tree = commit.tree
                        
                        // Walk the tree
                        val treeWalk = TreeWalk(repo)
                        treeWalk.addTree(tree)
                        treeWalk.isRecursive = true
                        
                        val fileList = mutableListOf<GitFile>()
                        while (treeWalk.next()) {
                            fileList.add(
                                GitFile(
                                    path = treeWalk.pathString,
                                    mode = treeWalk.fileMode.toString()
                                )
                            )
                        }
                        _selectedCommitFiles.value = fileList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearSelectedCommit() {
        _selectedCommitFiles.value = emptyList()
    }
}
