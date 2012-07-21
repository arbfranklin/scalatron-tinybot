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
abstract class ReactContext(val view: View, params: Map[String, String]) {
  def name = params("name")

  def energy = params("energy").toInt

  /** estimate of the total number of bots */
  def botCount = if (params.contains("botCount")) params("botCount").toInt else 0

  /** last move */
  def lastMove: Move = if (params.contains("lastMove")) Move(params("lastMove")) else Move.Center

  /** did the previous move result in a collision? */
  def isCollision: Option[Move] = if (params.contains("collision")) Some(Move(params("collision"))) else None

  /** iteration number */
  def time = params("time").toInt

  def apocalypse = params("apocalypse").toInt

  def tillApocalypse = apocalypse - time

  /**distance wrapper, assume we're the center of the view */
  def distTo(xy: XY) = view.center.distTo(xy)
}

case class MasterContext(override val view: View, params: Map[String, String]) extends ReactContext(view, params) {

}

case class SlaveContext(override val view: View, params: Map[String, String]) extends ReactContext(view, params) {
  /** starting energy of this slave */
  def startEnergy = params("startEnergy").toInt

  /** the direction to the master */
  def masterDirection: Move = toMaster match {
    case Some(m) => m.step
    case None => Move.Center
  }

  /** the position of the master relative to our frame of view */
  def master: XY = toMaster match {
    case Some(m) => view.center + m
    case None => view.center
  }

  /** how to get to the master */
  private def toMaster: Option[Move] = if (params.contains("master")) Some(Move(params("master"))) else None
}
