package com.gu.crossword

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.xmluploader.models.{CrosswordXmlLambdaConfig, CrosswordXmlFile}
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}


class LambdaTest extends AnyFlatSpec with Matchers with TryValues {

  type PageCreator = (String, Elem) => Try[Unit]
  type Uploader = (String, Array[Byte]) => Try[String]

  trait FakeLambda extends CrosswordXmlUploaderLambda {
    var archiveCalled = 0
    var archiveFailedCalled = 0

    def archiveCrosswordXMLFile(bucketName: String, awsKey: String): Unit = {
      archiveCalled += 1
    }

    def archiveFailedCrosswordXMLFile(bucketName: String, awsKey: String): Unit = {
      archiveFailedCalled += 1
    }
  }

  def buildFakeLambda(
                       crosswordXmlFiles: List[CrosswordXmlFile] = List.empty,
                       pageCreator: PageCreator = (_, _) => Success(()),
                       uploader: Uploader = (_, _) => Success(<crossword/>.toString()),
                     ) = {
    new FakeLambda {
      override def getCrosswordXmlFiles(crosswordsBucketName: String): List[CrosswordXmlFile] = crosswordXmlFiles

      override def createPage(streamName: String)(key: String, xmlData: Elem): Try[Unit] = pageCreator(key, xmlData)

      override def upload(url: String)(id: String, data: Array[Byte]): Try[String] = uploader(id, data)

      override def getConfig(context: Context): CrosswordXmlLambdaConfig = CrosswordXmlLambdaConfig(
        crosswordsBucketName = "crosswords-bucket",
        crosswordMicroAppUrl = None,
        crosswordV2Url = "https://crossword-microapp-url",
      )
    }
  }

  val crosswordMicroAppResponse = Source.fromResource("example-crossword-microapp-response-quiptic-834.xml").getLines().mkString
  val crosswordMicroAppResponseXml = XML.loadString(crosswordMicroAppResponse)

  it should "archive correctly a successfully processed crossword" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Success(crosswordMicroAppResponseXml.toString())
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(1)
    fakeLambda.archiveFailedCalled should be(0)
  }

  it should "archive as failure a processed crossword with invalid xml" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Success("not xml at all is it?")
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include("Failures detected when uploading crossword xml files (key)!")

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword that fails to upload a crossword" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Failure(new Error("Failed to upload crossword")),
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include("Failures detected when uploading crossword xml files (key)!")

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }
}
