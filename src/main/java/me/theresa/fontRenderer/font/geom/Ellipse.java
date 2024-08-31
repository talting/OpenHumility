package me.theresa.fontRenderer.font.geom;

import me.theresa.fontRenderer.font.log.FastTrig;

import java.util.ArrayList;


public class Ellipse extends Shape {
    
    protected static final int DEFAULT_SEGMENT_COUNT = 50;
    
    
    private final int segmentCount;
    
    private float radius1;
    
    private float radius2;

    
    public Ellipse(float centerPointX, float centerPointY, float radius1, float radius2) {
        this(centerPointX, centerPointY, radius1, radius2, DEFAULT_SEGMENT_COUNT);
    }

    
    public Ellipse(float centerPointX, float centerPointY, float radius1, float radius2, int segmentCount) {
        this.x = centerPointX - radius1;
        this.y = centerPointY - radius2;
        this.radius1 = radius1;
        this.radius2 = radius2;
        this.segmentCount = segmentCount;
        checkPoints();
    }

    
    public void setRadii(float radius1, float radius2) {
    	setRadius1(radius1);
    	setRadius2(radius2);
    }

    
    public float getRadius1() {
        return radius1;
    }

    
    public void setRadius1(float radius1) {
    	if (radius1 != this.radius1) {
	        this.radius1 = radius1;
	        pointsDirty = true;
    	}
    }

    
    public float getRadius2() {
        return radius2;
    }

    
    public void setRadius2(float radius2) {
    	if (radius2 != this.radius2) {
	        this.radius2 = radius2;
	        pointsDirty = true;
    	}
    }

    
    protected void createPoints() {
        ArrayList tempPoints = new ArrayList();

        maxX = -Float.MIN_VALUE;
        maxY = -Float.MIN_VALUE;
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;

        float start = 0;
        float end = 359;
        
        float cx = x + radius1;
        float cy = y + radius2;
        
        int step = 360 / segmentCount;
        
        for (float a=start;a<=end+step;a+=step) {
            float ang = a;
            if (ang > end) {
                ang = end;
            }
            float newX = (float) (cx + (FastTrig.cos(Math.toRadians(ang)) * radius1));
            float newY = (float) (cy + (FastTrig.sin(Math.toRadians(ang)) * radius2));

            if(newX > maxX) {
                maxX = newX;
            }
            if(newY > maxY) {
                maxY = newY;
            }
            if(newX < minX) {
            	minX = newX;
            }
            if(newY < minY) {
            	minY = newY;
            }
            
            tempPoints.add(newX);
            tempPoints.add(newY);
        }
        points = new float[tempPoints.size()];
        for(int i=0;i<points.length;i++) {
            points[i] = (Float) tempPoints.get(i);
        }
    }

    
    public Shape transform(Transform transform) {
        checkPoints();
        
        Polygon resultPolygon = new Polygon();

        float[] result = new float[points.length];
        transform.transform(points, 0, result, 0, points.length / 2);
        resultPolygon.points = result;
        resultPolygon.checkPoints();

        return resultPolygon;
    }

    
    protected void findCenter() {
        center = new float[2];
        center[0] = x + radius1;
        center[1] = y + radius2;
    }

    
    protected void calculateRadius() {
        boundingCircleRadius = Math.max(radius1, radius2);
    }
}
