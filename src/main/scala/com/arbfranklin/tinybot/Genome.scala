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

package com.arbfranklin.tinybot

import collection.mutable

/**
 * the properties that define how this tinybot operates, every property is a double value between 0 and 1. The current
 * constants have been deduced through trial runs of {@link TinyBotGA}.
 */
class Genome private(props: mutable.Map[String, Double]) {
  /** master bot properties */
  val master = new Bundle {
    // unfortunately the winning strategy is very much to spawn as much as you can
    val spawn = new Bundle {
      val maxBots = (get("master/spawn:maxBots", 0.1) * 500).toInt
    }

    val avoidEnergyLoss = new Bundle {
      val weight = get("master/avoidEnergyLoss:weight", 0.7089)
    }

    val huntZugars = new Bundle {
      val weight = get("master/huntZugars:weight", 0.7966)
    }

    val huntFluppets = new Bundle {
      val weight = get("master/huntFluppets:weight", 0.6424)
    }

    val avoidOthers = new Bundle {
      val weight = get("master/avoidOtherMiniBots:weight", 0.0816)
    }

    val avoidWalls = new Bundle {
      val weight = get("master/avoidWalls:weight", 0.4)
    }

    val random = new Bundle {
      val weight = get("master/randomMove:weight", 0.05)
    }

    val velocity = new Bundle {
      val weight = get("master/velocity:weight", 0.17)
    }

    val stay = new Bundle {
      val weight = get("master/stay:weight", 0.9)
      val minTurns = (get("master/stay:weight", 0.1) * 100).toInt
    }
  }

  /** mini-bot properties */
  val slave = new Bundle {
    val spawn = new Bundle {
      val frequency = get("slave/spawn:freq", 0.99)
      val imbalance = (get("slave/spawn:imbalance", 0.1) * 10).toInt
    }

    val avoidEnergyLoss = new Bundle {
      val weight = get("slave/avoidEnergyLoss:weight", 1)
    }

    val huntZugars = new Bundle {
      val weight = get("slave/huntZugars:weight", 0.7802)
    }

    val huntFluppets = new Bundle {
      val weight = get("slave/huntFluppets:weight", 0.6106)
    }

    val spreadOut = new Bundle {
      val weight = get("slave/spreadOutBots:weight", 0.2466)
    }

    val home = new Bundle {
      val weight = get("slave/home:weight", 0.8)
      val roi = (get("slave/home:roi", 0.03) * 100).toInt
      val safetyMargin = get("slave/home:safetyMargin", 0.3) * 10
    }

    val random = new Bundle {
      val weight = get("slave/randomMove:weight", 0.041)
    }

    val velocity = new Bundle {
      val weight = get("slave/velocity:weight", 0.1)
    }

    val apocalypse = new Bundle {
      val minTurns = (get("slave/apocalypse:minTurns", 0.78) * 10).toInt
    }

    val antibump = new Bundle {
      val weight = get("slave/antibump:weight", 0.5)
    }

    val attack = new Bundle {
      val radius = (get("slave/attack:radius", 0.2) * 5).toInt + 2 // 2 - 7
      val maxEnergy = (get("slave/attack:maxEnergy", 0.5) * 2000).toInt
      val minCount = (get("slave/attack:minCount", 0.4) * 9).toInt + 1 // 1 - 10
    }
  }

  val shared = new Bundle {
    val spawn = new Bundle {
      val maxBots = (get("shared/spawn:maxBots", 0.7) * 1000).toInt
    }
  }

  //
  // IMPL
  //

  private def get(k: String, default: Double): Double = {
    if (!props.contains(k)) {
      props.put(k, default)
    }
    props.getOrElse(k, default)
  }

  def keys = props.keys.toList.sorted

  def get(k: String): Double = props(k)

  /** mate this genome with the provided one */
  def mate(partner: Genome): Genome = {
    val attrs = mutable.Map[String, Double]()
    props.foreach(x => {
      val key = x._1
      val v = (x._2 + get(key, 0)) / 2
      attrs += (key -> v)
    })
    new Genome(attrs)
  }

  /** mutate this genome with n mutations */
  def mutate(n: Int): Genome = {
    val rand = scala.util.Random

    val arr = props.toArray
    for (i <- 0 until n; if arr.length > 0) {
      val idx = rand.nextInt(arr.size)
      val (k, v) = arr(idx)
      val nv = math.min(math.max(v + (rand.nextDouble() - 0.5) * 0.1, 0), 1)
      arr(idx) = (k, nv)
    }

    var result = mutable.Map[String, Double]()
    arr.foreach(x => result += (x._1 -> x._2))
    new Genome(result)
  }

  override def toString = props.map(x => x._1 + "=" + x._2).mkString(",")
}

object Genome {
  def apply() = new Genome(mutable.Map[String, Double]())
}

trait Bundle
