plugins {
	id("java")
	id("org.jetbrains.kotlin.jvm") version "2.1.20"
	id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "com.sakura-sedaia"
version = "0.2.0-SNAPSHOT"

repositories {
	mavenCentral()
	intellijPlatform {
		defaultRepositories()
	}
}

dependencies {
	intellijPlatform {
		pycharm("2025.2.4")
		testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
		
		bundledPlugin("PythonCore")
		bundledPlugin("Pythonid")
	}
	testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
		pluginConfiguration {
			vendor {
				name = "Sakura Sedaia"
				url = "https://www.sakura-sedaia.com"
			}

			ideaVersion {
			sinceBuild = "252.25557"
		}
		
		changeNotes = """
			<b>Added</b>
			<ul>
				<li><b>Blender Dev Tools Project</b>: New specialized project type for Blender extension development.</li>
				<li><b>Improved Scanner</b>: Enhanced macOS and Linux Blender detection using the <code>which</code> command.</li>
				<li><b>Custom Versions</b>: Support for manual specification of Blender executable paths and versioning.</li>
				<li><b>Source Management</b>: Option to mark project folders as Blender source directories for better organization.</li>
				<li><b>Cross-Platform Compatibility</b>: Refined path handling for Windows, macOS, and Linux.</li>
				<li><b>Documentation</b>: Moved comprehensive guides to the <a href="https://wiki.sakura-sedaia.com/docs/blender-development-pycharm/index.html">external documentation site</a>.</li>
				<li><b>Unit Testing</b>: Initial suite of unit tests for core plugin functionality.</li>
				<li><b>Sandbox Control</b>: New setting to toggle sandboxing for Blender instances within the New Project Wizard.</li>
			</ul>
			<b>Changed</b>
			<ul>
				<li><b>Branding</b>: Renamed the plugin to <b>Blender Dev Tools</b> and updated all icons.</li>
				<li><b>Environment Setup</b>: Automated the detection and replication of system Blender configuration subdirectories to ensure a consistent sandboxed environment.</li>
				<li><b>Diagnostics</b>: Improved logging with per-day rotation and more detailed configuration.</li>
				<li><b>Run Configurations</b>: Updated templates for testing, building, and validation with a dynamic UI.
					<ul>
						<li>Removed redundant <code>--app-template pycharm</code> arguments when executing <code>build</code> and <code>validate</code> commands.</li>
						<li>Enhanced logic for detecting extension-specific commands.</li>
						<li>Standardized internal <code>src</code> path handling using Kotlin NIO.2 utilities for better OS reliability.</li>
					</ul>
				</li>
				<li><b>Licensing</b>: Updated project license to GNU GPL v3.</li>
			</ul>
			<b>Fixed</b>
			<ul>
				<li><b>Management UI</b>: Reworked the Blender version and sandbox management tool window for better stability.</li>
				<li><b>Manifest Formatting</b>: Switched Manifest IDs from kebab-case to snake_case to comply with Blender's validation requirements.</li>
				<li><b>CLI Arguments</b>: Corrected the extension command syntax in run configurations, fixing a pluralization error.</li>
				<li><b>Stability</b>: Fixed crashes in the version management tool window and resolved validation issues in the New Project Wizard.</li>
				<li><b>Path Resolution</b>: Fixed the <code>FATAL_ERROR: Missing local "src"</code> by utilizing absolute paths for the <code>--source-dir</code> argument.</li>
				<li><b>Process Management</b>: Configured the <code>GeneralCommandLine</code> working directory to ensure correct resolution of relative paths.</li>
				<li><b>Extension Logic</b>: Fixed a bug where <code>--app-template</code> was incorrectly applied to CLI-based extension operations.</li>
				<li><b>Readme Generation</b>: Resolved a missing argument error in the README generator template call.</li>
				<li><b>Licensing</b>: Updated project license to GNU GPL v3.</li>
			</ul>
		""".trimIndent()
	}
}

tasks {
	withType<JavaCompile> {
		sourceCompatibility = "21"
		targetCompatibility = "21"
	}

	buildSearchableOptions {
		enabled = false
	}

	prepareJarSearchableOptions {
		enabled = false
	}

	jarSearchableOptions {
		enabled = false
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}
