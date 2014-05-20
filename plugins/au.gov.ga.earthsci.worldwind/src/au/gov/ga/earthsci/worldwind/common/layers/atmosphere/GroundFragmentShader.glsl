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
	gl_FragColor = gl_Color + 0.25 * gl_SecondaryColor;
	gl_FragColor.a = pow(gl_FragColor.b, 0.5) * 0.3;
	gl_FragColor.rgb = 1.0 - exp(-fExposure * gl_FragColor.rgb);
}
