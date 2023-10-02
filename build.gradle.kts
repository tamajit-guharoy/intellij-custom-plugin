plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.tamajit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
//https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-available-in-all-products
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform
//    plugins = ['com.intellij.java']
    plugins.set(listOf("com.intellij.java"))
}
dependencies {
    // https://mvnrepository.com/artifact/com.intellij/openapi
//    implementation("com.intellij:openapi:7.0.3")
    compileOnly(fileTree("lib") {
        include("**/*.jar")
    })
//    implementation("com.jetbrains.intellij.maven:maven:231.9011.34")
//    implementation("com.jetbrains.intellij.platform:jps-model-serialization:182.2949.4")
//    implementation("com.jetbrains.intellij.platform:jps-model-impl:182.2949.4")
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
