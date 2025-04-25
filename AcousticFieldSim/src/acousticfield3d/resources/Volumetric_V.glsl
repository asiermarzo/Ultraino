attribute vec4 vertexPosition;
               
uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelMatrix;

varying vec4 wPos;

void main(){
    wPos = modelMatrix * vertexPosition;

    gl_Position =  modelViewProjectionMatrix * vertexPosition;
}