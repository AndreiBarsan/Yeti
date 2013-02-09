@echo off
rem This is a quick script to run the already-compiled engine from the command line on windows.
java -cp bin/;lib/jogamp-all-platforms/jar/jogl-all.jar;lib/jogamp-all-platforms/jar/gluegen-rt.jar barsan.opengl.editor.App
