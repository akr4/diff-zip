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
package net.physalis.diff_zip.diff

import net.physalis.diff_zip.zip._
import java.io.File

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
  def diff(zip1: File, zip2: File, detectDirectory: Boolean): FileDiffResult = {
    Zip.using(zip1) { e1 =>
      Zip.using(zip2) { e2 =>
        val r = diff(e1.toList, e2.toList, detectDirectory)
        FileDiffResult(zip1, zip2, r)
      }
    }
  }
  
  def diff(zip1: List[ZipEntry], zip2: List[ZipEntry], detectDirectory: Boolean): DiffResult = {
    def map(zip: List[ZipEntry]) =
      zip.filter { detectDirectory || !_.isDirectory }.map { e => (e.name, e) }.toMap

    val e1 = map(zip1)
    val e2 = map(zip2)

    val existsOnlyIn1 = diffKeys(e1, e2).toList.sorted
    val existsOnlyIn2 = diffKeys(e2, e1).toList.sorted
    val differentFiles = intersectKeys(e1, e2).
      filter { x => !(e1(x).digest sameElements e2(x).digest) }.toList.sorted

    DiffResult(existsOnlyIn1, existsOnlyIn2, differentFiles)
  }

  private def diffKeys[A](m1: Map[A, _], m2: Map[A, _]): Set[A] =
    m1.keys.toSet.diff(m2.keys.toSet)

  private def intersectKeys[A](m1: Map[A, _], m2: Map[A, _]): Set[A] =
    m1.keys.toSet.intersect(m2.keys.toSet)
}

