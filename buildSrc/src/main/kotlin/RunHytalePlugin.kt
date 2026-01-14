import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.io.InputStream
import java.net.URI
import java.security.MessageDigest

/**
 * Custom Gradle plugin for automated Hytale server testing.
 * 
 * Usage:
 *   runHytale {
 *       jarUrl = "https://example.com/hytale-server.jar"
 *       assetsPath = "Assets.zip"
 *   }
 *   
 *   ./gradlew runServer
 */
open class RunHytalePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("runHytale", RunHytaleExtension::class.java)

        val runTask = project.tasks.register("runServer", RunServerTask::class.java) {
            jarUrl.set(extension.jarUrl)
            extension.assetsPath?.let { assetsPath.set(it) }
            group = "hytale"
            description = "Downloads and runs the Hytale server with your plugin"
        }

        project.tasks.findByName("shadowJar")?.let {
            runTask.configure { dependsOn(it) }
        }
    }
}

/**
 * Extension for configuring the RunHytale plugin.
 */
open class RunHytaleExtension {
    var jarUrl: String = "https://example.com/hytale-server.jar"
    var assetsPath: String? = null
}

/**
 * Task that downloads, sets up, and runs a Hytale server with the plugin.
 */
open class RunServerTask : DefaultTask() {

    @Input
    val jarUrl = project.objects.property(String::class.java)
    
    @Input
    val assetsPath = project.objects.property(String::class.java)

    @TaskAction
    fun run() {
        // Create directories
        val runDir = File(project.projectDir, "run").apply { mkdirs() }
        val pluginsDir = File(runDir, "mods").apply { mkdirs() }
        val jarFile = File(runDir, "server.jar")

        // Cache directory for downloaded server JARs
        val cacheDir = File(
            project.layout.buildDirectory.asFile.get(), 
            "hytale-cache"
        ).apply { mkdirs() }

        // Normalize jarUrl to URI
        val jarUrlStr = jarUrl.get()
        val jarUri = when {
            jarUrlStr.startsWith("file://") -> URI.create(jarUrlStr)
            jarUrlStr.startsWith("http://") || jarUrlStr.startsWith("https://") -> URI.create(jarUrlStr)
            File(jarUrlStr).isAbsolute -> File(jarUrlStr).toURI()
            else -> File(project.projectDir, jarUrlStr).toURI()
        }

        // Compute hash of URI for caching
        val urlHash = MessageDigest.getInstance("SHA-256")
            .digest(jarUri.toString().toByteArray())
            .joinToString("") { "%02x".format(it) }
        val cachedJar = File(cacheDir, "$urlHash.jar")

        // Download server JAR if not cached
        if (!cachedJar.exists()) {
            println("Downloading Hytale server from ${jarUri}")
            try {
                jarUri.toURL().openStream().use { input ->
                    cachedJar.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("Server JAR downloaded and cached")
            } catch (e: Exception) {
                println("ERROR: Failed to download server JAR")
                println("Make sure the jarUrl in build.gradle.kts is correct")
                println("Error: ${e.message}")
                return
            }
        } else {
            println("Using cached server JAR")
        }

        // Copy server JAR to run directory
        cachedJar.copyTo(jarFile, overwrite = true)

        // Copy plugin JAR to plugins folder
        project.tasks.findByName("shadowJar")?.outputs?.files?.firstOrNull()?.let { shadowJar ->
            val targetFile = File(pluginsDir, shadowJar.name)
            shadowJar.copyTo(targetFile, overwrite = true)
            println("Plugin copied to: ${targetFile.absolutePath}")
        } ?: run {
            println("WARNING: Could not find shadowJar output")
        }

        // Copy assets file (mandatory)
        val assetsPathStr = assetsPath.orNull
            ?: throw IllegalStateException(
                "assetsPath is required but not set. " +
                "Please configure it in build.gradle.kts: runHytale { assetsPath = \"Assets.zip\" }"
            )
        
        val sourceAssets = when {
            assetsPathStr.startsWith("file://") -> File(URI.create(assetsPathStr))
            File(assetsPathStr).isAbsolute -> File(assetsPathStr)
            else -> File(project.projectDir, assetsPathStr)
        }
        if (!sourceAssets.exists()) {
            throw IllegalStateException(
                "Assets file not found: ${sourceAssets.absolutePath}. " +
                "Please ensure assetsPath is correctly configured in build.gradle.kts"
            )
        }
        
        val assetsFile = File(runDir, sourceAssets.name)
        sourceAssets.copyTo(assetsFile, overwrite = true)
        println("Assets copied to: ${assetsFile.absolutePath}")

        println("Starting Hytale server...")
        println("Press Ctrl+C to stop the server")

        // Check if debug mode is enabled
        val debugMode = project.hasProperty("debug")
        val javaArgs = mutableListOf<String>()
        
        if (debugMode) {
            javaArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            println("Debug mode enabled. Connect debugger to port 5005")
        }
        
        javaArgs.addAll(listOf("-jar", jarFile.name))
        
        // Add assets argument (mandatory)
        javaArgs.add("--assets")
        javaArgs.add(assetsFile.name)

        // Start the server process
        val process = ProcessBuilder("java", *javaArgs.toTypedArray())
            .directory(runDir)
            .start()

        // Handle graceful shutdown
        project.gradle.buildFinished {
            if (process.isAlive) {
                println("\nStopping server...")
                process.destroy()
            }
        }

        // Forward process streams to console
        forwardStream(process.inputStream) { println(it) }
        forwardStream(process.errorStream) { System.err.println(it) }
        
        // Forward stdin to server (for commands)
        Thread {
            System.`in`.bufferedReader().useLines { lines ->
                lines.forEach {
                    process.outputStream.write((it + "\n").toByteArray())
                    process.outputStream.flush()
                }
            }
        }.start()

        // Wait for server to exit
        val exitCode = process.waitFor()
        println("Server exited with code $exitCode")
    }

    /**
     * Forwards an input stream to a consumer in a background thread.
     */
    private fun forwardStream(inputStream: InputStream, consumer: (String) -> Unit) {
        Thread {
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach(consumer)
            }
        }.start()
    }
}
