package o.sur.gradledevcontainers

import o.sur.gradledevcontainers.kafka.KafkaComposeRunner
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleDevContainersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("RUN_KAFKA") { task ->
            task.group = "DevContainersPlugin"
            task.doLast { KafkaComposeRunner.runKafkaBundle() }
        }
    }
}

