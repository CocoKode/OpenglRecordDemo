attribute vec4 vPosition;
attribute vec4 vCoord;
uniform mat4 uMatrix;
uniform mat4 uCoordMatrix;
varying vec2 vTextureCoord;

void main() {
    gl_Position = uMatrix * vPosition;
    vTextureCoord = (uCoordMatrix * vCoord).xy;
}

