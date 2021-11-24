package com.gu.imagechecker

import scala.jdk.CollectionConverters._
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration
import software.amazon.awssdk.services.lambda.model.ListFunctionsRequest
import software.amazon.awssdk.services.lambda.model.PackageType
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest

class ImageChecker(lambdaClient: LambdaClient) {
  def getLambdaImageUris() =
    for(l <- getImageLambdas(); name = l.functionName(); imageUri = getLambdaImageUri(name))
    yield (name -> imageUri)

  def getLambdaImageUri(name: String) = {
    val res = lambdaClient.getFunction(
      GetFunctionRequest.builder()
        .functionName(name)
        .build())
    res.code().imageUri()
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
