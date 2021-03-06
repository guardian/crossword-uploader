import sbt._
import Keys._
import com.typesafe.sbt.SbtScalariform.scalariformSettings

object CrosswordUploaderBuild extends Build {

  val basicSettings = Seq(
    organization  := "com.gu",
    description   := "AWS Lambda to upload crossword pdf files.",
    scalaVersion  := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )

  val root = Project("crossword-pdf-uploader", file("."))
    .settings(

      libraryDependencies ++= Seq(
        "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
        "com.amazonaws" % "aws-java-sdk-s3" % "1.9.30",
        "com.google.guava" % "guava" % "18.0",
        "com.squareup.okhttp" % "okhttp" % "2.5.0",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test"
      )
    )
    .settings(basicSettings)
    .settings(scalariformSettings)

}