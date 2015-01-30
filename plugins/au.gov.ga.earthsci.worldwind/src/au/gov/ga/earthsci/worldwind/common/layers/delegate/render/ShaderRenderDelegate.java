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
import javax.xml.xpath.XPath;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRenderDelegate;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * {@link IRenderDelegate} that binds a GLSL shader before rendering.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShaderRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "Shader";

	protected final String vertexShader;
	protected final String fragmentShader;

	protected ShaderRenderDelegateShader shader;

	@SuppressWarnings("unused")
	private ShaderRenderDelegate()
	{
		this(null, null);
	}

	public ShaderRenderDelegate(String vertexShader, String fragmentShader)
	{
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}

	@Override
	public void preRender(DrawContext dc)
	{
		if (shader == null)
		{
			GL2 gl = dc.getGL().getGL2();
			shader = new ShaderRenderDelegateShader(vertexShader, fragmentShader);
			shader.create(gl);
		}
		shader.use(dc);
	}

	@Override
	public void postRender(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		shader.unuse(gl);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (DEFINITION_STRING.equalsIgnoreCase(definition))
		{
			XPath xpath = XMLUtil.makeXPath();
			Element vertexElement = XMLUtil.getElement(layerElement, "Shaders/VertexShader", xpath);
			Element fragmentElement = XMLUtil.getElement(layerElement, "Shaders/FragmentShader", xpath);
			if (vertexElement != null && fragmentElement != null)
			{
				return new ShaderRenderDelegate(vertexElement.getTextContent(), fragmentElement.getTextContent());
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		Document document = layerElement.getOwnerDocument();
		Element shadersElement = document.createElement("Shaders");
		layerElement.appendChild(shadersElement);
		Element vertexShaderElement = document.createElement("VertexShader");
		shadersElement.appendChild(vertexShaderElement);
		CDATASection vertexShaderText = document.createCDATASection(vertexShader);
		vertexShaderElement.appendChild(vertexShaderText);
		Element fragmentShaderElement = document.createElement("FragmentShader");
		shadersElement.appendChild(fragmentShaderElement);
		CDATASection fragmentShaderText = document.createCDATASection(fragmentShader);
		fragmentShaderElement.appendChild(fragmentShaderText);
		return DEFINITION_STRING;
	}
}
