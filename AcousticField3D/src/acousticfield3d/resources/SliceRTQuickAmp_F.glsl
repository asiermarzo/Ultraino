#define N_TRANS _N_TRANS_

#define PI 3.1415926535897932384626433832795

uniform int colouring;
uniform float minNegColor;
uniform float maxNegColor;
uniform float minPosColor;
uniform float maxPosColor;

uniform float k;
uniform float apperture;

uniform vec3 tPos[N_TRANS]; //x y z
uniform float tNorm[N_TRANS]; //ny
uniform vec2 tSpecs[N_TRANS]; //phase, amp

uniform vec4 colorMod;

varying vec4 wPos;

vec2 fieldAt(vec3 point){
    vec2 field = vec2(0.0);

    for(int i = 0; i < N_TRANS; ++i){ //try loop unroll
        vec3 diffVec = point - tPos[i];
        vec3 tNormI = vec3(0.0, tNorm[i], 0.0);

        float dist = length(diffVec);

        float angle = acos( dot(diffVec, tNormI) / dist);

        float dum = k * 0.5 * apperture * sin( angle );
        float directivity;
        if(dum == 0.0){
            directivity = 1.0;
        }else{
            directivity = sin(dum) / dum;
        }
        
        float ampDirAtt = tSpecs[i].y * directivity / dist;
        float kdPlusPhase = k * dist + tSpecs[i].x;
   
        field.x += ampDirAtt * cos(kdPlusPhase);
        field.y += ampDirAtt * sin(kdPlusPhase);
    }

    return field;
}

//INCLUDE Colouring.glsl

void main()
{
    vec2 field = fieldAt(wPos.xyz);
    
    float value = length(field);
    gl_FragColor = vec4(colorFunc(value),  colorMod.a);
}

