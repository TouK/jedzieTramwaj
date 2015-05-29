package pl.touk.jedzieTramwaj

import org.scalatest.FlatSpec

class ZtmDataProviderTest extends FlatSpec {

  it should "fetch data" in {
    new ZtmDataProvider().fetch()
  }

}
