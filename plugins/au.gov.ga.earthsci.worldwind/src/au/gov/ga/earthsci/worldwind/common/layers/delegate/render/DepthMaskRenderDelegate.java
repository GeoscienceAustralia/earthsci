/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRenderDelegate;

/**
 * {@link IRenderDelegate} that disables writing into the depth buffer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthMaskRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "DepthMask";
	protected boolean oldValue = false;

	@Override
	public void preRender(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		byte[] result = new byte[1];
		gl.glGetBooleanv(GL2.GL_DEPTH_WRITEMASK, result, 0);
		oldValue = result[0] == GL2.GL_TRUE;
		gl.glDepthMask(false);
	}

	@Override
	public void postRender(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		gl.glDepthMask(oldValue);
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
		{
			return new DepthMaskRenderDelegate();
		}
		return null;
	}
}
