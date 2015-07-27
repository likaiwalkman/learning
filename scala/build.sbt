name := "scala-learning"

version := "0.1.0"

scalaVersion := "2.11.7"

val akkaVersion = "2.3.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-agent" % akkaVersion,
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M4"
)

scalacOptions ++= Seq("-feature")
