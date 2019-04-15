package betr.performance

import java.net.{URI, URL}

import pureconfig.generic.auto._
import eu.timepit.refined.pureconfig._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.ws.WsConnectRequestBuilder

abstract class PublicApiSimulation extends Simulation {

  val publicApiConfig: PublicApiConfig = AppConfigLoader.load[PublicApiConfig]("public-api")

  val wsProtocol: HttpProtocolBuilder = http
    .baseUrl(publicApiConfig.httpBaseUrl.toString)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
    .wsBaseUrl(publicApiConfig.wsBaseUrl.toString)

  def wsConnectToPublicApi(): WsConnectRequestBuilder = ws("WS Connect").connect(publicApiConfig.wsPath)
}

case class PublicApiConfig(httpBaseUrl: URI, wsBaseUrl: URI, wsPath: String)
