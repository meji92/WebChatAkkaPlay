name := """play-java"""

version := "1.0-SNAPSHOT"

val akkaVersion = "2.3.9"
//val akkaVersion = "2.4-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion
  //"com.typesafe.akka" % "akka-cluster-tools_2.11" % "2.4-M2",
  //"com.typesafe.akka" % "akka-contrib_2.11" % akkaVersion
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := false