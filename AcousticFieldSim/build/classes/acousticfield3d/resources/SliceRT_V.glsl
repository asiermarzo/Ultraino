attribute vec4 vertexPosition;
               
uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelMatrix;

varying vec4 wPos;
varying vec4 mPos;

void main()
{
    mPos = vertexPosition;
    wPos = modelMatrix * vertexPosition;
    gl_Position =  modelViewProjectionMatrix * vertexPosition;
}