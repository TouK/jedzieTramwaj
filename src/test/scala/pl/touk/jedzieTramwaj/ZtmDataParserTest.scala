package pl.touk.jedzieTramwaj

import org.scalatest.{Matchers, FlatSpec}

class ZtmDataParserTest extends FlatSpec with Matchers {

  it should "parse file" in {
    new ZtmDataParser().parse(getClass.getResourceAsStream("/ztm.data")) should not be (empty)
  }

}
