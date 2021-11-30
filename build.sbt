scalaVersion := "2.13.7"

enablePlugins(PlayScala)

lazy val awsSdkVersion = "2.17.87"

lazy val dockerJavaVersion = "3.2.12"

libraryDependencies ++= Seq(
  "com.softwaremill.macwire" %% "macros" % "2.5.0",
  "software.amazon.awssdk" % "lambda" % awsSdkVersion,
  "com.github.docker-java" % "docker-java-core" % dockerJavaVersion,
  "com.github.docker-java" % "docker-java-transport-httpclient5" % dockerJavaVersion
)
