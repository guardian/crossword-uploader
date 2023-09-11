package com.gu.crossword.pdfuploader

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.pdfuploader.models.CrosswordPdfLambdaConfig
import com.gu.crossword.services.AWS.s3Client

import java.util.Properties
import scala.util.Try


trait CrosswordConfigRetriever {
  def getConfig(context: Context): CrosswordPdfLambdaConfig
}

trait S3CrosswordConfigRetriever extends CrosswordConfigRetriever {
  def getConfig(context: Context): CrosswordPdfLambdaConfig = {

    val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(false)
    val stage = if (isProd) "PROD" else "CODE"
    val config = loadConfig(stage)

    val crosswordMicroAppUrl = Option(config.getProperty("crosswordmicroapp.url")) getOrElse sys.error("'crosswordmicroapp.url' property missing.")
    val crosswordPdfPublicBucketName = s"crosswords-pdf-public-${stage.toLowerCase}"
    val crosswordPdfPublicFileLocation = if (isProd) s"https://crosswords-static.guim.co.uk" else s"https://s3-eu-west-1.amazonaws.com/$crosswordPdfPublicBucketName"

    val crosswordsBucketName: String =
      if (isProd)
        "crossword-files-for-processing"
      else
        "crossword-files-for-processing-code"

    CrosswordPdfLambdaConfig(
      crosswordMicroAppUrl,
      crosswordsBucketName,
      crosswordPdfPublicBucketName,
      crosswordPdfPublicFileLocation
    )
  }

  private def loadConfig(stage: String) = {
    println("Loading config for stage: " + stage)

    val configFileKey = s"crossword-pdf-uploader/$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-uploader-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(configInputStream)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}