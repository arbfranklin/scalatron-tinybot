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
 * A strategy which will attempt to maximize the average distance from the bot to all matching tiles.
 */
class CreateDistance(tile: Tile) extends Strategy {
  override def name = "avoid " + tile

  /** evaluate against the given context and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    val prey = ctx.view.find(tile)
    if (prey.isEmpty) {
      Vote.Abstain
    } else {
      val cDist = prey.map(z => ctx.distTo(z)).sum / prey.size

      moves.map(m => {
        val proposed = ctx.view.toXY(m)
        val pDist = prey.map(z => proposed.distTo(z)).sum / prey.size
        Vote(m, score(cDist, pDist), name)
      })
    }
  }

  def score(cDist: Int, pDist: Int) = {
    val c = pDist - cDist

    if (c == 0) Score.Abstain
    else if (c > 0) Score.High
    else Score(-0.1)
  }
}
