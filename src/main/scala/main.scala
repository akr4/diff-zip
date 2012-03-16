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
  def diff(war1: File, war2: File): DiffResult = {
    managed(zip(war1)).acquireAndGet { in1 =>
      managed(zip(war2)).acquireAndGet { in2 =>
	diff(new ZipEntries(in1).toList, new ZipEntries(in2).toList)
      }
    }
  }
  
  def diff(war1: List[ZipEntry], war2: List[ZipEntry]): DiffResult = {
    val e1 = war1.map { e => (e.name, e) }.toMap
    val e2 = war2.map { e => (e.name, e) }.toMap

    val existsOnlyIn1 = diffKeys(e1, e2).toList
    val existsOnlyIn2 = diffKeys(e2, e1).toList
    val differentFiles = intersectKeys(e1, e2).filter { x => e1(x).digest != e2(x).digest }.toList

    DiffResult(existsOnlyIn1, existsOnlyIn2, differentFiles)
  }

  private def diffKeys[A](m1: Map[A, _], m2: Map[A, _]): Set[A] =
    m1.keys.toSet.diff(m2.keys.toSet)

  private def intersectKeys[A](m1: Map[A, _], m2: Map[A, _]): Set[A] =
    m1.keys.toSet.intersect(m2.keys.toSet)

  private def zip(file: File) = new ZipInputStream(new java.io.FileInputStream(file))
}

object Main {
  private def print(diff: DiffResult) {
    println("Exists only in war1")
    println("====================")
    diff.existsOnlyIn1.foreach(println)

    println("Exists only in war2")
    println("====================")
    diff.existsOnlyIn2.foreach(println)

    println("Exists in both but not same")
    println("================================")
    diff.differentFiles.foreach(println)
  }

  def main(args: Array[String]) {
    val a = Args(args)
    print(Diff.diff(a.war1, a.war2))
  }
}

case class Args(war1: File, war2: File)
object Args {
  import org.clapper.argot._

  private implicit def toFile(s: String, opt: CommandLineArgument[File]) = new File(s)

  def apply(args: Array[String]): Args = {
    val parser = new ArgotParser("diff-war")
    val war1 = parser.parameter[File]("WAR1", "war file 1", false)
    val war2 = parser.parameter[File]("WAR2", "war file 2", false)
    Args(war1.value.get, war2.value.get)
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
