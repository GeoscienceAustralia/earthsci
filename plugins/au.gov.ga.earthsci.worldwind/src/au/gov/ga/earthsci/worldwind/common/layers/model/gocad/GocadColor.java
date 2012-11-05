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
package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper enum that converts GOCAD "*color:" lines to a Java Color. The enum
 * constants list contain all the HTML color names, which the GOCAD colors seem
 * to match.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum GocadColor
{
	AliceBlue(0xF0F8FF),
	AntiqueWhite(0xFAEBD7),
	Aqua(0x00FFFF),
	Aquamarine(0x7FFFD4),
	Azure(0xF0FFFF),
	Beige(0xF5F5DC),
	Bisque(0xFFE4C4),
	Black(0x000000),
	BlanchedAlmond(0xFFEBCD),
	Blue(0x0000FF),
	BlueViolet(0x8A2BE2),
	Brown(0xA52A2A),
	BurlyWood(0xDEB887),
	CadetBlue(0x5F9EA0),
	Chartreuse(0x7FFF00),
	Chocolate(0xD2691E),
	Coral(0xFF7F50),
	CornflowerBlue(0x6495ED),
	Cornsilk(0xFFF8DC),
	Crimson(0xDC143C),
	Cyan(0x00FFFF),
	DarkBlue(0x00008B),
	DarkCyan(0x008B8B),
	DarkGoldenRod(0xB8860B),
	DarkGray(0xA9A9A9),
	DarkGrey(0xA9A9A9),
	DarkGreen(0x006400),
	DarkKhaki(0xBDB76B),
	DarkMagenta(0x8B008B),
	DarkOliveGreen(0x556B2F),
	Darkorange(0xFF8C00),
	DarkOrchid(0x9932CC),
	DarkRed(0x8B0000),
	DarkSalmon(0xE9967A),
	DarkSeaGreen(0x8FBC8F),
	DarkSlateBlue(0x483D8B),
	DarkSlateGray(0x2F4F4F),
	DarkSlateGrey(0x2F4F4F),
	DarkTurquoise(0x00CED1),
	DarkViolet(0x9400D3),
	DeepPink(0xFF1493),
	DeepSkyBlue(0x00BFFF),
	DimGray(0x696969),
	DimGrey(0x696969),
	DodgerBlue(0x1E90FF),
	FireBrick(0xB22222),
	FloralWhite(0xFFFAF0),
	ForestGreen(0x228B22),
	Fuchsia(0xFF00FF),
	Gainsboro(0xDCDCDC),
	GhostWhite(0xF8F8FF),
	Gold(0xFFD700),
	GoldenRod(0xDAA520),
	Gray(0x808080),
	Grey(0x808080),
	Green(0x008000),
	GreenYellow(0xADFF2F),
	HoneyDew(0xF0FFF0),
	HotPink(0xFF69B4),
	IndianRed(0xCD5C5C),
	Indigo(0x4B0082),
	Ivory(0xFFFFF0),
	Khaki(0xF0E68C),
	Lavender(0xE6E6FA),
	LavenderBlush(0xFFF0F5),
	LawnGreen(0x7CFC00),
	LemonChiffon(0xFFFACD),
	LightBlue(0xADD8E6),
	LightCoral(0xF08080),
	LightCyan(0xE0FFFF),
	LightGoldenRodYellow(0xFAFAD2),
	LightGray(0xD3D3D3),
	LightGrey(0xD3D3D3),
	LightGreen(0x90EE90),
	LightPink(0xFFB6C1),
	LightSalmon(0xFFA07A),
	LightSeaGreen(0x20B2AA),
	LightSkyBlue(0x87CEFA),
	LightSlateGray(0x778899),
	LightSlateGrey(0x778899),
	LightSteelBlue(0xB0C4DE),
	LightYellow(0xFFFFE0),
	Lime(0x00FF00),
	LimeGreen(0x32CD32),
	Linen(0xFAF0E6),
	Magenta(0xFF00FF),
	Maroon(0x800000),
	MediumAquaMarine(0x66CDAA),
	MediumBlue(0x0000CD),
	MediumOrchid(0xBA55D3),
	MediumPurple(0x9370D8),
	MediumSeaGreen(0x3CB371),
	MediumSlateBlue(0x7B68EE),
	MediumSpringGreen(0x00FA9A),
	MediumTurquoise(0x48D1CC),
	MediumVioletRed(0xC71585),
	MidnightBlue(0x191970),
	MintCream(0xF5FFFA),
	MistyRose(0xFFE4E1),
	Moccasin(0xFFE4B5),
	NavajoWhite(0xFFDEAD),
	Navy(0x000080),
	OldLace(0xFDF5E6),
	Olive(0x808000),
	OliveDrab(0x6B8E23),
	Orange(0xFFA500),
	OrangeRed(0xFF4500),
	Orchid(0xDA70D6),
	PaleGoldenRod(0xEEE8AA),
	PaleGreen(0x98FB98),
	PaleTurquoise(0xAFEEEE),
	PaleVioletRed(0xD87093),
	PapayaWhip(0xFFEFD5),
	PeachPuff(0xFFDAB9),
	Peru(0xCD853F),
	Pink(0xFFC0CB),
	Plum(0xDDA0DD),
	PowderBlue(0xB0E0E6),
	Purple(0x800080),
	Red(0xFF0000),
	RosyBrown(0xBC8F8F),
	RoyalBlue(0x4169E1),
	SaddleBrown(0x8B4513),
	Salmon(0xFA8072),
	SandyBrown(0xF4A460),
	SeaGreen(0x2E8B57),
	SeaShell(0xFFF5EE),
	Sienna(0xA0522D),
	Silver(0xC0C0C0),
	SkyBlue(0x87CEEB),
	SlateBlue(0x6A5ACD),
	SlateGray(0x708090),
	SlateGrey(0x708090),
	Snow(0xFFFAFA),
	SpringGreen(0x00FF7F),
	SteelBlue(0x4682B4),
	Tan(0xD2B48C),
	Teal(0x008080),
	Thistle(0xD8BFD8),
	Tomato(0xFF6347),
	Turquoise(0x40E0D0),
	Violet(0xEE82EE),
	Wheat(0xF5DEB3),
	White(0xFFFFFF),
	WhiteSmoke(0xF5F5F5),
	Yellow(0xFFFF00),
	YellowGreen(0x9ACD32);

	public final String pretty;
	public final Color color;

	private final static Pattern color4Pattern = Pattern
			.compile("[^:]+:\\s*([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern color3Pattern = Pattern
			.compile("[^:]+:\\s*([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern colorNamePattern = Pattern.compile("[^:]+:(.+)");

	private GocadColor(int hex)
	{
		this.color = new Color(hex);
		this.pretty = splitCamelCase(name());
	}

	private static String splitCamelCase(String s)
	{
		return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}

	private static Map<String, GocadColor> prettyToColor = new HashMap<String, GocadColor>();
	private static Map<String, GocadColor> nameToColor = new HashMap<String, GocadColor>();
	static
	{
		for (GocadColor color : values())
		{
			prettyToColor.put(color.pretty.toLowerCase(), color);
			nameToColor.put(color.name().toLowerCase(), color);
		}
	}

	/**
	 * Convert a GOCAD "*color:" line to a Java {@link Color}. The following
	 * formats are supported:
	 * <ul>
	 * <li>*solid*color:1 0.447059 0.337255 1</li>
	 * <li>*solid*color:1 0.447059 0.337255</li>
	 * <li>*solid*color:dark olive green</li>
	 * <li>*solid*color:darkolivegreen</li>
	 * </ul>
	 * The "*solid*color" part can be replaced by any text that doesn't contain
	 * a ':'.
	 * 
	 * @param gocadLine
	 *            Line of text to convert to a {@link Color}
	 * @return {@link Color} from line
	 */
	public static Color gocadLineToColor(String gocadLine)
	{
		if (gocadLine == null)
		{
			return null;
		}

		Matcher matcher = color4Pattern.matcher(gocadLine);
		if (matcher.matches())
		{
			double r = Double.parseDouble(matcher.group(1));
			double g = Double.parseDouble(matcher.group(2));
			double b = Double.parseDouble(matcher.group(3));
			double a = Double.parseDouble(matcher.group(4));
			return new Color((float) r, (float) g, (float) b, (float) a);
		}

		matcher = color3Pattern.matcher(gocadLine);
		if (matcher.matches())
		{
			double r = Double.parseDouble(matcher.group(1));
			double g = Double.parseDouble(matcher.group(2));
			double b = Double.parseDouble(matcher.group(3));
			return new Color((float) r, (float) g, (float) b, 1.0f);
		}

		matcher = colorNamePattern.matcher(gocadLine);
		if (matcher.matches())
		{
			String name = matcher.group(1).toLowerCase();
			GocadColor gc = prettyToColor.get(name);
			if (gc == null)
			{
				gc = nameToColor.get(name);
			}
			if (gc != null)
			{
				return gc.color;
			}
		}

		return null;
	}
}
