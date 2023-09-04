package o.sur.gradledevcontainers.kafka

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import o.sur.gradledevcontainers.PluginDockerClient
import o.sur.gradledevcontainers.utils.ConfigLoader

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