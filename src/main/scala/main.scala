package com.gu.imagechecker

import scala.jdk.CollectionConverters._
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.PackageType
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration

// def printStats(l: FunctionConfiguration) = {
//   println(
//     (l.lastUpdateStatusAsString() ::
//       l.stateAsString() ::
//       l.revisionId() ::
// //      l.imageConfigResponse() ::
//       l.layers().asScala.mkString(";") ::
//       Nil).mkString(", ")
//   )
// }

@main
def main = {
  val defaultLambdaClient = LambdaClient.builder()
    .credentialsProvider(ProfileCredentialsProvider.create("mobile"))
    .build()
  val imageChecker = new ImageChecker(defaultLambdaClient)
  val lambdas = imageChecker.getLambdaImageUris()
  for((nm, uri) <- lambdas) {
    println(s"$nm => $uri")
  }
}
