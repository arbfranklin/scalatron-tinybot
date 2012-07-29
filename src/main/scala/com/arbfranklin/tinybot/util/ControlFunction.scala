/*
 * Copyright (c) 2012, Andrew Franklin
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.arbfranklin.tinybot.util

import java.util.concurrent.atomic.AtomicInteger

/**
 * Heavily borrows from example bootstrap at:
 *   https://github.com/scalatron/scalatron/blob/master/Scalatron/samples/Tutorial%20Bot%2011%20-%20Handler%20Methods/src/Bot.scala
 */
class ControlFunction(r: BotResponder) {
  var globals = Map[String, String]()

  /** counters for figuring out the overall bot count */
  val botCount = new AtomicInteger()

  /** the world map we're building */
  var worldMap = MapOfWorld()

  def respond(input: String): String = {
    try {
      _respond(input).map(_.toWire).mkString("|")
    } catch {
      case t: Throwable => {
        t.printStackTrace(); throw t
      }
    }
  }

  // this method is called by the server
  def _respond(input: String): List[Action] = {
    val (opcode, params) = CommandParser(input)
    opcode match {
      case "Welcome" =>
        globals = params
        r.init(params)
        List()
      case "React" =>
        react(
          params("generation").toInt,
          View(params("view")),
          params
        )
      case "Goodbye" =>
        r.goodbye(
          globals("round").toInt,
          params("energy").toInt,
          globals("apocalypse").toInt
        )
        List()
      case _ =>
        List() // OK
    }
  }

  def react(generation: Int, view: View, params: Map[String, String]): List[Action] = {
    val time = params("time").toInt

    // how many times has react been called for this generation?
    if (generation==0) {
      globals += ("botCount" -> (botCount.intValue()/2).toString)
      botCount.set(0)
    }
    botCount.incrementAndGet()

    // sub-delegation
    if (generation == 0) {
      r.reactAsMaster(MasterContext(view, worldMap, globals ++ params))
    } else {
      // time to update the world map?
      val map = if (isUpdateMap(time)) {
        val master = Move(params("master"))
        worldMap.combine(XY(-master.right, -master.down), view)
      } else {
        worldMap
      }
      worldMap = map

      r.reactAsSlave(SlaveContext(view, map, globals ++ params))
    }
  }

  /** As the world map isn't cheap to update, only do it near the game end when path finding is very important */
  def isUpdateMap(time: Int) = globals("apocalypse").toInt - time < 350
}

/**Utility methods for parsing strings containing a single command of the format
 * "Command(key=value,key=value,...)"
 */
object CommandParser {
  /**"Command(..)" => ("Command", Map( ("key" -> "value"), ("key" -> "value"), ..}) */
  def apply(command: String): (String, Map[String, String]) = {
    /**"key=value" => ("key","value") */
    def splitParameterIntoKeyValue(param: String): (String, String) = {
      val segments = param.split('=')
      (segments(0), if (segments.length >= 2) segments(1) else "")
    }

    val segments = command.split('(')
    if (segments.length != 2)
      throw new IllegalStateException("invalid command: " + command)
    val opcode = segments(0)
    val params = segments(1).dropRight(1).split(',')
    val keyValuePairs = params.map(splitParameterIntoKeyValue).toMap
    (opcode, keyValuePairs)
  }
}