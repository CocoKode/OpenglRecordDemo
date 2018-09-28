/*
 *
 * FastDrawerHelper.java
 * 
 * Created by Wuwang on 2016/11/17
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.crearo.recordapplication.utils;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Description:
 */
public class Gl2Utils {

    public static final String TAG="GLUtils";
    public static boolean DEBUG=true;

    public static final int TYPE_FITXY=0;
    public static final int TYPE_CENTERCROP=1;
    public static final int TYPE_CENTERINSIDE=2;
    public static final int TYPE_FITSTART=3;
    public static final int TYPE_FITEND=4;

    private Gl2Utils(){

    }

    public static FloatBuffer array2Buffer(float[] array) {
        FloatBuffer fb = ByteBuffer.allocateDirect(array.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(array).position(0);
//        fb.put(array).flip();
        return fb;
    }

    public static void getShowMatrix(float[] matrix,int texWidth,int texHeight,int viewWidth,int
        viewHeight){
        if(texHeight > 0 && texWidth > 0 && viewWidth > 0 && viewHeight > 0){
            float[] viewMatrix = new float[16];
            float[] projectMatrix = new float[16];
            float tRatio = (float) texWidth / texHeight;
            float vRatio = (float) viewWidth / viewHeight;
            if (tRatio > vRatio) {
                Matrix.orthoM(projectMatrix, 0, -1f, 1f, -tRatio / vRatio, tRatio / vRatio, 1f, 3f);
//                Matrix.orthoM(projectMatrix, 0, -vRatio / tRatio, vRatio / tRatio, -1f, 1f, 1f, 3f);
            } else {
                Matrix.orthoM(projectMatrix, 0, -vRatio / tRatio, vRatio / tRatio, -1f, 1f, 1f, 3f);
//                Matrix.orthoM(projectMatrix, 0, -1f, 1f, -tRatio / vRatio, tRatio / vRatio, 1f, 3f);
            }
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);
            Matrix.multiplyMM(matrix, 0, projectMatrix, 0, viewMatrix, 0);
        }
    }

    public static void getShowMatrix2(float[] matrix,int imgWidth,int imgHeight,int viewWidth,int
            viewHeight){
        if(imgHeight>0&&imgWidth>0&&viewWidth>0&&viewHeight>0){
            float sWhView=(float)viewWidth/viewHeight;
            float sWhImg=(float)imgWidth/imgHeight;
            float[] projection=new float[16];
            float[] camera=new float[16];
            if(sWhImg>sWhView){
                Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
            }else{
                Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);

            }
            Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
            Matrix.multiplyMM(matrix,0,projection,0,camera,0);
        }
    }

    public static void getMatrix(float[] matrix,int type,int imgWidth,int imgHeight,int viewWidth,
                                 int viewHeight){
        if(imgHeight>0&&imgWidth>0&&viewWidth>0&&viewHeight>0){
            float[] projection=new float[16];
            float[] camera=new float[16];
            if(type==TYPE_FITXY){
                Matrix.orthoM(projection,0,-1,1,-1,1,1,3);
                Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
                Matrix.multiplyMM(matrix,0,projection,0,camera,0);
            }
            float sWhView=(float)viewWidth/viewHeight;
            float sWhImg=(float)imgWidth/imgHeight;
            if(sWhImg>sWhView){
                switch (type){
                    case TYPE_CENTERCROP:
                        Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
                        break;
                    case TYPE_CENTERINSIDE:
                        Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
                        break;
                    case TYPE_FITSTART:
                        Matrix.orthoM(projection,0,-1,1,1-2*sWhImg/sWhView,1,1,3);
                        break;
                    case TYPE_FITEND:
                        Matrix.orthoM(projection,0,-1,1,-1,2*sWhImg/sWhView-1,1,3);
                        break;
                }
            }else{
                switch (type){
                    case TYPE_CENTERCROP:
                        Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
                        break;
                    case TYPE_CENTERINSIDE:
                        Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
                        break;
                    case TYPE_FITSTART:
                        Matrix.orthoM(projection,0,-1,2*sWhView/sWhImg-1,-1,1,1,3);
                        break;
                    case TYPE_FITEND:
                        Matrix.orthoM(projection,0,1-2*sWhView/sWhImg,1,-1,1,1,3);
                        break;
                }
            }
            Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
            Matrix.multiplyMM(matrix,0,projection,0,camera,0);
        }
    }

    public static void getCenterInsideMatrix(float[] matrix,int imgWidth,int imgHeight,int viewWidth,int
            viewHeight){
        if(imgHeight>0&&imgWidth>0&&viewWidth>0&&viewHeight>0){
            float sWhView=(float)viewWidth/viewHeight;
            float sWhImg=(float)imgWidth/imgHeight;
            float[] projection=new float[16];
            float[] camera=new float[16];
            if(sWhImg>sWhView){
                Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
            }else{
                Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
            }
            Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
            Matrix.multiplyMM(matrix,0,projection,0,camera,0);
        }
    }

    public static float[] rotate(float[] m,float angle){
        Matrix.rotateM(m,0,angle,0,0,1);
        return m;
    }

    public static float[] flip(float[] m,boolean x,boolean y){
        if(x||y){
            Matrix.scaleM(m,0,x?-1:1,y?-1:1,1);
        }
        return m;
    }

    public static float[] getOriginalMatrix(){
        return new float[]{
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1
        };
    }

    public static String uRes(Resources mRes,String path){
        StringBuilder result = new StringBuilder();
        try{
            InputStream is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch=is.read(buffer))){
                result.append(new String(buffer,0, ch));
            }
        }catch (Exception e){
            return null;
        }
        return result.toString().replaceAll("\\r\\n","\n");
    }

    public static int createGlProgramByRes(Resources res,String vert,String frag){
        return createGlProgram(uRes(res,vert),uRes(res,frag));
    }

    public static int createGlProgram(String vertexSource, String fragmentSource){
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if(vertex == 0) {
            return 0;
        }
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if(fragment == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        if(program != 0){
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,linkStatus,0);
            if(linkStatus[0] != GLES20.GL_TRUE){
                glError(1,"Could not link program:"+ GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static int loadShader(int shaderType, String source){
        int shader= GLES20.glCreateShader(shaderType);
        if(0 != shader){
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compiled,0);
            if(compiled[0] == 0){
                glError(1,"Could not compile shader:" + shaderType);
                glError(1,"GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static void glError(int code, Object index){
        if(DEBUG && (code != 0)){
            Log.e(TAG,"glError:"+code+"---"+index);
        }
    }

}
