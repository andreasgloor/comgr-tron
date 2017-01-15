package tron;

import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

public class BonusItem {
	public enum Type {
		RemoveTail, ExtendTail, ShrinkTail
	}
	
	private Vec3 position = null;
	private RGBA color = null;
	private IMesh mesh = null;
	private float angle = 0;
	private float speed = 0.8f;
	private Type type = null;
	
	public BonusItem(Vec3 position, RGBA color, Type type) {
		this.position = position;
		this.color = color;
		this.type = type;
	}

	public IMesh getMesh() {
		if(mesh == null) {
			mesh = MeshUtilities.createCube(new ColorMaterial(color));
			
			mesh.setPosition(position);
		} 
		return mesh;
	}
	
	public void animate() {
		angle += speed;

        Mat4 transform = Mat4.multiply(Mat4.rotate(angle, Vec3.Z), Mat4.translate(position), Mat4.scale(0.1f));
		mesh.setTransform(transform);
	}
	
	public Type getType() {
		return this.type;
	}
	
	public BoundingBox getBoundingBox() {
		return this.mesh.getBounds();
	}
}
