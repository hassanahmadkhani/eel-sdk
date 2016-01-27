package com.sksamuel.eel

import java.nio.file.{Paths, Path}

import com.sksamuel.eel.source.CsvSource
import org.scalatest.{Matchers, WordSpec}

class CsvTest extends WordSpec with Matchers {

  "CsvTest" should {
    "read from path" in {
      val path = Paths.get(getClass.getResource("/csvtest.csv").getFile)
      CsvSource(path).size shouldBe 3
    }
  }
}