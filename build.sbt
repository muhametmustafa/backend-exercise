name := """backend-exercise"""
organization := "prime"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.3"

libraryDependencies += guice
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
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"
  )
