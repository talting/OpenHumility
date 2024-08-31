package me.theresa.fontRenderer.font.geom;

import java.util.ArrayList;


public class GeomUtil {
	
	public float EPSILON = 0.0001f;
	
	public float EDGE_SCALE = 1f;
	
	public int MAX_POINTS = 10000;
	
	public GeomUtilListener listener;
	
	
	public Shape[] subtract(Shape target, Shape missing) {	
		target = target.transform(new Transform());
		missing = missing.transform(new Transform());

		int count = 0;
		for (int i=0;i<target.getPointCount();i++) {
			if (missing.contains(target.getPoint(i)[0], target.getPoint(i)[1])) {
				count++;
			}
		}
		
		if (count == target.getPointCount()) {
			return new Shape[0];
		}
		
		if (!target.intersects(missing)) {
			return new Shape[] {target};
		}
		
		int found = 0;
		for (int i=0;i<missing.getPointCount();i++) {
			if (target.contains(missing.getPoint(i)[0], missing.getPoint(i)[1])) {
				if (onPath(target, missing.getPoint(i)[0], missing.getPoint(i)[1])) {
					found++;
				}
			}
		}
		for (int i=0;i<target.getPointCount();i++) {
			if (missing.contains(target.getPoint(i)[0], target.getPoint(i)[1])) {
				if (onPath(missing, target.getPoint(i)[0], target.getPoint(i)[1]))
				{
					found++;
				}
			}
		}
		
		if (found < 1) {
			return new Shape[] {target};
		}
		
		return combine(target, missing, true);
	}

	
	private boolean onPath(Shape path, float x, float y) {
		for (int i=0;i<path.getPointCount()+1;i++) {
			int n = rationalPoint(path, i+1);
			Line line = getLine(path, rationalPoint(path, i), n);
			if (line.distance(new Vector2f(x,y)) < EPSILON*100) {
				return false;
			}
		}
		
		return true;
	}
	
	
	public void setListener(GeomUtilListener listener) {
		this.listener = listener;
	}
	
	
	public Shape[] union(Shape target, Shape other) {
		target = target.transform(new Transform());
		other = other.transform(new Transform());
		
		if (!target.intersects(other)) {
			return new Shape[] {target, other};
		}
		
		// handle the case where intersects is true but really we're talking
		// about edge points
		boolean touches = false;
		int buttCount = 0;
		for (int i=0;i<target.getPointCount();i++) {
			if (other.contains(target.getPoint(i)[0], target.getPoint(i)[1])) {
				if (!other.hasVertex(target.getPoint(i)[0], target.getPoint(i)[1])) {
					touches = true;
					break;
				}
			}
			if (other.hasVertex(target.getPoint(i)[0], target.getPoint(i)[1])) {
				buttCount++;
			} 
		}
		for (int i=0;i<other.getPointCount();i++) {
			if (target.contains(other.getPoint(i)[0], other.getPoint(i)[1])) {
				if (!target.hasVertex(other.getPoint(i)[0], other.getPoint(i)[1])) {
					touches = true;
					break;
				}
			}
		}
		
		if ((!touches) && (buttCount < 2)) {
			return new Shape[] {target, other};
		}
		
		// so they are definitely touching, consider the union
		return combine(target, other, false);
	}
	
	
	private Shape[] combine(Shape target, Shape other, boolean subtract) {
		if (subtract) {
			ArrayList shapes = new ArrayList();
			ArrayList used = new ArrayList();
			
			// remove any points that are contianed in the shape we're removing, these
			// are implicitly used
			for (int i=0;i<target.getPointCount();i++) {
				float[] point = target.getPoint(i);
				if (other.contains(point[0], point[1])) {
					used.add(new Vector2f(point[0], point[1]));
					if (listener != null) {
						listener.pointExcluded(point[0], point[1]);
					}
				}
			}

			for (int i=0;i<target.getPointCount();i++) {
				float[] point = target.getPoint(i);
				Vector2f pt = new Vector2f(point[0], point[1]);
				
				if (!used.contains(pt)) {
					Shape result = combineSingle(target, other, true, i);
					shapes.add(result);
					for (int j=0;j<result.getPointCount();j++) {
						float[] kpoint = result.getPoint(j);
						Vector2f kpt = new Vector2f(kpoint[0], kpoint[1]);
						used.add(kpt);
					}
				}
			}
			
			return (Shape[]) shapes.toArray(new Shape[0]);
		} else {
			for (int i=0;i<target.getPointCount();i++) {
				if (!other.contains(target.getPoint(i)[0], target.getPoint(i)[1])) {
					if (!other.hasVertex(target.getPoint(i)[0], target.getPoint(i)[1])) {
						Shape shape = combineSingle(target, other, false, i);
						return new Shape[] {shape};
					}
				}
			}
			
			return new Shape[] {other};
		}
	}
	
	
	private Shape combineSingle(Shape target, Shape missing, boolean subtract, int start) {
		Shape current = target;
		Shape other = missing;
		int point = start;
		int dir = 1;
		
		Polygon poly = new Polygon();
		boolean first = true;
		
		int loop = 0;
		
		// while we've not reached the same point
		float px = current.getPoint(point)[0];
		float py = current.getPoint(point)[1];
		
		while (!poly.hasVertex(px, py) || (first) || (current != target)) {
			first = false;
			loop++;
			if (loop > MAX_POINTS) {
				break;
			}
			
			// add the current point to the result shape
			poly.addPoint(px,py);
			if (listener != null) {
				listener.pointUsed(px,py);
			}

			// if the line between the current point and the next one intersect the
			// other shape work out where on the other shape and start traversing it's 
			// path instead
			Line line = getLine(current, px, py, rationalPoint(current, point+dir));
			HitResult hit = intersect(other, line);
			
			if (hit != null) {
				Line hitLine = hit.line;
				Vector2f pt = hit.pt;
				px = pt.x;
				py = pt.y;
				
				if (listener != null) {
					listener.pointIntersected(px,py);
				}
				
				if (other.hasVertex(px, py)) {
					point = other.indexOf(pt.x,pt.y);
					dir = 1;
					px = pt.x;
					py = pt.y;
					
					Shape temp = current;
					current = other;
					other = temp;
					continue;
				}
				
				float dx = hitLine.getDX() / hitLine.length();
				float dy = hitLine.getDY() / hitLine.length();
				dx *= EDGE_SCALE;
				dy *= EDGE_SCALE;
				
				if (current.contains(pt.x + dx, pt.y + dy)) {
					// the point is the next one, we need to take the first and traverse
					// the path backwards
					if (subtract) {
						if (current == missing) {
							point = hit.p2;
							dir = -1;
						} else {
							point = hit.p1;
							dir = 1;
						}
					} else {
						if (current == target) {
							point = hit.p2;
							dir = -1;
						} else {
							point = hit.p2;
							dir = -1;
						}
					}
					
					// swap the shapes over, we'll traverse the other one 
					Shape temp = current;
					current = other;
					other = temp;
				} else if (current.contains(pt.x - dx, pt.y - dy)) {
					if (subtract) {
						if (current == target) {
							point = hit.p2;
							dir = -1;
						} else {
							point = hit.p1;
							dir = 1;
						}
					} else {
						if (current == missing) {
							point = hit.p1;
							dir = 1;
						} else {
							point = hit.p1;
							dir = 1;
						}
					}
					
					// swap the shapes over, we'll traverse the other one 
					Shape temp = current;
					current = other;
					other = temp;
				} else {
					// give up
					if (subtract) {
						break;
					} else {
						point = hit.p1;
						dir = 1;
						Shape temp = current;
						current = other;
						other = temp;
						
						point = rationalPoint(current, point+dir);
						px = current.getPoint(point)[0];
						py = current.getPoint(point)[1];
					}
				}
			} else {
				// otherwise just move to the next point in the current shape
				point = rationalPoint(current, point+dir);
				px = current.getPoint(point)[0];
				py = current.getPoint(point)[1];
			}
		}

		poly.addPoint(px,py);
		if (listener != null) {
			listener.pointUsed(px,py);
		}
		
		return poly;
	}
	
	
	public HitResult intersect(Shape shape, Line line) {
		float distance = Float.MAX_VALUE;
		HitResult hit = null;
		
		for (int i=0;i<shape.getPointCount();i++) {
			int next = rationalPoint(shape, i+1);
			Line local = getLine(shape, i, next);
		
			Vector2f pt = line.intersect(local, true);
			if (pt != null) {
				float newDis = pt.distance(line.getStart());
				if ((newDis < distance) && (newDis > EPSILON)) {
					hit = new HitResult();
					hit.pt = pt;
					hit.line = local;
					hit.p1 = i;
					hit.p2 = next;
					distance = newDis;
				}
			}
		}
		
		return hit;
	}
	
	
	public static int rationalPoint(Shape shape, int p) {
		while (p < 0) {
			p += shape.getPointCount();
		}
		while (p >= shape.getPointCount()) {
			p -=  shape.getPointCount();
		}
		
		return p;
	}
	
	
	public Line getLine(Shape shape, int s, int e) {
		float[] start = shape.getPoint(s);
		float[] end = shape.getPoint(e);

        return new Line(start[0],start[1],end[0],end[1]);
	}

	
	public Line getLine(Shape shape, float sx, float sy, int e) {
		float[] end = shape.getPoint(e);

        return new Line(sx,sy,end[0],end[1]);
	}
	
	
	public static class HitResult {
		
		public Line line;
		
		public int p1;
		
		public int p2;
		
		public Vector2f pt;
	}
}
