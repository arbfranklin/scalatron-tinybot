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

import org.specs2.mutable._
import org.specs2.matcher.{ContainMatcher, ContainAnyOfMatcher}

/**
 * The pathfinder should be able to find the best path for anything within the view bounds.. outside of that it gives up.
 */
class PathSolverSpec extends Specification {
  "a simple unblocked path" should {
    val view = toView("""
      |_______
      |_______
      |_______
      |___M___
      |_______
      |_______
      |_______
    """.stripMargin)

    "be solved along diagonals" in {
      solve(view, Move(-2,-2))  must beIn(Move(-1,-1))
      solve(view, Move(2,2))    must beIn(Move(1,1))
      solve(view, Move(-2,2))   must beIn(Move(-1,1))
      solve(view, Move(2,-2))   must beIn(Move(1,-1))
    }

    "be solved along orthogonals" in {
      solve(view, Move(0,2))  must beIn(Move(0,1), Move(1,1), Move(-1,1))
      solve(view, Move(0,-2)) must beIn(Move(0,-1), Move(-1,-1), Move(1,-1))
      solve(view, Move(-2,0)) must beIn(Move(-1,0), Move(-1,-1), Move(-1,1))
      solve(view, Move(2,0))  must beIn(Move(1,0), Move(1,-1), Move(1,1))
    }

    "be unsolved past extents" in {
      solve(view, Move(-20,-20))  must beEmpty
      solve(view, Move(20,20))    must beEmpty
    }
  }

  "a fully blocked path" should {
    val view = toView("""
      |_______W?
      |_______W?
      |_______W?
      |_______W?
      |____M__W?
      |_______W?
      |_______W?
      |_______W?
      |_______W?
    """.stripMargin)

    "not be solvable" in {
      solve(view, Move(4,0)) must beEmpty
      solve(view, Move(4,-4)) must beEmpty // we can allow us to get closer
      solve(view, Move(4,4)) must beEmpty
    }
  }

  "a partially blocked path" should {
    "be solved with occlusion" in {
      val view = toView("""
        |_________
        |_________
        |____WWWW?
        |_______W?
        |____M__W?
        |_______W?
        |__WWWWWW?
        |_________
        |_________
      """.stripMargin)

      solve(view, Move(4,0)) must beIn(Move(0,-1), Move(-1,-1))
      solve(view, Move(4,-4)) must beIn(Move(0,-1), Move(-1,-1))
      solve(view, Move(4,4)) must beIn(Move(-1,0), Move(-1,1))
    }

    "be solved with no occlusion" in {
      val view = toView("""
        |_________
        |_________
        |____WWWWW
        |________W
        |____M___W
        |________W
        |__WWWWWWW
        |_________
        |_________
      """.stripMargin)

      solve(view, Move(4,0)) must beEmpty
      solve(view, Move(4,-4)) must beIn(Move(0,-1), Move(-1,-1))
      solve(view, Move(4,4)) must beIn(Move(-1,0), Move(-1,1))
    }

  }

  /** helpers */
  def solve(view: View, move: Move) = PathSolver(view, view.center, view.center + move).solve()
  def toView(s: String) = View(s.replaceAll("\\s*",""))

  def beIn(vals: Move*) = {
    new ContainMatcher(vals.seq).only
  }
}
