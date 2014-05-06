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
package au.gov.ga.earthsci.worldwind.common.layers.stars;

import java.awt.Color;

/**
 * Helper class that maps star B-V magnitude and star type/class to an RGB
 * color.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StarColor
{
	/**
	 * Convert a star's B-V magnitude to RGB.
	 * <p/>
	 * Used function from here: <a
	 * href="http://stackoverflow.com/a/22630970">Spektre's Stack Overflow
	 * answer</a>.
	 * 
	 * @param bv
	 *            Star's B-V magnitude
	 * @return Star color
	 */
	public static Color bvToColor(double bv)
	{
		double t;
		double r = 0, g = 0, b = 0;
		bv = Math.max(-0.4, Math.min(2.0, bv));
		if ((bv >= -0.40) && (bv < 0.00))
		{
			t = (bv + 0.40) / (0.00 + 0.40);
			r = 0.61 + (0.11 * t) + (0.1 * t * t);
		}
		else if ((bv >= 0.00) && (bv < 0.40))
		{
			t = (bv - 0.00) / (0.40 - 0.00);
			r = 0.83 + (0.17 * t);
		}
		else if ((bv >= 0.40) && (bv < 2.10))
		{
			t = (bv - 0.40) / (2.10 - 0.40);
			r = 1.00;
		}
		if ((bv >= -0.40) && (bv < 0.00))
		{
			t = (bv + 0.40) / (0.00 + 0.40);
			g = 0.70 + (0.07 * t) + (0.1 * t * t);
		}
		else if ((bv >= 0.00) && (bv < 0.40))
		{
			t = (bv - 0.00) / (0.40 - 0.00);
			g = 0.87 + (0.11 * t);
		}
		else if ((bv >= 0.40) && (bv < 1.60))
		{
			t = (bv - 0.40) / (1.60 - 0.40);
			g = 0.98 - (0.16 * t);
		}
		else if ((bv >= 1.60) && (bv < 2.00))
		{
			t = (bv - 1.60) / (2.00 - 1.60);
			g = 0.82 - (0.5 * t * t);
		}
		if ((bv >= -0.40) && (bv < 0.40))
		{
			t = (bv + 0.40) / (0.40 + 0.40);
			b = 1.00;
		}
		else if ((bv >= 0.40) && (bv < 1.50))
		{
			t = (bv - 0.40) / (1.50 - 0.40);
			b = 1.00 - (0.47 * t) + (0.1 * t * t);
		}
		else if ((bv >= 1.50) && (bv < 1.94))
		{
			t = (bv - 1.50) / (1.94 - 1.50);
			b = 0.63 - (0.6 * t * t);
		}
		return new Color((float) r, (float) g, (float) b);
	}

	/**
	 * Convert the star type/class to an RGB color.
	 * <p/>
	 * Lookup table from here: <a href=
	 * "http://www.vendian.org/mncharity/dir3/starcolor/UnstableURLs/starcolors.html"
	 * >http://www.vendian.org/</a>
	 * 
	 * @param type
	 *            Star type (eg G4)
	 * @param classs
	 *            Star class (eg IV)
	 * @return Star color
	 */
	@SuppressWarnings("nls")
	public static Color typeClassToColor(String type, String classs)
	{
		if ("O5".equals(type) && "V".equals(classs))
		{
			return new Color(155, 176, 255);
		}
		if ("O6".equals(type) && "V".equals(classs))
		{
			return new Color(162, 184, 255);
		}
		if ("O7".equals(type) && "V".equals(classs))
		{
			return new Color(157, 177, 255);
		}
		if ("O8".equals(type) && "V".equals(classs))
		{
			return new Color(157, 177, 255);
		}
		if ("O9".equals(type) && "V".equals(classs))
		{
			return new Color(154, 178, 255);
		}
		if ("O9.5".equals(type) && "V".equals(classs))
		{
			return new Color(164, 186, 255);
		}
		if ("B0".equals(type) && "V".equals(classs))
		{
			return new Color(156, 178, 255);
		}
		if ("B0.5".equals(type) && "V".equals(classs))
		{
			return new Color(167, 188, 255);
		}
		if ("B1".equals(type) && "V".equals(classs))
		{
			return new Color(160, 182, 255);
		}
		if ("B2".equals(type) && "V".equals(classs))
		{
			return new Color(160, 180, 255);
		}
		if ("B3".equals(type) && "V".equals(classs))
		{
			return new Color(165, 185, 255);
		}
		if ("B4".equals(type) && "V".equals(classs))
		{
			return new Color(164, 184, 255);
		}
		if ("B5".equals(type) && "V".equals(classs))
		{
			return new Color(170, 191, 255);
		}
		if ("B6".equals(type) && "V".equals(classs))
		{
			return new Color(172, 189, 255);
		}
		if ("B7".equals(type) && "V".equals(classs))
		{
			return new Color(173, 191, 255);
		}
		if ("B8".equals(type) && "V".equals(classs))
		{
			return new Color(177, 195, 255);
		}
		if ("B9".equals(type) && "V".equals(classs))
		{
			return new Color(181, 198, 255);
		}
		if ("B9.5".equals(type) && "V".equals(classs))
		{
			//added, interpolated between above and below
			return new Color(183, 199, 255);
		}
		if ("A0".equals(type) && "V".equals(classs))
		{
			return new Color(185, 201, 255);
		}
		if ("A1".equals(type) && "V".equals(classs))
		{
			return new Color(181, 199, 255);
		}
		if ("A2".equals(type) && "V".equals(classs))
		{
			return new Color(187, 203, 255);
		}
		if ("A3".equals(type) && "V".equals(classs))
		{
			return new Color(191, 207, 255);
		}
		if ("A5".equals(type) && "V".equals(classs))
		{
			return new Color(202, 215, 255);
		}
		if ("A6".equals(type) && "V".equals(classs))
		{
			return new Color(199, 212, 255);
		}
		if ("A7".equals(type) && "V".equals(classs))
		{
			return new Color(200, 213, 255);
		}
		if ("A8".equals(type) && "V".equals(classs))
		{
			return new Color(213, 222, 255);
		}
		if ("A9".equals(type) && "V".equals(classs))
		{
			return new Color(219, 224, 255);
		}
		if ("F0".equals(type) && "V".equals(classs))
		{
			return new Color(224, 229, 255);
		}
		if ("F1".equals(type) && "V".equals(classs))
		{
			//added, interpolated between above and below
			return new Color(230, 234, 255);
		}
		if ("F2".equals(type) && "V".equals(classs))
		{
			return new Color(236, 239, 255);
		}
		if ("F4".equals(type) && "V".equals(classs))
		{
			return new Color(224, 226, 255);
		}
		if ("F5".equals(type) && "V".equals(classs))
		{
			return new Color(248, 247, 255);
		}
		if ("F6".equals(type) && "V".equals(classs))
		{
			return new Color(244, 241, 255);
		}
		if ("F7".equals(type) && "V".equals(classs))
		{
			return new Color(246, 243, 255);
		}
		if ("F8".equals(type) && "V".equals(classs))
		{
			return new Color(255, 247, 252);
		}
		if ("F9".equals(type) && "V".equals(classs))
		{
			return new Color(255, 247, 252);
		}
		if ("G0".equals(type) && "V".equals(classs))
		{
			return new Color(255, 248, 252);
		}
		if ("G1".equals(type) && "V".equals(classs))
		{
			return new Color(255, 247, 248);
		}
		if ("G2".equals(type) && "V".equals(classs))
		{
			return new Color(255, 245, 242);
		}
		if ("G4".equals(type) && "V".equals(classs))
		{
			return new Color(255, 241, 229);
		}
		if ("G5".equals(type) && "V".equals(classs))
		{
			return new Color(255, 244, 234);
		}
		if ("G6".equals(type) && "V".equals(classs))
		{
			return new Color(255, 244, 235);
		}
		if ("G7".equals(type) && "V".equals(classs))
		{
			return new Color(255, 244, 235);
		}
		if ("G8".equals(type) && "V".equals(classs))
		{
			return new Color(255, 237, 222);
		}
		if ("G9".equals(type) && "V".equals(classs))
		{
			return new Color(255, 239, 221);
		}
		if ("K0".equals(type) && "V".equals(classs))
		{
			return new Color(255, 238, 221);
		}
		if ("K1".equals(type) && "V".equals(classs))
		{
			return new Color(255, 224, 188);
		}
		if ("K2".equals(type) && "V".equals(classs))
		{
			return new Color(255, 227, 196);
		}
		if ("K3".equals(type) && "V".equals(classs))
		{
			return new Color(255, 222, 195);
		}
		if ("K4".equals(type) && "V".equals(classs))
		{
			return new Color(255, 216, 181);
		}
		if ("K5".equals(type) && "V".equals(classs))
		{
			return new Color(255, 210, 161);
		}
		if ("K7".equals(type) && "V".equals(classs))
		{
			return new Color(255, 199, 142);
		}
		if ("K8".equals(type) && "V".equals(classs))
		{
			return new Color(255, 209, 174);
		}
		if ("M0".equals(type) && "V".equals(classs))
		{
			return new Color(255, 195, 139);
		}
		if ("M1".equals(type) && "V".equals(classs))
		{
			return new Color(255, 204, 142);
		}
		if ("M2".equals(type) && "V".equals(classs))
		{
			return new Color(255, 196, 131);
		}
		if ("M3".equals(type) && "V".equals(classs))
		{
			return new Color(255, 206, 129);
		}
		if ("M4".equals(type) && "V".equals(classs))
		{
			return new Color(255, 201, 127);
		}
		if ("M5".equals(type) && "V".equals(classs))
		{
			return new Color(255, 204, 111);
		}
		if ("M6".equals(type) && "V".equals(classs))
		{
			return new Color(255, 195, 112);
		}
		if ("M8".equals(type) && "V".equals(classs))
		{
			return new Color(255, 198, 109);
		}
		if ("B1".equals(type) && "IV".equals(classs))
		{
			return new Color(157, 180, 255);
		}
		if ("B2".equals(type) && "IV".equals(classs))
		{
			return new Color(159, 179, 255);
		}
		if ("B3".equals(type) && "IV".equals(classs))
		{
			return new Color(166, 188, 255);
		}
		if ("B6".equals(type) && "IV".equals(classs))
		{
			return new Color(175, 194, 255);
		}
		if ("B7".equals(type) && "IV".equals(classs))
		{
			return new Color(170, 189, 255);
		}
		if ("B9".equals(type) && "IV".equals(classs))
		{
			return new Color(180, 197, 255);
		}
		if ("A0".equals(type) && "IV".equals(classs))
		{
			return new Color(179, 197, 255);
		}
		if ("A3".equals(type) && "IV".equals(classs))
		{
			return new Color(190, 205, 255);
		}
		if ("A4".equals(type) && "IV".equals(classs))
		{
			return new Color(195, 210, 255);
		}
		if ("A5".equals(type) && "IV".equals(classs))
		{
			return new Color(212, 220, 255);
		}
		if ("A7".equals(type) && "IV".equals(classs))
		{
			return new Color(192, 207, 255);
		}
		if ("A9".equals(type) && "IV".equals(classs))
		{
			return new Color(224, 227, 255);
		}
		if ("F0".equals(type) && "IV".equals(classs))
		{
			return new Color(218, 224, 255);
		}
		if ("F2".equals(type) && "IV".equals(classs))
		{
			return new Color(227, 230, 255);
		}
		if ("F3".equals(type) && "IV".equals(classs))
		{
			return new Color(227, 230, 255);
		}
		if ("F5".equals(type) && "IV".equals(classs))
		{
			return new Color(241, 239, 255);
		}
		if ("F7".equals(type) && "IV".equals(classs))
		{
			return new Color(240, 239, 255);
		}
		if ("F8".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 252, 253);
		}
		if ("G0".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 248, 245);
		}
		if ("G2".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 244, 242);
		}
		if ("G3".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 238, 226);
		}
		if ("G4".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 245, 238);
		}
		if ("G5".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 235, 213);
		}
		if ("G6".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 242, 234);
		}
		if ("G7".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 231, 205);
		}
		if ("G8".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 233, 211);
		}
		if ("K0".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 225, 189);
		}
		if ("K1".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 216, 171);
		}
		if ("K2".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 229, 202);
		}
		if ("K3".equals(type) && "IV".equals(classs))
		{
			return new Color(255, 219, 167);
		}
		if ("O7".equals(type) && "III".equals(classs))
		{
			return new Color(158, 177, 255);
		}
		if ("O8".equals(type) && "III".equals(classs))
		{
			return new Color(157, 178, 255);
		}
		if ("O9".equals(type) && "III".equals(classs))
		{
			return new Color(158, 177, 255);
		}
		if ("B0".equals(type) && "III".equals(classs))
		{
			return new Color(158, 177, 255);
		}
		if ("B1".equals(type) && "III".equals(classs))
		{
			return new Color(158, 177, 255);
		}
		if ("B2".equals(type) && "III".equals(classs))
		{
			return new Color(159, 180, 255);
		}
		if ("B3".equals(type) && "III".equals(classs))
		{
			return new Color(163, 187, 255);
		}
		if ("B5".equals(type) && "III".equals(classs))
		{
			return new Color(168, 189, 255);
		}
		if ("B7".equals(type) && "III".equals(classs))
		{
			return new Color(171, 191, 255);
		}
		if ("B9".equals(type) && "III".equals(classs))
		{
			return new Color(178, 195, 255);
		}
		if ("A0".equals(type) && "III".equals(classs))
		{
			return new Color(188, 205, 255);
		}
		if ("A3".equals(type) && "III".equals(classs))
		{
			return new Color(189, 203, 255);
		}
		if ("A5".equals(type) && "III".equals(classs))
		{
			return new Color(202, 215, 255);
		}
		if ("A6".equals(type) && "III".equals(classs))
		{
			return new Color(209, 219, 255);
		}
		if ("A7".equals(type) && "III".equals(classs))
		{
			return new Color(210, 219, 255);
		}
		if ("A8".equals(type) && "III".equals(classs))
		{
			return new Color(209, 219, 255);
		}
		if ("A9".equals(type) && "III".equals(classs))
		{
			return new Color(209, 219, 255);
		}
		if ("F0".equals(type) && "III".equals(classs))
		{
			return new Color(213, 222, 255);
		}
		if ("F2".equals(type) && "III".equals(classs))
		{
			return new Color(241, 241, 255);
		}
		if ("F4".equals(type) && "III".equals(classs))
		{
			return new Color(241, 240, 255);
		}
		if ("F5".equals(type) && "III".equals(classs))
		{
			return new Color(242, 240, 255);
		}
		if ("F6".equals(type) && "III".equals(classs))
		{
			return new Color(241, 240, 255);
		}
		if ("F7".equals(type) && "III".equals(classs))
		{
			return new Color(241, 240, 255);
		}
		if ("G0".equals(type) && "III".equals(classs))
		{
			return new Color(255, 242, 233);
		}
		if ("G1".equals(type) && "III".equals(classs))
		{
			return new Color(255, 243, 233);
		}
		if ("G2".equals(type) && "III".equals(classs))
		{
			return new Color(255, 243, 233);
		}
		if ("G3".equals(type) && "III".equals(classs))
		{
			return new Color(255, 243, 233);
		}
		if ("G4".equals(type) && "III".equals(classs))
		{
			return new Color(255, 243, 233);
		}
		if ("G5".equals(type) && "III".equals(classs))
		{
			return new Color(255, 236, 211);
		}
		if ("G6".equals(type) && "III".equals(classs))
		{
			return new Color(255, 236, 215);
		}
		if ("G8".equals(type) && "III".equals(classs))
		{
			return new Color(255, 231, 199);
		}
		if ("G9".equals(type) && "III".equals(classs))
		{
			return new Color(255, 231, 196);
		}
		if ("K0".equals(type) && "III".equals(classs))
		{
			return new Color(255, 227, 190);
		}
		if ("K1".equals(type) && "III".equals(classs))
		{
			return new Color(255, 223, 181);
		}
		if ("K2".equals(type) && "III".equals(classs))
		{
			return new Color(255, 221, 175);
		}
		if ("K3".equals(type) && "III".equals(classs))
		{
			return new Color(255, 216, 167);
		}
		if ("K4".equals(type) && "III".equals(classs))
		{
			return new Color(255, 211, 146);
		}
		if ("K5".equals(type) && "III".equals(classs))
		{
			return new Color(255, 204, 138);
		}
		if ("K7".equals(type) && "III".equals(classs))
		{
			return new Color(255, 208, 142);
		}
		if ("M0".equals(type) && "III".equals(classs))
		{
			return new Color(255, 203, 132);
		}
		if ("M1".equals(type) && "III".equals(classs))
		{
			return new Color(255, 200, 121);
		}
		if ("M2".equals(type) && "III".equals(classs))
		{
			return new Color(255, 198, 118);
		}
		if ("M3".equals(type) && "III".equals(classs))
		{
			return new Color(255, 200, 119);
		}
		if ("M4".equals(type) && "III".equals(classs))
		{
			return new Color(255, 206, 127);
		}
		if ("M5".equals(type) && "III".equals(classs))
		{
			return new Color(255, 197, 124);
		}
		if ("M6".equals(type) && "III".equals(classs))
		{
			return new Color(255, 178, 121);
		}
		if ("M7".equals(type) && "III".equals(classs))
		{
			return new Color(255, 165, 97);
		}
		if ("M8".equals(type) && "III".equals(classs))
		{
			return new Color(255, 167, 97);
		}
		if ("M9".equals(type) && "III".equals(classs))
		{
			return new Color(255, 233, 154);
		}
		if ("B2".equals(type) && "II".equals(classs))
		{
			return new Color(165, 192, 255);
		}
		if ("B5".equals(type) && "II".equals(classs))
		{
			return new Color(175, 195, 255);
		}
		if ("F0".equals(type) && "II".equals(classs))
		{
			return new Color(203, 217, 255);
		}
		if ("F2".equals(type) && "II".equals(classs))
		{
			return new Color(229, 233, 255);
		}
		if ("G5".equals(type) && "II".equals(classs))
		{
			return new Color(255, 235, 203);
		}
		if ("M3".equals(type) && "II".equals(classs))
		{
			return new Color(255, 201, 119);
		}
		if ("O9".equals(type) && "I".equals(classs))
		{
			return new Color(164, 185, 255);
		}
		if ("B0".equals(type) && "I".equals(classs))
		{
			return new Color(161, 189, 255);
		}
		if ("B1".equals(type) && "I".equals(classs))
		{
			return new Color(168, 193, 255);
		}
		if ("B2".equals(type) && "I".equals(classs))
		{
			return new Color(177, 196, 255);
		}
		if ("B3".equals(type) && "I".equals(classs))
		{
			return new Color(175, 194, 255);
		}
		if ("B4".equals(type) && "I".equals(classs))
		{
			return new Color(187, 203, 255);
		}
		if ("B5".equals(type) && "I".equals(classs))
		{
			return new Color(179, 202, 255);
		}
		if ("B6".equals(type) && "I".equals(classs))
		{
			return new Color(191, 207, 255);
		}
		if ("B7".equals(type) && "I".equals(classs))
		{
			return new Color(195, 209, 255);
		}
		if ("B8".equals(type) && "I".equals(classs))
		{
			return new Color(182, 206, 255);
		}
		if ("B9".equals(type) && "I".equals(classs))
		{
			return new Color(204, 216, 255);
		}
		if ("A0".equals(type) && "I".equals(classs))
		{
			return new Color(187, 206, 255);
		}
		if ("A1".equals(type) && "I".equals(classs))
		{
			return new Color(214, 223, 255);
		}
		if ("A2".equals(type) && "I".equals(classs))
		{
			return new Color(199, 214, 255);
		}
		if ("A5".equals(type) && "I".equals(classs))
		{
			return new Color(223, 229, 255);
		}
		if ("F0".equals(type) && "I".equals(classs))
		{
			return new Color(202, 215, 255);
		}
		if ("F2".equals(type) && "I".equals(classs))
		{
			return new Color(244, 243, 255);
		}
		if ("F5".equals(type) && "I".equals(classs))
		{
			return new Color(219, 225, 255);
		}
		if ("F8".equals(type) && "I".equals(classs))
		{
			return new Color(255, 252, 247);
		}
		if ("G0".equals(type) && "I".equals(classs))
		{
			return new Color(255, 239, 219);
		}
		if ("G2".equals(type) && "I".equals(classs))
		{
			return new Color(255, 236, 205);
		}
		if ("G3".equals(type) && "I".equals(classs))
		{
			return new Color(255, 231, 203);
		}
		if ("G5".equals(type) && "I".equals(classs))
		{
			return new Color(255, 230, 183);
		}
		if ("G8".equals(type) && "I".equals(classs))
		{
			return new Color(255, 220, 167);
		}
		if ("K0".equals(type) && "I".equals(classs))
		{
			return new Color(255, 221, 181);
		}
		if ("K1".equals(type) && "I".equals(classs))
		{
			return new Color(255, 220, 177);
		}
		if ("K2".equals(type) && "I".equals(classs))
		{
			return new Color(255, 211, 135);
		}
		if ("K3".equals(type) && "I".equals(classs))
		{
			return new Color(255, 204, 128);
		}
		if ("K4".equals(type) && "I".equals(classs))
		{
			return new Color(255, 201, 118);
		}
		if ("K5".equals(type) && "I".equals(classs))
		{
			return new Color(255, 209, 154);
		}
		if ("M0".equals(type) && "I".equals(classs))
		{
			return new Color(255, 204, 143);
		}
		if ("M1".equals(type) && "I".equals(classs))
		{
			return new Color(255, 202, 138);
		}
		if ("M2".equals(type) && "I".equals(classs))
		{
			return new Color(255, 193, 104);
		}
		if ("M3".equals(type) && "I".equals(classs))
		{
			return new Color(255, 192, 118);
		}
		if ("M4".equals(type) && "I".equals(classs))
		{
			return new Color(255, 185, 104);
		}
		return null;
	}

}
