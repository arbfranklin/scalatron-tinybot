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
import com.arbfranklin.tinybot.util.Spawn

/**
 * To maxmimize our changes from the start, the first moves are all spawns.
 */
class MasterSpawn(maxBots: Int) extends Strategy {
  /** Last bot issued before apocalypse */
  val StopBeforeApocalypse = 50

  /** minimum energy for spawn */
  val MinimumEnergyForSpawn = 100

  /**a human readable name for the strategy */
  override def name = "spawn"

  /**evaluate against the given context and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    if (spawn(ctx)) {
      Vote(Spawn(Move.Center, MinimumEnergyForSpawn), Score.Mandate, name)
    } else {
      Vote.Abstain
    }
  }

  def spawn(ctx: ReactContext): Boolean = {
    if (ctx.tillApocalypse <= StopBeforeApocalypse) return false
    if (ctx.botCount>maxBots) return false // the slaves will do the work
    ctx.energy>MinimumEnergyForSpawn
  }
}
