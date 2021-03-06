package jetbrains.buildServer.clouds.ecs

import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.clouds.CloudClientParameters
import jetbrains.buildServer.clouds.CloudImage
import jetbrains.buildServer.clouds.ecs.apiConnector.EcsApiConnector
import org.jmock.Expectations
import org.jmock.Mockery
import org.junit.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 29.09.17.
 */
@Test
class EcsCloudClientTest : BaseTestCase() {
    private lateinit var m:Mockery
    private lateinit var api:EcsApiConnector

    @BeforeMethod
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        m = Mockery()
        api = m.mock(EcsApiConnector::class.java)
    }

    @AfterMethod
    @Throws(Exception::class)
    public override fun tearDown() {
        m.assertIsSatisfied()
        super.tearDown()
    }

    private fun createClient(images: List<EcsCloudImage>): EcsCloudClient {
        return createClient(images, CloudClientParameters())
    }

    private fun createClient(images: List<EcsCloudImage>, cloudClientParameters: CloudClientParameters): EcsCloudClient {
        return createClient("server-uuid", "profile-id", images, cloudClientParameters)
    }

    private fun createClient(serverUuid: String, profileId: String, images: List<EcsCloudImage>, cloudClientParameters: CloudClientParameters): EcsCloudClient {
        return EcsCloudClient(images, api, EcsCloudClientParametersImpl(cloudClientParameters), serverUuid, profileId)
    }

    @Test
    @Throws(Exception::class)
    fun testCanStartNewInstance_UnknownImage() {
        val cloudClient = createClient(emptyList())
        val image = m.mock(EcsCloudImage::class.java)
        m.checking(object : Expectations() {
            init {
                allowing<CloudImage>(image).id
                will(Expectations.returnValue("image-1-id"))
            }
        })
        Assert.assertFalse(cloudClient.canStartNewInstance(image))
    }

    @Test
    @Throws(Exception::class)
    fun testCanStartNewInstance() {
        val image = m.mock(EcsCloudImage::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(image).name; will(Expectations.returnValue("image-1-name"))
                allowing(image).id; will(Expectations.returnValue("image-1-id"))
                allowing(image).instanceCount; will(Expectations.returnValue(0))
                allowing(image).instanceLimit; will(Expectations.returnValue(0))
            }
        })
        val images = listOf(image)
        val cloudClient = createClient(images)
        Assert.assertTrue(cloudClient.canStartNewInstance(image))
    }

    @Test
    @Throws(Exception::class)
    fun testCanStartNewInstance_ProfileLimit() {
        val image = m.mock(EcsCloudImage::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(image).id; will(Expectations.returnValue("image-1-id"))
                allowing(image).instanceCount; will(Expectations.returnValue(1))
                allowing(image).instanceLimit; will(Expectations.returnValue(2))
            }
        })
        val images = listOf(image)
        val cloudClientParameters = CloudClientParameters()
        cloudClientParameters.setParameter(PROFILE_INSTANCE_LIMIT_PARAM, "1")
        val cloudClient = createClient(images, cloudClientParameters)
        Assert.assertFalse(cloudClient.canStartNewInstance(image))
    }

    @Test
    @Throws(Exception::class)
    fun testCanStartNewInstance_ImageLimit() {
        val image = m.mock(EcsCloudImage::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(image).name; will(Expectations.returnValue("image-1-name"))
                allowing(image).id; will(Expectations.returnValue("image-1-id"))
                allowing(image).instanceCount; will(Expectations.returnValue(1))
                allowing(image).instanceLimit; will(Expectations.returnValue(1))
            }
        })
        val images = listOf(image)
        val cloudClient = createClient(images)
        Assert.assertFalse(cloudClient.canStartNewInstance(image))
    }

    @Test
    @Throws(Exception::class)
    fun testDuplicateImageName() {
        val image1 = m.mock(EcsCloudImage::class.java, "1")
        val image2 = m.mock(EcsCloudImage::class.java, "2")
        m.checking(object : Expectations() {
            init {
                allowing(image1).id; will(Expectations.returnValue("image-1-id"))
                allowing(image1).name; will(Expectations.returnValue("image"))
                allowing(image1).instanceCount; will(Expectations.returnValue(0))
                allowing(image2).id; will(Expectations.returnValue("image-2-id"))
                allowing(image2).name; will(Expectations.returnValue("image"))
                allowing(image2).instanceCount; will(Expectations.returnValue(0))
            }
        })
        createClient(listOf(image1, image2))
    }
}