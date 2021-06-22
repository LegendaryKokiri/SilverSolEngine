package silverSol.engine.render.opengl.object;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import silverSol.engine.render.texture.Texture;

public class Fbo {
	
	private int fboID;
	private int width, height;
	private int x, y;
	
	private boolean hasTextureAttachment;
		private Texture textureAttachment;
		
	private boolean hasDepthTextureAttachment;
		private Texture depthTextureAttachment;
	
	private boolean hasDepthBufferAttachment;
		private int depthBufferAttachmentID;
	
	//This constructor is only intended for use within SilverSol Engine for the default FBO.
	public Fbo() {
		this.fboID = 0;
		this.x = 0;
		this.y = 0;
		this.width = Display.getWidth();
		this.height = Display.getHeight();
	}
	
	public Fbo(int x, int y, int width, int height) {
		this.fboID = GL30.glGenFramebuffers();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);		
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		unbind();
	}
	
	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		GL11.glViewport(x, y, width, height);
	}
	
	public int createTextureAttachment() {
		bind();
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height,
				0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
		unbind();
		
		this.hasTextureAttachment = true;
		this.textureAttachment = new Texture(texture);
		return texture;
	}
	
	public int createDepthTextureAttachment() {
		bind();
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
				0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
		unbind();
		
		this.hasDepthTextureAttachment = true;
		this.depthTextureAttachment = new Texture(texture);
		return texture;
	}

	public int createDepthBufferAttachment() {
		bind();
		int depthBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
		unbind();
		
		this.hasDepthBufferAttachment = true;
		this.depthBufferAttachmentID = depthBuffer;
		return depthBuffer;
	}
	
	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}

	public int getFboID() {
		return fboID;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public Texture getTextureAttachment() {
		return textureAttachment;
	}

	public Texture getDepthTextureAttachment() {
		return depthTextureAttachment;
	}

	public void delete() {
		GL30.glDeleteFramebuffers(fboID);
		if(hasTextureAttachment) textureAttachment.delete();
		if(hasDepthTextureAttachment) depthTextureAttachment.delete();
		if(hasDepthBufferAttachment) GL30.glDeleteRenderbuffers(depthBufferAttachmentID);
	}
}
