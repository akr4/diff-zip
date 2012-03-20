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
import java.io.File

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
      case Right(Args(zip1, zip2, detectDirectory)) => print(Diff.diff(zip1, zip2, detectDirectory))
      case Left(e) => println(e.message)
    }
  }
}

case class ArgsException(message: String) extends Exception
case class Args(zip1: File, zip2: File, detectDirectory: Boolean)
object Args {
  import org.clapper.argot._
  import ArgotConverters._

  def apply(args: Array[String]): Either[ArgsException, Args] = {
    val parser = new ArgotParser("diff-zip")

    implicit def toFile(s: String, opt: CommandLineArgument[File]) = {
      val f = new File(s)
      if (!f.exists) parser.usage(s + " not found.")
      f
    }

    val zip1 = parser.parameter[File]("ZIP1", "zip file 1", false)
    val zip2 = parser.parameter[File]("ZIP2", "zip file 2", false)
    val detectDirectory = parser.flag[Boolean](List("d", "detect-dir"), "Detect directory change")

    try {
      parser.parse(args)
      Right(Args(zip1.value.get, zip2.value.get, detectDirectory.value.getOrElse(false)))
    } catch {
      case e: ArgotUsageException => Left(ArgsException(e.message))
    }
  }
}

