lazy val commonSettings = Seq(
  organization := "com.gu",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
    "com.squareup.okhttp" % "okhttp" % "2.5.0",
    "com.google.guava" % "guava" % "18.0",
    "org.scalatest" %% "scalatest" % "2.2.5" % "test"
  )
)

lazy val `crossword-xml-uploader` = (project in file("crossword-xml-uploader"))
  .settings(commonSettings)
  .settings(
    description := "AWS Lambda to upload crossword xml files.",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % "1.10.39", // FIXME upgrade and merge with pdf-uploader's dependency declaration
      "com.amazonaws" % "aws-java-sdk-kinesis" % "1.10.39",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
    )
  )

lazy val `crossword-pdf-uploader` = (project in file("crossword-pdf-uploader"))
  .settings(commonSettings)
  .settings(
    description := "AWS Lambda to upload crossword pdf files.",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % "1.9.30"
    )
  )


lazy val root = (project in file("."))
  .aggregate(`crossword-xml-uploader`, `crossword-pdf-uploader`)