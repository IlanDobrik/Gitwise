package com.example.gitwise.gitmanager

import android.content.Context
import com.example.gitwise.logger.TAG
import android.util.Log
import org.eclipse.jgit.api.Git
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
            git.push().call()
            Log.i(TAG, "pushed successfully")
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

    fun pull() {
        Git.open(repoPath).use { git ->
            val pullCommand = git.pull()
            getCredentialsProvider()?.let {
                pullCommand.setCredentialsProvider(it)
            }
            pullCommand.call()
        }
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
    }
    catch (e: Exception) {
        Log.i(TAG, "Error cloning repository: $e")
    }

}

fun getGitManager(repoBase: File, commit: Boolean) : GitManager{
    val gitManager = GitManager(commit, repoBase, null, null)

    if (!repoBase.exists()) {
        clone(REPO_URL, repoBase)
    }

    // TODO change when move to repo orientation
    gitManager.checkout("data")
    gitManager.pull()
    return gitManager
}

fun getDataDirectory(repoBase: File) : File {
    return File(repoBase, "transactions")
}

fun getRepoBase(context: Context) : File {
    return File(context.filesDir, "GitWise")
}
