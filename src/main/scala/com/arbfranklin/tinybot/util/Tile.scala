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

object Tile extends Enumeration {
  def apply(c: Char): Tile = c match {
    case 'W' => Wall
    case '?' => Occluded
    case '_' => Empty
    case 'M' => Bot
    case 'S' => MiniBot
    case 'P' => Zugar
    case 'p' => Toxifera
    case 'B' => Fluppet
    case 'b' => Snorg
    case 'm' => OtherBot
    case 's' => OtherMiniBot
    case _ => throw new IllegalArgumentException("tile: " + c)
  }

  def toChar(t: Tile): Char = t match {
    case Wall => 'W'
    case Occluded => '?'
    case Empty => '_'
    case Bot => 'M'
    case MiniBot => 'S'
    case Zugar => 'P'
    case Toxifera => 'p'
    case Fluppet => 'B'
    case Snorg => 'b'
    case OtherBot => 'm'
    case OtherMiniBot => 's'
    case Me => '@'
    case _ => throw new IllegalArgumentException("tile: " + t)
  }

  type Tile = Value
  val Me, Bot, MiniBot, Fluppet, Snorg, Zugar, Toxifera, Wall, Empty, Occluded, OtherBot, OtherMiniBot = Value
}
