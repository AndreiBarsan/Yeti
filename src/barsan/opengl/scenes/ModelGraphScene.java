package barsan.opengl.scenes;

import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.AmbientLight;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.Cube;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

import com.jogamp.opengl.util.gl2.GLUT;

public class ModelGraphScene extends Scene {
	
	long start = System.currentTimeMillis();
	ModelInstance s1, s2, s3;

	PointLight light = new PointLight(new Vector3(0, 10, 0));
	AmbientLight ambientLight = new AmbientLight(Color.WHITE);
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		BasicMaterial red = new BasicMaterial(new Color(0.9f, 0.2f, 0.4f, 1.0f), Color.WHITE);
		BasicMaterial blue = new BasicMaterial(new Color(0.4f, 0.3f, 0.9f, 1.0f), Color.WHITE);
		BasicMaterial yellow = new BasicMaterial(new Color(0.8f, 0.9f, 0.2f, 1.0f), Color.WHITE);
		
		GL2 gl = Yeti.get().gl;
		
		try {
			ResourceLoader.loadObj("bunny", "res/models/bunny.obj");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		s1 = new ModelInstance(
				new Cube(gl, 1.0f),
				red,
				new Matrix4()
				);
		modelInstances.add(s1);
		
		s2 =  new ModelInstance(
				ResourceLoader.model("bunny"),
				blue,
				new Matrix4()
				);
		s1.addChild(s2);
		
		s3 = new ModelInstance(
				new Cube(gl, 2.0f),
			yellow,
			new Matrix4().setTranslate(0.0f, 3.0f, 0.0f)
		);
		modelInstances.add(s3);
		
		
		camera.setPosition(new Vector3(0.0f, 0.0f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f).normalize());
		//pointLights.add(new PointLight(new Vector3(0f, 20f, 10f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		//ambientLight.setColor(new Color(0.33f, 0.33f, 0.33f));
	}
	
	Matrix4Stack ms = new Matrix4Stack();
	
	@Override
	public void display(GLAutoDrawable drawable) {
		
		float a = (System.currentTimeMillis() - start) / 1000.0f;
		a *= 25.0f;
		float radius = 3.0f;
		
		GL2 gl = Yeti.get().gl;
		
		//s1.getTransform().setRotate(a / 2, 1.0f, 0.0f, 0.0f);
		//Matrix4 wtf = new Matrix4(s1.getTransform()).setRotateNoQ(a / 2, 1.0f, 0.0f, 0.0f);
		//s2.getTransform().setTranslate(0.0f, 2.0f, 0.0f).mul(new Matrix4().setRotate(a * 8, 0.0f, 1.0f, 0.0f));
		//s2.getTransform().setRotate(a, 0.0f, 1.0f, 0.0f);
		
		/*.mul(new Matrix4().setTranslate(
				-(float)Math.sin(a * 3.0f) * radius,
					0.33f,
				+(float)Math.cos(a * 3.0f) * radius
			));
		//*/
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getProjection().getData(), 0);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		//gl.glLoadMatrixf(camera.getView().getData(), 0);


		//gl.glPushMatrix();
		ms.push(new Matrix4().setRotate(a, 0.0f, 0.0f, 1.0f));
		gl.glLoadMatrixf(new Matrix4(camera.getView()).mul(ms.result()).getData(), 0);
		//gl.glMultMatrixf(new Matrix4().setRotate(a, 0.0f, 0.0f, 1.0f).getData(), 0);

		/*
		Shader s = ResourceLoader.shader("phong");
		gl.glUseProgram(s.getHandle());
		
		Matrix4 view = getCamera().getView();
		Matrix4 projection = getCamera().getProjection();
		Matrix4 viewModel = new Matrix4(view).mul(ms.result());
		
		
		s.setUMatrix4("mvpMatrix", new Matrix4(projection).mul(view).mul(ms.result()));
		s.setUMatrix4("mvMatrix", viewModel);
		s.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
		
		
		s.setUVector3f("vLightPosition", light.getPosition());
		
		s.setUVector4f("ambientColor", ambientLight.getColor().getData());
		s.setUVector4f("diffuseColor", light.getDiffuse().getData());
		s.setUVector4f("specularColor", light.getSpecular().getData());
		
		s.setU1i("useTexture", 0);
		s.setU1i("fogEnabled", 0);
		s.setU1i("shininess", 128);
		
		Color diffuse = Color.BLUE;
		s.setUVector4f("matColor", diffuse.getData());
		//*/
		new GLUT().glutSolidTeapot(1.0d);		
		
		ms.pop();
		
		
		//gl.glPopMatrix();
		
		//super.display(drawable);
	}
}
