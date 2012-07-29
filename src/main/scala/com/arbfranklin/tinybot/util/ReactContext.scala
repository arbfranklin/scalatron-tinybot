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

package com.arbfranklin.tinybot.util

/** a bots reaction context */
abstract class ReactContext(val view: View, val mapOfWorld: MapOfWorld, params: Map[String, String]) {
  val name = params("name")

  val energy = params("energy").toInt

  val time = params("time").toInt

  val apocalypse = params("apocalypse").toInt

  /** estimate of the total number of bots */
  val botCount = if (params.contains("botCount")) params("botCount").toInt else 0

  /** last move */
  def lastMove: Move = if (params.contains("lastMove")) Move(params("lastMove")) else Move.Center

  /** how many turns till the apocalypse */
  def tillApocalypse = apocalypse - time

  /** did the previous move result in a collision? */
  def isCollision: Option[Move] = if (params.contains("collision")) Some(Move(params("collision"))) else None

  /**distance wrapper, assume we're the center of the view */
  def distTo(xy: XY) = view.center.distTo(xy)
}

case class MasterContext(override val view: View, override val mapOfWorld: MapOfWorld, params: Map[String, String])
  extends ReactContext(view, mapOfWorld, params)

case class SlaveContext(override val view: View, override val mapOfWorld: MapOfWorld, params: Map[String, String])
  extends ReactContext(view, mapOfWorld, params)
{
  /** starting energy of this slave */
  def startEnergy = params("startEnergy").toInt

  /** the position of the master relative to our frame of view */
  def master: XY = view.center + masterMove

  /** how to get to the master */
  def masterMove = Move(params("master"))
}
