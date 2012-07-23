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

/** place a potential out of bounds value into the view */
object TargetBounder {
  /** @return a bounding point, a representation of xy bounded within the view */
  def bound(view: View, goal: XY) : Option[XY] = {
    if (view.isBounded(goal)) {
      Some(goal)
    } else {
      val bd= view.bounded(goal)

      // we need a bounding point on the extent
      val extents = if (bd.x==0 || bd.x==view.cols-1) {
        // x-extent
        for (y <- 0 until view.cols) yield XY(bd.x,y)
      } else {
        // y-extent
        for (x <- 0 until view.cols) yield XY(x,bd.y)
      }
      val candidates = extents.filter(xy => view.at(xy)!=Tile.Wall)
      if (candidates.isEmpty) {
        None
      } else {
        Some(candidates.minBy(xy => xy.distTo(goal)))
      }
    }
  }
}
