package me.theresa.fontRenderer.font;

import me.theresa.fontRenderer.font.opengl.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SpriteSheet extends Image {

    private final int tw;

    private final int th;

    private int margin = 0;

    private Image[][] subImages;

    private int spacing;

    private final Image target;


    public SpriteSheet(URL ref, int tw, int th) throws SlickException, IOException {
        this(new Image(ref.openStream(), ref.toString(), false), tw, th);
    }


    public SpriteSheet(Image image, int tw, int th) {
        super(image);
		
		this.target = image;
		this.tw = tw;
		this.th = th;
		
		// call init manually since constructing from an image will have previously initialised
		// from incorrect values 
		initImpl();
	}

	
	public SpriteSheet(Image image,int tw,int th,int spacing,int margin) {
		super(image);
		
		this.target = image;
		this.tw = tw;
		this.th = th;
		this.spacing = spacing;
		this.margin = margin; 

		// call init manually since constructing from an image will have previously initialised
		// from incorrect values 
		initImpl();
	}

	
	public SpriteSheet(Image image,int tw,int th,int spacing) {
		this(image,tw,th,spacing,0);
	}
	
	
	public SpriteSheet(String ref,int tw,int th, int spacing) throws SlickException {
		this(ref,tw,th,null,spacing);
	}
	
	
	public SpriteSheet(String ref,int tw,int th) throws SlickException {
		this(ref,tw,th,null);
	}

	
	public SpriteSheet(String ref,int tw,int th, Color col) throws SlickException {
		this(ref, tw, th, col, 0);
	}
	
	
	public SpriteSheet(String ref,int tw,int th, Color col, int spacing) throws SlickException {
		super(ref, false, FILTER_NEAREST, col);

		this.target = this;
		this.tw = tw;
		this.th = th;
		this.spacing = spacing;
	}
	
	
	public SpriteSheet(String name, InputStream ref,int tw,int th) throws SlickException {
		super(ref,name,false);

		this.target = this;
		this.tw = tw;
		this.th = th;
	}
	
	
	protected void initImpl() {
		if (subImages != null) {
			return;
		}
		
		int tilesAcross = ((getWidth()-(margin*2) - tw) / (tw + spacing)) + 1;
		int tilesDown = ((getHeight()-(margin*2) - th) / (th + spacing)) + 1; 
		if ((getHeight() - th) % (th+spacing) != 0) {
			tilesDown++;
		}
		
		subImages = new Image[tilesAcross][tilesDown];
		for (int x=0;x<tilesAcross;x++) {
			for (int y=0;y<tilesDown;y++) {
				subImages[x][y] = getSprite(x,y);
			}
		}
	}

	
	public Image getSubImage(int x, int y) {
		init();
		
		if ((x < 0) || (x >= subImages.length)) {
			throw new RuntimeException("SubImage out of sheet bounds: "+x+","+y);
		}
		if ((y < 0) || (y >= subImages[0].length)) {
			throw new RuntimeException("SubImage out of sheet bounds: "+x+","+y);
		}
		
		return subImages[x][y];
	}
	
	
	public Image getSprite(int x, int y) {
		target.init();
		initImpl();

		if ((x < 0) || (x >= subImages.length)) {
			throw new RuntimeException("SubImage out of sheet bounds: "+x+","+y);
		}
		if ((y < 0) || (y >= subImages[0].length)) {
			throw new RuntimeException("SubImage out of sheet bounds: "+x+","+y);
		}

		return target.getSubImage(x*(tw+spacing) + margin, y*(th+spacing) + margin,tw,th); 
	}
	
	
	public int getHorizontalCount() {
		target.init();
		initImpl();
		
		return subImages.length;
	}
	
	
	public int getVerticalCount() {
		target.init();
		initImpl();
		
		return subImages[0].length;
	}
	
	
	public void renderInUse(int x,int y,int sx,int sy) {
		subImages[sx][sy].drawEmbedded(x, y, tw, th);
	}

	
	public void endUse() {
		if (target == this) {
			super.endUse();
			return;
		}
		target.endUse();
	}

	
	public void startUse() {
		if (target == this) {
			super.startUse();
			return;
		}
		target.startUse();
	}
	
	public void setTexture(Texture texture) {
		if (target == this) {
			super.setTexture(texture);
			return;
		}
		target.setTexture(texture);
	}
}
