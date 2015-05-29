package pl.touk.jedzieTramwaj

import org.scalatest.{Matchers, FlatSpec}
import pl.touk.jedzieTramwaj.model.Location

class ZtmDataParserTest extends FlatSpec with Matchers {
  import org.scalatest.OptionValues._

  it should "parse file" in {
    val result = new ZtmDataParser().parse(getClass.getResourceAsStream("/ztm.data"))
    result should not be empty
    result.head.name shouldEqual "KIJOWSKA"
  }

  it should "found indnents" in {
    val result = new ZtmDataParser().indnentOnContent(getClass.getResourceAsStream("/ztm.data"))
    println(result.next())
  }

  it should "parse name" in {
    new ZtmDataParser().parseName("   1001   KIJOWSKA,                        --  WARSZAWA") shouldEqual "KIJOWSKA"
  }

  it should "parse bus stop" in {
    val content = "         100101   2      Ul./Pl.: TARGOWA,                          Kier.: AL.ZIELENIECKA,                   Y= 52.248670     X= 21.044260"
    new ZtmDataParser().parseBusStop(content, "foo").value shouldEqual BusStop("foo", "TARGOWA", "AL.ZIELENIECKA", Location(52.248670, 21.044260))
  }

}
