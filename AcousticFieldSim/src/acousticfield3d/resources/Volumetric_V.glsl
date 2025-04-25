attribute vec4 vertexPosition;
               
uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelMatrix;

uniform vec3 cubeCenter;
uniform vec3 cubeSize;

varying vec4 wPos;
varying vec3 cPos;

void main()
{
    wPos = modelMatrix * vertexPosition;
    cPos = ((vec3(wPos) - cubeCenter) / cubeSize) + vec3(0.5); 
    
    gl_Position =  modelViewProjectionMatrix * vertexPosition;
}