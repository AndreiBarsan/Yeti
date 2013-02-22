TO-DO LIST OF THINGS TO DO
===============================================================================
Bugs:
 - collision checking fails at low fps (due to it being a hacky implementation)
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
   
General TODOs:
 - use tangent & binormal for normal mapping, it's faster (cached values, duh);
 Valve also uses this approach!
 - optimize omnidirectional shadow maps with dot products instead of lengths, maybe?
 - use shadow samplers for the shadow computations, they should be faster
 - use input polling for the camera -> smoother movement (needed later for the char controls anyway)
 - start working on bounding volumes to start doing lighting the right way!
 - standardized texture units - only bind things like the shadow map ONCE
 - camera update() method (automatically called by the scene - keep everything in sync, prevent recalculations of the view matrix etc.)
 - perpixel fog & fix fog computation
 - multiple lights
 
 - optional utility to draw:
   - pie chart render data
   - axes
   - NORMALS!
 - when creating post-process effects, compile basic vertex shader, get 
  all other fragment shaders, and link all fragments to the same vertex shader,
  saving (n-1) useless recompilations of the postprocess vertex shaders
  
  - global rendering settings should be part of every material (think
  gamma correction and tone mapping); should gamma correction be part of a
  post-processing system?
 
 - use uniform blocks for shaders (with possibility of loading multiple items
  in a single action from the App)
 - list of all lights and entities
 - edit lights and entities (entity transforms) through editor
 - load .obj dialog with "recents" list in editor
 - log object
 

Milestones:
================================================================================
Version 1.0 (project presentation 25 Feb 2013):
--------------------------------------------------------------------------------
 - simple (vertex interpolation-based) animations
 - simple (platformer-style) 2D physics
 - simple (only buttons for now) GUI system
 - glitch-free toggle-able shadows
 - playable PlanetHeads tech demo
 - (?) simple (load and play wav sounds) OpenAL sound support
 
Version 2.0 (sometime during the 4th semester OpenGL Praktikum):
--------------------------------------------------------------------------------
 - batched render calls
 - specular/emmisive maps
 - pallalax mapping
 - depth-of-field post-processing
 - tone mapping & HDR
 - bloom
 - post-process cel-shading 
 - Version 2.0 *could* be re-written in C++ (no more Java requirement for the 
 Praktikum yay!)