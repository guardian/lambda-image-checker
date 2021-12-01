package com.gu.imagechecker

import controllers.AssetsComponents
import play.api._
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

import scala.jdk.CollectionConverters._
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.dockerjava.core.DockerClientImpl

class ImageCheckerComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with AssetsComponents {
  import com.softwaremill.macwire._

  val defaultLambdaClient = LambdaClient.builder()
    .credentialsProvider(ProfileCredentialsProvider.create("mobile"))
    .build()

  val defaultDockerClient = {
    val dockerCfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    val dockerHttp = new ApacheDockerHttpClient.Builder()
      .dockerHost(dockerCfg.getDockerHost())
      .sslConfig(dockerCfg.getSSLConfig())
      .build();
    DockerClientImpl.getInstance(dockerCfg, dockerHttp)
  }

  val imageReference = ReferenceImageConfig("public.ecr.aws/lambda/java")

  lazy val imageChecker = wire[ImageChecker]
  lazy val imageCheckerController = wire[ImageCheckerController]
  val router: Router = {
    // add the prefix string in local scope for the Routes constructor
    val prefix: String = "/"
    wire[Routes]
  }
}

class ImageCheckerApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new ImageCheckerComponents(context).application
  }
}
