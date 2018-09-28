attribute vec4 vPosition;
attribute vec2 vCoord;
uniform mat4 uMatrix;
varying vec2 vTextureCoord;

void main(){
    gl_Position = uMatrix * vPosition;
    vTextureCoord = vCoord;
}