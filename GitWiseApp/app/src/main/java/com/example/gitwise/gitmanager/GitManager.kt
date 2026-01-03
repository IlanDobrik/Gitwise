package com.example.gitwise.gitmanager

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.example.gitwise.config.getConfig
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File


val REPO_URL = "https://github.com/IlanDobrik/Gitwise.git"

class GitManager(
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

    fun commit(context: Context, message: String) {
        val config = getConfig(context)
        if (!config.commit) {
            Log.i(TAG, "Not committing")
            // Do nothing
            return
        }

        Log.i(TAG, "commiting")
        val git = Git.open(repoPath)
        try {
            git.add().addFilepattern(".").call()
            git.commit().setMessage(message).call()
            Log.i(TAG, "committed successfully")
        } finally {
            git.close()
        }
    }

    fun commitPush(context: Context, message: String) {
        commit(context, message)
        push()
    }

    fun push() {
        Log.i(TAG, "pushing")
        val git = Git.open(repoPath)
        try {
            git.push().call()
            Log.i(TAG, "pushed successfully")
        } finally {
            git.close()
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
        val git = Git.open(repoPath)
        try {
            fetch()

            Log.i(TAG, "Attempting to checkout: $branch")

            val localBranchExists = git.branchList().call()
                .any { it.name == "refs/heads/$branch" }

            val checkoutCommand = git.checkout().setName(branch)

            if (!localBranchExists) {
                Log.i(TAG, "Branch '$branch' does not exist locally. Creating it from 'origin/$branch'")
                // 3. If local doesn't exist, create it tracking the remote branch
                checkoutCommand.setCreateBranch(true)
                checkoutCommand.setStartPoint("origin/$branch")
            }

            checkoutCommand.call()
            Log.i(TAG, "Checked out to $branch successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Checkout failed for branch '$branch': ${e.message}")
            throw e
        } finally {
            git.close()
        }
    }

    fun pull() {
        val git = Git.open(repoPath)
        try {
            val pullCommand = git.pull()
            getCredentialsProvider()?.let {
                pullCommand.setCredentialsProvider(it)
            }
            pullCommand.call()
        } finally {
            git.close()
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
        Log.i(TAG, "Error cloning repository: " + e)
    }

}

fun getRepoPath(context: Context) : File {
    return File(context.filesDir.path + "/GitWise")
}

fun getGitManager(context: Context) : GitManager{
    val repoPath = getRepoPath(context)
    val gitManager = GitManager(repoPath, null, null)
    val config = getConfig(context)

    if (!repoPath.exists()) {
        clone(REPO_URL, repoPath)
    }

    gitManager.checkout(config.branchName ?: "data")
    gitManager.pull()
    return gitManager
}

fun getDataFile(context: Context) : File {
    getGitManager(context) // Make sure git repo is initialized
    return File(getRepoPath(context).absolutePath + "\\data.json")
}