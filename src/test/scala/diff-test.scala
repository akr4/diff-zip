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
