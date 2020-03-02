name := """stocks"""
organization := "com.alex"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.yahoofinance-api" % "YahooFinanceAPI" % "3.15.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.alex.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.alex.binders._"
