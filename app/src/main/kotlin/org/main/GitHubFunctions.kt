package org.main

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

// Function for connecting to the GitHub API
fun connectToGitHub(githubToken: String): GitHub {
    return GitHub.connectUsingOAuth(githubToken)
}

// Function for listing the available repositories
fun listRepositories(github: GitHub): List<GHRepository> {
    return github.myself.listRepositories().toList()
}

// Function for selecting the repository
fun selectRepository(repos: List<GHRepository>): GHRepository {
    println("Available repositories:")
    repos.forEachIndexed { index, repo -> println("${index + 1}. ${repo.name}") }

    var selectedRepoIndex: Int
    while (true) {
        print("Select a repository (1-${repos.size}): ")
        selectedRepoIndex = readlnOrNull()?.toIntOrNull()?.minus(1) ?: -1
        if (selectedRepoIndex !in repos.indices) {
            println("Invalid selection.")
            continue
        }
        break
    }
    return repos[selectedRepoIndex]
}

// Function for creating a branch
fun createBranch(selectedRepo: GHRepository): String {
    var chosenBranchName: String
    while (true) {
        print("Enter the new branch name: ")
        chosenBranchName = readlnOrNull().toString()

        if (chosenBranchName.isBlank()) {
            println("Branch name cannot be empty.")
            continue
        }

        val branches = selectedRepo.branches
        if (branches.keys.contains(chosenBranchName)) {
            println("Branch '$chosenBranchName' already exists. Please choose a different name.")
            continue
        }

        val defaultBranchName = selectedRepo.defaultBranch
        val masterBranch = selectedRepo.getBranch(defaultBranchName)
        val sha1 = masterBranch.shA1
        selectedRepo.createRef("refs/heads/$chosenBranchName", sha1)
        println("Created new branch: $chosenBranchName")
        break
    }
    return chosenBranchName
}

// Function for adding a file to the branch
fun addFileToBranch(selectedRepo: GHRepository, branchName: String) {
    var fileName: String
    while(true){
        // Ask the user for file name and content
        print("Enter the file name (e.g., Hello.txt): ")
        fileName = readlnOrNull().toString()

        if (fileName.isBlank()) {
            println("File name cannot be empty.")
            continue
        }
        break
    }
    println("Enter the content of the file (type 'END' to finish): ")
    val fileContent = buildString {
        var line: String
        while (true) {
            line = readlnOrNull().orEmpty()
            if (line == "END") {
                break
            }
            appendLine(line)
        }
    }

    selectedRepo.createContent()
        .content(fileContent)
        .path(fileName)
        .message("Add $fileName with custom content")
        .branch(branchName)
        .commit()
    println("Added $fileName to branch $branchName with content: \n'\n$fileContent'")
}

// Function for creating a pull request
fun createPullRequest(selectedRepo: GHRepository, branchName: String) {
    var pullRequestTitle:String
    while(true) {
        // Ask for the pull request title and message
        print("Enter the title of the pull request: ")
        pullRequestTitle = readlnOrNull().toString()

        if (pullRequestTitle.isBlank()) {
            println("Title of the pull request cannot be empty.")
            continue
        }
        break
    }

    print("Enter the message of the pull request: ")
    val pullRequestMessage = readlnOrNull().orEmpty()

    val defaultBranchName = selectedRepo.defaultBranch
    selectedRepo.createPullRequest(
        pullRequestTitle,
        branchName,
        defaultBranchName,
        pullRequestMessage
    )
    println("Pull request '$pullRequestTitle' created successfully.")
}