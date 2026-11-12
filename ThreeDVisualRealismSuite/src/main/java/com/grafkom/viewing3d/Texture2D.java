package com.grafkom.viewing3d;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture2D {
    private final int id;
    public Texture2D(String path){
        id=glGenTextures();glBindTexture(GL_TEXTURE_2D,id);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
        STBImage.stbi_set_flip_vertically_on_load(true);
        try(MemoryStack stack=MemoryStack.stackPush()){
            IntBuffer w=stack.mallocInt(1),h=stack.mallocInt(1),c=stack.mallocInt(1);
            ByteBuffer data=STBImage.stbi_load(path,w,h,c,4);
            if(data==null)throw new IllegalStateException("Texture tidak dapat dibaca: "+path+" - "+STBImage.stbi_failure_reason());
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,w.get(0),h.get(0),0,GL_RGBA,GL_UNSIGNED_BYTE,data);
            glGenerateMipmap(GL_TEXTURE_2D);STBImage.stbi_image_free(data);
        }
        setBilinearMipmapped();
    }
    public void bind(int unit){glActiveTexture(GL_TEXTURE0+unit);glBindTexture(GL_TEXTURE_2D,id);}
    public void setNearest(){glBindTexture(GL_TEXTURE_2D,id);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST_MIPMAP_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);}
    public void setBilinearMipmapped(){glBindTexture(GL_TEXTURE_2D,id);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);}
    public void delete(){glDeleteTextures(id);}
}
