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
 * A random move function which will retain velocity. ie. The strategy will always choose to move in the same direction
 * as previously chosen unless such a move will hit a wall. In this case we "bounce" off the wall at a perpendicular,
 * much like how a ball would bounce.
 */
class VelocityMove extends Strategy {
  /** randomizer for choosing a velocity when we have no prior velocity */
  val rand = scala.util.Random

  /**a human readable name for the strategy */
  override def name = "velocity"

  /** all available directions */
  val directions = Move.values - Move.Center

  /**evaluate against the given contect and provide a serious of potential actions and their associated score */
  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    val current = velocity(ctx)
    val v = newVelocity(ctx.view, current)
    Vote(v, Score.High, name)
  }

  /** determine the new velocity */
  def newVelocity(view: View, v: Move): Move = {
    if (!isWall(view, v)) return v

    var p1 = v.perpendicular
    if (!isWall(view, p1)) return p1

    var p2 = p1.negate
    if (!isWall(view, p2)) return p2

    // back to where we came
    v.negate
  }

  /** determine the current velocity */
  def velocity(ctx: ReactContext): Move = {
    val m = ctx.lastMove
    if (m != Move.Center) {
      m
    } else {
      directions(rand.nextInt(directions.length))
    }
  }

  def isWall(view: View, m: Move) = Set(Tile.Wall, Tile.MiniBot).contains(view.at(view.toXY(m)))
}
