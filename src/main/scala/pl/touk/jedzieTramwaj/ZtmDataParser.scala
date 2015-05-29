package pl.touk.jedzieTramwaj

import java.io.InputStream

import pl.touk.jedzieTramwaj.model.Location

import scala.io.Source

class ZtmDataParser {

  def parse(stream: InputStream): Seq[BusStop] = {
    var lastName: String = null
    var list = List.empty[Option[BusStop]]
    indnentOnContent(stream).foreach {
      case (1, content) =>
        lastName = parseName(content)
      case (3, content) =>
        list = parseBusStop(content, lastName) :: list
      case (4, content) =>
        val (head :: tail) = list
          list = head.map(_.add(parseLines(content))) :: tail
      case _ => // skip
    }
    list.flatten.reverse
  }

  def parseName(content: String): String = {
    content.substring(10, 43).trim.dropRight(1)
  }

  def parseBusStop(content: String, name: String): Option[BusStop] = {
    for {
      lon <- parseLocationPart(content.substring(112, 126))
      lat <- parseLocationPart(content.substring(129, 138))
    } yield {
      val description = content.substring(34, 68).trim.dropRight(1)
      val direction = content.substring(75, 109).trim.dropRight(1)
      BusStop(name, description, direction, Location(lon, lat))
    }
  }

  private def parseLocationPart(str: String): Option[Double] = {
    try {
      Some(java.lang.Double.parseDouble(str.trim))
    } catch {
      case e: NumberFormatException => None
    }
  }

  def parseLines(content: String): Seq[String] = {
    content.substring(40, content.length).trim.split(" +").map(_.replace("\\^", ""))
  }

  def indnentOnContent(stream: InputStream): Iterator[(Int, String)] = {
    Source.fromInputStream(stream).getLines().map { line =>
      val Pattern = "^( *).*".r("indent")
      line match {
        case Pattern(indent) => (indent.length/3) -> line
      }
    }
  }
}

case class BusStop(name: String, description: String, direction: String, loc: Location, lines: Seq[String] = IndexedSeq.empty) {
  def add(next: Seq[String]) = copy(lines = lines ++ next)
}