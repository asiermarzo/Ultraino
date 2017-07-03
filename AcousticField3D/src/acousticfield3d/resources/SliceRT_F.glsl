#define N_TRANS _N_TRANS_

_USE_AMP_ #define USE_AMP 1
_USE_PHASE_ #define USE_PHASE 1
_USE_AMPPHASE_ #define USE_AMPPHASE 1

#define PI 3.1415926535897932384626433832795

uniform int colouring;
uniform float minNegColor;
uniform float maxNegColor;
uniform float minPosColor;
uniform float maxPosColor;
uniform vec3 tPos[N_TRANS];
uniform vec3 tNorm[N_TRANS];
uniform vec4 tSpecs[N_TRANS];

uniform vec4 colorMod;

varying vec4 wPos;

vec3 hsv2rgb(vec3 c){
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

//TEMPLATE FIELD
//INCLUDE Colouring.glsl

void main()
{
    vec2 field = fieldAt(wPos.xyz);
    
#ifdef USE_AMP //Amplitude
    float value =length(field);
    gl_FragColor = vec4(colorFunc(value),  colorMod.a);
#endif


#ifdef USE_PHASE //Phase
     float phase = clamp( (atan(field.y, field.x) / PI + 1.0) / 2.0, 0.0, 1.0);
     vec3 rgb = hsv2rgb(vec3(phase,1.0,1.0));
     
    gl_FragColor = vec4( rgb , colorMod.a);
#endif 

#ifdef USE_AMPPHASE //Amp + Phase
    float amplitude = clamp( (length(field)-minPosColor) / (maxPosColor-minPosColor), 0.0, 1.0);
    float phase = clamp( (atan(field.y, field.x) / PI + 1.0) / 2.0, 0.0, 1.0);
    vec3 rgb = hsv2rgb(vec3(phase,1,amplitude));
    gl_FragColor = vec4( rgb.x, rgb.y, rgb.z, colorMod.a);
#endif

}

