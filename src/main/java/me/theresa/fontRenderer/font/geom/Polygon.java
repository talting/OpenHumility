package me.theresa.fontRenderer.font.geom;

import java.util.ArrayList;

public class Polygon extends Shape {
	
	private boolean allowDups = false;
	
	private boolean closed = true;
	
    
    public Polygon(float[] points) {
        int length = points.length;

        this.points = new float[length];
        maxX = -Float.MIN_VALUE;
        maxY = -Float.MIN_VALUE;
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        x = Float.MAX_VALUE;
        y = Float.MAX_VALUE;
        
        for(int i=0;i<length;i++) {
            this.points[i] = points[i];
            if(i % 2 == 0) {
                if(points[i] > maxX) {
                    maxX = points[i];
                }
                if(points[i] < minX) {
                	minX = points[i];
                }
                if(points[i] < x) {
                    x = points[i];
                }
            }
            else {
                if(points[i] > maxY) {
                    maxY = points[i];
                }
                if(points[i] < minY) {
                	minY = points[i];
                }
                if(points[i] < y) {
                    y = points[i];
                }
            }
        }
        
        findCenter();
        calculateRadius();
        pointsDirty = true;
    }
    
    public Polygon(){
        points = new float[0];
        maxX = -Float.MIN_VALUE;
        maxY = -Float.MIN_VALUE;
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
    }
    
    
    public void setAllowDuplicatePoints(boolean allowDups) {
    	this.allowDups = allowDups;
    }
    
    
    public void addPoint(float x, float y) {
    	if (hasVertex(x,y) && (!allowDups)) {
    		return;
    	}
    	
        ArrayList tempPoints = new ArrayList();
        for (float point : points) {
            tempPoints.add(point);
        }
        tempPoints.add(x);
        tempPoints.add(y);
        int length = tempPoints.size();
        points = new float[length];
        for(int i=0;i<length;i++) {
            points[i] = (Float) tempPoints.get(i);
        }
        if(x > maxX) {
            maxX = x;
        }
        if(y > maxY) {
            maxY = y;
        }
        if(x < minX) {
            minX = x;
        }
        if(y < minY) {
            minY = y;
        }
        findCenter();
        calculateRadius();
        
        pointsDirty = true;
    }


    
    public Shape transform(Transform transform) {
        checkPoints();
        
        Polygon resultPolygon = new Polygon();

        float[] result = new float[points.length];
        transform.transform(points, 0, result, 0, points.length / 2);
        resultPolygon.points = result;
        resultPolygon.findCenter();
        resultPolygon.closed = closed;

        return resultPolygon;
    }
    
    
    public void setX(float x) {
        super.setX(x);
        
        pointsDirty = false;
    }
    
    
    public void setY(float y) {
        super.setY(y);
        
        pointsDirty = false;
    }
    
    
    protected void createPoints() {}
    
    
	public boolean closed() {
		return closed;
	}
	
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	
	public Polygon copy() {
		float[] copyPoints = new float[points.length];
		System.arraycopy(points, 0, copyPoints, 0, copyPoints.length);
		
		return new Polygon(copyPoints);
	}
}
