package jetbrains.buildServer.clouds.ecs

import jetbrains.buildServer.clouds.CloudImageParameters

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 21.09.17.
 */

val PROFILE_INSTANCE_LIMIT_PARAM = "profileInstanceLimit"
val IMAGE_INSTANCE_LIMIT_PARAM = "maxInstances"
val TASK_DEFINITION_PARAM = "taskDefinition"
val TASK_GROUP_PARAM = "taskGroup"
val CLUSTER_PARAM = "cluster"
val AGENT_NAME_PREFIX = "agentNamePrefix"

class EcsParameterConstants{
    val agentNamePrefix: String = AGENT_NAME_PREFIX
    val taskDefinition: String = TASK_DEFINITION_PARAM
    val cluster: String = CLUSTER_PARAM
    val taskGroup: String = TASK_GROUP_PARAM
    val maxInstances: String = IMAGE_INSTANCE_LIMIT_PARAM
    val agentPoolIdField: String = CloudImageParameters.AGENT_POOL_ID_FIELD
    val profileInstanceLimit: String = PROFILE_INSTANCE_LIMIT_PARAM
}