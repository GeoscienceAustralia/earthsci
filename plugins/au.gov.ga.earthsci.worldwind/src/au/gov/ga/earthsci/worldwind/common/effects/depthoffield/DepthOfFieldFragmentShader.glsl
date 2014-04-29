//see http://developer.amd.com/media/gpu_assets/Scheuermann_DepthOfField.pdf

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
uniform sampler2D blurTexture;
uniform float cameraNear;
uniform float cameraFar;
uniform float focalLength;
uniform vec2 pixelSize;
uniform float blurTextureScale;

#define NUM_TAPS 12

const vec2 poisson_old[NUM_TAPS] =
{
	{-.326,-.406}, {-.840,-.074}, {-.696, .457}, {-.203, .621}, { .962,-.195}, { .473,-.480},
	{ .519, .767}, { .185,-.893}, { .507, .064}, { .896, .412}, {-.322,-.933}, {-.792,-.598}
};

const vec2 poisson[NUM_TAPS] =
{
	{ 0.00,  0.00}, { 0.07, -0.45}, {-0.15, -0.33}, { 0.35, -0.32}, {-0.39, -0.26}, { 0.10, -0.23},
	{ 0.36, -0.12}, {-0.31, -0.01}, {-0.38,  0.22}, { 0.36,  0.23}, {-0.13,  0.29}, { 0.14,  0.41}
};

const float maxCoCRadius = 5.0; //max circle of confusion radius
const float radiusScale = 0.4; //scale factor for max CoC size on blur image

float LinearizeDepth(float depth)
{
	return (cameraFar * cameraNear) /
		((cameraNear - cameraFar) * (depth - cameraFar / (cameraFar - cameraNear)));
}

float NormalizeDepth(float depth)
{
	float blur;
	if(depth < focalLength)
	{
		blur = (depth - focalLength) / (focalLength - cameraNear);
	}
	else
	{
		blur = (depth - focalLength) / (cameraFar - focalLength);
	}
	return clamp(blur, -1.0, 1.0);
}

void main()
{
	vec2 texCoord = gl_TexCoord[0].st;
	float unnormalizedDepth = texture2D(depthTexture, texCoord).r;
	float depth = NormalizeDepth(LinearizeDepth(unnormalizedDepth));
	
	//vec4 color = texture2D(colorTexture, gl_TexCoord[0].st);
	//vec4 blurc = texture2D(blurTexture, gl_TexCoord[0].st);
	//gl_FragColor = mix(color, blurc, depth);
	
	float centerDepth = depth * 0.5 + 0.5; //[-1..1] -> [0..1]
	float discRadiusHigh = abs(centerDepth * maxCoCRadius * 2.0 - maxCoCRadius);
	float discRadiusLow = discRadiusHigh * radiusScale;
	
	vec4 colorAccum = vec4(0.0);
	
	for(int t = 0; t < NUM_TAPS; t++)
	{
		vec2 coordLow = texCoord + pixelSize * blurTextureScale * poisson[t] * discRadiusLow;
		vec2 coordHigh = texCoord + pixelSize * poisson[t] * discRadiusHigh;
		
		vec4 tapLow = texture2D(blurTexture, coordLow);
		vec4 tapHigh = texture2D(colorTexture, coordHigh);
		
		float tapDepth = NormalizeDepth(LinearizeDepth(texture2D(depthTexture, coordHigh).r));
		float tapBlur = abs(tapDepth);
		vec4 tap = mix(tapHigh, tapLow, tapBlur);
		
		float factor = 1.0;
		if(tapDepth < centerDepth)
		{
			factor = tapDepth;
		}
		
		colorAccum.rgb += tap.rgb * factor;
		colorAccum.a += factor;
	}
	
	gl_FragColor = colorAccum / colorAccum.a;
	gl_FragDepth = unnormalizedDepth;
}
