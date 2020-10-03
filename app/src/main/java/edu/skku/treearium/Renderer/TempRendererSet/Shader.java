package edu.skku.treearium.Renderer.TempRendererSet;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Shader {
	private final String TAG = this.getClass().getName();
	
	private final int glTrue = 1, glFalse = 0;
	private int programID;
	private Map<Integer, String> shaderCode = new HashMap<>();
	
	public Shader(Context context, String fragPath, String vertPath) {
		this.shaderCode.put(GLES20.GL_FRAGMENT_SHADER, readFromAssets(context, fragPath));
		this.shaderCode.put(GLES20.GL_VERTEX_SHADER, readFromAssets(context, vertPath));
	}
	
	public void bind() {
		GLSupport.checkError(TAG, "in Shader Bind");
		GLES20.glUseProgram(programID);
		GLSupport.checkError(TAG, "in Shader Bind");
	}
	
	public void unBind() {
		GLSupport.checkError(TAG, "in Shader Unbind");
		GLES20.glUseProgram(0);
		GLSupport.checkError(TAG, "in Shader Unbind");
	}
	
	String readFromAssets(Context context, String filePath) {
		StringBuilder shaderCode = new StringBuilder();
		try (Scanner scanner = new Scanner(context.getAssets().open(filePath))) {
			while (scanner.hasNextLine()) {
				shaderCode.append(scanner.nextLine());
				shaderCode.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return shaderCode.toString();
	}
	
	int makeShader(int glEnum) {
		GLSupport.checkError(TAG, "in Shader MakeShader");
		int ID;
		ID = GLES20.glCreateShader(glEnum);
		GLES20.glShaderSource(ID, shaderCode.get(glEnum));
		GLES20.glCompileShader(ID);
		
		int[] infoLogLen = new int[1];
		GLES20.glGetShaderiv(ID, GLES20.GL_INFO_LOG_LENGTH, infoLogLen, 0);
		
		if (infoLogLen[0] != glTrue) {
			String log = GLES20.glGetShaderInfoLog(ID);
			Log.d(this.getClass().getName(), "glMakeShader: " + log);
		}
		
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(ID, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		
		if (compileStatus[0] != glTrue) {
			Log.e(this.getClass().getName(), "Error compiling shader: " + GLES20.glGetShaderInfoLog(ID));
			GLES20.glDeleteShader(ID);
			ID = 0;
		}
		
		if (ID == glFalse) {
			throw new RuntimeException("Error creating shader.");
		}
		GLSupport.checkError(TAG, "in Shader MakeShader");
		
		
		return ID;
	}
	
	public Shader makeProgram() {
		GLSupport.checkError(TAG, "in Shader MakeProgram");
		programID = GLES20.glCreateProgram();
		int fragID = makeShader(GLES20.GL_FRAGMENT_SHADER);
		int vertID = makeShader(GLES20.GL_VERTEX_SHADER);
		
		GLES20.glAttachShader(programID, fragID);
		GLES20.glAttachShader(programID, vertID);
		
		GLES20.glLinkProgram(programID);
		
		final int[] infoLogLen = new int[1];
		GLES20.glGetProgramiv(programID, GLES20.GL_INFO_LOG_LENGTH, infoLogLen, 0);
		
		if (infoLogLen[0] != glTrue) {
			String info = GLES20.glGetProgramInfoLog(programID);
			Log.d(this.getClass().getName(), "glMakeProgram: " + info);
		}
		
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != glTrue) {
			GLES20.glDeleteProgram(programID);
		}
		
		GLES20.glValidateProgram(programID);
		
		GLES20.glDeleteShader(vertID);
		GLES20.glDeleteShader(fragID);
		GLSupport.checkError(TAG, "in Shader MakeProgram");
		
		return this;
	}
	
	// TODO : 암튼 바꾸고 이거 지우기
	public int getProgramID() {
		return programID;
	}
	
	public void setUniform(String target, float f1) {
		GLSupport.checkError(TAG, "in Shader SetUniform");
		this.bind();
		GLES20.glUniform1f(GLES20.glGetUniformLocation(programID, target), f1);
		this.unBind();
		GLSupport.checkError(TAG, "in Shader SetUniform");
	}
	
	public void setUniform(String target, float f1, float f2, float f3, float f4) {
		GLSupport.checkError(TAG, "in Shader SetUniform");
		this.bind();
		GLES20.glUniform4f(GLES20.glGetUniformLocation(programID, target), f1, f2, f3, f4);
		this.unBind();
		GLSupport.checkError(TAG, "in Shader SetUniform");
	}
	
	public void setUniform(String target, int count, boolean t, float[] value, int offset) {
		GLSupport.checkError(TAG, "in Shader SetUniform");
		this.bind();
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programID, target), count, t, value, offset);
		this.unBind();
		GLSupport.checkError(TAG, "in Shader SetUniform");
	}
	
	public void setAttrib(GLObject glObject, String string, int size, int type, boolean normalized, int stride, int offset) {
		GLSupport.checkError(TAG, "in Shader SetAttrib");
		glObject.bind();
		this.bind();
		
		int pos = GLES20.glGetAttribLocation(programID, string);
		GLES20.glEnableVertexAttribArray(pos);
		GLES20.glVertexAttribPointer(pos, size, type, normalized, stride, offset);
		
		this.unBind();
		glObject.unBind();
		GLSupport.checkError(TAG, "in Shader SetAttrib");
	}
	
	public void setAttrib(GLObject glObject, String string, int size, int type, boolean normalized, int stride, Buffer buffer) {
		GLSupport.checkError(TAG, "in Shader SetAttrib");
		glObject.bind();
		this.bind();
		
		buffer.position(0);
		
		int pos = GLES20.glGetAttribLocation(programID, string);
		GLES20.glVertexAttribPointer(pos, size, type, normalized, stride, buffer);
		GLES20.glEnableVertexAttribArray(pos);
		
		this.unBind();
		glObject.unBind();
		GLSupport.checkError(TAG, "in Shader SetAttrib");
	}
	
	public void freeAtrib(GLObject glObject, String string) {
		GLSupport.checkError(TAG, "in Shader FreeAttrib");
		glObject.bind();
		this.bind();
		
		int pos = GLES20.glGetAttribLocation(programID, string);
		GLES20.glDisableVertexAttribArray(pos);
		
		this.unBind();
		glObject.unBind();
		GLSupport.checkError(TAG, "in Shader FreeAttrib");
	}
}
