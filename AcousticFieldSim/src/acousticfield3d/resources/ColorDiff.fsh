varying vec4 normal;
varying vec4 wPos;

uniform vec4 colorMod;
uniform vec4 lightPos;

uniform float ambient;
uniform float diffuse;

void main()
{
    vec3 N = normalize(normal.xyz);
    vec3 L = normalize(lightPos.xyz - wPos.xyz);
	
    float lambertTerm = abs( dot(N,L) );

    gl_FragColor = (ambient + diffuse * lambertTerm) * colorMod;
    gl_FragColor.w = colorMod.w;
}