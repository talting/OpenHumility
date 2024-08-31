package me.theresa.fontRenderer.font.opengl;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class PNGImageData implements LoadableImageData {
	
	private int width;
	
	private int height;
	
	private int texHeight;
	
	private int texWidth;
	
	private PNGDecoder decoder;
	
	private int bitDepth;
	
	private ByteBuffer scratch;
	
    
	public int getDepth() {
		return bitDepth;
	}

	
	public ByteBuffer getImageBufferData() {
		return scratch;
	}

	
	public int getTexHeight() {
		return texHeight;
	}

	
	public int getTexWidth() {
		return texWidth;
	}

	
	public ByteBuffer loadImage(InputStream fis) throws IOException {
		return loadImage(fis, false, null);
	}

	
	public ByteBuffer loadImage(InputStream fis, boolean flipped, int[] transparent) throws IOException {
		return loadImage(fis, flipped, false, transparent);
	}

	
	public ByteBuffer loadImage(InputStream fis, boolean flipped, boolean forceAlpha, int[] transparent) throws IOException {
		if (transparent != null) {
			forceAlpha = true;
			throw new IOException("Transparent color not support in custom PNG Decoder");
		}
		
		PNGDecoder decoder = new PNGDecoder(fis);
		
		if (!decoder.isRGB()) {
			throw new IOException("Only RGB formatted images are supported by the PNGLoader");
		}
		
		width = decoder.getWidth();
		height = decoder.getHeight();
		texWidth = get2Fold(width);
		texHeight = get2Fold(height);
		
		int perPixel = decoder.hasAlpha() ? 4 : 3;
		bitDepth = decoder.hasAlpha() ? 32 : 24;
		
		// Get a pointer to the image memory
		scratch = BufferUtils.createByteBuffer(texWidth * texHeight * perPixel);
		decoder.decode(scratch, texWidth * perPixel, perPixel == 4 ? PNGDecoder.RGBA : PNGDecoder.RGB);

		if (height < texHeight-1) {
			int topOffset = (texHeight-1) * (texWidth*perPixel);
			int bottomOffset = (height-1) * (texWidth*perPixel);
			for (int x=0;x<texWidth;x++) {
				for (int i=0;i<perPixel;i++) {
					scratch.put(topOffset+x+i, scratch.get(x+i));
					scratch.put(bottomOffset+(texWidth*perPixel)+x+i, scratch.get(bottomOffset+x+i));
				}
			}
		}
		if (width < texWidth-1) {
			for (int y=0;y<texHeight;y++) {
				for (int i=0;i<perPixel;i++) {
					scratch.put(((y+1)*(texWidth*perPixel))-perPixel+i, scratch.get(y*(texWidth*perPixel)+i));
					scratch.put((y*(texWidth*perPixel))+(width*perPixel)+i, scratch.get((y*(texWidth*perPixel))+((width-1)*perPixel)+i));
				}
			}
		}
		
		if (!decoder.hasAlpha() && forceAlpha) {
			ByteBuffer temp = BufferUtils.createByteBuffer(texWidth * texHeight * 4);
			for (int x=0;x<texWidth;x++) {
				for (int y=0;y<texHeight;y++) {
					int srcOffset = (y*3)+(x*texHeight*3);
					int dstOffset = (y*4)+(x*texHeight*4);
					
					temp.put(dstOffset, scratch.get(srcOffset));
					temp.put(dstOffset+1, scratch.get(srcOffset+1));
					temp.put(dstOffset+2, scratch.get(srcOffset+2));
					if ((x < getHeight()) && (y < getWidth())) {
						temp.put(dstOffset+3, (byte) 255);
					} else {
						temp.put(dstOffset+3, (byte) 0);
					}
				}
			}
			
			bitDepth = 32;
			scratch = temp;
		}
			
		scratch.position(0);
		
		return scratch;
	}
	
	
	private int toInt(byte b) {
		if (b < 0) {
			return 256+b;
		}
		
		return b;
	}
	
    
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }
    
	public void configureEdging(boolean edging) {
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}

