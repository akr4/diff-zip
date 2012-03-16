import org.scalatest.FunSuite

class DiffSuite extends FunSuite {

  val SAME = DiffResult(List.empty, List.empty, List.empty)

  test("same") {
    val result = Diff.diff(
      List(ZipEntry("a", "A"), ZipEntry("b", "B")),
      List(ZipEntry("a", "A"), ZipEntry("b", "B"))
    )

    assert(result === SAME)
  }

  test("found in the other") {
    val result = Diff.diff(
      List(ZipEntry("a", "A"), ZipEntry("b", "B")),
      List(ZipEntry("a", "A"), ZipEntry("c", "C"))
    )

    assert(result === DiffResult(
      List("b"),
      List("c"),
      List.empty
    ))
  }

  test("diff") {
    val result = Diff.diff(
      List(ZipEntry("a", "A")),
      List(ZipEntry("a", "AA"))
    )

    assert(result === DiffResult(
      List.empty,
      List.empty,
      List("a")
    ))
  }
}
