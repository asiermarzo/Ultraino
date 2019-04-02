
varying vec4 normal;
varying vec4 wPos;

uniform vec4 colorMod;
uniform vec4 lightPos;
uniform vec4 eyePos;

uniform float ambient;
uniform float diffuse;
uniform float specular;
uniform float shininess;

void main()
{                    
    vec3 N = normalize(normal.xyz);
    vec3 L = normalize(lightPos.xyz - wPos.xyz);
    vec3 E = normalize(eyePos.xyz - wPos.xyz);
    vec3 HV = normalize(L + E);

    float lambertTerm = abs( dot(N,L) );
    float specularTerm = pow( abs( dot(N, HV) ), shininess);
    
    vec4 textureColor = colorMod;
    gl_FragColor = (ambient + diffuse * lambertTerm) * textureColor + specularTerm * specular * vec4(1.0);			    
}
