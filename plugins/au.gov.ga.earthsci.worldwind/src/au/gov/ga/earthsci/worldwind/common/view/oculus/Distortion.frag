uniform sampler2D texture0;

varying float oVignette;

void main()
{
    float r = texture2D(texture0, gl_TexCoord[0].st).r;
    float g = texture2D(texture0, gl_TexCoord[1].st).g;
    float b = texture2D(texture0, gl_TexCoord[2].st).b;

    gl_FragColor = vec4(r, g, b, 1.0) * oVignette;
}