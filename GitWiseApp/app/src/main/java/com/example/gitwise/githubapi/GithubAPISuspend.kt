package com.example.gitwise.githubapi

import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

// Wraps the functions, so it will be usable in the UI
suspend fun GitHubAPI.validateTokenSuspend(): Pair<Boolean, String?> =
    suspendCancellableCoroutine { cont ->
        val resumed = AtomicBoolean(false)

        fun resumeOnce(value: Pair<Boolean, String?>) {
            if (resumed.compareAndSet(false, true) && cont.isActive) {
                cont.resume(value)
            }
        }

        try {
            validateToken { success, msg ->
                resumeOnce(success to msg)
            }
        } catch (t: Throwable) {
            resumeOnce(false to ("Crash: ${t.message}"))
        }
    }

// Wraps the functions, so it will be usable in the UI
suspend fun GitHubAPI.fetchRepositoriesSuspend(): Pair<List<GitHubRepo>?, String?> =
    suspendCancellableCoroutine { cont ->
        val resumed = AtomicBoolean(false)

        fun resumeOnce(value: Pair<List<GitHubRepo>?, String?>) {
            if (resumed.compareAndSet(false, true) && cont.isActive) {
                cont.resume(value)
            }
        }

        try {
            fetchRepositories { repos, err ->
                resumeOnce(repos to err)
            }
        } catch (t: Throwable) {
            resumeOnce(null to ("Crash: ${t.message}"))
        }
    }
