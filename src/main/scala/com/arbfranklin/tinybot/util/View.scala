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

import Tile._
import scala.Array
import collection.mutable.ListBuffer

class View(val cols: Int, val cells: Array[Tile]) {
  /** center of the view (me) */
  val center = XY(cols / 2, cols / 2)
  private val middle = cells.length/2

  /** a move to an absolute co-ordinate */
  def toXY(move: Move): XY = center + move

  /** the tile at the given abs co-ordinate */
  def at(pos: XY): Tile = {
    val c = toInt(pos.x, pos.y)
    if (c < 0 || c >= cells.length) Occluded else cells(c)
  }

  /** non-empty items around the given position */
  def around(pos: XY): List[Tile] = Move.values.map(m => at(pos + m))

  /** find all instances of the given type. Imperative for performance. */
  def find(t: Tile): Seq[XY] = {
    val result = new ListBuffer[XY]()
    var i = 0
    while (i < cells.length) {
      if (cells(i) == t && i!=middle) {
        result += toXY(i)
      }
      i += 1
    }
    result
  }

  /** find multiple types in a single pass. Imperative for performance. */
  def find(tiles: Set[Tile]): Seq[XY] = {
    val result = new ListBuffer[XY]()
    var i = 0
    while (i < cells.length) {
      if (tiles.contains(cells(i)) && i!=middle) {
        result += toXY(i)
      }
      i += 1
    }
    result
  }

  /** is the point bounded in the view */
  def contains(pos: XY): Boolean = {
    pos.x >= 0 && pos.x < cols && pos.y >= 0 && pos.y < cols
  }

  /** @return a point that is potentially outside the view, bounded to the view extents */
  def bounded(xy: XY): XY = {
    // go to the tile on the edge
    val bx = if (xy.x < 0) 0 else if (xy.x >= cols) cols - 1 else xy.x
    val by = if (xy.y < 0) 0 else if (xy.y >= cols) cols - 1 else xy.y
    XY(bx,by)
  }

  protected def toInt(x: Int, y: Int) = y * cols + x

  protected def toXY(n: Int) = {
    val x = n % cols
    val y = n / cols
    XY(x, y)
  }

  override def toString = {
    val s = new collection.mutable.StringBuilder()
    for (i <- 0 until cells.length) {
      if (i % cols == 0) s.append('\n')
      s.append(Tile.toChar(cells(i)))
    }
    s.drop(1).toString()
  }
}

object View {
  def apply(s: String): View = {
    val cols = math.sqrt(s.size).toInt
    val tiles = new Array[Tile](s.length)
    for (i <- 0 until s.length) {
      tiles(i) = Tile(s(i))
    }
    new View(cols, tiles)
  }
}