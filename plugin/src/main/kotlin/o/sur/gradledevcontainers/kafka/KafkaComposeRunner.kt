package o.sur.gradledevcontainers.kafka

import com.github.dockerjava.api.command.LogContainerCmd
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback
import o.sur.gradledevcontainers.PluginDockerClient
import java.util.concurrent.CountDownLatch

object KafkaComposeRunner {

    fun runKafkaBundle() {
        runZookeeper().also { zookeeperId -> waitForZookeeper(zookeeperId!!) }
            .also { runKafka() }
    }

    private fun waitForZookeeper(containerId: String) {
        val latch = CountDownLatch(1)
        val logCmd: LogContainerCmd = PluginDockerClient.dockerClient
            .logContainerCmd(containerId)
            .withStdErr(true)
            .withStdOut(true)
            .withFollowStream(true)
            .withSince(0)

        logCmd.exec(object : LogContainerResultCallback() {
            override fun onNext(item: Frame?) {
                val logLine = String(item!!.payload)
                if (logLine.contains("binding to port 0.0.0.0/0.0.0.0:2181")) {
                    latch.countDown()
                }
            }
        })
        latch.await()
    }

    private fun runZookeeper(): String? {
        val container = ZookeeperContainerBuilder().build()
        PluginDockerClient.dockerClient.startContainerCmd(container.id).exec()
        return container.id
    }

    private fun runKafka() {
        val container = KafkaContainerBuilder().build()
        PluginDockerClient.dockerClient.startContainerCmd(container.id).exec()
        println("Kafka container started with ID: " + container.id)
    }

}