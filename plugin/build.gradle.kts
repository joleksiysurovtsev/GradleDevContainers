plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "github.io"
version = "${parent?.version}"

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.github.docker-java:docker-java:3.3.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("1.9.10")
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
            }
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest("1.9.10")
            dependencies {
                implementation(project())
                implementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
            }

            targets {
                all {
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = group.toString()
            version = project.version.toString()
            from(components["kotlin"])
        }
    }
    repositories {
        maven {
            url = uri("...")
            credentials {
                username = "username"
                password = "password"
            }
        }
    }
}

gradlePlugin {
    website.set("https://github.com/joleksiysurovtsev/GradleDevContainers")
    vcsUrl.set("https://github.com/joleksiysurovtsev/GradleDevContainers")
    plugins {
        create("DevContainersPlugin") {
            id = "github.io.surovtsev.gradle-dev-containers-starter"
            implementationClass = "github.io.o.surovtsev.gradledevcontainers.GradleDevContainersPlugin"
            displayName = "Dev Containers Plugin for Kafka and Zookeeper"
            description = """
                |A Gradle plugin to automate the deployment of Kafka and Zookeeper containers using Docker. 
                |It handles container creation, initialization, and network setup.
                |With each new version, new containers and tasks will be added
                |""".trimMargin()
            tags.set(listOf("kafka", "docker"))
            version = project.version.toString()
        }
    }
}