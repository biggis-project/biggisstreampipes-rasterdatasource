name := "RasterdataSource"

version := "1.0"

scalaVersion := "2.12.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  guice,
  "org.apache.kafka" % "kafka-clients" % "0.9.0.0"
)

routesGenerator := InjectedRoutesGenerator
