package com.gu.imagechecker

case class ImageRecord(imageUri: String)

case class LambdaRecord(name: String, lastModified: String, image: ImageRecord)
