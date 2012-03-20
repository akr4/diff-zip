/*
 * Copyright 2012 Akira Ueda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.physalis.diff_zip.zip

import scalax.io._
import resource._
import java.io.File
import java.util.zip.ZipInputStream
import java.util.zip.{ ZipEntry => JZE }

object Zip {
  def using[A](file: File)(f: ZipEntries => A) = {
    managed(new ZipInputStream(new java.io.FileInputStream(file))).acquireAndGet { s =>
      f(new ZipEntries(s))
    }
  }
}

trait ZipEntry {
  val name: String
  val digest: Array[Byte]
  val isDirectory: Boolean
}

private case class ZipEntryImpl(
  private val underlying: JZE,
  digest: Array[Byte]
) extends ZipEntry {
  lazy val name = underlying.getName
  lazy val isDirectory = underlying.isDirectory
}

class ZipEntries private[zip](stream: ZipInputStream) extends collection.Traversable[ZipEntry] {
  import java.security.MessageDigest

  def foreach[U](f: ZipEntry => U) {
    var e = stream.getNextEntry;
    while(e != null) {
      val d = makeDigest(MessageDigest.getInstance("MD5"), stream)
      f(ZipEntryImpl(e, d))
      e = stream.getNextEntry
    }

    @annotation.tailrec
    def makeDigest(d: MessageDigest, stream: ZipInputStream): Array[Byte] = {
      val buf = new Array[Byte](4096)
      val length = stream.read(buf)
      if (length > 0) {
        d.update(buf, 0, length)
        makeDigest(d, stream)
      } else {
        d.digest
      }
    }
  }
}
