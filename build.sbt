name := """backend-exercise"""
organization := "prime"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.3"
val akkaManagementVersion = "1.0.8"
val akkaVersion = "2.6.8"
val akkaHTTPVersion = "10.1.12"


libraryDependencies ++= Seq(
  javaWs,
  guice,
  ehcache,
  javaJdbc,
  filters,
  "junit" % "junit" % "4.12",
  "org.mongodb" % "mongodb-driver-sync" % "4.1.0",
  "org.projectlombok" % "lombok" % "1.18.12",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0",
  "com.auth0" % "java-jwt" % "3.11.0",
  //"com.typesafe.play" %% "play-mailer" % "6.0.1",
  //"com.typesafe.play" %% "play-mailer-guice" % "6.0.1",

  "org.hibernate" % "hibernate-validator" % "6.1.5.Final",
  "org.glassfish" % "javax.el" % "3.0.0",
  // akka related stuff
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  // akka cluster related stuff
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
  // akka htttp related stuff
  "com.typesafe.akka" %% "akka-http-core" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHTTPVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHTTPVersion,

  "com.github.karelcemus" %% "play-redis" % "2.5.0"
)
