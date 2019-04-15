name := "gatling-amqp-to-ws-demo"
version := "0.1"
scalaVersion := "2.12.8"

scalacOptions := Seq(
  "-encoding",
  "UTF-8",
  "-target:jvm-1.8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:postfixOps")

enablePlugins(GatlingPlugin)

val Versions = new {
  val gatling = "3.0.3"
  val log4j2  = "2.11.2"
  val amqpClient = "5.6.0"
  val coreUtils = "0.2.1"
  val sprayJson = "1.3.5"
}

libraryDependencies ++= List(
  // gatling
  "io.gatling.highcharts" % "gatling-charts-highcharts" % Versions.gatling % "compile,test,it",
  "io.gatling"            % "gatling-test-framework"    % Versions.gatling % "compile,test,it",

  "io.spray" %%  "spray-json" % Versions.sprayJson,

  // amqp
  "com.rabbitmq" % "amqp-client" % Versions.amqpClient,

  // config
  "com.github.pureconfig" %% "pureconfig" % "0.10.2",
  "eu.timepit" %% "refined-pureconfig" % "0.9.4",

  // logging
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.log4j2,
  "org.apache.logging.log4j" % "log4j-api"        % Versions.log4j2,
  "org.apache.logging.log4j" % "log4j-core"       % Versions.log4j2,
)
libraryDependencies ~= (_.map(_.excludeAll(ExclusionRule(organization = "ch.qos.logback"))))