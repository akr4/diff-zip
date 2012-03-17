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

object Diff {
  def diff(zip1: File, zip2: File): DiffResult = {
    managed(openZipFile(zip1)).acquireAndGet { in1 =>
      managed(openZipFile(zip2)).acquireAndGet { in2 =>
	diff(new ZipEntries(in1).toList, new ZipEntries(in2).toList)
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
  private def print(diff: DiffResult) {
    println("Exists only in zip1")
    println("====================")
    diff.existsOnlyIn1.foreach(println)

    println("")
    println("Exists only in zip2")
    println("====================")
    diff.existsOnlyIn2.foreach(println)

    println("")
    println("Exists in both but not same")
    println("================================")
    diff.differentFiles.foreach(println)
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

  private implicit def toFile(s: String, opt: CommandLineArgument[File]) = new File(s)

  def apply(args: Array[String]): Either[ArgsException, Args] = {
    val parser = new ArgotParser("diff-zip")
    val zip1 = parser.parameter[File]("ZIP1", "zip file 1", false);
    val zip2 = parser.parameter[File]("ZIP2", "zip file 2", false)

    try {
      parser.parse(args)
      Right(Args(zip1.value.get, zip2.value.get))
    } catch {
      case e: ArgotUsageException => Left(ArgsException(e.message))
    }
  }
}

case class ZipEntry(name: String, digest: String)
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
    def makeDigest(d: MessageDigest, stream: ZipInputStream): String = {
      val buf = new Array[Byte](4096)
      val length = stream.read(buf)
      if (length > 0) {
	d.update(buf, 0, length)
	makeDigest(d, stream)
      } else {
	toHashString(d.digest)
      }
    }

    def toHashString(bytes: Array[Byte]): String = {
      bytes.foldLeft("") { (r, b) =>
	if (b < 0x10) r + "0" + Integer.toHexString(b)
	else r + Integer.toHexString(b)
      }
    }
  }
}
