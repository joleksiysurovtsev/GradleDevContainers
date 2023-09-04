package o.sur.gradledevcontainers.kafka

import com.github.dockerjava.api.command.CreateContainerResponse

interface ContainerBuilder {

    val containerServiceName: String

    fun build(): CreateContainerResponse

}