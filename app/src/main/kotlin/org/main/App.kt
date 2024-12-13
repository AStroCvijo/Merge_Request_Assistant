package org.main

fun main() {
    try {
        // Load the GitHub token and connect to the API
        val config = loadConfig("config.json")
        val github = connectToGitHub(config.githubToken)

        // List all available repositories
        val repos = listRepositories(github)
        if (repos.isEmpty()) {
            println("No repositories found for the authenticated user.")
            return
        }

        // Select the repository
        val selectedRepo = selectRepository(repos)
        println("Selected repository: ${selectedRepo.name}")

        // Create a branch
        val chosenBranchName = createBranch(selectedRepo)

        // Add the file to the branch
        addFileToBranch(selectedRepo, chosenBranchName)

        // Create the pull request
        createPullRequest(selectedRepo, chosenBranchName)

    } catch (e: Exception) {
        println("An error occurred: ${e.message}")
    }
}