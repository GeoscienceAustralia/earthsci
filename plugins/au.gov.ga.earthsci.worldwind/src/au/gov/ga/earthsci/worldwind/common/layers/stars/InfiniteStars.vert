const float minMag = -1.4600000381469727;
const float maxMag = 7.960000038146973;

void main(void)
{
	gl_TexCoord[0] = gl_MultiTexCoord0;
	gl_Position = gl_ModelViewProjectionMatrix * vec4(gl_Vertex.xyz, 1.0);
	
	float normalizedMagnitude = (gl_Vertex.w - minMag) / (maxMag - minMag);
	float size = pow(normalizedMagnitude, 10.0);
	gl_PointSize = (10.0 + size * 50.0);
	gl_FrontColor = pow(gl_Color, 0.5) * size * 5.0;
}
