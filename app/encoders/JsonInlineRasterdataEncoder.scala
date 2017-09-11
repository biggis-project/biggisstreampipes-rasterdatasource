package encoders

import java.io.File
import java.util.Base64

import org.apache.kafka.clients.producer.ProducerRecord
import play.api.libs.json.Json

/**
  * Created by Jochen Lutz on 2017-09-10.
  */
class JsonInlineRasterdataEncoder(aTopic: String) extends BaseRasterdataEncoder(aTopic) {
  val kafkaTopic = aTopic

  override def encodeMessage(file: File): ProducerRecord[String, String] = {
    val b64 = Base64.getEncoder.encodeToString(getFileContent(file))

    val json = Json.obj(
      "latitude" -> 0,//TODO
      "longitude" -> 0,
      "altitude" -> 0,
      "filename" -> file.getName,
      "raster-data" -> b64
    )

    return new ProducerRecord(kafkaTopic, "raster-data-test-2", json.toString())
  }
}
