assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
test in assembly := {}

val akkaHttp = "1.0-RC3"

// Minimal usage
libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % akkaHttp

libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttp

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttp

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"

name := "jedzieTramwaj"

version := "0.1"

scalaVersion := "2.11.5"
