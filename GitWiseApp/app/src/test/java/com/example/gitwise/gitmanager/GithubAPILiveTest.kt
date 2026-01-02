package com.example.gitwise.gitmanager;

import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class GitHubAPIManualTest {

    @Test
    fun validateToken_manual() {
        val token = System.getenv("GITHUB_TOKEN")
            ?: error("Missing GITHUB_TOKEN env variable");

        val integration = GitHubAPI(token);

        val latch = CountDownLatch(1);

        integration.validateToken { success: Boolean, username: String? ->
            println("success=$success username=$username");
            latch.countDown();
        };

        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    fun fetchRepositories_manual() {
        val token = System.getenv("GITHUB_TOKEN")
            ?: error("Missing GITHUB_TOKEN env variable");

        val integration = GitHubAPI(token);

        val latch = CountDownLatch(1);

        integration.fetchRepositories { repos: List<GitHubRepo>?, error: String? ->
            if (repos != null) {
                repos.forEach {
                    println("name=${it.name}");
                    println("clone_url=${it.clone_url}");
                    println("ssh_url=${it.ssh_url}");
                    println("html_url=${it.html_url}");
                    println("-----");
                };
            } else {
                println("error=$error");
            };
            latch.countDown();
        };

        latch.await(10, TimeUnit.SECONDS);
    }
}
