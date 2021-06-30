package edu.imtl.BlueKare.Renderer.TempRendererSet;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class VertexBuffer implements GLObject {
	private final int BYTES_PER_FLOAT = Float.SIZE / 8; // float 는 몇바이트인지
	private final int FLOATS_PER_POINT = 4; // 한 점에 몇개의 수가 들어가는지
	private final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT; // 한 점에 몇 바이트가 들어가는지
	private final int INITIAL_BUFFER_POINTS = 1000; // ?
	private final String TAG = this.getClass().getName();
	
	private int bufferID;
	private int bufferSize = INITIAL_BUFFER_POINTS * BYTES_PER_POINT;
	private int glDrawWayEnum;
	
	public VertexBuffer(int glDrawWayEnum) {
		GLSupport.checkError(TAG, "in VertexBuffer Constructor");
		this.glDrawWayEnum = glDrawWayEnum;
		
		int[] buffers = new int[1];
		GLES20.glGenBuffers(1, buffers, 0);
		bufferID = buffers[0];
		this.bind();
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferSize, null, glDrawWayEnum);
		this.unBind();
		GLSupport.checkError(TAG, "in VertexBuffer Constructor");
	}
	
	
	@Override
	public void bind() {
		GLSupport.checkError(TAG, "in VertexBuffer Bind");
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferID);
		GLSupport.checkError(TAG, "in VertexBuffer Bind");
	}
	
	@Override
	public void unBind() {
		GLSupport.checkError(TAG, "in VertexBuffer Unbind");
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLSupport.checkError(TAG, "in VertexBuffer Unbind");
	}
	
	public int getVBOSize() {
		return bufferSize;
	}
	
	public void reSize(int numPoints) {
		GLSupport.checkError(TAG, "in VertexBuffer Resize");
		if (numPoints * BYTES_PER_POINT > bufferSize) {
			while (numPoints * BYTES_PER_POINT > bufferSize) {
				bufferSize *= 2;
			}
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferSize, null, glDrawWayEnum);
		}
		GLSupport.checkError(TAG, "in VertexBuffer Resize");
	}
	
	public void fillData(FloatBuffer floatBuffer) {
		GLSupport.checkError(TAG, "in VertexBuffer FillData");
		this.bind();
		int numPoints = floatBuffer.remaining() / FLOATS_PER_POINT;
		this.reSize(numPoints);
		GLES20.glBufferSubData(
				GLES20.GL_ARRAY_BUFFER, 0, numPoints * BYTES_PER_POINT, floatBuffer
		                      );
		this.unBind();
		GLSupport.checkError(TAG, "in VertexBuffer FillData");
	}
}
