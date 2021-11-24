scalaVersion := "3.1.0"

lazy val awsSdkVersion = "2.17.87"

libraryDependencies ++= Seq(
  "software.amazon.awssdk" % "lambda" % awsSdkVersion
)
