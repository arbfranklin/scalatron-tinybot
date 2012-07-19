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

/**
 * Used in BotWar. If we find that our nearby radius is being dominated by competitor bots then we use our existing
 * energy to do a limited explosion designed to damage these nearby bots as much as possible.
 */
class AttackBots(radius: Int, minCount: Int, maxEnergy: Int) extends Strategy {
  require(radius >= 2 && radius <= 7)

  /**a human readable name for the strategy */
  override def name = "attack"

  /** evaluate against the given context and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    val targets = ctx.view.find(Set(Tile.OtherBot, Tile.OtherMiniBot))

    // how many bots are within the target radius?
    val nearby = targets.map(t => ctx.distTo(t)).filter(_ <= radius)

    // TODO: Add movement to actually encourage the situation
    if (nearby.size >= minCount && ctx.energy <= maxEnergy) {
      // limit the radius as much as possible to do more damage
      Vote(Explode(nearby.max + 1), Score.High, name)
    } else {
      Vote.Abstain
    }
  }
}
