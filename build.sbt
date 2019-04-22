name := "capstone"

version := "0.1"

scalaVersion := "2.12.8"

val sparkVersion = "2.3.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.1.7",
  "net.liftweb" %% "lift-json" % "3.3.0",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.spark" %% "spark-graphx" % sparkVersion
)



