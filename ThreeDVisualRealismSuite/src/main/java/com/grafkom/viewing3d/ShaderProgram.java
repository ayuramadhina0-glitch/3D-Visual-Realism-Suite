package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int id;
    public ShaderProgram(String vertexSource, String fragmentSource) {
        int vs = compile(GL_VERTEX_SHADER, vertexSource);
        int fs = compile(GL_FRAGMENT_SHADER, fragmentSource);
        id = glCreateProgram();
        glAttachShader(id, vs); glAttachShader(id, fs); glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE)
            throw new IllegalStateException("Program link error: " + glGetProgramInfoLog(id));
        glDeleteShader(vs); glDeleteShader(fs);
    }
    private int compile(int type, String source) {
        int shader = glCreateShader(type); glShaderSource(shader, source); glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new IllegalStateException("Shader compile error: " + glGetShaderInfoLog(shader));
        return shader;
    }
    public void use() { glUseProgram(id); }
    public void setInt(String n,int v){glUniform1i(glGetUniformLocation(id,n),v);}
    public void setFloat(String n,float v){glUniform1f(glGetUniformLocation(id,n),v);}
    public void setVec3(String n,Vector3f v){glUniform3f(glGetUniformLocation(id,n),v.x,v.y,v.z);}
    public void setMat4(String n,Matrix4f m){
        try(MemoryStack s=MemoryStack.stackPush()){FloatBuffer b=s.mallocFloat(16);m.get(b);glUniformMatrix4fv(glGetUniformLocation(id,n),false,b);}
    }
    public void delete(){glDeleteProgram(id);}
}
