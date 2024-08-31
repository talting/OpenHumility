package me.theresa.fontRenderer.font;

import me.theresa.fontRenderer.font.effect.Renderable;
import me.theresa.fontRenderer.font.log.Log;
import org.lwjgl.Sys;

import java.util.ArrayList;


public class Animation implements Renderable {
	
	private ArrayList frames = new ArrayList();
	
	private int currentFrame = -1;
	
	private long nextChange = 0;
	
	private boolean stopped = false;
	
	private long timeLeft;
	
	private float speed = 1.0f;
	
	private int stopAt = -2;
	
	private long lastUpdate;
	
	private boolean firstUpdate = true;
	
	private boolean autoUpdate = true;
	
	private int direction = 1;
	
	private boolean pingPong;
	
	private boolean loop = true;
	
	private SpriteSheet spriteSheet = null;
	
	
	public Animation() {
		this(true);
	}

	
	public Animation(Image[] frames, int duration) {
		this(frames, duration, true);
	}
	
	
	public Animation(Image[] frames, int[] durations) {
		this(frames, durations, true);
	}
	
	
	public Animation(boolean autoUpdate) {
		currentFrame = 0;
		this.autoUpdate = autoUpdate;
	}

	
	public Animation(Image[] frames, int duration, boolean autoUpdate) {
		for (Image frame : frames) {
			addFrame(frame, duration);
		}
		currentFrame = 0;
		this.autoUpdate = autoUpdate;
	}
	
	
	public Animation(Image[] frames, int[] durations, boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		if (frames.length != durations.length) {
			throw new RuntimeException("There must be one duration per frame");
		}
		
		for (int i=0;i<frames.length;i++) {
			addFrame(frames[i], durations[i]);
		}
		currentFrame = 0;
	}
	
	
	public Animation(SpriteSheet frames, int duration) {
		this(frames, 0,0,frames.getHorizontalCount()-1,frames.getVerticalCount()-1,true,duration,true);
	}
	
	
	public Animation(SpriteSheet frames, int x1, int y1, int x2, int y2, boolean horizontalScan, int duration, boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		
		if (!horizontalScan) {
			for (int x=x1;x<=x2;x++) {
				for (int y=y1;y<=y2;y++) {
					addFrame(frames.getSprite(x, y), duration);
				}
			}
		} else {
			for (int y=y1;y<=y2;y++) {
				for (int x=x1;x<=x2;x++) {
					addFrame(frames.getSprite(x, y), duration);
				}
			}
		}
	}
	
	
	public Animation(SpriteSheet ss, int[] frames, int[] duration){
		spriteSheet = ss;
	    int x = -1;
	    int y = -1;
	    
	    for(int i = 0; i < frames.length/2; i++){
	       x = frames[i*2];
	       y = frames[i*2 + 1];
	       addFrame(duration[i], x, y);
	    }
	}
	
	
	public void addFrame(int duration, int x, int y){
	   if (duration == 0) {
	      Log.error("Invalid duration: "+duration);
	      throw new RuntimeException("Invalid duration: "+duration);
	   }
	 
	    if (frames.isEmpty()) {
	      nextChange = (int) (duration / speed);
	   }
	   
	   frames.add(new Frame(duration, x, y));
	   currentFrame = 0;      
	} 
	
	
	public void setAutoUpdate(boolean auto) {
		this.autoUpdate = auto;
	}
	
	
	public void setPingPong(boolean pingPong) {
		this.pingPong = pingPong;
	}
	
	
	public boolean isStopped() {
		return stopped;
	}

	
	public void setSpeed(float spd) {
		if (spd > 0) {
			// Adjust nextChange
			nextChange = (long) (nextChange * speed / spd);

			speed = spd;
		} 
	}

	
	public float getSpeed() {
	   return speed;
	}

	
	
