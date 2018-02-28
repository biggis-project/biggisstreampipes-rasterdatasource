package encoders
import java.io.File

import org.apache.kafka.clients.producer.ProducerRecord
import play.api.libs.json.Json

/**
  * Created by Jochen Lutz on 2017-09-13.
  */
class JsonOobRasterdataEncoder(aTopic: String, aBaseUrl: String) extends BaseRasterdataEncoder(aTopic) {
  val kafkaTopic = aTopic + ".oob"
  val baseUrl = aBaseUrl

  override def encodeMessage(file: File): ProducerRecord[String, String] = {
    val json = Json.obj(
      "latitude" -> 0,//TODO
      "longitude" -> 0,
      "altitude" -> 0,
      "filename" -> file.getName,
      "raster-data-location" -> (baseUrl + file.getPath)
    )

    return new ProducerRecord(kafkaTopic, json.toString())
  }
}
