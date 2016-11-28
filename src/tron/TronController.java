package tron;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class TronController extends DefaultController {
	private static final String[] HELP = { 
		//@formatter:off
		"Controls", 
		"", 
		"Arrows: change direction", 
		"Tab: change camera view",
		"Space bar: action button" 
		//@formatter:on
	};
	
	private Vec3 direction = new Vec3(0,-0.01,0);
	private Vec3 position = Vec3.ZERO;
	private final Mat4 scale = Mat4.scale(0.005f);
	private Mat4 rotation = Mat4.rotate(0, Vec3.Z);
	private final List<IMesh> falcon;
	
	private boolean fixCameraPos = false;

	public TronController(List<IMesh> falcon) {
		this.falcon = falcon;
	}

	@Override
	public void keyPressed(IKeyEvent e) {
		switch (e.getKey()) {
		//case GLFW.GLFW_KEY_UP:
		//case GLFW.GLFW_KEY_DOWN:
		case GLFW.GLFW_KEY_RIGHT:
			turn(-90);
			break;
		case GLFW.GLFW_KEY_LEFT:
			turn(90);
			break;
		case GLFW.GLFW_KEY_H:
			printHelp(HELP);
			break;
			
		case GLFW.GLFW_KEY_TAB:
			fixCameraPos = !fixCameraPos;
			break;
		default:
			super.keyPressed(e);
		}
	}
	
	private void turn(int angle) {
		rotation = Mat4.multiply(rotation, Mat4.rotate(angle, Vec3.Z));
		if(direction.y > 0) {
			direction = direction.add(new Vec3(angle > 0 ? -0.01 : 0.01, -0.01, 0));
		} else if(direction.y < 0) {
			direction = direction.add(new Vec3(angle > 0 ? 0.01 : -0.01, 0.01, 0));
		} else if(direction.x > 0) {
			direction = direction.add(new Vec3(-0.01, angle > 0 ? 0.01 : -0.01, 0));
		} else {
			direction = direction.add(new Vec3(0.01, angle > 0 ? -0.01 : 0.01, 0));
		}
		System.out.println(direction);
	}
	
	public void animationTick() {
		Mat4 tr = Mat4.multiply(scale,rotation);
		position = position.add(direction);
		falcon.forEach(mesh -> {
			mesh.setTransform(tr);
			mesh.setPosition(position);
		});
		
		
		ICamera cam = getCamera(getCurrentView());
		if(fixCameraPos) {
			cam.setPosition(new Vec3(0,0,10));
		} else {
			cam.setPosition(position.add(new Vec3(-direction.x*100, -direction.y*100, 0.5)));
		}
		cam.setTarget(position.add(new Vec3(0, 0, 0.25)));
	}
}
