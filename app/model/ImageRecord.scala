package com.gu.imagechecker

case class ImageRecord(imageUri: String, baseImageVersion: Option[String])

case class LambdaRecord(name: String, lastModified: String, image: ImageRecord)
