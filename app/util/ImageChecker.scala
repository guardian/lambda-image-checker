package com.gu.imagechecker

import scala.jdk.CollectionConverters._
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest
import software.amazon.awssdk.services.lambda.model.PackageType
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Image

class ImageChecker(lambdaClient: LambdaClient, dockerClient: DockerClient) {

  def getDockerImageInfo(imageName: String): Option[String] = {
    val req = dockerClient.listImagesCmd()
    req.getFilters.put("reference", List(imageName).asJava)
    val images = req.exec().asScala.toList
    images.headOption.flatMap { image =>
      val labels = image.getLabels.asScala
      if(labels == null) None else labels.get("sdkBaseVersion")
    }
  }

  def getAllLambdaRecords() =
    for(l <- getImageLambdas(); name = l.functionName()) yield getLambdaRecord(name)

  def getLambdaRecord(name: String) = {
    val res = lambdaClient.getFunction(
      GetFunctionRequest.builder()
        .functionName(name)
        .build())
    val imageUri = res.code().imageUri()
    val baseImage = getDockerImageInfo(imageUri)
    LambdaRecord(name, res.configuration().lastModified(), ImageRecord(imageUri, baseImage))
  }

  def getLambdas(nextMarker: String = null): LazyList[FunctionConfiguration] = {
    val res = lambdaClient.listFunctions(
      ListFunctionsRequest.builder()
        .marker(nextMarker)
        .build()
    )
    val functions = res.functions().asScala
    LazyList.concat(functions) :++ (
      if(res.nextMarker() == null) LazyList.empty else getLambdas(res.nextMarker())
    )
  }

  def getImageLambdas() = getLambdas().filter(_.packageType() == PackageType.IMAGE)
}
