package org.main

import com.google.gson.Gson
import java.io.File

// Config file class
data class Config(val githubToken: String)

// Function for loading the config.json file
fun loadConfig(path: String): Config {
    val configFile = File(path)
    if (!configFile.exists()) {
        throw IllegalArgumentException("Configuration file not found at $path")
    }
    return try {
        Gson().fromJson(configFile.readText(), Config::class.java)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid configuration file format.")
    }
}