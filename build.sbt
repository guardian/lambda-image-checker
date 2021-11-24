scalaVersion := "2.13.7"

enablePlugins(PlayScala)

lazy val awsSdkVersion = "2.17.87"

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % "2.5.0",
  "software.amazon.awssdk" % "lambda" % awsSdkVersion
)
