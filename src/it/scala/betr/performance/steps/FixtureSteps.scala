package betr.performance.steps

import betr.performance.contracts.PublicApi._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.action.ws.WsSendTextFrameBuilder

import scala.concurrent.duration._

object FixtureSteps {

  def subscribedForFixtures(): WsSendTextFrameBuilder = {
    ws("SubscribeFixtures")
      .sendText(subscribeFixturesRequest)
      .await(10.seconds)(
        ws.checkTextMessage("SubscribeFixtures response")
          .matching(hasRequestId)
          .matching(hasStatus(200))
          .check(jsonPath("$.payload..id").exists))
  }
}
