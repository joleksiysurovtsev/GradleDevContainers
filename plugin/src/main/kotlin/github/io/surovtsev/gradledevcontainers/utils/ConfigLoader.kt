package github.io.surovtsev.gradledevcontainers.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.FileNotFoundException

object ConfigLoader {

    fun getConfigurationByServiceName(name: String): List<String> {
        val envMap = loadConfiguration().getValue(name) as Map<*, *>
        return envMap.map { (it, s) -> "$it=$s" }
    }

    private fun loadConfiguration(): Map<String, Any> {
        val builder = KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()

        val mapper = ObjectMapper(YAMLFactory()).registerModule(builder)
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("plugin-config.yml")
            ?: throw FileNotFoundException("plugin-config.yml not found")

        resource.openStream().use { inputStream -> return mapper.readValue(inputStream) }
    }
}
