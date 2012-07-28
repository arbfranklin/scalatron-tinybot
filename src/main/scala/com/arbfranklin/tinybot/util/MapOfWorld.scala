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

import com.arbfranklin.tinybot.util.Tile._

/** A view representing the entire world */
class MapOfWorld private(override val cols: Int, override val cells: Array[Tile]) extends View(cols, cells) {
  /** combine the given view into this view, either mutating this one or returning a new instance */
  def combine(offset: XY, other: View): MapOfWorld = {
    val ncols = 2*(math.abs(offset.x) max math.abs(offset.y)) + other.cols
    val result = expand(ncols max cols)

    // reshift the center
    val extent = (result.cols-other.cols)/2
    val noffset = XY(extent+offset.x, extent+offset.y)
    result.set(noffset, other)
    result
  }

  private def expand(ncols: Int): MapOfWorld = {
    // create a new resized view
    require(ncols >= cols)

    val result = new MapOfWorld(ncols, Array.fill(math.pow(ncols, 2).toInt)(Tile.Occluded))
    val extent = (ncols-cols)/2
    result.set(XY(extent,extent), this)
    result
  }

  /** apply the sub-view into this view */
  private def set(offset: XY, subview: View) {
    // needs to cleanly fit
    require(subview.cols+offset.x <= cols)
    require(subview.cols+offset.y <= cols)

    for (n <- 0 until subview.cells.length) {
      val t = subview.cells(n)
      if (t!=Tile.Occluded) {
        val x = (n % subview.cols) + offset.x
        val y = (n / subview.cols) + offset.y
        val nidx = toInt(x, y)
        cells(nidx) = t
      }
    }
  }
}

object MapOfWorld {
  def apply() = new MapOfWorld(1,Array[Tile](Tile.Bot))

  def apply(s: String) = {
    val cols = math.sqrt(s.size).toInt
    val result = s.map(c => Tile(c)).toArray
    new MapOfWorld(cols, result)
  }
}
