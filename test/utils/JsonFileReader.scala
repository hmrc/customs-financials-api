package utils

import play.api.libs.json.{JsValue, Json}

import scala.io.Source

trait JsonFileReader {
  def readJsonFromFile(filePath: String): JsValue = {
    val filePathResource = Source.fromURL(getClass.getResource(filePath))
    val path = filePathResource.mkString
    filePathResource.close()
    Json.parse(path)
  }
}
