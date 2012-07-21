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

  override def name = "home"

  /**evaluate against the given context and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    val slave = ctx.asInstanceOf[SlaveContext]

    /** roi obtained? */
    def roiReached = slave.energy > (slave.startEnergy * roi)

    // nearing the end, we want to race home regardless of ROI
    def eol: Boolean = {
      if (globalRecall) return true

      val turnsRemaining = ctx.apocalypse - ctx.time
      val distHome = ctx.distTo(slave.master)

      /**when we are almost out of time, we need this many turns times the dist count to make it back */
      if (turnsRemaining < (distHome * turnHomeMargin)) {
        globalRecall = true
        true
      } else {
        false
      }
    }

    if (roiReached || eol) {
      val path = pathHome(ctx.view, slave.master)

      if (path.isEmpty) {
        Vote.Abstain
      } else {
        val a = ctx.view.center
        val b = path.take(2).last
        val move = Move(b.x - a.x, b.y - a.y)

        val score = if (eol) Score(0.9999) else Score.High

        Vote(move, score, name)
      }
    } else {
      Vote.Abstain
    }
  }

  def pathHome(view: View, goal: XY): List[XY] = {
    // go to the tile on the edge
    val bx = if (goal.x < 0) 0 else if (goal.x >= view.cols) view.cols - 1 else goal.x
    val by = if (goal.y < 0) 0 else if (goal.y >= view.cols) view.cols - 1 else goal.y

    new AStarSearch(view, view.center, XY(bx, by)).solve()
  }
}
