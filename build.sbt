name := "capstone"

version := "0.1"

scalaVersion := "2.11.12"


libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.1.7",
  "net.liftweb" %% "lift-json" % "3.3.0",
  "com.lihaoyi" %% "ujson" % "0.7.1",
  "org.apache.spark" % "spark-core_2.11" % "2.1.0",
)



