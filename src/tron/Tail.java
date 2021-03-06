package tron;

import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

public class Tail {
	private Vec3 start;
	private Vec3 end;
	private IMesh mesh = null; 
	private BoundingBox boundingBox = null;
	private Vec3 direction;
	
	public Tail(Vec3 start, Vec3 end, Vec3 direction) {
		this.setStart(start); 
		this.setEnd(end);
		this.setDirection(direction);
	}
	
	public void setDirection(Vec3 direction) {
		this.direction = direction;
	}
	
	public Vec3 getDirection() {
		return this.direction;
	}
	
	public Vec3 getEnd() {
		return end;
	}
	public void setEnd(Vec3 end) {
		this.end = end;
	}
	public Vec3 getStart() {
		return start;
	}
	public void setStart(Vec3 start) {
		this.start = start;
	}
	
	public void setMesh(IMesh mesh) {
		this.mesh = mesh;
		this.boundingBox = mesh.getBounds();
	}
	
	public void updateBoundingBox() {
		this.boundingBox = this.mesh.getBounds();
	}
	
	public IMesh getMesh() {
		return this.mesh; 
	}
	
	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}
}
