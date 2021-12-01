package com.gu.imagechecker

import java.time.Instant
import scala.jdk.CollectionConverters._
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest
import software.amazon.awssdk.services.lambda.model.PackageType
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Image

case class ReferenceImageConfig(imageName: String)

class ImageChecker(lambdaClient: LambdaClient, dockerClient: DockerClient, referenceImage: ReferenceImageConfig) {

  // tells us which is the latest version of the base
  // image -- i.e. this is the one we should be using.
  def getReferenceImage(): Option[ImageRecord] = {
    dockerClient.pullImageCmd(referenceImage.imageName + ":latest")
      .start().awaitCompletion()
    val image = dockerClient.inspectImageCmd(referenceImage.imageName + ":latest").exec()
    val createdDate = Instant.parse(image.getCreated())
    for {
      allDigests <- Option(image.getRepoDigests().asScala) // Option(..) because it can be null (it's a java api)
      firstDigest <- allDigests.headOption
    } yield ImageRecord(firstDigest, Some(createdDate))
  }

  def getImageByName(imageName: String): Option[Image] = {
    // dockerClient.pullImageCmd(imageName)
    //   .start().awaitCompletion()
    val req = dockerClient.listImagesCmd()
    // https://github.com/docker-java/docker-java/issues/1516
    req.getFilters.put("reference", List(imageName).asJava)
    req.exec().asScala.toList.headOption
  }

  def getBaseImageLabel(imageName: String): Option[String] = {
    // dockerClient.pullImageCmd(imageName)
    //   .start().awaitCompletion()
    for {
      image <- getImageByName(imageName)
      labels <- Option(image.getLabels.asScala)
      baseVersion <- labels.get("sdkBaseVersion")
    } yield baseVersion
  }

  def getBaseImageRecord(imageUri: String): Option[ImageRecord] =
    for {
      baseVersion <- getBaseImageLabel(imageUri)
      baseImage <- getImageByName(baseVersion)
    } yield ImageRecord(baseVersion, Some(Instant.ofEpochSecond(baseImage.getCreated())))

  def getAllLambdaRecords() =
    for(l <- getImageLambdas(); name = l.functionName()) yield getLambdaRecord(name)

  def getLambdaRecord(name: String) = {
    val res = lambdaClient.getFunction(
      GetFunctionRequest.builder()
        .functionName(name)
        .build())
    val imageUri = res.code().imageUri()
    val imageCreatedAt = getImageByName(imageUri)
      .map(image => Instant.ofEpochSecond(image.getCreated()))
    val baseImage = getBaseImageRecord(imageUri)

    LambdaRecord(name, res.configuration().lastModified(), ImageRecord(imageUri, imageCreatedAt, baseImage))
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
