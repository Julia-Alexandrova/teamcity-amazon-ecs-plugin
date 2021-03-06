package jetbrains.buildServer.clouds.ecs

import jetbrains.buildServer.clouds.CloudInstance

interface EcsCloudInstance : CloudInstance {
    fun terminate()
    fun generateAgentName(): String
}