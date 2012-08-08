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
import collection.GenIterable

/**
 * TinyBot. Implementation of a Scalatron bot with a configuration managed by the provided Genome.
 */
class TinyBot(val g: Genome) extends BotResponder {
  /**weighted strategies to use for reducing the choice of next move */
  val masterStrategies = Map(
    new AvoidEnergyLoss() -> g.master.avoidEnergyLoss.weight,
    new FairHunter(Zugar) -> g.master.huntZugars.weight,
    new FairHunter(Fluppet) -> g.master.huntFluppets.weight,
    new CreateDistance(Wall) -> g.master.avoidWalls.weight,
    new RandomMove() -> g.master.random.weight,
    new VelocityMove() -> g.master.velocity.weight,
    new StayForHomeComing(g.master.stay.minTurns) -> g.master.stay.weight,
    new MasterSpawn(g.master.spawn.maxBots) -> 1d
  ).toList

  val slaveStrategies = Map(
    new AvoidEnergyLoss() -> g.slave.avoidEnergyLoss.weight,
    new FairHunter(Zugar) -> g.slave.huntZugars.weight,
    new FairHunter(Fluppet) -> g.slave.huntFluppets.weight,
    new RandomMove() -> g.slave.random.weight,
    new VelocityMove() -> g.slave.velocity.weight,
    new ReturnSlaveHome(g.slave.home.roi, g.slave.home.safetyMargin, g.shared.spawn.maxBots) -> g.slave.home.weight,
    new StuckKamakazi() -> 1d,
    new EnemyChicken() -> 1d,
    new ExplodeOnApocalypse(g.slave.apocalypse.minTurns) -> 1d,
    new SlaveSpawn(g.slave.spawn.frequency, g.slave.spawn.imbalance, g.shared.spawn.maxBots) -> 1d,
    new AttackBots(g.slave.attack.radius, g.slave.attack.minCount, g.slave.attack.maxEnergy) -> 1d
  ).toList

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
    eval(ctx, masterStrategies)
  }

  /**called when it's the slave's turn to react */
  override def reactAsSlave(ctx: SlaveContext): List[Action] = {
    val actions = eval(ctx, slaveStrategies)

    // remove status updates that are too noisy
    actions.map(a => {
      if (a.isInstanceOf[Status]) {
        val msg = a.asInstanceOf[Status].text
        if (msg.length<=3) a else Status("")
      } else a
    })
  }

  //
  // VOTE EVALUATION
  //

  /**evaluation handler */
  def eval(ctx: ReactContext, strategies: List[(Strategy,Double)]): List[Action] = {
    // collate all the various actions that we could make
    val actions = collateActions(ctx, strategies)

    // explosions are the first priority
    val explosions = actions.filter(_.action.isInstanceOf[Explode])
    if (!explosions.isEmpty) {
      // find the biggest requested explosion
      val biggest = explosions.maxBy(_.action.asInstanceOf[Explode].size)
      return List(biggest.action, Status(biggest.reason))
    }

    // collate any state setting
    val states = actions.filter(_.action.isInstanceOf[SetState]).map(_.asInstanceOf[SetState]).toList

    // now spawning, but no matter what we need a move location
    val fm = findMove(actions)
    val move = fm.action.asInstanceOf[Move]
    val reason = fm.reason

    // the move instructions
    val moveInstructions = List(move, SetState("lastMove", move.toString))

    // spawn?
    val spawns = actions.filter(_.action.isInstanceOf[Spawn])
    if (!spawns.isEmpty && fm.score.isPositive) {
      // find the biggest spawn
      val biggest = spawns.maxBy(_.action.asInstanceOf[Spawn].energy)

      var spawn = biggest.action.asInstanceOf[Spawn]
      if (spawn.direction != Move.Center) {
        return List(spawn, Status(biggest.reason)) ::: moveInstructions ::: states
      } else {
        // where to spawn?
        val moves = Move.values.filter(m => ctx.view.at(ctx.view.toXY(m))==Tile.Empty) - move
        if (!moves.isEmpty) {
          return List(Spawn(randFrom(moves), spawn.energy), Status(biggest.reason)) ::: moveInstructions ::: states
        }
      }
      // unable to complete the spawn
    }

    // if we're here, it's just a regular move
    Status(reason) :: moveInstructions ::: states
  }

  /**collate all the potential actions */
  def collateActions(ctx: ReactContext, strategies: List[(Strategy,Double)]): GenIterable[Vote] = {
    // TODO: apply parallel collections? Slower on my mac, but maybe not on a high-end server?
    val allowedMoves = Move.values.filter(m => ctx.view.at(ctx.view.toXY(m)) != Tile.Wall).toSet

    // iterate all the move suggestions the strategies have made
    strategies.map(t => {
      val strategy = t._1
      val w = t._2

      // obtain the candidates and re-weight them
      val votes = strategy.eval(ctx, allowedMoves)
      votes.map(v => Vote(v.action, v.score * w, v.reason))
    }).flatten
  }

  /**find the best move in the action set */
  def findMove(actions: GenIterable[Vote]): Vote = {
    // only interested in move actions at this point
    val moves = actions.map(_.action).filter(_.isInstanceOf[Move])

    // collaborate across the votes for each move square
    val cscores = moves.map(move => {
      val votes = actions.filter(_.action == move)
      val score = Score.combine(votes.map(_.score))
      val dominant = votes.maxBy(v => {
        if (v.score == Score.Veto) Double.PositiveInfinity else v.score.d
      }) // veto is dominant

      // new colaborated vote
      Vote(move, score, dominant.reason)
    })

    if (cscores.isEmpty) {
      Vote(Move.Center, Score.Abstain, "*stuck*")
    } else {
      // TODO: the max score is the move, but not necessarily the reason
      cscores.maxBy(_.score)
    }
  }

  def randFrom[T](list: List[T]): T = {
    val rand = scala.util.Random
    list(rand.nextInt(list.length))
  }
}
