package io.eels.component.csv

import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

import com.sksamuel.exts.Logging
import com.sksamuel.exts.io.Using
import com.univocity.parsers.csv.CsvParser
import io.eels.Chunk
import io.eels.datastream.{DataStream, Publisher, Subscriber, Subscription}
import io.eels.schema.StructType

class CsvPublisher(createParser: () => CsvParser,
                   inputFn: () => InputStream,
                   header: Header,
                   skipBadRows: Boolean,
                   schema: StructType) extends Publisher[Chunk] with Logging with Using {

  val rowsToSkip: Int = header match {
    case Header.FirstRow => 1
    case _ => 0
  }


  override def subscribe(subscriber: Subscriber[Chunk]): Unit = {
    val input = inputFn()
    val parser = createParser()

    try {
      parser.beginParsing(input)

      val running = new AtomicBoolean(true)
      subscriber.subscribed(Subscription.fromRunning(running))

      val iter: Iterator[Chunk] = Iterator.continually(parser.parseNext)
        .takeWhile(_ != null)
        .takeWhile(_ => running.get)
        .drop(rowsToSkip)
        .map(_.toArray[Any])
        .grouped(DataStream.DefaultBatchSize)

      subscriber.completed()

    } catch {
      case t: Throwable => subscriber.error(t)

    } finally {
      parser.stopParsing()
      input.close()
    }
  }
}