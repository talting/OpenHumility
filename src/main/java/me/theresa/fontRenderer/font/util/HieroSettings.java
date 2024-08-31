
package me.theresa.fontRenderer.font.util;

import me.theresa.fontRenderer.font.SlickException;
import me.theresa.fontRenderer.font.effect.ConfigurableEffect;
import me.theresa.fontRenderer.font.effect.ConfigurableEffect.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class HieroSettings {
	
	private int fontSize = 12;
	
	private boolean bold = false;
	
	private boolean italic = false;
	
	private int paddingTop;
	
	private int paddingLeft;
	
	private int paddingBottom;
	
	private int paddingRight;
	
	private int paddingAdvanceX;
	
	private int paddingAdvanceY;
	
	private int glyphPageWidth = 512;
	
	private int glyphPageHeight = 512;
	
	private final List effects = new ArrayList();

	
	public HieroSettings() {
	}

	
	public HieroSettings(String hieroFileRef) throws SlickException {
		this(ResourceLoader.getResourceAsStream(hieroFileRef));
	}
	
	
	public HieroSettings(InputStream in) throws SlickException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.length() == 0) continue;
				String[] pieces = line.split("=", 2);
				String name = pieces[0].trim();
				String value = pieces[1];
				if (name.equals("font.size")) {
					fontSize = Integer.parseInt(value);
				} else if (name.equals("font.bold")) {
					bold = Boolean.parseBoolean(value);
				} else if (name.equals("font.italic")) {
					italic = Boolean.parseBoolean(value);
				} else if (name.equals("pad.top")) {
					paddingTop = Integer.parseInt(value);
				} else if (name.equals("pad.right")) {
					paddingRight = Integer.parseInt(value);
				} else if (name.equals("pad.bottom")) {
					paddingBottom = Integer.parseInt(value);
				} else if (name.equals("pad.left")) {
					paddingLeft = Integer.parseInt(value);
				} else if (name.equals("pad.advance.x")) {
					paddingAdvanceX = Integer.parseInt(value);
				} else if (name.equals("pad.advance.y")) {
					paddingAdvanceY = Integer.parseInt(value);
				} else if (name.equals("glyph.page.width")) {
					glyphPageWidth = Integer.parseInt(value);
				} else if (name.equals("glyph.page.height")) {
					glyphPageHeight = Integer.parseInt(value);
				} else if (name.equals("effect.class")) {
					try {
						effects.add(Class.forName(value).newInstance());
					} catch (Exception ex) {
						throw new SlickException("Unable to create effect instance: " + value, ex);
					}
				} else if (name.startsWith("effect.")) {
					// Set an effect value on the last added effect.
					name = name.substring(7);
					ConfigurableEffect effect = (ConfigurableEffect)effects.get(effects.size() - 1);
					List values = effect.getValues();
					for (Object o : values) {
						Value effectValue = (Value) o;
						if (effectValue.getName().equals(name)) {
							effectValue.setString(value);
							break;
						}
					}
					effect.setValues(values);
				}
			}
			reader.close();
		} catch (Exception ex) {
			throw new SlickException("Unable to load Hiero font file", ex);
		}
	}

	
	public int getPaddingTop () {
		return paddingTop;
	}

	
	public void setPaddingTop(int paddingTop) {
		this.paddingTop = paddingTop;
	}

	
	public int getPaddingLeft() {
		return paddingLeft;
	}

	
	public void setPaddingLeft(int paddingLeft) {
		this.paddingLeft = paddingLeft;
	}

	
	public int getPaddingBottom() {
		return paddingBottom;
	}

	
	public void setPaddingBottom(int paddingBottom) {
		this.paddingBottom = paddingBottom;
	}

	
	public int getPaddingRight() {
		return paddingRight;
	}

	
	public void setPaddingRight(int paddingRight) {
		this.paddingRight = paddingRight;
	}

	
	public int getPaddingAdvanceX() {
		return paddingAdvanceX;
	}

	
	public void setPaddingAdvanceX(int paddingAdvanceX) {
		this.paddingAdvanceX = paddingAdvanceX;
	}

	
	public int getPaddingAdvanceY() {
		return paddingAdvanceY;
	}

	
	public void setPaddingAdvanceY(int paddingAdvanceY) {
		this.paddingAdvanceY = paddingAdvanceY;
	}

	
	public int getGlyphPageWidth() {
		return glyphPageWidth;
	}

	
	public void setGlyphPageWidth(int glyphPageWidth) {
		this.glyphPageWidth = glyphPageWidth;
	}

	
	public int getGlyphPageHeight() {
		return glyphPageHeight;
	}

	
	public void setGlyphPageHeight(int glyphPageHeight) {
		this.glyphPageHeight = glyphPageHeight;
	}

	
	public int getFontSize() {
		return fontSize;
	}

	
	public void setFontSize (int fontSize) {
		this.fontSize = fontSize;
	}

	
	public boolean isBold () {
		return bold;
	}

	
	public void setBold (boolean bold) {
		this.bold = bold;
	}

	
	public boolean isItalic () {
		return italic;
	}

	
	public void setItalic (boolean italic) {
		this.italic = italic;
	}

	
	public List getEffects() {
		return effects;
	}

	public void save(File file) throws IOException {
		PrintStream out = new PrintStream(new FileOutputStream(file));
		out.println("font.size=" + fontSize);
		out.println("font.bold=" + bold);
		out.println("font.italic=" + italic);
		out.println();
		out.println("pad.top=" + paddingTop);
		out.println("pad.right=" + paddingRight);
		out.println("pad.bottom=" + paddingBottom);
		out.println("pad.left=" + paddingLeft);
		out.println("pad.advance.x=" + paddingAdvanceX);
		out.println("pad.advance.y=" + paddingAdvanceY);
		out.println();
		out.println("glyph.page.width=" + glyphPageWidth);
		out.println("glyph.page.height=" + glyphPageHeight);
		out.println();
		for (Object item : effects) {
			ConfigurableEffect effect = (ConfigurableEffect) item;
			out.println("effect.class=" + effect.getClass().getName());
			for (Object o : effect.getValues()) {
				Value value = (Value) o;
				out.println("effect." + value.getName() + "=" + value.getString());
			}
			out.println();
		}
		out.close();
	}
}
