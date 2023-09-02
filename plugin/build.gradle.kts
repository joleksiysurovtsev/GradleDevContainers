plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.github.docker-java:docker-java:3.3.3")
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

gradlePlugin {
    val greeting by plugins.creating {
        id = "o.sur.gradledevcontainers.greeting"
        implementationClass = "o.sur.gradledevcontainers.GradleDevContainersPlugin"
    }
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}

gradlePlugin {
    plugins {
        create("CombinedPlugin") {
            id = "o.sur.plugins.dev-containers"
            implementationClass = "o.sur.gradledevcontainers.GradleDevContainersPlugin"
            description = "TODO()"
        }
    }
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