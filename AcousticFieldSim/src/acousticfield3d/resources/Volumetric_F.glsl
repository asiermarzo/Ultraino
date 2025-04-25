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

uniform int isTimeDomain;
uniform float timestamp;
uniform int renderType; //1 = MIPS, 2 = ISO

uniform vec4 eyePos;
uniform vec3 maxCube, minCube;
uniform float rayStep;
varying vec4 wPos;

#define PI 3.1415926535897932384626433832795

//FieldCalcsStencil.glsl
//INCLUDE FieldCalcsStencilNoDir.glsl
//INCLUDE Colouring.glsl

float val(vec2 p){
    if (isTimeDomain == 0){
        return length( p );
    }else{
        return dot(p, vec2(cos(timestamp), sin(timestamp)));
    }
}

vec3 getNormalAt(vec3 p, float h){
    vec3 n = vec3( val(fieldAt(p - vec3(h,0.0,0.0) )) - val(fieldAt(p + vec3(h,0.0,0.0))),
                   val(fieldAt(p - vec3(0.0,h,0.0) )) - val(fieldAt(p + vec3(0.0,h,0.0))),
                   val(fieldAt(p - vec3(0.0,0.0,h) )) - val(fieldAt(p + vec3(0.0,0.0,h))) );
    return normalize(n);
}

void main(){
    vec3 rayDir = normalize( vec3(wPos - eyePos) );
    vec3 rayInc = rayDir * rayStep;
    vec3 w = wPos.xyz;

    if (renderType == 1){ //MIPS
        float maxValue = 0.0;
        while( (any(greaterThan(w, maxCube)) || any(lessThan(w, minCube))) == false ){
            float amp = val( fieldAt(w)  );
            if ( abs(amp) > abs(maxValue)){
                maxValue = amp;
            }
            w += rayInc;
        }
        gl_FragColor = vec4(colorFunc(maxValue),  1.0);
    }else if (renderType == 2){ //ISO
        vec3 prevW = w;
        float prevAmp = 0.0;
        while( (any(greaterThan(w, maxCube)) || any(lessThan(w, minCube))) == false ){    
            float amp = val( fieldAt(w) );
            float absAmp = abs(amp);
            if (absAmp >= isoValue){
                vec3 pos = mix(w,prevW, (isoValue-absAmp) / (prevAmp-absAmp) );
                
                vec3 N = getNormalAt(pos, length(rayInc) );
                vec3 L = normalize(lightPos.xyz - wPos.xyz);
                vec3 E = -rayDir; //same as normalize(eyePos.xyz - wPos.xyz);
                vec3 HV = normalize(L + E);

                float lambertTerm = abs( dot(N,L) );
                float specularTerm = pow( abs( dot(N, HV) ), shininess);
                vec3 color = colorFunc(isoValue * sign(amp) );
                vec3 fColor = (ambient + diffuse * lambertTerm) * color + specularTerm * specular * vec3(1.0);
                gl_FragColor = vec4(fColor, colorMod.a);
                return;
            }
            prevAmp = absAmp;
            prevW = w;
            w += rayInc;
        }
        //gl_FragColor = vec4(0.0);
        discard;
    }
    

}