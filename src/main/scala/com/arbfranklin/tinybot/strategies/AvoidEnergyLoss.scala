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
 * General strategy which attempts to avoid Snorgs by not stepping into a square that the Snorg can reach us from on
 * it's next turn. The strategy will also discourage us from stepping on Toxifera.
 */
class AvoidEnergyLoss extends Strategy {
  override def name = "avoid"

  /** evaluate against the given contect and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = moves.map(m => Vote(m, score(ctx, m), name))

  /**score a particular action */
  def score(ctx: ReactContext, m: Move): Score = {
    val xy = ctx.view.toXY(m)
    val tile = ctx.view.at(xy)
    val score = tileScore(tile)

    if (score == Score.Veto) {
      Score.Veto
    } else {
      // add the score of all snorgs that can bite this square
      val snorgCount = ctx.view.around(xy).filter(x => x == Snorg || x == OtherBot).size
      snorgCount match {
        case 0 => score
        case 1 => Score(math.min(score.v, tileScore(Snorg).v)) // TODO: Not just snorgs
        case _ => Score(math.min(score.v, -Score.High.v))
      }
    }
  }

  /**max energy gained from a square */
  val maxEnergy = 200d

  def tileScore(t: Tile): Score = t match {
    case Wall => Score.Veto
    case Snorg => Score(Score.High.v * (-150 / maxEnergy))
    case Toxifera => Score(Score.High.v * (-100 / maxEnergy))

    case MiniBot => Score.Veto
    case OtherBot => Score.Veto
    case OtherMiniBot => Score.Abstain
    case _ => Score.Abstain
  }
}
