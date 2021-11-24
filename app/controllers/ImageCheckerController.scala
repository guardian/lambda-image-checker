package com.gu.imagechecker

import play.api.mvc._
// import play.api.mvc.Request
// import play.api.mvc.AnyContent

class ImageCheckerController(cc: ControllerComponents, imageChecker: ImageChecker) extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    val recs = imageChecker.getLambdaRecords().toList
    Ok(views.html.dashboard(recs))
  }
}
