package com.gu.imagechecker

import org.threeten.extra.AmountFormats
import java.time.{LocalDate, Period, Instant}
import java.time.ZoneId

case class ImageRecord(imageUri: String, createdDate: Option[Instant], baseImageRecord: Option[ImageRecord] = None) {
  def age = createdDate.map(cd =>
    Period.between(LocalDate.ofInstant(cd, ZoneId.systemDefault()), LocalDate.now()))
  def ageString = age.map(age => AmountFormats.wordBased(age, java.util.Locale.getDefault()))
  def upToDateMessage(expectedVersion: Option[ImageRecord]): String = (expectedVersion, baseImageRecord) match {
    case (None, _) | (_, None) => "unknown"
    case (Some(ImageRecord(exp, _, _)), Some(ImageRecord(actual, _, _))) if exp == actual =>
      "up-to-date"
    case _ => "out-of-date"
  }
}

case class LambdaRecord(name: String, lastModified: String, image: ImageRecord)
