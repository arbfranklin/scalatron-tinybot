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
 * A strategy whose sole purpose is to help a mini-bot navigate back to the master bot. A mini-bot will come home
 * under two circumstances:
 *
 * #1 - A desired Energy ROI is reached relative to the initial energy investment. If the bot loses energy on it's way
 *      home then it will abandon the trip until the ROI is reached again.
 * #2 - The apocalypse is near and we want to cash out.
 *
 * Once condition #2 is met for any bot, all mini-bots instantly go into state #2 in-order to not block the path home.
 */
class ReturnSlaveHome(roi: Double, turnHomeMargin: Double) extends Strategy {
  /** should all bots return home now */
  var globalRecall = false

  override def name = "*home*"

  /**evaluate against the given context and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    val slave = ctx.asInstanceOf[SlaveContext]

    /** roi obtained? */
    val roiReached = slave.energy > (slave.startEnergy * roi)

    // nearing the end, we want to race home regardless of ROI
    def eol: Boolean = {
      if (!globalRecall) {
        val turnsRemaining = ctx.apocalypse - ctx.time
        val distHome = ctx.distTo(slave.master)

        // when we are almost out of time, we need this many turns times the dist count to make it back
        globalRecall = turnsRemaining < (distHome * turnHomeMargin)
      }
      globalRecall
    }

    if (roiReached || eol) {
      val moves = solve(slave)
      if (moves.isEmpty) {
        Vote.Abstain
      } else {
        val score = if (eol) Score(0.999999) else Score.High
        moves.map(m => Vote(m, score, name))
      }
    } else {
      Vote.Abstain
    }
  }

  def solve(ctx: SlaveContext) = {
    if (ctx.view.contains(ctx.master)) {
      PathSolver(ctx.view, ctx.view.center, ctx.master).solve()
    } else {
      val center = ctx.mapOfWorld.center
      val me = XY(center.x - ctx.masterMove.right, center.y - ctx.masterMove.down)
      if (ctx.mapOfWorld.contains(me)) {
        PathSolver(ctx.mapOfWorld, me, center).solve()
      } else {
        // try a direct path solution or out of bounds
        new DirectPathSolver(ctx.view, ctx.view.center, ctx.master).solve()
      }
    }
  }
}
