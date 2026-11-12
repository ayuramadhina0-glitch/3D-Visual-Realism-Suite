package com.grafkom.viewing3d;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;
public class ShadowMapFBO {
    private final int fbo,depthTexture; private final int size=2048;
    public ShadowMapFBO(){
        fbo=glGenFramebuffers();depthTexture=glGenTextures();glBindTexture(GL_TEXTURE_2D,depthTexture);
        glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH_COMPONENT24,size,size,0,GL_DEPTH_COMPONENT,GL_FLOAT,(java.nio.ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D,GL_TEXTURE_BORDER_COLOR,new float[]{1,1,1,1});
        glBindFramebuffer(GL_FRAMEBUFFER,fbo);glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,depthTexture,0);
        glDrawBuffer(GL_NONE);glReadBuffer(GL_NONE);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER)!=GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Shadow FBO tidak lengkap");
        glBindFramebuffer(GL_FRAMEBUFFER,0);
    }
    public void bindForWriting(){glViewport(0,0,size,size);glBindFramebuffer(GL_FRAMEBUFFER,fbo);glClear(GL_DEPTH_BUFFER_BIT);}
    public void unbind(int w,int h){glBindFramebuffer(GL_FRAMEBUFFER,0);glViewport(0,0,w,h);}
    public int getDepthTextureID(){return depthTexture;}
    public void delete(){glDeleteFramebuffers(fbo);glDeleteTextures(depthTexture);}
}
