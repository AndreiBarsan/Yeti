The Yeti Java OpenGL Engine
===========================

Summary
-------
This engine is a personal project of mine, so it's not really recommended for 
serious projects (like for serious game development endeavors). I try to document
it decently, so one might learn one or two things from it. Or at learn about 
what nasty hacks to avoid in the future.

Looking forward to any fork! :)

![Renderer screenshot](http://siegedog.com/renderer.jpg "The renderer in action")
Here's Yeti with a few Bioshock models loaded (deferred renderer).

Running the thing
-----------------
The project used to be quite easy to build.
Now it's even easier, since it uses Maven.

1. Clone repo
2. Load the project in Eclipse. It should figure out which jars to use thanks to
pom.xml.
3. Run either barsan.opengl.tools.DeferredTest or barsan.opengl.planetHeads.Game
4. Note that Java 7 and OpenGL 3 are needed (the GL3bc profile, actually - which is why
the project doesn't work on most Macs due to them only supporting either GL2 or GL3)
5. If the game crashes, it's very likely because:
	a) your video card doesn't support a certain feature
	b) your video card has stricter shader compilation rules and chokes on something 
	In both cases, it would really help to open a ticket on the issue tracker here on GitHub.
6. If it's runnig, congratulations, you got it going! Remember, pull requests are always welcome! :)

Of course, Eclipse isn't *required* to build the engine. You can `javac` it, no 
problem, you can use a different folder for the libs, and so on.

Features
--------
Among the most important features implemented so far are:
 - forward and deferred rendering (using stencil tests for light receiver culling)
 - basic Wavefront *.obj loading
 - vertex interpolation keyframe animations
 - phong lighting model (with directional, point and spot lights)
 - normal mapping
 - skyboxes and environment mapping
 - shadow mapping (for all light types - omnidirectional shadowmaps generated
 in a single pass using a geometry shader)
 - simple and axis-aligned billboarding
 - scene management
 - multipass rendering with post-processing support (right now used for FBO-based
 multisampling and a naive SSAO implementation)
 - simple 2D platformer physics

Miscellaneous things & philosophy
---------------------------------
First and foremost, this is a learning experience. I am learning OpenGL as I'm
writing this, so don't expect the engine to be in any way clean or optimized.
Some things worth noting are the major roadblocks I've hit (and overcome!)
while developing this project - for most of them, I've left some sort of "monument"
in the source code, describing the nature of the problem and how I've fixed it. I
find that by actually writing this down and then seeing it over and over again helps
you remember common mistakes that you've made and thus helps you avoid them in the
future.

I obviously don't recommend doing this in an actual serious project (that's what
bug trackers are for!), but I like to use it in small personal projects.

The code is designed to fail as often as possible. And by that I mean that whenever
it detects something that's wrong, it instantly crashes telling you what went 
wrong. It doesn't struggle to chug along at any cost. Again, for a professional
project this might not be the right technique all of the time, and you'd probably 
want to have some sort of crash recovery system in some of the cases.

License
-------
Yeti is licensed under the BSD 2-clause license.

Copyright (c) 2012-2013, Andrei Bârsan
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this 
list of conditions and the following disclaimer in the documentation and/or other
 materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.

The JOGL source code is mostly licensed under the New BSD 2-clause license,
however it contains other licensed material as well. See the jogl.LICENSE file for details.