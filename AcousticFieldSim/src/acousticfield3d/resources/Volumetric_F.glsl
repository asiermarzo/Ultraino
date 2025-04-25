#define N_TRANS _N_TRANS_
uniform vec3 tPos[N_TRANS];
uniform vec3 tNorm[N_TRANS];
uniform vec4 tSpecs[N_TRANS];

uniform int colouring;
uniform float minNegColor;
uniform float maxNegColor;
uniform float minPosColor;
uniform float maxPosColor;
uniform float isoValue;

uniform vec4 lightPos;
uniform float ambient;
uniform float diffuse;
uniform float specular;
uniform float shininess;
uniform vec4 colorMod;

uniform bool isTimeDomain;
uniform float timestamp;
uniform int renderType; //1 = MIPS, 2 = ISO

uniform vec4 eyePos;
uniform vec3 cubeSize;
uniform float raySteps;
varying vec4 wPos;
varying vec3 cPos;

#define PI 3.1415926535897932384626433832795

//FieldCalcsStencil.glsl
//INCLUDE FieldCalcsStencilNoDir.glsl
//INCLUDE Colouring.glsl

vec3 getNormalAt(vec3 p, float h){
    vec3 n = vec3( length(fieldAt(p - vec3(h,0.0,0.0) )) - length(fieldAt(p + vec3(h,0.0,0.0))),
                   length(fieldAt(p - vec3(0.0,h,0.0) )) - length(fieldAt(p + vec3(0.0,h,0.0))),
                   length(fieldAt(p - vec3(0.0,0.0,h) )) - length(fieldAt(p + vec3(0.0,0.0,h))) );
    return normalize(n);
}

void main(){
    vec3 wDir = normalize( vec3(wPos - eyePos) );
    vec3 cDir = normalize( wDir / cubeSize );
    vec3 cInc = cDir * raySteps;
    vec3 wInc = wDir * raySteps * cubeSize;

    vec3 c = cPos;
    vec3 w = wPos.xyz;
    vec3 ones = vec3(1.0);
    vec3 zeroes = vec3(0.0);

    if (renderType == 1){ //MIPS
        float maxValue = 0.0;
        //while( c.x <= 1.0 && c.y <= 1.0 && c.z <= 1.0 && c.x >= 0.0 && c.y >= 0.0 && c.z >= 0.0 ){
        while(! any(bvec2(     any(greaterThan(c, ones)),   any(lessThan(c, zeroes))   ))){
            float amp = length( fieldAt(w)  );
            maxValue = max(maxValue, amp);
            w += wInc;
            c += cInc;
        }
        gl_FragColor = vec4(colorFunc(maxValue),  1.0);
    }else if (renderType == 2){ //ISO
        vec3 prevW = w;
        float prevAmp = 0.0;
        while(! any(bvec2(     any(greaterThan(c, ones)),   any(lessThan(c, zeroes))   ))){      
            float amp = length( fieldAt(w) );
            if (amp >= isoValue){
                vec3 pos = mix(w,prevW, (isoValue-amp) / (prevAmp-amp) );
                
                vec3 N = getNormalAt(pos, length(wInc) );
                vec3 L = normalize(lightPos.xyz - wPos.xyz);
                vec3 E = -wDir; //same as normalize(eyePos.xyz - wPos.xyz);
                vec3 HV = normalize(L + E);

                float lambertTerm = abs( dot(N,L) );
                float specularTerm = pow( abs( dot(N, HV) ), shininess);
                vec3 fColor = (ambient + diffuse * lambertTerm) * colorMod.rgb + specularTerm * specular * vec3(1.0);
                gl_FragColor = vec4(fColor, colorMod.a);
                return;
            }
            prevAmp = amp;
            prevW = w;
            w += wInc;
            c += cInc;
        }
        //gl_FragColor = vec4(0.0);
        discard;
    }
    

}