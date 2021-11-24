package com.gu.imagechecker

import controllers.AssetsComponents
import play.api._
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

class ImageCheckerComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with AssetsComponents {
  import com.softwaremill.macwire._

  val defaultLambdaClient = LambdaClient.builder()
    .credentialsProvider(ProfileCredentialsProvider.create("mobile"))
    .build()

//  lazy val defaultAssets = assets()
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
