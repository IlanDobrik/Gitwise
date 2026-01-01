package com.example.gitwise.gitmanager

import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class GitManagerTest {

    private lateinit var repoDir: File
    private lateinit var git: Git
    private lateinit var gitManager: GitManager

    @Before
    fun setUp() {
        repoDir = File.createTempFile("test_repo", "")
        repoDir.delete()
        repoDir.mkdirs()

        // Initialize a new git repository
        git = Git.init().setDirectory(repoDir).call()
        
        // Config user for commit
        val config = git.repository.config
        config.setString("user", null, "name", "Test User")
        config.setString("user", null, "email", "test@example.com")
        config.save()

        gitManager = GitManager(repoDir)
    }

    @After
    fun tearDown() {
        git.close()
        repoDir.deleteRecursively()
    }

    @Test
    fun testCommit() {
        // Create a file
        val file = File(repoDir, "test.txt")
        file.writeText("Hello World")

        gitManager.commit("Initial commit")

        val status = git.status().call()
        assertTrue(status.isClean)

        val logs = git.log().call().toList()
        assertEquals(1, logs.size)
        assertEquals("Initial commit", logs[0].fullMessage)
    }
    
    // Testing pull requires a remote, which is hard to mock with simple temporary folders 
    // without setting up a second repo as remote.
    // We will skip pull test for this unit test or implement a more complex setup if needed.
    // For now, commit test proves GitManager can interact with the repo.
}
