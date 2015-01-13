/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.application.parts;

import javax.inject.Inject;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;
import com.jogamp.opengl.util.Animator;

/**
 * OpenGL test part which tests the {@link NewtCanvasSWT}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OpenGLTestPart
{
	private int rot = 0;

	@Inject
	public void init(Composite parent)
	{
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		final GLWindow glWindow = GLWindow.create(caps);
		new NewtCanvasSWT(parent, SWT.NONE, glWindow)
		{
			@Override
			public void setBounds(int x, int y, int width, int height)
			{
				//do not allow a size of 0,0, because NEWT window becomes invisible (https://jogamp.org/bugzilla/show_bug.cgi?id=822)
				super.setBounds(x, y, Math.max(1, width), Math.max(1, height));
			}
		};

		glWindow.addGLEventListener(new MyGlEventListener());

		Animator animator = new Animator(glWindow);
		animator.setUpdateFPSFrames(1, null);
		animator.start();
	}

	/**
	 * GLEventListener which draws a spinning torus. From
	 * https://github.com/sgothel
	 * /jogl-demos/blob/master/src/demos/swt/Snippet209.java.
	 */
	private class MyGlEventListener implements GLEventListener
	{
		@Override
		public void init(GLAutoDrawable drawable)
		{
			GL2 gl = drawable.getGL().getGL2();
			gl.setSwapInterval(1);
			gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			gl.glColor3f(1.0f, 0.0f, 0.0f);
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
			gl.glClearDepth(1.0);
			gl.glLineWidth(2);
			gl.glEnable(GL2.GL_DEPTH_TEST);
		}

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
		{
			float fAspect = (float) width / (float) height;
			GL2 gl = drawable.getGL().getGL2();
			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU glu = new GLU();
			glu.gluPerspective(45.0f, fAspect, 0.5f, 400.0f);
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		@Override
		public void display(GLAutoDrawable drawable)
		{
			GL2 gl = drawable.getGL().getGL2();
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			gl.glClearColor(.3f, .5f, .8f, 1.0f);
			gl.glLoadIdentity();
			gl.glTranslatef(0.0f, 0.0f, -10.0f);
			gl.glRotatef(0.15f * rot, 2.0f * rot, 10.0f * rot, 1.0f);
			gl.glRotatef(0.3f * rot, 3.0f * rot, 1.0f * rot, 1.0f);
			rot++;
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			gl.glColor3f(0.9f, 0.9f, 0.9f);
			drawTorus(gl, 1, 1.9f + ((float) Math.sin((0.004f * rot))), 15, 15);
		}

		@Override
		public void dispose(GLAutoDrawable drawable)
		{
		}

		protected void drawTorus(GL2 gl, float r, float R, int nsides, int rings)
		{
			float ringDelta = 2.0f * (float) Math.PI / rings;
			float sideDelta = 2.0f * (float) Math.PI / nsides;
			float theta = 0.0f, cosTheta = 1.0f, sinTheta = 0.0f;
			for (int i = rings - 1; i >= 0; i--)
			{
				float theta1 = theta + ringDelta;
				float cosTheta1 = (float) Math.cos(theta1);
				float sinTheta1 = (float) Math.sin(theta1);
				gl.glBegin(GL2.GL_QUAD_STRIP);
				float phi = 0.0f;
				for (int j = nsides; j >= 0; j--)
				{
					phi += sideDelta;
					float cosPhi = (float) Math.cos(phi);
					float sinPhi = (float) Math.sin(phi);
					float dist = R + r * cosPhi;
					gl.glNormal3f(cosTheta1 * cosPhi, -sinTheta1 * cosPhi, sinPhi);
					gl.glVertex3f(cosTheta1 * dist, -sinTheta1 * dist, r * sinPhi);
					gl.glNormal3f(cosTheta * cosPhi, -sinTheta * cosPhi, sinPhi);
					gl.glVertex3f(cosTheta * dist, -sinTheta * dist, r * sinPhi);
				}
				gl.glEnd();
				theta = theta1;
				cosTheta = cosTheta1;
				sinTheta = sinTheta1;
			}
		}
	}
}
