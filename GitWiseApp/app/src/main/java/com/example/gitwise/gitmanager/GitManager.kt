package com.example.gitwise.gitmanager

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

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

    fun commit(message: String) {
        val git = Git.open(repoPath)
        try {
            git.add().addFilepattern(".").call()
            git.commit().setMessage(message).call()
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