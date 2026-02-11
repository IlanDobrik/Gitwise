package com.example.gitwise.gitmanager

import android.content.Context
import com.example.gitwise.logger.TAG
import android.util.Log
import com.example.gitwise.datatypes.Person
import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File


const val REPO_URL = "https://github.com/IlanDobrik/Gitwise.git"

class GitManager(
    private val commit: Boolean,
    private val repoPath: File,
    private val username: String? = null,
    private val token: String? = null
) {
    private fun getCredentialsProvider(): UsernamePasswordCredentialsProvider? {
        return if (username != null && token != null) {
            UsernamePasswordCredentialsProvider(username, token)
        } else {
            null
        }
    }

    fun commit(message: String) {
        if (!commit) {
            Log.i(TAG, "Not committing")
            // Do nothing
            return
        }

        Log.i(TAG, "commiting")
        Git.open(repoPath).use { git ->
            git.add().addFilepattern(".").call()
            git.commit().setMessage(message).call()
            Log.i(TAG, "committed successfully")
        }
    }

    fun commitPush(message: String) {
        commit(message)
        push()
    }

    fun push() {
        Log.i(TAG, "pushing")
        Git.open(repoPath).use { git ->
            try {
                val pushCommand = git.push()
                getCredentialsProvider()?.let {
                    pushCommand.setCredentialsProvider(it)
                }
                pushCommand.call()
                Log.i(TAG, "pushed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Push failed", e)
                throw e
            }
        }
    }

    fun fetch() {
        Git.open(repoPath).use { git ->
            val remotes = git.remoteList().call()
            for (remote in remotes) {
                val fetch = git.fetch()
                fetch.setRemote(remote.name)
                fetch.setRefSpecs(remote.fetchRefSpecs)
                fetch.call()
            }
        }
    }

    fun checkout(branch: String) {
        Log.i(TAG, "Attempting to checkout: $branch")
        try {
            Git.open(repoPath).use { git ->
                fetch()
                val localBranchExists = git.branchList().call()
                    .any { it.name == "refs/heads/$branch" }
                val checkoutCommand = git.checkout().setName(branch)

                if (!localBranchExists) {
                    Log.i(TAG, "Branch '$branch' does not exist locally. Creating it from 'origin/$branch'")
                    checkoutCommand.setCreateBranch(true)
                    checkoutCommand.setStartPoint("origin/$branch")
                }

                checkoutCommand.call()
                Log.i(TAG, "Checked out to $branch successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Checkout failed for branch '$branch': ${e.message}")
            throw e
        }
    }

    // Returns true if rebase was successful, false if it failed and reset was performed
    fun pull(): Boolean {
        Git.open(repoPath).use { git ->
            try {
                val pullCommand = git.pull()
                pullCommand.setRebase(true)
                getCredentialsProvider()?.let {
                    pullCommand.setCredentialsProvider(it)
                }
                
                val result = pullCommand.call()
                if (!result.isSuccessful) {
                    throw Exception("Pull returned unsuccessful result: ${result.mergeResult.mergeStatus}")
                }
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Pull failed with rebase, resetting to remote", e)
                
                try {
                    val currentBranch = git.repository.branch
                    Log.i(TAG, "Resetting $currentBranch to origin/$currentBranch")
                    // Fetch to ensure we have the latest
                    try {
                        val fetch = git.fetch()
                        getCredentialsProvider()?.let {
                            fetch.setCredentialsProvider(it)
                        }
                        fetch.call()
                    } catch (fetchEx: Exception) {
                         Log.e(TAG, "Fetch failed before reset", fetchEx)
                         // Continue to try reset anyway if we have the ref?
                    }

                    git.reset()
                        .setMode(ResetCommand.ResetType.HARD)
                        .setRef("origin/$currentBranch")
                        .call()
                    
                    return false
                } catch (resetEx: Exception) {
                    Log.e(TAG, "Reset also failed", resetEx)
                    throw resetEx
                }
            }
        }
    }

    fun getRepoMembers(): List<String> {
        val membersDir = getMembersDirectory(repoPath)
        if (!membersDir.exists()) {
            return emptyList()
        }
        val gson = Gson()
        return membersDir.listFiles()?.mapNotNull { file ->
            try {
                val person = gson.fromJson(file.readText(), Person::class.java)
                person.name
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse member file: ${file.name}", e)
                null
            }
        } ?: emptyList()
    }

    fun addMember(person: Person) {
        val membersDir = getMembersDirectory(repoPath)
        if (!membersDir.exists()) {
            membersDir.mkdirs()
        }
        val gson = Gson()
        val personFile = File(membersDir, person.name)
        personFile.writeText(gson.toJson(person))
        commit("Added member: ${person.name}")
    }
}

private fun clone(url: String, path: File) {
    path.deleteRecursively()

    try {
        Git.cloneRepository()
            .setURI(url)
            .setDirectory(path)
            .call()
        Log.i(TAG, "cloned successfully")
    } catch (e: Exception) {
        Log.i(TAG, "Error cloning repository: $e")
    }

}

fun getGitManager(repoBase: File, commit: Boolean, branchName: String?): GitManager {
    val gitManager = GitManager(commit, repoBase, null, null)

    if (!repoBase.exists()) {
        clone(REPO_URL, repoBase)
    }

    gitManager.checkout(branchName ?: "data")
    // Note: removed implicit pull() to allow caller to handle pull results (like reset Toast)
    return gitManager
}

fun getDataDirectory(repoBase: File): File {
    return File(repoBase, "transactions")
}

fun getMembersDirectory(repoBase: File): File {
    return File(repoBase, "members")
}

fun getRepoBase(context: Context): File {
    return File(context.filesDir, "GitWise")
}
