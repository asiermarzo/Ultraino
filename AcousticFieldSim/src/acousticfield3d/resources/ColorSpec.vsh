  
attribute vec4 vertexPosition;
attribute vec4 vertexNormal;

varying vec4 normal;
varying vec4 wPos;

uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelMatrix;

void main()
{
    
    gl_Position = modelViewProjectionMatrix * vertexPosition;

    normal = modelMatrix * vec4(vertexNormal.xyz, 0.0);
    
    wPos = modelMatrix * vertexPosition;
}