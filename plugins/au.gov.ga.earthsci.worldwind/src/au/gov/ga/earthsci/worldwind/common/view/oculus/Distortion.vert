uniform vec2 eyeToSourceUVscale;
uniform vec2 eyeToSourceUVoffset;
uniform mat4 eyeRotationStart;
uniform mat4 eyeRotationEnd;

varying float oVignette;

vec2 timeWarpTexCoord(vec2 texCoord, mat4 rotMat)
{
    vec3 transformed = (rotMat * vec4(texCoord.xy, 1.0, 1.0)).xyz;
    vec2 flattened = transformed.xy / transformed.z;
    return eyeToSourceUVscale * flattened + eyeToSourceUVoffset;
}

void main()
{
	float timewarpLerpFactor = gl_Vertex.z;
	float vignette = gl_Vertex.w;
	
    mat4 lerpedEyeRot = eyeRotationStart * (1.0 - timewarpLerpFactor) + eyeRotationEnd * timewarpLerpFactor;

    gl_TexCoord[0].st = timeWarpTexCoord(gl_MultiTexCoord0.st, lerpedEyeRot);
    gl_TexCoord[1].st = timeWarpTexCoord(gl_Color.st, lerpedEyeRot);
    gl_TexCoord[2].st = timeWarpTexCoord(gl_Color.pq, lerpedEyeRot);

    gl_Position = vec4(gl_Vertex.xy, 0.5, 1.0);

    oVignette = vignette;
}