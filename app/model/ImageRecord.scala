package com.gu.imagechecker

import org.threeten.extra.AmountFormats
import java.time.{LocalDate, Period, Instant}
import java.time.ZoneId

case class BaseImageRecord(version: String, createdDate: Instant) {
  def age = Period.between(LocalDate.ofInstant(createdDate, ZoneId.systemDefault()), LocalDate.now())
  def ageString = AmountFormats.wordBased(age, java.util.Locale.getDefault())
}

case class ImageRecord(imageUri: String, baseImageRecord: Option[BaseImageRecord]) {
  def upToDateMessage(expectedVersion: Option[String]): String = (expectedVersion, baseImageRecord) match {
    case (exp, actual) if exp.isEmpty || actual.isEmpty => "unknown"
    case (Some(exp), Some(BaseImageRecord(actual, _))) if exp == actual => "up-to-date"
    case _ => "out-of-date"
  }
}

case class LambdaRecord(name: String, lastModified: String, image: ImageRecord)
