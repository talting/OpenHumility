package me.theresa.fontRenderer.font;

import me.theresa.fontRenderer.font.opengl.renderer.Renderer;
import me.theresa.fontRenderer.font.opengl.renderer.SGL;

import java.io.Serializable;
import java.nio.FloatBuffer;


public class Color implements Serializable {
	
	private static final long serialVersionUID = 1393939L;
	
	protected transient SGL GL = Renderer.get();
	
    public static final Color transparent = new Color(0.0f,0.0f,0.0f,0.0f);
	
	public static final Color white = new Color(1.0f,1.0f,1.0f,1.0f);
	
	public static final Color yellow = new Color(1.0f,1.0f,0,1.0f);
	
	public static final Color red = new Color(1.0f,0,0,1.0f);
	
	public static final Color blue = new Color(0,0,1.0f,1.0f);
	
	public static final Color green = new Color(0,1.0f,0,1.0f);
	
	public static final Color black = new Color(0,0,0,1.0f);
	
	public static final Color gray = new Color(0.5f,0.5f,0.5f,1.0f);
	
	public static final Color cyan = new Color(0,1.0f,1.0f,1.0f);
	
	public static final Color darkGray = new Color(0.3f,0.3f,0.3f,1.0f);
	
	public static final Color lightGray = new Color(0.7f,0.7f,0.7f,1.0f);
	
    public final static Color pink      = new Color(255, 175, 175, 255);
	
    public final static Color orange 	= new Color(255, 200, 0, 255);
	
    public final static Color magenta	= new Color(255, 0, 255, 255);
    
	
	public float r;
	
	public float g;
	
	public float b;
	
	public float a = 1.0f;
	
	
	public Color(Color color) {
		r = color.r;
		g = color.g;
		b = color.b;
		a = color.a;
	}

	
	public Color(FloatBuffer buffer) {
		this.r = buffer.get();
		this.g = buffer.get();
		this.b = buffer.get();
		this.a = buffer.get();
	}
	
	
	public Color(float r,float g,float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1;
	}

	
	public Color(float r,float g,float b,float a) {
		this.r = Math.min(r, 1);
		this.g = Math.min(g, 1);
		this.b = Math.min(b, 1);
		this.a = Math.min(a, 1);
	}

	
	public Color(int r,int g,int b) {
		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
		this.a = 1;
	}

	
	public Color(int r,int g,int b,int a) {
		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
		this.a = a / 255.0f;
	}
	
	
	public Color(int value) {
		int r = (value & 0x00FF0000) >> 16;
		int g = (value & 0x0000FF00) >> 8;
		int b =	(value & 0x000000FF);
		int a = (value & 0xFF000000) >> 24;
				
		if (a < 0) {
			a += 256;
		}
		if (a == 0) {
			a = 255;
		}
		
		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
		this.a = a / 255.0f;
	}
	
	
	public static Color decode(String nm) {
		return new Color(Integer.decode(nm));
	}
	
	
	public void bind() {
		GL.glColor4f(r,g,b,a);
	}
	
	
	public int hashCode() {
		return ((int) (r+g+b+a)*255);
	}
	
	
	public boolean equals(Object other) {
		if (other instanceof Color) {
			Color o = (Color) other;
			return ((o.r == r) && (o.g == g) && (o.b == b) && (o.a == a));
		}
		
		return false;
	}
	
	
	public String toString() {
		return "Color ("+r+","+g+","+b+","+a+")";
	}

	
	public Color darker() {
		return darker(0.5f);
	}
	
	
	public Color darker(float scale) {
        scale = 1 - scale;

        return new Color(r * scale,g * scale,b * scale,a);
	}

	
	public Color brighter() {
		return brighter(0.2f);
	}

	
	public int getRed() {
		return (int) (r * 255);
	}

	
	public int getGreen() {
		return (int) (g * 255);
	}

	
	public int getBlue() {
		return (int) (b * 255);
	}

	
	public int getAlpha() {
		return (int) (a * 255);
	}
	
	
	public int getRedByte() {
		return (int) (r * 255);
	}

	
	public int getGreenByte() {
		return (int) (g * 255);
	}

	
	public int getBlueByte() {
		return (int) (b * 255);
	}

	
	public int getAlphaByte() {
		return (int) (a * 255);
	}
	
	
	public Color brighter(float scale) {
        scale += 1;

        return new Color(r * scale,g * scale,b * scale,a);
	}
	
	
	public Color multiply(Color c) {
		return new Color(r * c.r, g * c.g, b * c.b, a * c.a);
	}

	
	public void add(Color c) {
		r += c.r;
		g += c.g;
		b += c.b;
		a += c.a;
	}
	
	
	public void scale(float value) {
		r *= value;
		g *= value;
		b *= value;
		a *= value;
	}
	
	
	public Color addToCopy(Color c) {
		Color copy = new Color(r,g,b,a);
		copy.r += c.r;
		copy.g += c.g;
		copy.b += c.b;
		copy.a += c.a;
		
		return copy;
	}
	
	
	public Color scaleCopy(float value) {
		Color copy = new Color(r,g,b,a);
		copy.r *= value;
		copy.g *= value;
		copy.b *= value;
		copy.a *= value;
		
		return copy;
	}
}
