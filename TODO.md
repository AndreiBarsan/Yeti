TO-DO LIST OF THINGS TO DO
===============================================================================
Bugs:
 - models with bump maps but no diffuse texture aren't showing right since the
 engine is only checking whether an object has a diffuse texture before binding
 the tex coords
 - collision checking fails at low fps (due to it being a hacky implementation)
 - JVM Crash - condition: game scene, last thing added is player, no heaven
 beam, attempt to leave scene; at the first render call in the menu the JVM 
 crashes; (maybe the updated JOGL fixed it (17.03 - check it!)
 - view matrix straight down causes problems
 - multitexture material broken
 - actually find and write down the (rather old) matrix multiplication BUG
 
Architecture issues:
 - figure out what to with textures - if I'm going to move them to the
   respective component altogether, how will we interact with the VBO with the
   texture coords? Maybe some sort of VBO channel interface that belongs to a
   component? Texture -> UV, WorldTransform -> Geometry, WTNormals -> Geometry + normals
   Bump component (example) -> Tangents
 - this also causes numerous undefined states when certain samplers are unbound
   and shouldn't be used, but they get sampled anyway due to how branching works
   (or, well, doesn't work) on graphics cards
 - handling global paradigm shifts like forward/deferred rendering is ATM rather
   tricky; using TECHNIQUES instead of FORCED MATERIALS could potentially help
   
General TODOs:
 - no longer send position - compute from screen coords & z; maybe better? though
 we don't really have a bandwidth problem - we don't use motion blur for instance
 - also in nessie refactoring, since you are rewriting the way data gets sent
 to the shaders, USE DAMN BLOCKS! (after you get the basic shit working, ofc)
 - optimize omnidirectional shadow maps with dot products instead of lengths, maybe?
 - use dedicated shadow samplers for the shadow computations, they should be faster
 - use input polling for the camera -> smoother movement (needed later for the char controls anyway)
 - related to the above - a general polling-based input provider should be created
 - start working on bounding volumes to start doing lighting, shadowing and maybe
 3D collision detection the right way!
 - standardized texture units - only bind things like the shadow map ONCE
 - camera update() method (automatically called by the scene - keep everything in sync, prevent recalculations of the view matrix etc.)
 - perpixel fog & fix fog computation
 - multiple lights
 
 - optional utility to draw:
   - pie chart render data (time spent on various draw phases - this is going
   to be tricky to implement as it is quite hard to figure out what proportion
   of the time is actually spend rendering, and what proportion is being devoured
   by the calls into native code)
   - axes
   - NORMALS!
 - when creating post-process effects, compile basic vertex shader, get 
  all other fragment shaders, and link all fragments to the same vertex shader,
  saving (n-1) useless recompilations of the postprocess vertex shaders
  
  - global rendering settings should be part of every material (think
  gamma correction and tone mapping);
  - should gamma correction be part of a post-processing system? YES - the pixels
  get converted from floating-point color values to integers afterwards
 
 - use uniform blocks for shaders (with possibility of loading multiple items
  in a single action from the App)
 - list of all lights and entities and allow editing
 - load .obj dialog with "recents" list in editor
 - log object
 

Milestones:
================================================================================
Version 1.0 (project presentation 25 Feb 2013):
--------------------------------------------------------------------------------
 - [v] simple (vertex interpolation-based) animations
 - [v] simple (platformer-style) 2D physics
 - [v] simple (only buttons for now) GUI system
 - [v] glitch-free toggle-able shadows
 - [v] playable PlanetHeads tech demo
 
Version 1.1 (sometime during the 4th semester OpenGL Praktikum):
--------------------------------------------------------------------------------
 - deferred rendering with functional bumpmapping and shadow mapping
 - more flexible material system -> technique based rendering, no more rendering
 code in the model and material objects
 - dynamic post-processing pipeline
 
Future
--------------------------------------------------------------------------------
 - simple (load and play wav sounds) OpenAL sound support
 - particle systems
 - batched render calls
 - specular/emmisive maps
 - parallax mapping
 - depth-of-field post-processing
 - tone mapping & HDR
 - bloom
 - post-process cel-shading 
 - Version 2.0 *could* be re-written in C++ (no more Java requirement for the 
 Praktikum yay!); sadly, not enough time is available;