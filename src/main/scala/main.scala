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
import java.io.File
import scalax.io._
import scalax.file._
import resource._
import java.util.zip.ZipInputStream

case class DiffResult(
  existsOnlyIn1: List[String],
  existsOnlyIn2: List[String],
  differentFiles: List[String]
)
case class FileDiffResult(
  zip1: File,
  zip2: File,
  diffResult: DiffResult
)
object Diff {
  def diff(zip1: File, zip2: File): FileDiffResult = {
    managed(openZipFile(zip1)).acquireAndGet { in1 =>
      managed(openZipFile(zip2)).acquireAndGet { in2 =>
	val r = diff(new ZipEntries(in1).toList, new ZipEntries(in2).toList)
	FileDiffResult(zip1, zip2, r)
      }
    }
  }
  
  def diff(zip1: List[ZipEntry], zip2: List[ZipEntry]): DiffResult = {
    val e1 = zip1.map { e => (e.name, e) }.toMap
    val e2 = zip2.map { e => (e.name, e) }.toMap

    val existsOnlyIn1 = diffKeys(e1, e2).toList
    val existsOnlyIn2 = diffKeys(e2, e1).toList
    val differentFiles = intersectKeys(e1, e2).filter { x => e1(x).digest != e2(x).digest }.toList

    DiffResult(existsOnlyIn1, existsOnlyIn2, differentFiles)
  }

  private def diffKeys[A](m1: Map[A, _], m2: Map[A, _]): Set[A] =
    m1.keys.toSet.diff(m2.keys.toSet)

  private def intersectKeys[A](m1: Map[A, _], m2: Map[A, _]): Set[A] =
    m1.keys.toSet.intersect(m2.keys.toSet)

  private def openZipFile(file: File) = new ZipInputStream(new java.io.FileInputStream(file))
}

object Main {
  private def print(diff: FileDiffResult) {
    println("Files exist only in " + diff.zip1)
    println("===========================================")
    diff.diffResult.existsOnlyIn1.foreach(println)

    println("")
    println("Files exist only in " + diff.zip2)
    println("===========================================")
    diff.diffResult.existsOnlyIn2.foreach(println)

    println("")
    println("Files exist in both but not same")
    println("===========================================")
    diff.diffResult.differentFiles.foreach(println)
  }

  def main(args: Array[String]) {
    Args(args) match {
      case Right(Args(zip1, zip2)) => print(Diff.diff(zip1, zip2))
      case Left(e) => println(e.message)
    }
  }
}

case class ArgsException(message: String) extends Exception
case class Args(zip1: File, zip2: File)
object Args {
  import org.clapper.argot._

  def apply(args: Array[String]): Either[ArgsException, Args] = {
    val parser = new ArgotParser("diff-zip")

    implicit def toFile(s: String, opt: CommandLineArgument[File]) = {
      val f = new File(s)
      if (!f.exists) parser.usage(s + " not found.")
      f
    }

    val zip1 = parser.parameter[File]("ZIP1", "zip file 1", false)
    val zip2 = parser.parameter[File]("ZIP2", "zip file 2", false)

    try {
      parser.parse(args)
      Right(Args(zip1.value.get, zip2.value.get))
    } catch {
      case e: ArgotUsageException => Left(ArgsException(e.message))
    }
  }
}

case class ZipEntry(name: String, digest: Array[Byte])
class ZipEntries(stream: ZipInputStream) extends collection.Traversable[ZipEntry] {
  import java.security.MessageDigest

  def foreach[U](f: ZipEntry => U) {
    var e = stream.getNextEntry;
    while(e != null) {
      val d = makeDigest(MessageDigest.getInstance("MD5"), stream)
      f(ZipEntry(e.getName, d))
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
