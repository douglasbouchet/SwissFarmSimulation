lazy val root = (project in file(".")).settings(
  name := "economic_simulations",
  organization := "ch.epfl.data",
  version := "1.0",
  //scalaVersion := "2.11.1"
  scalaVersion := "2.13.1"
)

// libraryDependencies += "com.quantifind" %% "wisp" % "0.0.4"

libraryDependencies  ++= Seq(

  "org.scalanlp" %% "breeze" % "1.2", 
  "org.scalanlp" %% "breeze-viz" % "1.2", 
  "org.scalactic" %% "scalactic" % "3.2.9", 
  "org.scalatest" %% "scalatest" % "3.2.9" % "test", 
  //Added for scalax 
  "org.scala-graph" %% "graph-core" % "1.13.2",

  /** Used to import excel files */
  "org.apache.poi" % "poi" % "3.17",
  "org.apache.poi" % "poi-ooxml" % "3.17",

  /** Used to pickle objects */



)

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"

