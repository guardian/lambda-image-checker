package com.gu.imagechecker

import play.api.mvc._
import play.api.libs.json._
import com.github.dockerjava.api.DockerClient

import scala.jdk.CollectionConverters._
import com.github.dockerjava.api.model.Image

// import play.api.mvc.Request
// import play.api.mvc.AnyContent

class ImageCheckerController(cc: ControllerComponents, imageChecker: ImageChecker, dockerClient: DockerClient) extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    val recs = imageChecker.getAllLambdaRecords().toList
    Ok(views.html.dashboard(recs, imageChecker.getReferenceImage()))
  }

  implicit val imageWrites = Writes { i: Image =>
    Json.obj(
      "id" -> i.getId(),
      "repoTags" -> Option(i.getRepoTags()),
      "repoDigests" -> Option(i.getRepoDigests()),
    )
  }

  def listImages() = Action { implicit request: Request[AnyContent] =>
    val searchFor = "201359054765.dkr.ecr.eu-west-1.amazonaws.com/notificationworker-lambda-images:2555"
    val images = {
      val cmd = dockerClient.listImagesCmd().withShowAll(false)
//      cmd.getFilters().put("reference",   List("201359054765.dkr.ecr.eu-west-1.amazonaws.com/notificationworker-lambda-images:2555").asJava)
//      cmd.getFilters().put("reference", List("201359054765.dkr.ecr.eu-west-1.amazonaws.com/notificationworker-lambda-images:*").asJava)
      cmd.exec().asScala.toList.filter { i =>
        Option(i.getRepoTags()).exists(_.contains(searchFor))
      }
    }

    Ok(Json.toJson(images))
  }
}
