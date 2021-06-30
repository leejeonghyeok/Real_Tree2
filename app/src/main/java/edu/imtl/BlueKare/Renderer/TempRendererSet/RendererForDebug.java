/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.imtl.BlueKare.Renderer.TempRendererSet;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.ar.core.PointCloud;

import java.nio.FloatBuffer;

public class RendererForDebug {
	private static final String TAG = PointCloud.class.getSimpleName();
	
	private static final String VERTEX_SHADER_PATH = "shaders/point_cloud.vert";
	private static final String FRAGMENT_SHADER_PATH = "shaders/point_cloud.frag";
	
	private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
	private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
	private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT;
	
	VertexBuffer vertexBuffer;
	Context context;
	Shader shader;
	
	public RendererForDebug() {
	}
	
	public void createOnGlThread(Context context) {
		this.context = context;
		vertexBuffer = new VertexBuffer(GLES20.GL_DYNAMIC_DRAW);
		shader = new Shader(context, FRAGMENT_SHADER_PATH, VERTEX_SHADER_PATH);
		shader.makeProgram().bind();
	}
	
	public void cubeDraw(FloatBuffer floatBuffer, float[] modelViewProjection) {
		vertexBuffer.fillData(floatBuffer);
		shader.setAttrib(vertexBuffer, "a_Position", 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
		shader.setUniform("u_ModelViewProjection", 1, false, modelViewProjection, 0);
		shader.setUniform("u_PointSize", -1.0f);
		shader.setUniform("u_Color", Color.WHITE, Color.WHITE, Color.WHITE, 1.0f);
		Renderer.wireDraw(shader, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		shader.setUniform("u_Color", Color.WHITE, Color.WHITE, Color.WHITE, 0.5f);
		Renderer.draw(shader, GLES20.GL_TRIANGLES, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		shader.freeAtrib(vertexBuffer, "a_Position");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void pointDraw(FloatBuffer floatBuffer, float[] modelViewProjection, Color color, float pointSize) {
		vertexBuffer.fillData(floatBuffer);
		shader.setAttrib(vertexBuffer, "a_Position", 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
		shader.setUniform("u_Color", color.red(), color.green(), color.blue(), color.alpha());
		shader.setUniform("tmp", -10.0f);
		shader.setUniform("u_ModelViewProjection", 1, false, modelViewProjection, 0);
		shader.setUniform("u_PointSize", pointSize);
		Renderer.draw(shader, GLES20.GL_POINTS, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		shader.freeAtrib(vertexBuffer, "a_Position");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void debugDraw(FloatBuffer floatBuffer, float[] modelViewProjection, Color color, float pointSize) {
		vertexBuffer.fillData(floatBuffer);
		shader.setAttrib(vertexBuffer, "a_Position", 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
		shader.setUniform("u_Color", color.red(), color.green(), color.blue(), color.alpha());
//		shader.setUniform("u_ModelViewProjection", 1, false, modelViewProjection, 0);
		shader.setUniform("u_PointSize", pointSize);
		Renderer.draw(shader, GLES20.GL_POINTS, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		shader.freeAtrib(vertexBuffer, "a_Position");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void planeDraw(FloatBuffer floatBuffer, float[] modelViewProjection, Color color) {
		vertexBuffer.fillData(floatBuffer);
		shader.setAttrib(vertexBuffer, "a_Position", 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
		shader.setUniform("u_Color", color.red(), color.green(), color.blue(), 0.5f);
		shader.setUniform("u_ModelViewProjection", 1, false, modelViewProjection, 0);
		shader.setUniform("u_PointSize", 20.0f);
		Renderer.draw(shader, GLES20.GL_TRIANGLES, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		shader.freeAtrib(vertexBuffer, "a_Position");
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	public void lineDraw(FloatBuffer floatBuffer, float[] modelViewProjection, Color color, float pointSize) {
		vertexBuffer.fillData(floatBuffer);
		
		shader.bind();
		GLES20.glLineWidth(pointSize);
		shader.unBind();
		
		shader.setAttrib(vertexBuffer, "a_Position", 4, GLES20.GL_FLOAT, false, BYTES_PER_POINT, 0);
		shader.setUniform("u_Color", color.red(), color.green(), color.blue(), color.alpha());
		shader.setUniform("u_ModelViewProjection", 1, false, modelViewProjection, 0);
		shader.setUniform("u_PointSize", 20.0f);
		Renderer.draw(shader, GLES20.GL_LINES, 0, floatBuffer.remaining() / FLOATS_PER_POINT);
		shader.freeAtrib(vertexBuffer, "a_Position");
	}
	
}
