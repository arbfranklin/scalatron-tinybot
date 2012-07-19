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
import com.arbfranklin.tinybot.util.Tile._

/**
 * A hunting algorithm which instead of looking for the nearest matching tile, we will see whether another one of our
 * mini-bots is closer and let it hunt the tile instead. If we're not the closest mini-bot to any tile then we give up
 * hunting for this turn and favour alternative strategies.
 */
class FairHunter(tile: Tile) extends Hunter(tile) {
  /** find the target to hunt for */
  override def findTarget(view: View): Option[XY] = {
    val preyByDistance = view.find(tile).sortBy(z => view.center.distTo(z))
    if (preyByDistance.isEmpty) {
      return None
    }

    // alternative hunters
    val hunters = view.find(Tile.MiniBot)
    if (hunters.isEmpty) {
      return Some(preyByDistance.head)
    }

    preyByDistance.foreach(p => {
      val dist = view.center.distTo(p)

      // is there a hunter who is closer?
      val shortest = hunters.map(h => h.distTo(p)).min

      if (dist <= shortest) return Some(p)
    })

    // give up, we can do something other than hunt
    None
  }
}
