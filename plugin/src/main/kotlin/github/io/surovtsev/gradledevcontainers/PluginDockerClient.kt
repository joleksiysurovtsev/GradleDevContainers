package github.io.surovtsev.gradledevcontainers

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import java.util.*

private const val TCP = "tcp://localhost:2375"
private const val UNIX = "unix:///var/run/docker.sock"

object PluginDockerClient {
    private val dockerHostUri: String =
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")) TCP else UNIX

    val dockerClient: DockerClient = newDockerClient(dockerHostUri)

    val networkId: String = dockerClient.createNetworkCmd().withName("kafka_network_${System.currentTimeMillis()}").exec().id

    private fun newDockerClient(host: String?): DockerClient {
        val config: DockerClientConfig =
            DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(host).build()
        return DockerClientBuilder.getInstance(config).build()
    }
}