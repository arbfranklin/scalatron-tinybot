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

case class Move(right: Int, down: Int) extends Action {
  def +(xy: XY) = XY(xy.x + right, xy.y + down)

  def +(m: Move) = Move(right + m.right, down + m.down)

  def negate = Move(-right, -down)

  def perpendicular = Move(-down, right)

  override def toString = right + ":" + down

  def toWire = "Move(direction=" + toString + ")"

  /** convert the full movement (which is potentially many steps, to a single step) */
  def step: Move = {
    def clean(n: Int) = if (n > 0) 1 else if (n < 0) -1 else 0
    new Move(clean(right), clean(down))
  }
}

object Move {
  val LeftUp = Move(-1, -1)
  val Up = Move(0, -1)
  val RightUp = Move(1, -1)

  val Left = Move(-1, 0)
  val Center = Move(0, 0)
  val Right = Move(1, 0)

  val LeftDown = Move(-1, 1)
  val Down = Move(0, 1)
  val RightDown = Move(1, 1)

  def apply(s: String): Move = {
    val p = s.split(":").map(_.toInt)
    new Move(p(0), p(1))
  }

  /**all values */
  def values = List(LeftUp, Up, RightUp, Right, RightDown, Down, LeftDown, Left, Center)
}
