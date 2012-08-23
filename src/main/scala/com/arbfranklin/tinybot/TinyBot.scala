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

package com.arbfranklin.tinybot

import strategies._
import util._
import util.Tile._

/**
 * TinyBot. Implementation of a Scalatron bot with a configuration managed by the provided Genome.
 */
class TinyBot(val g: Genome) extends BotResponder {
  /**weighted strategies to use for reducing the choice of next move */
  val masterStrategies = StrategySet()
    .add(new MasterSpawn(g.master.spawn.maxBots))
    .add(new AvoidEnergyLoss() -> g.master.avoidEnergyLoss.weight)
    .add(new AvoidBlastRadius(g.master.avoidBlastRadius.radius) -> g.master.avoidBlastRadius.weight)
    .add(new FairHunter(Zugar) -> g.master.huntZugars.weight)
    .add(new FairHunter(Fluppet) -> g.master.huntFluppets.weight)
    .add(new CreateDistance(Wall) -> g.master.avoidWalls.weight)
    .add(new CreateDistance(OtherMiniBot) -> g.master.stay.weight)
    .add(new ReduceDistance(MiniBot) -> g.master.stay.weight)
    .add(new VelocityMove() -> g.master.velocity.weight)

  val slaveStrategies = StrategySet()
    .add(new StuckKamakazi())
    .add(new EnemyChicken())
    .add(new SlaveBomber(g.slave.bomb.radius))
    .add(new ExplodeOnApocalypse(g.slave.apocalypse.minTurns))
    .add(new SlaveSpawn(g.slave.spawn.frequency, g.slave.spawn.imbalance, g.shared.spawn.maxBots))
    .add(new AttackBots(g.slave.attack.radius, g.slave.attack.minCount, g.slave.attack.maxEnergy))
    .add(new AvoidEnergyLoss() -> g.slave.avoidEnergyLoss.weight)
    .add(new AvoidBlastRadius(g.slave.avoidBlastRadius.radius) -> g.slave.avoidBlastRadius.weight)
    .add(new FairHunter(Zugar) -> g.slave.huntZugars.weight)
    .add(new FairHunter(Fluppet) -> g.slave.huntFluppets.weight)
    .add(new FairHunter(OtherBot) -> g.slave.masterHunter.weight)
    .add(new VelocityMove() -> g.slave.velocity.weight)
    .add(new ReturnSlaveHome(g.slave.home.roi, g.slave.home.safetyMargin, g.shared.spawn.maxBots) -> g.slave.home.weight)

  var startTime = 0L

  /**called to denote game start */
  override def init(params: Map[String, String]) {
    startTime = System.currentTimeMillis
  }

  /**called on end */
  override def goodbye(round: Int, energy: Int, time: Int) {
    val endTime = System.currentTimeMillis

    println("TinyBot: round=" + round + ", score=" + energy + ", time=" + ((endTime - startTime) / 1000) + "s")
  }

  /**called when it's the master's turn to react */
  override def reactAsMaster(ctx: MasterContext): List[Action] = {
    if (Debug.enabled) {
      if (ctx.time % 500 == 0 && ctx.time!=0) {
        println("[%4d] score=%d".format(ctx.time, ctx.energy))
      }

      print("\r[%4d] score=%d\r".format(ctx.time, ctx.energy))
    }
    masterStrategies.eval(ctx)
  }

  /**called when it's the slave's turn to react */
  override def reactAsSlave(ctx: SlaveContext): List[Action] = {
    val actions = slaveStrategies.eval(ctx)

    // remove status updates that are too noisy
    actions.map(a => {
      if (a.isInstanceOf[Status]) {
        val msg = a.asInstanceOf[Status].text
        if (msg.length<=3) a else Status("")
      } else a
    })
  }
}
