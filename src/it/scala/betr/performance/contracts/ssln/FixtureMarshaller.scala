package betr.performance.contracts.ssln

import betr.performance.contracts.ssln.FixtureDtos.{FixtureStatus, FixtureUpdated}
import spray.json._

object FixtureMarshaller {

  implicit val fixtureStatusFormat: JsonFormat[FixtureStatus] = new JsonFormat[FixtureStatus] {

    override def write(obj: FixtureStatus): JsValue = obj match {
      case FixtureStatus.Created   => JsString("CREATED")
      case FixtureStatus.Active    => JsString("ACTIVE")
      case FixtureStatus.Suspended => JsString("SUSPENDED")
      case FixtureStatus.Closed    => JsString("CLOSED")
      case FixtureStatus.Resulted  => JsString("RESULTED")
    }

    override def read(json: JsValue): FixtureStatus = ???
  }

  implicit val fixtureUpdatedFormat: RootJsonFormat[FixtureUpdated] = new RootJsonFormat[FixtureUpdated] {
    def write(obj: FixtureUpdated) = JsObject(
      "id"          -> JsString(obj.id),
      "status"      -> obj.status.toJson,
      "startTime"   -> JsString(obj.startTime.toString)
    )

    override def read(json: JsValue): FixtureUpdated = ???
  }
}
