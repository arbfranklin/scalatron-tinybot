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

/** if the target is outside the view range, we find an equivalent point to chase inside the view */
class TargetBounderSpec extends Specification {
  "a simple open view" should {
    val view = toView("""
      _____
      _____
      __M__
      _____
      _____
    """)

    "not modify already bounded values" in {
      solve(view, XY(0,0)) must beSome(XY(0,0))
      solve(view, XY(4,4)) must beSome(XY(4,4))
    }

    "retract to extents" in {
      solve(view, XY(-1,-1)) must beSome(XY(0,0))
      solve(view, XY(7,7)) must beSome(XY(4,4))
    }
  }

  "a partially closed view" should {
    val view = toView("""
      _____
      ____W
      __M_W
      ____W
      ____W
    """)

    "have a solution" in {
      solve(view, XY(10,3)) must beSome(XY(4,0))
      solve(view, XY(10,5)) must beSome(XY(4,0))
    }
  }

  "a fully closed view" should {
    val view = toView("""
      ____W
      ____W
      __M_W
      ____W
      ____W
    """)

    "have no solution" in {
      solve(view, XY(10,3)) must beEmpty
      solve(view, XY(10,5)) must beEmpty
    }
  }

  /** helpers */
  def toView(s: String) = View(s.replaceAll("\\s*",""))

  /** tester function */
  def solve(view: View, xy: XY) : Option[XY] = TargetBounder.bound(view,xy)
}
