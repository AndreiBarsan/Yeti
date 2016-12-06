package barsan.opengl.resources;

import java.nio.ByteBuffer;

import com.jogamp.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.StaticModel;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

public class HeightmapBuilder {
	
	static float defaultGridSizeX = 3.0f;
	static float defaultGridSizeY = 3.0f;
	
	static float defaultMinHeight = -10.0f;
	static float defaultMaxHeight =  25.0f;
	
	public static StaticModel modelFromMap(GL3 gl, Texture map, TextureData data) {
		return modelFromMap(gl, map, data, defaultGridSizeX, defaultGridSizeY, defaultMinHeight, defaultMaxHeight);
	}
	
	private static Vector3 makeNormal(ByteBuffer buff, int w, int h, int x, int y, int step) {
		Vector3 result = new Vector3();
		float cur = buff.get( (y * w + x) * 3);
		
		// Don't forget about the color channels! (2-3 hours lost here).
		// Hehe, nailed this one after two beers one of which was a 7.5-er :D
		// 13-14.12.12
		float Az = (x + step) <  w ? buff.get( (y * w + x + step) * 3) 		: cur;
		float Bz = (y + step) <  h ? buff.get( ((y + step) * w + x) * 3) 	: cur;
		float Cz = (x - step) >= 0 ? buff.get( (y * w + x - step) * 3) 		: cur;
		float Dz = (y - step) >= 0 ? buff.get( ((y - step) * w + x) * 3) 	: cur;
		
		float scale = 1.0f;
		return result.set(Cz - Az, 0.6f * scale, Dz - Bz).normalize();
	}
	
	public static StaticModel modelFromMap(GL3 gl, Texture map, TextureData data,
			float gridSizeX, float gridSizeY,
			float minHeight, float maxHeight) {
		// process pixelz and generate geometry
		StaticModel result = new StaticModel(gl, "heightmap");
		result.setPointsPerFace(3); // Using triangles
		
		map.bind(gl);
		FBObject fbo = new FBObject();
		
		//TextureAttachment ta = fbo.attachTexture2D(gl, 0, false);
		Face face = new Face();
	  // TODO(andrei): fbo.reset possible weirdness.
		fbo.reset(gl, data.getWidth(), data.getHeight(), fbo.getNumSamples());
		fbo.bind(gl);
		gl.glFramebufferTexture2D(GL3.GL_DRAW_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0,
				map.getTarget(), map.getTextureObject(gl), 0);
		
		int w = map.getWidth(), h = map.getHeight();
		
		ByteBuffer buff = ByteBuffer.allocate(Buffers.SIZEOF_BYTE * 3 * w * h);
		Yeti.debug("Building heightmap. Warning - coercing bytes to GL_BYTE size!");
		long start = System.currentTimeMillis();
		// Skip some of the map's pixels
		int step = 4;
		Vector3 c1 = new Vector3(), c2 = new Vector3(), c3 = new Vector3(), c4 = new Vector3();
		
		// TODO: try and use glGetTexImage instead, avoiding the creation of
		// an unnecessary FBO
		gl.glReadPixels(0, 0, w, h, data.getPixelFormat(), GL3.GL_BYTE, buff);
		
		int hw = w / 2;
		int hh = h / 2;
		
		for(int x = 0; x < w - 1; x += step) {
			for(int y = 0; y < h - 1; y += step) {
				
				int nextX = Math.min(x + step, w - 1);
				int nextY = Math.min(y + step, h - 1);
				
				int bl = buff.get( (y * w + nextX) * 3 );
				int br = buff.get( (nextY * w + nextX) * 3);
				int tr = buff.get( (nextY * w + x) * 3);
				int tl = buff.get( (y * w + x ) * 3 );
				
				face = new Face();
				c1.set( (x - hw) * gridSizeX, MathUtil.lerp(minHeight, maxHeight, (float)tr / 255.0f), (nextY - hh) * gridSizeY);
				c2.set( (nextX - hw) * gridSizeX, MathUtil.lerp(minHeight, maxHeight, (float)br / 255.0f), (nextY - hh) * gridSizeY);
				c3.set( (nextX - hw) * gridSizeX, MathUtil.lerp(minHeight, maxHeight, (float)bl / 255.0f), (y - hh) * gridSizeY);
				c4.set( (x - hw) * gridSizeX, MathUtil.lerp(minHeight, maxHeight, (float)tl / 255.0f), (y - hh) * gridSizeY);
				face.points = new Vector3[] {
						new Vector3(c1), new Vector3(c2), new Vector3(c3)
				};
				
				Vector3 c1n = makeNormal(buff, w, h, x, nextY, step);
				Vector3 c2n = makeNormal(buff, w, h, nextX, nextY, step);
				Vector3 c3n = makeNormal(buff, w, h, nextX, y, step);
				Vector3 c4n = makeNormal(buff, w, h, x, y, step);
				
				face.normals = new Vector3[] { c1n, c2n, c3n };
				
				// TODO: better mapping
				/*
				face.texCoords = new Vector3[] {
						new Vector3( (float)x / (float)w,  		(float)y / (float)h, 0.0f),
						new Vector3( (float)nextX /(float)w,  	(float)nextY /(float)h, 0.0f),
						new Vector3( (float)nextX /(float)w,  	(float)y / (float)h, 0.0f),
						new Vector3( (float)x / (float)w,  		(float)nextY /(float)h, 0.0f),
						
				};*/
				face.texCoords = new Vector3[] {
						new Vector3( 0.0f, 0.0f, 0.0f ),
						new Vector3( 0.0f, 1.0f, 0.0f ),
						new Vector3( 1.0f, 1.0f, 0.0f ),
						new Vector3( 1.0f, 0.0f, 0.0f )
				};
				
				result.addFace(face);
				
				face = new Face();
				face.points = new Vector3[] { new Vector3(c3), new Vector3(c4), new Vector3(c1) };
				face.texCoords = new Vector3[] {
						new Vector3( 0.0f, 0.0f, 0.0f ),
						new Vector3( 0.0f, 1.0f, 0.0f ),
						new Vector3( 1.0f, 1.0f, 0.0f ),
						new Vector3( 1.0f, 0.0f, 0.0f )
				};
				face.normals = new Vector3[] { c3n, c4n, c1n };
				
				result.addFace(face);
			}
		}
		Yeti.debug("Should build " + w * h + " faces!");
		result.buildVBOs();
		Yeti.debug("Finished uploading heightmap data! %d ms", System.currentTimeMillis() - start);
		
		fbo.detachAll(gl);
		fbo.unbind(gl);
		
		
		return result;
	}
}
