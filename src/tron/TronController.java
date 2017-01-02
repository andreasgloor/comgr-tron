package tron;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

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
	
	private static final double MOVE_PER_SECOND = 0.66;
	
	private Vec3 direction = new Vec3(0, -1, 0);
	private Vec3 direction2 = new Vec3(0, -1, 0);
	private Vec3 position = Vec3.ZERO;
	private Vec3 position2 = Vec3.ZERO;
	private final Mat4 scale = Mat4.scale(0.005f);
	private Mat4 rotation = Mat4.rotate(0, Vec3.Z);
	private Mat4 rotation2 = Mat4.rotate(0, Vec3.Z);
	private final List<IMesh> falcon, falcon2;
	private final BoundingBox bbBuilding, bbElevator;	
	
	private boolean fixCameraPos = false;
	private boolean hasTurned = false;
	private boolean hasTurned2 = false;
	private int ticksToIgnoreCamMove = 0;
	private int ticksToIgnoreCamMove2 = 0;

	public TronController(List<IMesh> falcon, List<IMesh> falcon2, BoundingBox bbBuilding, BoundingBox bbElevator) {
		this.falcon = falcon;
		this.falcon2 = falcon2;
		this.bbBuilding = bbBuilding;
		this.bbElevator = bbElevator;
	}

    @Override
	public void keyPressed(IKeyEvent e) {
		switch (e.getKey()) {
		//case GLFW.GLFW_KEY_UP:
		//case GLFW.GLFW_KEY_DOWN:
		case GLFW.GLFW_KEY_RIGHT:
			turn(-90);
			hasTurned = true;
			break;
		case GLFW.GLFW_KEY_LEFT:
			turn(90);
			hasTurned = true;
			break;
		//case GLFW.GLFW_KEY_W:
		//case GLFW.GLFW_KEY_S:
		case GLFW.GLFW_KEY_D:
			turn2(-90);
			hasTurned2 = true;
			break;
		case GLFW.GLFW_KEY_A:
			turn2(90);
			hasTurned2 = true;
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
			direction = direction.add(new Vec3(angle > 0 ? -1 : 1, -1, 0));
		} else if(direction.y < 0) {
			direction = direction.add(new Vec3(angle > 0 ? 1 : -1, 1, 0));
		} else if(direction.x > 0) {
			direction = direction.add(new Vec3(-1, angle > 0 ? 1 : -1, 0));
		} else {
			direction = direction.add(new Vec3(1, angle > 0 ? -1 : 1, 0));
		}
	}
	private void turn2(int angle) {
		rotation2 = Mat4.multiply(rotation2, Mat4.rotate(angle, Vec3.Z));
		if(direction2.y > 0) {
			direction2 = direction2.add(new Vec3(angle > 0 ? -1 : 1, -1, 0));
		} else if(direction2.y < 0) {
			direction2 = direction2.add(new Vec3(angle > 0 ? 1 : -1, 1, 0));
		} else if(direction2.x > 0) {
			direction2 = direction2.add(new Vec3(-1, angle > 0 ? 1 : -1, 0));
		} else {
			direction2 = direction2.add(new Vec3(1, angle > 0 ? -1 : 1, 0));
		}
	}
	
	private void detectCollisions() {
	    BoundingBox bbFalcon = new BoundingBox();
        for (IMesh mesh : falcon) {
            bbFalcon.add(mesh.getBounds());
        }
        
        boolean isOutOfMap = !bbBuilding.contains2D(bbFalcon);
        boolean isInElevator = bbElevator.intersects2D(bbFalcon);
        //System.out.println(isOutOfMap + " | " + isInElevator);
	}
	private double time_last = 0;
	public void animationTick(double time, double interval) {
		if(time_last == 0){
			 time_last = time;
			 return;
		}
		double dt = (time - time_last) * MOVE_PER_SECOND;
		time_last = time;
		
		if(getViews().size() < 2){
			return;
		}

		Mat4 tr = Mat4.multiply(scale, rotation);
		position = position.add(direction.scale((float)dt));
		falcon.forEach(mesh -> {
			mesh.setTransform(tr);
			mesh.setPosition(position);
		});
		
		Mat4 tr2 = Mat4.multiply(scale, rotation2);
		position2 = position2.add(direction2.scale((float)dt));
		falcon2.forEach(mesh -> {
			mesh.setTransform(tr2);
			mesh.setPosition(position2);
		});
		
		detectCollisions();
		
		ICamera cam = getCamera(getViews().get(0));
		ICamera cam2 = getCamera(getViews().get(1));
		
		/*ICamera cam = getCamera(getCurrentView());
		ICamera cam2 = getCamera(getCurrentView());*/

		if(hasTurned) {
		    hasTurned = false;
		    ticksToIgnoreCamMove = 100;
		}
		if(hasTurned2) {
		    hasTurned2 = false;
		    ticksToIgnoreCamMove2 = 100;
		}
		
		if(fixCameraPos) {
			cam.setPosition(new Vec3(0, 0, 5));
			cam2.setPosition(new Vec3(0, 0, 5));
		} else {
			if(ticksToIgnoreCamMove == 0) {
		        cam.setPosition(position.add(new Vec3(-direction.x, -direction.y, 0.5)));
			} else {
			    Vec3 oldCamPos = cam.getPosition();
			    Vec3 futureCamPos = position.add(new Vec3(-direction.x, -direction.y, 0.5));
			    Vec3 difference = futureCamPos.subtract(oldCamPos);
			    difference = difference.scale((1/(float) ticksToIgnoreCamMove--));
			    cam.setPosition(oldCamPos.add(difference));
			}
			
			if(ticksToIgnoreCamMove2 == 0) {
				cam2.setPosition(position2.add(new Vec3(-direction2.x, -direction2.y, 0.5)));
			} else {
			    Vec3 oldCamPos = cam2.getPosition();
			    Vec3 futureCamPos = position2.add(new Vec3(-direction2.x, -direction2.y, 0.5));
			    Vec3 difference = futureCamPos.subtract(oldCamPos);
			    difference = difference.scale((1/(float) ticksToIgnoreCamMove2--));
			    cam2.setPosition(oldCamPos.add(difference));
			}
		}
		
		cam.setTarget(position.add(new Vec3(0, 0, 0.25)));
		cam2.setTarget(position2.add(new Vec3(0, 0, 0.25)));
	}
}
