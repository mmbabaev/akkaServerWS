name := """akka-sample-persistence-scala"""

version := "2.3.10"

scalaVersion := "2.11.6"

libraryDependencies += "com.lihaoyi" %% "upickle" % "0.3.4"

libraryDependencies ++= {
  val akkaHttpVersion = "1.0-RC4"
  Seq(
    "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.10",
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion,
    "org.java-websocket" % "Java-WebSocket" % "1.3.0"
  )
}






fork in run := true