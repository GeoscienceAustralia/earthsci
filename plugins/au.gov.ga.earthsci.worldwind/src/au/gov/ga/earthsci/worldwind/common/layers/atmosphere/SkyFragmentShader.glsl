//
// Atmospheric scattering fragment shader
//
// Author: Sean O'Neil
//
// Copyright (c) 2004 Sean O'Neil
//
// Slight modifications by Michael de Hoog
//

uniform vec3 v3LightPos;
uniform float g;
uniform float g2;
uniform sampler2D texture;
uniform float fExposure;

varying vec3 v3Direction;

// Mie phase function
float getMiePhase(float fCos, float fCos2)
{
	return 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos2) / pow(1.0 + g2 - 2.0*g*fCos, 1.5);
}

// Rayleigh phase function
float getRayleighPhase(float fCos2)
{
	//return 0.75 + 0.75 * fCos2;
	return 0.75 * (2.0 + 0.5 * fCos2);
}

void main (void)
{
	float fCos = dot(v3LightPos, v3Direction) / length(v3Direction);
	float fCos2 = fCos * fCos;
	float fMiePhase = getMiePhase(fCos, fCos2);
	float fRayleighPhase = getRayleighPhase(fCos2);
	gl_FragColor = fRayleighPhase * gl_Color + fMiePhase * gl_SecondaryColor;
	gl_FragColor.a = pow(gl_FragColor.b, 0.5);
	gl_FragColor.rgb = 1.0 - exp(-fExposure * gl_FragColor.rgb);
}
