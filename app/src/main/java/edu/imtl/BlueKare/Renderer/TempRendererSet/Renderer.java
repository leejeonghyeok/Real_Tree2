package edu.imtl.BlueKare.Renderer.TempRendererSet;

import android.opengl.GLES20;

public class Renderer {
	static public void draw(Shader shader, int glModeEnum, int first, int count) {
		shader.bind();
		GLES20.glDrawArrays(glModeEnum, first, count);
		shader.unBind();
	}
	
	static public void thick(Shader shader) {
		shader.bind();
		GLES20.glLineWidth(8);
		shader.unBind();
	}
	
	static public void blendDraw(Shader shader, int glModeEnum, int first, int count) {
		shader.bind();
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

//		// Enable depth test
//		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//// Accept fragment if it closer to the camera than the former one
//		GLES20.glDepthFunc(GLES20.GL_LESS);
		GLES20.glDrawArrays(glModeEnum, first, count);
		shader.unBind();
	}
	
	static public void wireDraw(Shader shader, int first, int count) {
		shader.bind();
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);
		
		GLES20.glLineWidth(10);
		GLES20.glDrawArrays(GLES20.GL_LINES, first, count);
		
		GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
		GLES20.glPolygonOffset(1.0f, 1.0f);
		
		shader.unBind();
	}
	
	// VBO 랑 다르게 , Texture 는 그려지기 전에 bind 되어야 할 필요가 있는듯 함!
	static public void draw(Shader shader, GLObject glObject, int glModeEnum, int first, int count) {
		shader.bind();
		glObject.bind();
		GLES20.glDrawArrays(glModeEnum, first, count);
		glObject.unBind();
		shader.unBind();
	}
}
