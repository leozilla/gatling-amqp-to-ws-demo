package betr.performance.contracts

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.core.session.Session
import io.gatling.http.Predef._
import io.gatling.http.check.ws.{WsTextCheck, WsTextFrameCheck}

object PublicApi {

  val subscribeFixturesRequest: String = """{
                                  |	"action": "SUB",
                                  | "resource": "fixtures",
                                  |	"requestId": "${requestId}"
                                  |}""".stripMargin

  def setNewRequestId(session: Session): Session = session.set("requestId", UUID.randomUUID().toString)

  def isEvent: WsTextCheck =
    jsonPath("$.eventId").ofType[String].exists

  def isResponse: WsTextCheck =
    jsonPath("$.requestId").ofType[String].exists

  def hasType(`type`: String): WsTextCheck =
    jsonPath("$.type").ofType[String].is(`type`)

  def hasRequestId: WsTextCheck =
    jsonPath("$.requestId").ofType[String].is("${requestId}")

  def hasStatus(status: Int): WsTextCheck =
    jsonPath("$.status").ofType[Int].is(status)

  def checkFixtureUpdated(name: String, fixtureId: String, status: String): WsTextFrameCheck = {
    ws.checkTextMessage(name)
      .matching(isEvent)
      .matching(hasType("FixtureUpdated"))
      .matching(jsonPath("$.payload.id").ofType[String].is(fixtureId))
      .check(jsonPath("$.payload.status").ofType[String].is(status))
  }

  def checkMarketUpdated(name: String, marketId: String, status: String): WsTextFrameCheck = {
    ws.checkTextMessage(name)
      .matching(isEvent)
      .matching(hasType("MarketUpdated"))
      .matching(jsonPath("$.payload.id").ofType[String].is(marketId))
      .check(jsonPath("$.payload.status").ofType[String].is(status))
  }
}
