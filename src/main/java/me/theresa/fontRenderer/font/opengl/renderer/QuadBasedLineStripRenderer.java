package me.theresa.fontRenderer.font.opengl.renderer;

public class QuadBasedLineStripRenderer implements LineStripRenderer {

	private final SGL GL = Renderer.get();


	public static int MAX_POINTS = 10000;

	private boolean antialias;

	private float width = 1;

	private final float[] points;

	private final float[] colours;

	private int pts;

	private int cpt;


	private final DefaultLineStripRenderer def = new DefaultLineStripRenderer();

	private boolean renderHalf;


	private boolean lineCaps = false;


	public QuadBasedLineStripRenderer() {
		points = new float[MAX_POINTS * 2];
		colours = new float[MAX_POINTS * 4];
	}
	
	
	public void setLineCaps(boolean caps) {
		this.lineCaps = caps;
	}
	
	
	public void start() {
		if (width == 1) {
			def.start();
			return;
		}
		
		pts = 0;
		cpt = 0;
		GL.flush();
		
		float[] col = GL.getCurrentColor();
		color(col[0],col[1],col[2],col[3]);
	}
	
	
	public void end() {
		if (width == 1) {
			def.end();
			return;
		}
		
		renderLines(points, pts);
	}
	
	
	public void vertex(float x, float y) {
		if (width == 1) {
			def.vertex(x,y);
			return;
		}
		
		points[(pts*2)] = x;
		points[(pts*2)+1] = y;
		pts++;
		
		int index = pts-1;
		color(colours[(index*4)], colours[(index*4)+1], colours[(index*4)+2], colours[(index*4)+3]);
	}
	
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	
	public void setAntiAlias(boolean antialias) {
		def.setAntiAlias(antialias);
		this.antialias = antialias;
	}

	
	public void renderLines(float[] points, int count) {
		if (antialias) {
			GL.glEnable(SGL.GL_POLYGON_SMOOTH);
			renderLinesImpl(points,count,width+1f);
		}

		GL.glDisable(SGL.GL_POLYGON_SMOOTH);
		renderLinesImpl(points,count,width);
		
		if (antialias) {
			GL.glEnable(SGL.GL_POLYGON_SMOOTH);
		}
	}
	
	
	public void renderLinesImpl(float[] points, int count, float w) {
		float width = w / 2;
		
		float lastx1 = 0;
		float lasty1 = 0;
		float lastx2 = 0;
		float lasty2 = 0;

		GL.glBegin(SGL.GL_QUADS);
		for (int i=0;i<count+1;i++) {
			int current = i;
			int next = i+1;
			int prev = i-1;
			if (prev < 0) {
				prev += count;
			}
			if (next >= count) {
				next -= count;
			}
			if (current >= count) {
				current -= count;
			}

			float x1 = points[(current*2)];
			float y1 = points[(current*2)+1];
			float x2 = points[(next*2)];
			float y2 = points[(next*2)+1];
			
			// draw the next segment
			float dx = x2 - x1;
			float dy = y2 - y1;

			if ((dx == 0) && (dy == 0)) {
				continue;
			}
			
			float d2 = (dx*dx)+(dy*dy);
			float d = (float) Math.sqrt(d2);
			dx *= width;
			dy *= width;
			dx /= d;
			dy /= d;
			
			float tx = dy;
			float ty = -dx;

			if (i != 0) {
				bindColor(prev);
				GL.glVertex3f(lastx1,lasty1,0);
				GL.glVertex3f(lastx2,lasty2,0);
				bindColor(current);
				GL.glVertex3f(x1+tx,y1+ty,0);
				GL.glVertex3f(x1-tx,y1-ty,0);
			}
			
			lastx1 = x2-tx;
			lasty1 = y2-ty;
			lastx2 = x2+tx;
			lasty2 = y2+ty;
			
			if (i < count-1) {
				bindColor(current);
				GL.glVertex3f(x1+tx,y1+ty,0);
				GL.glVertex3f(x1-tx,y1-ty,0);
				bindColor(next);
				GL.glVertex3f(x2-tx,y2-ty,0);
				GL.glVertex3f(x2+tx,y2+ty,0);
			}
		}
		
		GL.glEnd();
		
		float step = width <= 12.5f ?  5 : 180 / (float)Math.ceil(width / 2.5); 
		
		// start cap
		if (lineCaps) {
			float dx = points[2] - points[0];
			float dy = points[3] - points[1];
			float fang = (float) Math.toDegrees(Math.atan2(dy,dx)) + 90;
			
			if ((dx != 0) || (dy != 0)) {
				GL.glBegin(SGL.GL_TRIANGLE_FAN);
				bindColor(0);
				GL.glVertex2f(points[0], points[1]);
				for (int i=0;i<180+step;i+=step) {
					float ang = (float) Math.toRadians(fang+i);
					GL.glVertex2f(points[0]+((float) (Math.cos(ang) * width)), 
								  points[1]+((float) (Math.sin(ang) * width)));
				}
				GL.glEnd();
			}
		}
		
		// end cap
		if (lineCaps) {
			float dx = points[(count*2)-2] - points[(count*2)-4];
			float dy = points[(count*2)-1] - points[(count*2)-3];
			float fang = (float) Math.toDegrees(Math.atan2(dy,dx)) - 90;
			
			if ((dx != 0) || (dy != 0)) {
				GL.glBegin(SGL.GL_TRIANGLE_FAN);
				bindColor(count-1);
				GL.glVertex2f(points[(count*2)-2], points[(count*2)-1]);
				for (int i=0;i<180+step;i+=step) {
					float ang = (float) Math.toRadians(fang+i);
					GL.glVertex2f(points[(count*2)-2]+((float) (Math.cos(ang) * width)), 
								  points[(count*2)-1]+((float) (Math.sin(ang) * width)));
				}
				GL.glEnd();
			}
		}
	}

	
	private void bindColor(int index) {
		if (index < cpt) {
			if (renderHalf) {
				GL.glColor4f(colours[(index*4)]*0.5f, colours[(index*4)+1]*0.5f,
						 	 colours[(index*4)+2]*0.5f, colours[(index*4)+3]*0.5f);
			} else {
				GL.glColor4f(colours[(index*4)], colours[(index*4)+1],
							 colours[(index*4)+2], colours[(index*4)+3]);
			}
		}
	}
	
	
	public void color(float r, float g, float b, float a) {
		if (width == 1) {
			def.color(r,g,b,a);
			return;
		}
		
		colours[(pts*4)] = r;
		colours[(pts*4)+1] = g;
		colours[(pts*4)+2] = b;
		colours[(pts*4)+3] = a;
		cpt++;
	}

	public boolean applyGLLineFixes() {
		if (width == 1) {
			return def.applyGLLineFixes();
		}
		
		return def.applyGLLineFixes();
	}
}
