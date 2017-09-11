package encoders

/**
  * Created by Jochen Lutz on 2017-09-11.
  */
object RasterdataEncoderFactory {
  def apply(serializerType: String, topic: String): BaseRasterdataEncoder = serializerType match {
    case "json-inline" => new JsonInlineRasterdataEncoder(topic)
  }
}
