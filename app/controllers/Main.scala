package controllers

import java.util.Properties
import java.util.concurrent.ExecutionException
import javax.inject._

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api._
import play.api.i18n.MessagesApi
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

  def index = Action {
    Ok("Test")
  }

  def startSourceHelp = Action {
    Ok("Send a JSON document to adjust behaviour. Defaults as below.\n\n" +
      "{\n" +
      "  \"startIndex\": 1,\n" +
      "  \"endIndex\": 5,\n" +
      "  \"interval\": 3\n" +
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

    for (i <- startIndex to endIndex) {
      val record = try {
        new ProducerRecord(kafkaTopic, "Test", s"Hallo Welt $i")
      }
      catch {
        case e: Exception => {
          Logger.error(s"Kafka error: ${e.getMessage}")
          null
        }
      }

      try {
        val f = kafka.send(record)
        f.get
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

    Ok("OK.")
  }
}
