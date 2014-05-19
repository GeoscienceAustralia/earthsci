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
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL2;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRenderDelegate;

/**
 * {@link IRenderDelegate} that allows customization of the blend equation and
 * function used by OpenGL.
 * <p/>
 * Usage:
 * <p/>
 * <code>&lt;Delegate&gt;Blending&lt;/Delegate&gt;</code></br>
 * <code>&lt;Delegate&gt;Blending(src,dst)&lt;/Delegate&gt;</code></br>
 * <code>&lt;Delegate&gt;Blending(src,dst,r,g,b,a)&lt;/Delegate&gt;</code></br>
 * <code>&lt;Delegate&gt;Blending(src,dst,eq,r,g,b,a)&lt;/Delegate&gt;</code>
 * <ul>
 * <li>src = Specifies how the red, green, blue, and alpha source blending
 * factors are computed (default = <code>SrcAlpha</code>)
 * <li>dst = Specifies how the red, green, blue, and alpha destination blending
 * factors are computed (default = <code>OneMinusSrcAlpha</code>)
 * <li>eq = Specifies how the source and destination colors are combined
 * (default = <code>FuncAdd</code>)
 * <li>r = value to use for the current color's red value (use
 * <code>alpha</code> for the current layer's opacity)
 * <li>g = value to use for the current color's green value (use
 * <code>alpha</code> for the current layer's opacity)
 * <li>b = value to use for the current color's blue value (use
 * <code>alpha</code> for the current layer's opacity)
 * <li>a = value to use for the current color's alpha value (use
 * <code>alpha</code> for the current layer's opacity)
 * </ul>
 * <p/>
 * Default setup is
 * <code>Blending(SrcAlpha,OneMinusSrcAlpha,FuncAdd,1.0,1.0,1.0,alpha)</code>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class BlendingRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "Blending";

	protected final static Map<Integer, String> reverse = new HashMap<Integer, String>()
	{
		{
			put(GL2.GL_ZERO, "Zero");
			put(GL2.GL_ONE, "One");
			put(GL2.GL_SRC_COLOR, "Src_Color");
			put(GL2.GL_ONE_MINUS_SRC_COLOR, "One_Minus_Src_Color");
			put(GL2.GL_DST_COLOR, "Dst_Color");
			put(GL2.GL_ONE_MINUS_DST_COLOR, "One_Minus_Dst_Color");
			put(GL2.GL_SRC_ALPHA, "Src_Alpha");
			put(GL2.GL_ONE_MINUS_SRC_ALPHA, "One_Minus_Src_Alpha");
			put(GL2.GL_DST_ALPHA, "Dst_Alpha");
			put(GL2.GL_ONE_MINUS_DST_ALPHA, "One_Minus_Dst_Alpha");
			put(GL2.GL_CONSTANT_COLOR, "Constant_Color");
			put(GL2.GL_ONE_MINUS_CONSTANT_COLOR, "One_Minus_Constant_Color");
			put(GL2.GL_CONSTANT_ALPHA, "Constant_Alpha");
			put(GL2.GL_ONE_MINUS_CONSTANT_ALPHA, "One_Minus_Constant_Alpha");
			put(GL2.GL_SRC_ALPHA_SATURATE, "Src_Alpha_Saturate");
			put(GL2.GL_FUNC_ADD, "Func_Add");
			put(GL2.GL_FUNC_SUBTRACT, "Func_Subtract");
			put(GL2.GL_FUNC_REVERSE_SUBTRACT, "Func_Reverse_Subtract");
			put(GL2.GL_MIN, "Min");
			put(GL2.GL_MAX, "Max");
		}
	};
	protected final static Map<String, Integer> constants = new HashMap<String, Integer>()
	{
		{
			for (Map.Entry<Integer, String> entry : reverse.entrySet())
			{
				String key = entry.getValue().toLowerCase();
				Integer value = entry.getKey();
				put(key, value);
				put(key.replace("_", ""), value);
			}
		}
	};
	protected final static String ALPHA_KEYWORD = "alpha";

	protected final int srcFactor;
	protected final int dstFactor;
	protected final int equation;
	protected final double[] color;
	protected final boolean[] alpha;

	@SuppressWarnings("unused")
	private BlendingRenderDelegate()
	{
		this(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}

	public BlendingRenderDelegate(int srcFactor, int dstFactor)
	{
		this(srcFactor, dstFactor, new double[] { 1.0, 1.0, 1.0, 1.0 }, new boolean[] { false, false, false, true });
	}

	public BlendingRenderDelegate(int srcFactor, int dstFactor, double[] color, boolean[] alpha)
	{
		this(srcFactor, dstFactor, GL2.GL_FUNC_ADD, color, alpha);
	}

	public BlendingRenderDelegate(int srcFactor, int dstFactor, int equation, double[] color, boolean[] alpha)
	{
		if (color.length != 4 || alpha.length != 4)
		{
			throw new IllegalArgumentException();
		}
		this.srcFactor = srcFactor;
		this.dstFactor = dstFactor;
		this.equation = equation;
		this.color = color;
		this.alpha = alpha;
	}

	@Override
	public void preRender(DrawContext dc)
	{
		Layer currentLayer = dc.getCurrentLayer();
		double opacity = currentLayer != null ? currentLayer.getOpacity() : 1.0;
		GL2 gl = dc.getGL().getGL2();
		gl.glColor4d(
				alpha[0] ? opacity : color[0],
				alpha[1] ? opacity : color[1],
				alpha[2] ? opacity : color[2],
				alpha[3] ? opacity : color[3]);
		gl.glBlendFunc(srcFactor, dstFactor);
		gl.glBlendEquation(equation);
	}

	@Override
	public void postRender(DrawContext dc)
	{
		//color and blending state changes are popped out by layer classes
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		String srcFactorString = reverse.get(this.srcFactor);
		String dstFactorString = reverse.get(this.dstFactor);
		String equationString = reverse.get(this.equation);
		return DEFINITION_STRING + "(" +
				(srcFactorString != null ? srcFactorString : this.srcFactor) + "," +
				(dstFactorString != null ? dstFactorString : this.dstFactor) + "," +
				(equationString != null ? equationString : this.equation) + "," +
				(alpha[0] ? ALPHA_KEYWORD : color[0]) + "," +
				(alpha[1] ? ALPHA_KEYWORD : color[1]) + "," +
				(alpha[2] ? ALPHA_KEYWORD : color[2]) + "," +
				(alpha[3] ? ALPHA_KEYWORD : color[3]) + ")";
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		String lower = definition.toLowerCase();
		if (lower.startsWith(DEFINITION_STRING.toLowerCase()))
		{
			String srcFactor = null, dstFactor = null, equation = null, c0 = null, c1 = null, c2 = null, c3 = null;

			Pattern pattern1 =
					Pattern.compile("(?:\\(([\\w]+),([\\w]+),([\\w]+),([\\w.-]+),([\\w.-]+),([\\w.-]+),([\\w.-]+)\\))");
			Pattern pattern2 =
					Pattern.compile("(?:\\(([\\w]+),([\\w]+),([\\w.-]+),([\\w.-]+),([\\w.-]+),([\\w.-]+)\\))");
			Pattern pattern3 =
					Pattern.compile("(?:\\(([\\w]+),([\\w]+)\\))");

			Matcher matcher;
			if ((matcher = pattern1.matcher(lower)).find())
			{
				srcFactor = matcher.group(1);
				dstFactor = matcher.group(2);
				equation = matcher.group(3);
				c0 = matcher.group(4);
				c1 = matcher.group(5);
				c2 = matcher.group(6);
				c3 = matcher.group(7);
			}
			else if ((matcher = pattern2.matcher(lower)).find())
			{
				srcFactor = matcher.group(1);
				dstFactor = matcher.group(2);
				c0 = matcher.group(3);
				c1 = matcher.group(4);
				c2 = matcher.group(5);
				c3 = matcher.group(6);
			}
			else if ((matcher = pattern3.matcher(lower)).find())
			{
				srcFactor = matcher.group(1);
				dstFactor = matcher.group(2);
			}

			int sf = stringToConstant(srcFactor, GL2.GL_SRC_ALPHA);
			int df = stringToConstant(dstFactor, GL2.GL_ONE_MINUS_SRC_ALPHA);
			int eq = stringToConstant(equation, GL2.GL_FUNC_ADD);
			double[] c = new double[] {
					stringToColor(c0, 1.0),
					stringToColor(c1, 1.0),
					stringToColor(c2, 1.0),
					stringToColor(c3, 1.0)
			};
			boolean[] a = new boolean[] {
					stringToAlpha(c0, false),
					stringToAlpha(c1, false),
					stringToAlpha(c2, false),
					stringToAlpha(c3, true)
			};
			return new BlendingRenderDelegate(sf, df, eq, c, a);
		}
		return null;
	}

	private int stringToConstant(String s, int def)
	{
		if (s == null)
		{
			return def;
		}
		if (constants.containsKey(s))
		{
			return constants.get(s);
		}
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return def;
		}
	}

	private double stringToColor(String s, double def)
	{
		try
		{
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			return def;
		}
	}

	private boolean stringToAlpha(String s, boolean def)
	{
		return s == null ? def : ALPHA_KEYWORD.equals(s);
	}
}
