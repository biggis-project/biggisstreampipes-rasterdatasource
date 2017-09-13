package controllers

import java.io.File
import javax.inject.Inject

import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Jochen Lutz on 2017-09-11.
  */
class OutOfBandFiles @Inject() (components: ControllerComponents, configuration: play.api.Configuration) extends AbstractController(components) {
  def getTileFile(filename: String) = Action {
    val file = new File("tiles/" + filename) //TODO: sauber!
    Ok.sendFile(file)
  }
}
