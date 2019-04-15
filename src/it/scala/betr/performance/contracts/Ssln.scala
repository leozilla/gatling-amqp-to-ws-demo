package betr.performance.contracts

import betr.performance.contracts.ssln.FixtureDtos.FixtureUpdated

object Ssln {

  private val BaseTopic = "betr"

  def fixtureUpdatedRoutingKey(m: FixtureUpdated) =
    s"$BaseTopic.${m.id}"
}
