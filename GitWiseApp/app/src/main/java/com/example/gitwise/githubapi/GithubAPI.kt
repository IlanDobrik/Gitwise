package com.example.gitwise.githubapi

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import okhttp3.MediaType.Companion.toMediaType;
import okhttp3.RequestBody.Companion.toRequestBody;
import java.io.IOException;

/**
 * Data model for the GitHub User API response.
 */
data class GitHubUser(val login: String);

/**
 * Data model for a GitHub Repository API response.
 * Includes clone links (HTTPS/SSH) and browser link.
 */
data class GitHubRepo(
    val name: String,
    val clone_url: String,
    val ssh_url: String,
    val html_url: String
);

/**
 * Data model for a GitHub "create repo" response (partial).
 */
data class GitHubCreatedRepo(val name: String, val html_url: String);

/**
 * Data model for GitHub error JSON (partial).
 */
data class GitHubErrorResponse(val message: String?);

/**
 * Handles all network interactions with the GitHub REST API.
 * This class is stateful and requires a Personal Access Token (PAT) for all operations.
 * @property token The GitHub Personal Access Token used for Bearer Authentication.
 */
class GitHubAPI(private val token: String) {

    private val client = OkHttpClient();
    private val gson = Gson();

    /**
     * Validates the token provided in the constructor by fetching the user's profile.
     * @param onResult Callback returning (Success: Boolean, Username/Error: String?)
     * If successful, the string is the GitHub username.
     * If failed, the string contains the error message.
     */
    fun validateToken(onResult: (Boolean, String?) -> Unit) {
        val request = Request.Builder()
            .url("https://api.github.com/user")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Network error: ${e.message}");
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string();
                if (response.isSuccessful && body != null) {
                    try {
                        val user = gson.fromJson(body, GitHubUser::class.java);
                        onResult(true, user.login);
                    } catch (e: Exception) {
                        onResult(false, "Parsing error: Unable to read profile.");
                    }
                } else {
                    val msg = parseGitHubMessage(body) ?: "Invalid token: GitHub rejected the request.";
                    onResult(false, msg);
                }
            }
        });
    }

    /**
     * Fetches the repositories for the authenticated user (full objects).
     * Includes clone links: clone_url (HTTPS), ssh_url (SSH), html_url (browser).
     * @param onResult Callback returning (Repos: List<GitHubRepo>?, Error: String?)
     */
    fun fetchRepositories(onResult: (List<GitHubRepo>?, String?) -> Unit) {
        val request = Request.Builder()
            .url("https://api.github.com/user/repos?sort=updated&per_page=100")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null, "Network error: ${e.message}");
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string();
                if (response.isSuccessful && body != null) {
                    try {
                        val type = object : TypeToken<List<GitHubRepo>>() {}.type;
                        val repos: List<GitHubRepo> = gson.fromJson(body, type);
                        onResult(repos, null);
                    } catch (e: Exception) {
                        onResult(null, "Parsing error: Unable to read repositories.");
                    }
                } else {
                    val msg = parseGitHubMessage(body) ?: "HTTP ${response.code} (Could not fetch repos)";
                    onResult(null, "Error: ${response.code} ($msg)");
                }
            }
        });
    }

    /**
     * Creates a repository for the authenticated user.
     * @param onResult Callback returning (Success: Boolean, RepoUrl/Error: String?)
     * On success, returns the created repository HTML URL.
     * On failure, returns an error message.
     */
    fun createRepository(
        name: String,
        isPrivate: Boolean = true,
        description: String? = null,
        autoInit: Boolean = false,
        onResult: (Boolean, String?) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType();
        val payloadMap = mutableMapOf<String, Any>(
            "name" to name,
            "private" to isPrivate,
            "auto_init" to autoInit
        );
        if (description != null) { payloadMap["description"] = description; };

        val json = gson.toJson(payloadMap);
        val body = json.toRequestBody(mediaType);

        val request = Request.Builder()
            .url("https://api.github.com/user/repos")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .post(body)
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Network error: ${e.message}");
            }

            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string();
                if (response.isSuccessful && respBody != null) {
                    try {
                        val created = gson.fromJson(respBody, GitHubCreatedRepo::class.java);
                        onResult(true, created.html_url);
                    } catch (e: Exception) {
                        onResult(true, "Created, but could not parse response.");
                    }
                } else {
                    val msg = parseGitHubMessage(respBody) ?: respBody ?: "HTTP ${response.code}";
                    onResult(false, "Create failed: ${response.code} $msg");
                }
            }
        });
    }

    /**
     * Deletes a repository. Requires admin rights to that repo.
     * Endpoint: DELETE /repos/{owner}/{repo}
     */
    fun deleteRepository(
        owner: String,
        repo: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .delete()
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Network error: ${e.message}");
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string();
                if (response.code == 204) {
                    onResult(true, "Deleted.");
                } else {
                    val msg = parseGitHubMessage(body) ?: body ?: "HTTP ${response.code}";
                    onResult(false, "Delete failed: ${response.code} $msg");
                }
            }
        });
    }

    /**
     * Adds a collaborator to a repository (invites them).
     * Endpoint: PUT /repos/{owner}/{repo}/collaborators/{username}
     * permission: pull | push | admin | maintain | triage
     */
    fun addCollaborator(
        owner: String,
        repo: String,
        username: String,
        permission: String = "push",
        onResult: (Boolean, String?) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType();
        val payload = gson.toJson(mapOf("permission" to permission));
        val reqBody = payload.toRequestBody(mediaType);

        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/collaborators/$username")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .put(reqBody)
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Network error: ${e.message}");
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string();
                if (response.code == 201) {
                    onResult(true, "Invitation created (user must accept).");
                } else if (response.code == 204) {
                    onResult(true, "User already had access (updated/no-op).");
                } else {
                    val msg = parseGitHubMessage(body) ?: body ?: "HTTP ${response.code}";
                    onResult(false, "Add collaborator failed: ${response.code} $msg");
                }
            }
        });
    }

    /**
     * Removes a collaborator from a repository.
     * Endpoint: DELETE /repos/{owner}/{repo}/collaborators/{username}
     */
    fun removeCollaborator(
        owner: String,
        repo: String,
        username: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/collaborators/$username")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .delete()
            .build();

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Network error: ${e.message}");
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string();
                if (response.code == 204) {
                    onResult(true, "Removed.");
                } else {
                    val msg = parseGitHubMessage(body) ?: body ?: "HTTP ${response.code}";
                    onResult(false, "Remove collaborator failed: ${response.code} $msg");
                }
            }
        });
    }

    private fun parseGitHubMessage(body: String?): String? {
        if (body == null) { return null; };
        return try {
            val err = gson.fromJson(body, GitHubErrorResponse::class.java);
            err.message;
        } catch (_: Exception) {
            null;
        };
    }
}
