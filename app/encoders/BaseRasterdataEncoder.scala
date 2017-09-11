package encoders

import java.io.{File, FileInputStream}

import org.apache.kafka.clients.producer.ProducerRecord

/**
  * Created by Jochen Lutz on 2017-09-07.
  */
abstract class BaseRasterdataEncoder(aTopic: String) {
  def encodeMessage(file: File): ProducerRecord[String, String];

  def getFileContent(file: File): Array[Byte] = {
    val fis = new FileInputStream(file)
    var content: Array[Byte] = new Array[Byte](file.length.toInt)
    fis.read(content)

    content
  }
}
