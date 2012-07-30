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
 * If we have been trapped such that we can't make a move without losing energy, we explode the mini-bot to walk away
 * from the situation with positive energy.
 */
class StuckKamakazi extends AvoidEnergyLoss {
  override def name = "kamakazi"

  /** don't consider a kamakazi unless we're carrying less energy than this */
  val MaxEnergy = 1000

  override def eval(ctx: ReactContext, moves: Set[Move]) = {
    val votes = super.eval(ctx, moves)

    // if we're carrying a lot of energy, maybe we can afford to get bitten a few times..
    if (votes.filter(_.score.v >= 0).isEmpty && ctx.energy<=MaxEnergy) {
      Vote(Explode(2), Score.High, name)
    } else {
      Vote.Abstain
    }
  }
}
