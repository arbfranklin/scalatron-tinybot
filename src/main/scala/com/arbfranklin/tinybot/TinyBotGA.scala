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

import java.io.File
import util.{MasterContext, SlaveContext, BotResponder}

/**A genetic algorithm approach to bot tweaking */
class TinyBotGA(var seeds: List[Genome], gsize: Int) extends BotResponder {
  /** round count */
  var iteration = 0
  var generation = 0

  /** number of mutations to make */
  val mutationCount = 2

  /** results */
  var scores: List[(Genome, Int)] = List()

  /** current run */
  var bot: TinyBot = _

  /** result listeners */
  var listeners = List[(Genome, Int) => Unit]()

  def addListener(f: (Genome, Int) => Unit) {
    listeners = (f :: listeners)
  }

  /** called to denote game start */
  override def init(params: Map[String, String]) {
    if (iteration == 0) {
      println("<" + generation + "> TinyBotGA")
    }

    iteration += 1

    // take the seed for this generation
    val seed = seeds.head
    seeds = seeds.tail

    // init the bot
    bot = new TinyBot(seed)
    bot.init(params)
  }

  /** called on end */
  override def goodbye(round: Int, energy: Int, time: Int) {
    bot.goodbye(round, energy, time)

    // store the genome
    scores = (bot.g, energy) :: scores

    listeners.foreach(f => f(bot.g, energy))

    // seed the next generation?
    if (seeds.isEmpty) {
      // bundle
      val genomes = scores.map(_._1).toSet

      // what was the average score for each genome
      val ascores = genomes.foldLeft(Map[Genome, Int]()) {
        (r, g) =>
          val gscores = scores.filter(_._1 == g).map(_._2)
          r + (g -> (gscores.sum / gscores.size))
      }

      // print stats for the round
      val dist = ascores.map(_._2).toList.sortWith(_ < _)
      println("> samples: " + dist.mkString(", "))
      println()

      // reseed
      seeds = TinyBotGA.seed(ascores, gsize, mutationCount)

      // reset this generations counters
      scores = List()
      generation += 1
      iteration = 0
    }
  }

  /** called when it's the master's turn to react */
  override def reactAsMaster(ctx: MasterContext) = bot.reactAsMaster(ctx)

  /** called when it's the slave's turn to react */
  override def reactAsSlave(ctx: SlaveContext) = bot.reactAsSlave(ctx)
}

object TinyBotGA {
  /** how many runs per genome */
  val testPerGenome = 3

  val writeARFF = System.getProperty("writeARFF", "false").toBoolean

  def apply(s: Genome, gsize: Int, mutationCount: Int): TinyBotGA = {
    val seeds = seed(Map(s -> 0), gsize, mutationCount)

    // create the bot
    val bot = new TinyBotGA(seeds, gsize)
    if (writeARFF) {
      bot.addListener(initARFFWriter(new File("tinybot-" + System.currentTimeMillis() + ".arff")))
    }
    bot
  }

  /** seed a list of genomes */
  def seed(s: Map[Genome, Int], count: Int, mcount: Int): List[Genome] = {
    var genomes = clone(best(s), count)

    // expand the sample set
    genomes.map(g => {
      val mutated = g.mutate(mcount)
      for (i <- 0 until testPerGenome) yield mutated
    }).flatten
  }

  def clone(l: List[Genome], sz: Int) = {
    var genomes = l
    while (genomes.size < sz) {
      genomes = (genomes ++ genomes)
    }
    genomes.take(sz)
  }

  /** obtain the best offspring */
  def best(s: Map[Genome, Int]): List[Genome] = {
    val genomes = s.toList.sortWith((e1, e2) => {
      e1._2 > e2._2
    }).map(_._1).toList

    if (s.size == 1) {
      genomes.take(1)
    } else if (s.size <= 3) {
      List(genomes(0).mate(genomes(1)))
    } else {
      // the top half genomes
      val topPerformers = genomes.take(genomes.size / 2)

      // breed them...
      val h1 = topPerformers.zipWithIndex.filter(_._2 % 2 == 0).map(_._1)
      val h2 = topPerformers.zipWithIndex.filter(_._2 % 2 == 1).map(_._1)
      h1.zip(h2).map(x => x._1.mate(x._2)) // 1 with 3, 2 with 4...
    }
  }

  //
  // WEKA
  //

  /** closure for file writing of Weka ARFF files */
  def initARFFWriter(f: File): (Genome, Int) => Unit = {
    val output = new java.io.PrintWriter(f)
    output.write("@RELATION tinybot\n\n")

    val tg = Genome()
    tg.keys.foreach(s => {
      output.write("@ATTRIBUTE " + s + "\tNUMERIC\n")
    })
    output.write("@ATTRIBUTE score NUMERIC\n\n")

    output.write("@DATA\n")

    (g: Genome, e: Int) => {
      output.write(g.keys.map(g.get(_)).mkString(",") + "," + e + "\n")
      output.flush()
    }
  }
}
