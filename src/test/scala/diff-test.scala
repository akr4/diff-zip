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
package net.physalis.diff_zip

import zip._
import diff._
import org.scalatest.FunSuite

case class TestingZipEntry(
  name: String,
  digest: Array[Byte],
  isDirectory: Boolean
) extends ZipEntry

class DiffSuite extends FunSuite {

  val a = TestingZipEntry("a", Array(1), false)
  val b = TestingZipEntry("b", Array(2), false)
  val c = TestingZipEntry("c", Array(3), false)

  val SAME = DiffResult(List.empty, List.empty, List.empty)

  test("same") {
    val result = Diff.diff(
      List(a, b),
      List(a, b),
      false
    )

    assert(result === SAME)
  }

  test("found in the other") {
    val result = Diff.diff(
      List(a, b),
      List(a, c),
      false
    )

    assert(result === DiffResult(
      List("b"),
      List("c"),
      List.empty
    ))
  }

  test("diff") {
    val a2 = TestingZipEntry("a", Array(2), false)
    val result = Diff.diff(
      List(a),
      List(a2),
      false
    )

    assert(result === DiffResult(
      List.empty,
      List.empty,
      List("a")
    ))
  }

  test("ignore directory") {
    val d1 = TestingZipEntry("d1", Array(1), true)

    val result = Diff.diff(List(d1), List.empty, false)

    assert(result === SAME)
  }

  test("detect directory") {
    val d1 = TestingZipEntry("d1", Array(1), true)

    val result = Diff.diff(List(d1), List.empty, true)

    assert(result === DiffResult(List("d1"), List.empty, List.empty))
  }
}