	public void stop() {
		if (frames.size() == 0) {
			return;
		}
		timeLeft = nextChange;
		stopped = true;
	}

	
	public void start() {
		if (!stopped) {
			return;
		}
		if (frames.size() == 0) {
			return;
		}
		stopped = false;
		nextChange = timeLeft;
	}
	
	
	public void restart() {
		if (frames.size() == 0) {
			return;
		}
		stopped = false;
		currentFrame = 0;
		nextChange = (int) (((Frame) frames.get(0)).duration / speed);
		firstUpdate = true;
		lastUpdate = 0;
	}
	
	
	public void addFrame(Image frame, int duration) {
		if (duration == 0) {
			Log.error("Invalid duration: "+duration);
			throw new RuntimeException("Invalid duration: "+duration);
		}

	    if (frames.isEmpty()) {
			nextChange = (int) (duration / speed);
		} 
	    
		frames.add(new Frame(frame, duration));
		currentFrame = 0;
	}

	
	public void draw() {
		draw(0,0);
	}

	
	public void draw(float x,float y) {
		draw(x,y,getWidth(),getHeight());
	}

	
	public void draw(float x,float y, Color filter) {
		draw(x,y,getWidth(),getHeight(), filter);
	}
	
	
	public void draw(float x,float y,float width,float height) {
		draw(x,y,width,height,Color.white);
	}
	
	
	public void draw(float x,float y,float width,float height, Color col) {
		if (frames.size() == 0) {
			return;
		}
		
		if (autoUpdate) {
			long now = getTime();
			long delta = now - lastUpdate;
			if (firstUpdate) {
				delta = 0;
				firstUpdate = false;
			}
			lastUpdate = now;
			nextFrame(delta);
		}
		
		Frame frame = (Frame) frames.get(currentFrame);
		frame.image.draw(x,y,width,height, col);
	}

	
	public void renderInUse(int x, int y){
	   if (frames.size() == 0) {
	      return;
	   }
	   
	   if (autoUpdate) {
	      long now = getTime();
	      long delta = now - lastUpdate;
	      if (firstUpdate) {
	         delta = 0;
	         firstUpdate = false;
	      }
	      lastUpdate = now;
	      nextFrame(delta);
	   }
	   
	   Frame frame = (Frame) frames.get(currentFrame);
	   spriteSheet.renderInUse(x, y, frame.x, frame.y);
	} 
	
	
	public int getWidth() {
		return ((Frame) frames.get(currentFrame)).image.getWidth();
	}

	
	public int getHeight() {
		return ((Frame) frames.get(currentFrame)).image.getHeight();
	}
	
	
	public void drawFlash(float x,float y,float width,float height) {
		drawFlash(x,y,width,height, Color.white);
	}
	
	
	public void drawFlash(float x,float y,float width,float height, Color col) {
		if (frames.size() == 0) {
			return;
		}
		
		if (autoUpdate) {
			long now = getTime();
			long delta = now - lastUpdate;
			if (firstUpdate) {
				delta = 0;
				firstUpdate = false;
			}
			lastUpdate = now;
			nextFrame(delta);
		}
		
		Frame frame = (Frame) frames.get(currentFrame);
		frame.image.drawFlash(x,y,width,height,col);
	}
	
	
	public void updateNoDraw() {
		if (autoUpdate) {
			long now = getTime();
			long delta = now - lastUpdate;
			if (firstUpdate) {
				delta = 0;
				firstUpdate = false;
			}
			lastUpdate = now;
			nextFrame(delta);
		}
	}
	
	
	public void update(long delta) {
		nextFrame(delta);
	}
	
	
	public int getFrame() {
		return currentFrame;
	}
	
	
	public void setCurrentFrame(int index) {
		currentFrame = index;
	}
	
	
	public Image getImage(int index) {
		Frame frame = (Frame) frames.get(index);
		return frame.image;
	}
	
	
	public int getFrameCount() {
		return frames.size();
	}
	
	
	public Image getCurrentFrame() {
		Frame frame = (Frame) frames.get(currentFrame);
		return frame.image;
	}
	
	
	private void nextFrame(long delta) {
		if (stopped) {
			return;
		}
		if (frames.size() == 0) {
			return;
		}
		
		nextChange -= delta;
		
		while (nextChange < 0 && (!stopped)) {
			if (currentFrame == stopAt) {
				stopped = true;
				break;
			}
			if ((currentFrame == frames.size() - 1) && (!loop) && (!pingPong)) {
	            stopped = true; 
				break;
			}
			currentFrame = (currentFrame + direction) % frames.size();
			
			if (pingPong) {
				if (currentFrame <= 0) {
					currentFrame = 0;
					direction = 1;   
					if (!loop) {            
                        stopped = true;            
                        break;     
                    }       
				}
				else if (currentFrame >= frames.size()-1) {
					currentFrame = frames.size()-1;
					direction = -1;
				}
			}
			int realDuration = (int) (((Frame) frames.get(currentFrame)).duration / speed);
			nextChange = nextChange + realDuration;
		}
	}
	
	
	public void setLooping(boolean loop) {
		this.loop = loop;
	}
	
	
	private long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	
	public void stopAt(int frameIndex) {
		stopAt = frameIndex; 
	}
	
	
	public int getDuration(int index) {
		return ((Frame) frames.get(index)).duration;
	}
	
	
	public void setDuration(int index, int duration) {
		((Frame) frames.get(index)).duration = duration;
	}
	
	
	public int[] getDurations() {
		int[] durations = new int[frames.size()];
		for (int i=0;i<frames.size();i++) {
			durations[i] = getDuration(i);
		}
		
		return durations;
	}
	
	
	
	public String toString() {
		StringBuilder res = new StringBuilder("[Animation (" + frames.size() + ") ");
		for (Object o : frames) {
			Frame frame = (Frame) o;
			res.append(frame.duration).append(",");
		}
		
		res.append("]");
		return res.toString();
	}
	
	
	public Animation copy() {
		Animation copy = new Animation();
		
		copy.spriteSheet = spriteSheet;
		copy.frames = frames;
		copy.autoUpdate = autoUpdate;
		copy.direction = direction;
		copy.loop = loop;
		copy.pingPong = pingPong;
		copy.speed = speed;
		
		return copy;
	}
	
	
	private class Frame {
		
		public Image image;
		
		public int duration; 
		
		public int x = -1;
		
		public int y = -1; 
	
		
		public Frame(Image image, int duration) {
			this.image = image;
			this.duration = duration;
		}
		
		public Frame(int duration, int x, int y) {
			this.image = spriteSheet.getSubImage(x, y);
			this.duration = duration;
			this.x = x;
			this.y = y;
		} 
	}
}
