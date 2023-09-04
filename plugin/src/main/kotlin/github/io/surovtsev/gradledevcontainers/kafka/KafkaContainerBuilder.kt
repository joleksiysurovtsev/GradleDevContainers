package github.io.surovtsev.gradledevcontainers.kafka

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import github.io.surovtsev.gradledevcontainers.PluginDockerClient
import github.io.surovtsev.gradledevcontainers.utils.ConfigLoader

class KafkaContainerBuilder : ContainerBuilder {

    override val containerServiceName: String = "kafka"

    override fun build(): CreateContainerResponse {
        return PluginDockerClient.dockerClient.createContainerCmd("confluentinc/cp-kafka:7.3.2")
            .withName("kafka-broker").withHostConfig(
                HostConfig.newHostConfig().withNetworkMode(PluginDockerClient.networkId)
                    .withPortBindings(PortBinding.parse("9092:9092"))
            ).withEnv(
                ConfigLoader.getConfigurationByServiceName(containerServiceName)
            ).exec()
    }
}