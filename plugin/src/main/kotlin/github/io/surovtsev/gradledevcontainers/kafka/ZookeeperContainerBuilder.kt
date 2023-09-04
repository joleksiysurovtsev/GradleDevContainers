package github.io.surovtsev.gradledevcontainers.kafka

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import github.io.surovtsev.gradledevcontainers.PluginDockerClient
import github.io.surovtsev.gradledevcontainers.utils.ConfigLoader

class ZookeeperContainerBuilder : ContainerBuilder {

    override val containerServiceName: String = "zookeeper"

    override fun build(): CreateContainerResponse =
        PluginDockerClient.dockerClient.createContainerCmd("confluentinc/cp-zookeeper:7.3.2")
            .withName("zookeeper")
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withNetworkMode(PluginDockerClient.networkId)
                    .withPortBindings(PortBinding.parse("2181:2181"))
            )
            .withEnv(
                ConfigLoader.getConfigurationByServiceName(containerServiceName)
            ).exec()
}