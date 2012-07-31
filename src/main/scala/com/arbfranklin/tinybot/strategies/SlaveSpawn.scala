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
 * A spawning strategy for mini-bots. If a mini-bot detects that it's the closest to a number of resources
 * "resource rich but hunter poor", it will spawn additional mini-bots to help hunt the vicinity.  The new
 * implementation evenly divides it's energy allocation to the new spawn.
 */
class SlaveSpawn(frequency: Double, imbalance: Int, maxBots: Int) extends Strategy {
  /** randomizer for breaking up frequency of spawn */
  val rand = scala.util.Random

  /** how many turns relative to the apocalypse should we stop spawning? */
  val MinTurnsRemaining = 50

  /** Minimum energy required for a split */
  val EnergyForSplit = 200

  override def name = "\u2442"

  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    if (spawn(ctx)) {
      // pure sub-division
      Vote(Spawn(Move.Center, ctx.energy/2), Score.Mandate, name)
    } else {
      Vote.Abstain
    }
  }

  def spawn(ctx: ReactContext): Boolean = {
    if (ctx.energy < EnergyForSplit) return false
    if (ctx.tillApocalypse <= MinTurnsRemaining) return false
    if (rand.nextDouble() > frequency) return false
    if (ctx.botCount >= maxBots) return false

    val hunters = ctx.view.find(Tile.MiniBot)
    if (!hunters.isEmpty) {
      // count zugars and fluppets vs potential huntsman
      val resources = ctx.view.find(Set(Tile.Zugar, Tile.Fluppet)).sortBy(xy => ctx.distTo(xy))

      // how many resources am i the closest to?
      val count = resources.foldLeft(0){ (c,p) =>
        val dist = ctx.distTo(p)

        // is there a hunter who is closer?
        val shortest = hunters.map(h => h.distTo(p)).min
        if (dist <= shortest) c+1 else c
      }

      count > imbalance
    } else {
      ctx.view.find(Set(Tile.Zugar, Tile.Fluppet)).size > imbalance
    }
  }
}
