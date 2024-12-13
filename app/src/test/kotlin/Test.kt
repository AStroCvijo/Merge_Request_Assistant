package org.main

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.io.ByteArrayInputStream
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.kohsuke.github.*
import org.kohsuke.github.GHContentUpdateResponse

class GitHubFunctionsIntegrationTest {

    // Path to config.json file
    private val configFilePath = "config.json"

    // config.json file tests
    @Test
    fun `test loadConfig with valid file`() {
        // Load config.json file
        val config = loadConfig(configFilePath)

        // Assertions
        assertNotNull(config)
    }

    @Test
    fun `test loadConfig with invalid file path`() {
        // Load config.json file with invalid path
        val exception = assertThrows(IllegalArgumentException::class.java) {
            loadConfig("invalid_path.json")
        }

        // Assertions
        assertEquals("Configuration file not found at invalid_path.json", exception.message)
    }

    @Test
    fun `test loadConfig with invalid JSON format`() {
        // Mock invalid_config.json file
        val invalidJson = """{ "githubToken": "invalidToken" """
        val path = "src/test/resources/invalid_config.json"
        Files.write(Paths.get(path), invalidJson.toByteArray())

        // Load invalid_config.json file
        val exception = assertThrows(IllegalArgumentException::class.java) {
            loadConfig(path)
        }

        // Assertions
        assertEquals("Invalid configuration file format.", exception.message)
    }

    // GitHub API tests
    @Test
    fun `test connectToGitHub with real API`() {
        // Load config.json file
        val config = loadConfig(configFilePath)

        // Connect to GitHub API
        val github = connectToGitHub(config.githubToken)

        // Assertions
        assertNotNull(github)
        assertTrue(github.isCredentialValid)
    }

    @Test
    fun `test listRepositories with real API`() {
        // Load config.json file
        val config = loadConfig(configFilePath)

        // Connect to GitHub API
        val github = connectToGitHub(config.githubToken)

        // Get a list of repos
        val repos = listRepositories(github)

        // Assertions
        assertTrue(repos.isNotEmpty())
    }

    // GitHub functions tests
    @Test
    fun `test selectRepository with valid input`() {
        val mockRepo1 = mockk<GHRepository>()
        val mockRepo2 = mockk<GHRepository>()
        val mockRepo3 = mockk<GHRepository>()

        every { mockRepo1.name } returns "Repo1"
        every { mockRepo2.name } returns "Repo2"
        every { mockRepo3.name } returns "Repo3"

        val repos = listOf(mockRepo1, mockRepo2, mockRepo3)

        // Simulate user input (selecting the 2nd repository)
        val userInput = "2\n"
        System.setIn(ByteArrayInputStream(userInput.toByteArray()))

        // Call the function and assert the result
        val selectedRepo = selectRepository(repos)
        assertEquals("Repo2", selectedRepo.name)

        // Verify interactions
        verify { mockRepo1.name }
        verify { mockRepo2.name }
        verify { mockRepo3.name }
    }

    @Test
    fun `test createBranch creates a new branch`() {
        val mockRepo = mockk<GHRepository>()
        val mockBranch = mockk<GHBranch>()
        val mockRef = mockk<GHRef>()

        every { mockRepo.branches } returns mapOf("main" to mockBranch)
        every { mockBranch.shA1 } returns "dummySHA1"
        every { mockRepo.defaultBranch } returns "main"
        every { mockRepo.getBranch("main") } returns mockBranch
        every { mockRepo.createRef("refs/heads/new-branch", "dummySHA1") } returns mockRef

        // Simulate user input for branch name
        val userInput = "new-branch\n"
        System.setIn(ByteArrayInputStream(userInput.toByteArray()))

        val branchName = createBranch(mockRepo)

        // Assertions
        assertEquals("new-branch", branchName)

        // Verify interactions
        verify { mockRepo.branches }
        verify { mockRepo.getBranch("main") }
        verify { mockRepo.createRef("refs/heads/new-branch", "dummySHA1") }
    }

    @Test
    fun `test addFileToBranch adds file successfully`() {
        val mockRepo = mockk<GHRepository>()
        val mockContentBuilder = mockk<GHContentBuilder>()
        val mockContentUpdater = mockk<GHContentUpdateResponse>()

        // Slot to capture the file content
        val filePathSlot = slot<String>()
        val contentSlot = slot<String>()
        val messageSlot = slot<String>()
        val branchSlot = slot<String>()

        every { mockRepo.createContent() } returns mockContentBuilder
        every {
            mockContentBuilder.content(capture(contentSlot))
                .path(capture(filePathSlot))
                .message(capture(messageSlot))
                .branch(capture(branchSlot))
                .commit()
        } returns mockContentUpdater

        // Simulate user input for file name and content
        val userInput = "Hello.txt\nThis is the content of the file.\nEND\n"
        System.setIn(ByteArrayInputStream(userInput.toByteArray()))

        addFileToBranch(mockRepo, "new-branch")

        // Assertions
        assertEquals("Hello.txt", filePathSlot.captured)
        assertEquals("This is the content of the file.\n", contentSlot.captured)
        assertEquals("Add Hello.txt with custom content", messageSlot.captured)
        assertEquals("new-branch", branchSlot.captured)

        // Verify interactions
        verify { mockRepo.createContent() }
    }

    @Test
    fun `test createPullRequest creates pull request successfully`() {
        val mockRepo = mockk<GHRepository>()
        val defaultBranch = "main"

        every { mockRepo.defaultBranch } returns defaultBranch
        every {
            mockRepo.createPullRequest(any(), any(), any(), any())
        } returns mockk<GHPullRequest>()

        // Simulate user input for pull request title and message
        val userInput = "New Feature PR\nThis is the PR message.\n"
        System.setIn(ByteArrayInputStream(userInput.toByteArray()))

        createPullRequest(mockRepo, "new-branch")

        // Verify interactions
        verify {
            mockRepo.createPullRequest(
                "New Feature PR",
                "new-branch",
                defaultBranch,
                "This is the PR message."
            )
        }
    }
}
