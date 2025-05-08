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
uniform float outlineCutSize;

uniform mat4 viewMatrix;
uniform mat4 projectionViewMatrix;

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
varying vec4 normal;

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
        do{
            float amp = val( fieldAt(w)  );
            if ( abs(amp) > abs(maxValue)){
                maxValue = amp;
            }
            w += rayInc;
        }while ( (any(greaterThan(w, maxCube)) || any(lessThan(w, minCube))) == false );
        gl_FragColor = vec4(colorFunc(maxValue),  1.0);
    }else if (renderType == 2){ //ISO
        vec3 prevW = w;
        float prevAmp = 0.0;
        bool firstIter = true;
        do{    
            float amp = val( fieldAt(w) );
            float absAmp = abs(amp);
            if (absAmp >= isoValue){
                vec3 pos = mix(w,prevW, (isoValue-absAmp) / (prevAmp-absAmp) );
                vec3 N, color;
                if (firstIter){ //this pixel is cutting the iso-surface
                    N = normalize( normal.xyz ); //the normal is of the bounding volume
                    if ( mod(abs(pos.y), outlineCutSize) > outlineCutSize * 0.5){
                        color = colorFunc( amp );
                    }else{
                        color = vec3(0.25);
                    }
                }else{
                    color = colorFunc(isoValue * sign(amp) );
                    N = getNormalAt(pos, length(rayInc/10.0) );
                }

                vec3 L = normalize(lightPos.xyz - pos.xyz);
                vec3 E = -rayDir; //same as normalize(eyePos.xyz - pos.xyz);
                vec3 HV = normalize(L + E);
                float lambertTerm = abs( dot(N,L) );
                float specularTerm = pow( abs( dot(N, HV) ), shininess);
                vec3 fColor = (ambient + diffuse * lambertTerm) * color + specularTerm * specular * vec3(1.0);
                gl_FragColor = vec4(fColor, colorMod.a);

                vec4 clipPos = projectionViewMatrix * vec4(pos, 1.0);
                float depth = (clipPos.z / clipPos.w) * 0.5 + 0.5; // Convert from [-1, 1] to [0, 1]
                gl_FragDepth = depth;

                return;
            }
            prevAmp = absAmp;
            prevW = w;
            w += rayInc;
            firstIter = false;
        }while ( (any(greaterThan(w, maxCube)) || any(lessThan(w, minCube))) == false );
        
        gl_FragDepth = gl_FragCoord.z;
        discard;
    }
    

}