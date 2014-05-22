//
// Atmospheric scattering fragment shader
//
// Author: Sean O'Neil
//
// Copyright (c) 2004 Sean O'Neil
//
// Slight modifications by Michael de Hoog
//

uniform float fExposure;

void main (void)
{
	gl_FragColor = (gl_Color + 0.25 * gl_SecondaryColor) * 0.5;
	gl_FragColor.rgb = 1.0 - exp(-fExposure * gl_FragColor.rgb);
}
