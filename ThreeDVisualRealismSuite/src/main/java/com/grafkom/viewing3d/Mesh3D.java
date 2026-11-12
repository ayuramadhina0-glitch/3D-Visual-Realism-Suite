package com.grafkom.viewing3d;

import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh3D {
    private final int vao, vbo, ebo, indexCount;
    public Mesh3D(float[] vertices, int[] indices) {
        indexCount=indices.length; vao=glGenVertexArrays(); vbo=glGenBuffers(); ebo=glGenBuffers();
        glBindVertexArray(vao);
        FloatBuffer vb=MemoryUtil.memAllocFloat(vertices.length);
        vb.put(vertices).flip(); glBindBuffer(GL_ARRAY_BUFFER,vbo); glBufferData(GL_ARRAY_BUFFER,vb,GL_STATIC_DRAW); MemoryUtil.memFree(vb);
        IntBuffer ib=MemoryUtil.memAllocInt(indices.length);
        ib.put(indices).flip(); glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,ebo); glBufferData(GL_ELEMENT_ARRAY_BUFFER,ib,GL_STATIC_DRAW); MemoryUtil.memFree(ib);
        int stride=8*Float.BYTES;
        glVertexAttribPointer(0,3,GL_FLOAT,false,stride,0L); glEnableVertexAttribArray(0);
        glVertexAttribPointer(1,3,GL_FLOAT,false,stride,3L*Float.BYTES); glEnableVertexAttribArray(1);
        glVertexAttribPointer(2,2,GL_FLOAT,false,stride,6L*Float.BYTES); glEnableVertexAttribArray(2);
        glBindVertexArray(0);
    }
    public void render(){glBindVertexArray(vao);glDrawElements(GL_TRIANGLES,indexCount,GL_UNSIGNED_INT,0L);glBindVertexArray(0);}
    public void delete(){glDeleteBuffers(vbo);glDeleteBuffers(ebo);glDeleteVertexArrays(vao);}
    public static Mesh3D createSphere(int stacks,int slices){
        float[] v=new float[(stacks+1)*(slices+1)*8]; int p=0;
        for(int i=0;i<=stacks;i++){double phi=Math.PI*i/stacks;
            for(int j=0;j<=slices;j++){double theta=2*Math.PI*j/slices;
                float x=(float)(Math.sin(phi)*Math.cos(theta)), y=(float)Math.cos(phi), z=(float)(Math.sin(phi)*Math.sin(theta));
                v[p++]=x;v[p++]=y;v[p++]=z;v[p++]=x;v[p++]=y;v[p++]=z;v[p++]=(float)j/slices;v[p++]=(float)i/stacks;
            }}
        int[] ix=new int[stacks*slices*6];p=0;int row=slices+1;
        for(int i=0;i<stacks;i++)for(int j=0;j<slices;j++){int a=i*row+j,b=a+row;
            ix[p++]=a;ix[p++]=a+1;ix[p++]=b;ix[p++]=a+1;ix[p++]=b+1;ix[p++]=b;}
        return new Mesh3D(v,ix);
    }
    public static Mesh3D createPlane(){
        float[] v={-6,0,-6, 0,1,0, 0,0,  6,0,-6, 0,1,0, 4,0,
                   6,0, 6, 0,1,0, 4,4, -6,0, 6, 0,1,0, 0,4};
        int[] i={0,2,1,0,3,2}; return new Mesh3D(v,i);
    }
}
