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

package com.arbfranklin.tinybot.strategies

import com.arbfranklin.tinybot.util._
import com.arbfranklin.tinybot.util.Tile._

/**
 * A simple tile hunting algorithm which will find the nearest matching tile and do any move which will reduce the
 * overall distance to this tile. We don't need to worry about clever path finding and we can only see non-occluded
 * tiles.
 */
class Hunter(tile: Tile) extends Strategy {
  override def name = "hunt " + tile

  /**evaluate against the given context and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    findTarget(ctx.view) match {
      case Some(prey) =>
        moves.map(m => {
          val proposed = ctx.view.toXY(m)
          val pDist = proposed.distTo(prey)

          Vote(m, score(ctx.view, ctx.distTo(prey), pDist), name)
        })

      case None => Vote.Abstain
    }
  }

  /** find the target to hunt for */
  def findTarget(view: View): Option[XY] = {
    val prey = view.find(tile)
    if (!prey.isEmpty) Some(prey.minBy(z => view.center.distTo(z))) else None
  }

  /**score a particular action */
  def score(view: View, cDist: Int, pDist: Int) = {
    // does this increase or reduce the distance?
    val c = cDist - pDist

    if (c == 0) Score.Low
    else if (c > 0) Score(Score.High.v * (1 - (pDist.toDouble / view.cols)))
    else Score(-Score.Low.v)
  }
}
