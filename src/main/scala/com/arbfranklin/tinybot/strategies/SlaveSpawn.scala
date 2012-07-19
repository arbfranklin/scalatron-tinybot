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
 * A spawning strategy for mini-bots. If a mini-bot detects that it's vicinity is "resource rich but hunter poor" it
 * will spawn additional mini-bots to help hunt the vicinity.
 */
class SlaveSpawn(frequency: Double, minForSpawn: Int, childEnergy: Int, ratio: Double) extends Strategy {
  /** randomizer for breaking up frequency of spawn */
  val rand = scala.util.Random

  /** how many turns relative to the apocalypse should we stop spawning? */
  val MinTurnsRemaining = 50

  override def name = "*spawn*"

  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    if (spawn(ctx)) {
      Vote(Spawn(Move.Center, childEnergy), Score.Mandate, name)
    } else {
      Vote.Abstain
    }
  }

  def spawn(ctx: ReactContext): Boolean = {
    if (ctx.energy < minForSpawn) return false
    if (ctx.tillApocalypse <= MinTurnsRemaining) return false
    if (rand.nextDouble() > frequency) return false

    // count zugars and fluppets vs potential huntsman
    val view = ctx.view
    val resources = view.find(Set(Tile.Zugar, Tile.Fluppet)).size
    val hunters = view.find(Set(Tile.MiniBot, Tile.OtherMiniBot)).size // only include minis which are the same speed as us

    (resources / (hunters + 1)) > ratio
  }
}
