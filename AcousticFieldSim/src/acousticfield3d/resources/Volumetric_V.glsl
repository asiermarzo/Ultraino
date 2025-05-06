attribute vec4 vertexPosition;
attribute vec4 vertexNormal;

uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelMatrix;

varying vec4 wPos;
varying vec4 normal;

void main(){
    wPos = modelMatrix * vertexPosition;
    normal = modelMatrix * vec4(vertexNormal.xyz, 0.0);
    gl_Position =  modelViewProjectionMatrix * vertexPosition;
}