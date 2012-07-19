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

import collection.GenIterable

/**a score for an item ranging from -1 to 1 */
case class Score(v: Double) {
  /**reweight a value */
  def weight(w: Double): Score = if (fixed) this else Score(w * v)

  def fixed = v <= (-1d) || v >= (1d)
}

object Score {
  /** special scores */
  val Veto = new Score(-1)
  val Abstain = new Score(0)
  val Mandate = new Score(1)

  /** helper scores */
  val High = new Score(0.8)
  val Low = new Score(0.1)

  def combine(scores: GenIterable[Score]): Score = {
    if (scores.isEmpty || !scores.filter(_.v <= (-1)).isEmpty) return Veto
    if (!scores.filter(_.v >= 1).isEmpty) return Mandate

    // average the scores
    val sum = scores.foldLeft(0d) {
      (total, s) => total + s.v
    }
    Score(sum / scores.size)
  }
}
