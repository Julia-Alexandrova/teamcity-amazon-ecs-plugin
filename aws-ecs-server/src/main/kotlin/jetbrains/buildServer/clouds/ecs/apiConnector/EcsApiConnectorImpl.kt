package jetbrains.buildServer.clouds.ecs.apiConnector

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.ecs.AmazonECS
import com.amazonaws.services.ecs.AmazonECSClientBuilder
import com.amazonaws.services.ecs.model.*
import jetbrains.buildServer.version.ServerVersionHolder

class EcsApiConnectorImpl(awsCredentials: AWSCredentials?, awsRegion: String?) : EcsApiConnector {
    private val apiClient: AmazonECS

    init {
        val builder = AmazonECSClientBuilder
                .standard()
                .withClientConfiguration(ClientConfiguration().withUserAgentPrefix("JetBrains TeamCity " + ServerVersionHolder.getVersion().displayVersion))
                .withRegion(awsRegion)
        if(awsCredentials != null){
            builder.withCredentials(object: AWSCredentialsProvider{
                override fun getCredentials(): AWSCredentials {
                    return awsCredentials
                }

                override fun refresh() {
                    //no-op
                }
            })
        }
        apiClient = builder.build()
    }

    override fun runTask(taskDefinition: EcsTaskDefinition, cluster: String?, taskGroup: String?, additionalEnvironment: Map<String, String>, startedBy: String?): List<EcsTask> {
        val containerOverrides = taskDefinition.containers.map {
            containerName -> ContainerOverride()
                                .withName(containerName)
                                .withEnvironment(additionalEnvironment.entries.map { entry -> KeyValuePair().withName(entry.key).withValue(entry.value) })
        }

        var request = RunTaskRequest()
                .withTaskDefinition(taskDefinition.arn)
                .withOverrides(TaskOverride().withContainerOverrides(containerOverrides))
                .withStartedBy(startedBy)
        if(cluster != null && !cluster.isEmpty()) request = request.withCluster(cluster)
        if(taskGroup != null && !taskGroup.isEmpty()) request = request.withGroup(taskGroup)

        val runTaskResult = apiClient.runTask(request)
        if (!runTaskResult.failures.isEmpty())
            throw EcsApiCallFailureException(runTaskResult.failures)

        return runTaskResult.tasks.map { it.wrap() }
    }

    override fun listTaskDefinitions(): List<String> {
        var taskDefArns:List<String> = ArrayList<String>()
        var nextToken: String? = null;
        do{
            var request = ListTaskDefinitionsRequest()
            if(nextToken != null) request = request.withNextToken(nextToken)
            val taskDefsResult = apiClient.listTaskDefinitions(request)
            taskDefArns = taskDefArns.plus(taskDefsResult.taskDefinitionArns)
            nextToken = taskDefsResult.nextToken
        }
        while(nextToken != null)
        return taskDefArns
    }

    override fun describeTaskDefinition(taskDefinitionArn: String): EcsTaskDefinition? {
        return apiClient.describeTaskDefinition(DescribeTaskDefinitionRequest().withTaskDefinition(taskDefinitionArn)).taskDefinition.wrap()
    }

    override fun stopTask(task: String, cluster: String?, reason: String?) {
        apiClient.stopTask(StopTaskRequest().withTask(task).withCluster(cluster))
    }

    override fun listTasks(cluster: String?, startedBy: String?): List<String> {
        var taskArns:List<String> = ArrayList()
        var nextToken: String? = null;
        do{
            var listTasksRequest = ListTasksRequest()
                    .withCluster(cluster)
                    .withStartedBy(startedBy)

            if(nextToken != null) listTasksRequest = listTasksRequest.withNextToken(nextToken)

            val tasksResult = apiClient.listTasks(listTasksRequest)
            taskArns = taskArns.plus(tasksResult.taskArns)
            nextToken = tasksResult.nextToken
        }
        while(nextToken != null)
        return taskArns
    }

    override fun describeTask(taskArn: String, cluster: String?): EcsTask? {
        val tasksResult = apiClient.describeTasks(DescribeTasksRequest().withTasks(taskArn).withCluster(cluster))
        if (!tasksResult.failures.isEmpty())
            throw EcsApiCallFailureException(tasksResult.failures)

        return tasksResult.tasks[0]?.wrap()
    }

    override fun listClusters(): List<String> {
        var clusterArns:List<String> = ArrayList()
        var nextToken: String? = null
        do{
            var request = ListClustersRequest()
            if(nextToken != null) request = request.withNextToken(nextToken)
            val tasksResult = apiClient.listClusters(request)
            clusterArns = clusterArns.plus(tasksResult.clusterArns)
            nextToken = tasksResult.nextToken
        }
        while(nextToken != null)
        return clusterArns
    }

    override fun describeCluster(clusterArn: String): EcsCluster? {
        val describeClustersResult = apiClient.describeClusters(DescribeClustersRequest().withClusters(clusterArn))
        if (!describeClustersResult.failures.isEmpty())
            throw EcsApiCallFailureException(describeClustersResult.failures)

        return describeClustersResult.clusters[0]?.wrap()
    }

    override fun testConnection(): TestConnectionResult {
        try {
            apiClient.listClusters()
            return TestConnectionResult("Connection successful", true)
        } catch (ex: Exception){
            return TestConnectionResult(ex.localizedMessage, false)
        }
    }
}