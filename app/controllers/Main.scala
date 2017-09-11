package controllers

import java.io.File
import java.util.Properties
import java.util.concurrent.ExecutionException
import javax.inject._

import encoders.RasterdataEncoderFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api._
import play.api.libs.json._
import play.api.mvc._

/**
  * Created by Jochen Lutz on 2017-09-06.
  */

@Singleton
class Main @Inject() (components: ControllerComponents, configuration: play.api.Configuration) extends AbstractController(components) {
  val kafkaProps = new Properties()
  kafkaProps.put("bootstrap.servers", configuration.get[String]("kafka.server"))
  kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  kafkaProps.put("max.block.ms", configuration.get[String]("kafka.maxBlockMs"))

  val kafka = try {
    new KafkaProducer[String, String](kafkaProps)
  }
  catch {
    case e: Exception => {
      Logger.error(s"Kafka startup error: ${e.getMessage}")
      null
    }
  }

  val kafkaTopic = configuration.get[String]("kafka.topic")

  //outer key: serializer, inner key: tileset
  var messagesCache = scala.collection.mutable.Map[String, Option[scala.collection.mutable.Map[String, Option[Array[ProducerRecord[String, String]]]]]]().withDefaultValue(null)

  def index = Action {
    Ok("Test")
  }

  def startSourceHelp = Action {
    Ok("Send a JSON document to adjust behaviour. Defaults as below.\n\n" +
      "{\n" +
      "  \"startIndex\": 1,\n" +
      "  \"endIndex\": 5,\n" +
      "  \"interval\": 3,\n" +
      "  \"tileset\": \"techpark\",\n" +
      "  \"serializer\": \"json-inline\"\n" +
      "}\n")
  }

  def startSource: Action[AnyContent] = Action { request: Request[AnyContent] =>
    val json = request.body.asJson

    val startIndex = json match {
      case Some(js: JsObject) => (js \ "startIndex").asOpt[Int].getOrElse(1)
      case default => 1
    }
    val endIndex = json match {
      case Some(js: JsObject) => (js \ "endIndex").asOpt[Int].getOrElse(5)
      case default => 5
    }
    val interval = json match {
      case Some(js: JsObject) => (js \ "interval").asOpt[Int].getOrElse(3)
      case default => 3
    }
    val tileset = json match {
      case Some(js: JsObject) => (js \ "tileset").asOpt[String].getOrElse("techpark")
      case default => "techpark"
    }
    val serializerType = json match {
      case Some(js: JsObject) => (js \ "serializer").asOpt[String].getOrElse("json-inline")
      case default => "json-inline"
    }

    val tiles = messagesCache(serializerType) match {
      case Some(m: Map[String, Option[Array[ProducerRecord[String, String]]]]) => {
        val messages = buildTileMessages(tileset, serializerType)

        m += tileset -> messages

        messages
      }
      case default => {
        val messages = buildTileMessages(tileset, serializerType)

        messagesCache += serializerType -> Some(scala.collection.mutable.Map(tileset -> messages))

        messages
      }
    }

    //val fastTiles = buildTileMessages(tileset, serializerType)
    tiles match {
      case Some(x) => {
        for (i <- startIndex to endIndex) {
          //TODO: Index-Check

          try {
            val f = kafka.send(tiles.get(i))
            f.get
            println("Sent message " + tiles.get(i).value().substring(0,100) + " ...")
          }
          catch {
            case e: ExecutionException => {
              Logger.error(s"Kafka execution exception: ${e.getMessage}")
              InternalServerError(s"Kafka not accessible: ${e.getMessage}")
            }
            case e: Exception => {
              Logger.error(s"Kafka error: ${e.getMessage}")
              InternalServerError(s"Kafka not accessible: ${e.getMessage}")
            }
          }

          Thread.sleep(interval * 1000)
        }

        Ok("OK.\n")
      }
      case None => NotFound("This tileset does not exist or contains no tiles.\n")
    }
  }

  def readAllFiles(tileset: String): Option[List[File]] = {
    val folder = new File(s"tiles/${tileset}")
    if (folder.exists && folder.isDirectory)
      return Some(folder.listFiles.toList.sortBy(_.getName))

    return None
  }

  def buildTileMessages(tileset: String, serializer: String): Option[Array[ProducerRecord[String, String]]] = {
    val ser = RasterdataEncoderFactory(serializer, kafkaTopic)

    val files = readAllFiles(tileset)

    files match {
      case Some(l: List[File]) => {
        Some(l.map(f => ser.encodeMessage(f)).toArray)
      }
      case None => None
    }
  }
}
