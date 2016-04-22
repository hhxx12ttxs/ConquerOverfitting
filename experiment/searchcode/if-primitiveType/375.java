package graphics.render.utils;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;

import java.util.ArrayDeque;
import java.util.Iterator;

public class RenderingPrimitivesManager {
	
	private ArrayDeque<RenderingChunk> deque;
	
	public RenderingPrimitivesManager(){
		deque = new ArrayDeque<RenderingChunk>();
	}
	
	public void addRenderingData(int primitiveType, int verticesAdded){
		if(deque.isEmpty()){			
			deque.add(new RenderingChunk(primitiveType, verticesAdded));
			return;
		}
		
		RenderingChunk lastAdded = deque.getLast();
		if(primitiveType==lastAdded.primitiveType && primitiveType!=GL_LINE_LOOP){
			lastAdded.verticesCount += verticesAdded;
		}else{
			deque.add(new RenderingChunk(primitiveType, verticesAdded));
		}
	}
	
	public RenderingChunk getNextChunk(){
		if(deque.isEmpty())
			return null;
		return deque.remove();
	}
	
	public String toString(){
		String s = "";
		s += "chunks: [ ";
		Iterator<RenderingChunk> it = deque.iterator();
		while(it.hasNext())
			s += it.next().toString() + "\n";
		s += "]";
		return s;
	}
	
	public class RenderingChunk{
		public int primitiveType;
		public int verticesCount;
		
		public RenderingChunk(int type, int count){
			this.primitiveType = type;
			this.verticesCount = count;
		}
		
		public String toString(){
			String type = "UNKNOWN";
			switch(primitiveType){
			case GL_TRIANGLES:
				type = "GL_TRIANGLE";break;
			case GL_LINES:
				type = "GL_LINES";break;
			case GL_LINE_LOOP:
				type = "GL_LINE_LOOP";break;
			case GL_TRIANGLE_FAN:
				type = "GL_TRIANGLE_FAN";break;
			}
			return "Primitive: " + type + " Vertices: " + verticesCount;
		}
	}

}

