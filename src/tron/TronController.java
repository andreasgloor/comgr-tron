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
	
	private Vec3 direction = new Vec3(0, -0.01, 0);
	private Vec3 position = new Vec3(0, -1, 0);
	private final Mat4 scale = Mat4.scale(0.005f);
	private Mat4 rotation = Mat4.rotate(0, Vec3.Z);
	private final List<IMesh> falcon;
	private final BoundingBox bbBuilding, bbElevator;
	private ICamera cam;
	
	private boolean fixCameraPos = false;
	private boolean hasTurned = false;
	private int ticksToIgnoreCamMove = 0;
	
	private enum ElevatorFace {FRONT, BACK, LEFT, RIGHT};
	private ElevatorFace potentialElevatorFaceToHit = null;

	public TronController(List<IMesh> falcon, BoundingBox bbBuilding, BoundingBox bbElevator) {
		this.falcon = falcon;
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
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.LEFT : ElevatorFace.RIGHT;
			direction = direction.add(new Vec3(angle > 0 ? -0.01 : 0.01, -0.01, 0));
			//System.out.println("old direction: UP" + "| new direction: " + (angle < 0 ? "RIGHT" : "LEFT") + " | face to hit: " + potentialElevatorFaceToHit);
		} else if(direction.y < 0) {
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.RIGHT : ElevatorFace.LEFT;
		    direction = direction.add(new Vec3(angle > 0 ? 0.01 : -0.01, 0.01, 0));
		    //System.out.println("old direction: DOWN" + "| new direction: " + (angle < 0 ? "LEFT" : "RIGHT") + " | face to hit: " + potentialElevatorFaceToHit);
		} else if(direction.x > 0) {
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.BACK : ElevatorFace.FRONT;
			direction = direction.add(new Vec3(-0.01, angle > 0 ? 0.01 : -0.01, 0));
	        //System.out.println("old direction: RIGHT" + " | new direction: " + (angle < 0 ? "DOWN" : "UP") + " | face to hit: " + potentialElevatorFaceToHit);
		} else {
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.FRONT : ElevatorFace.BACK;
			direction = direction.add(new Vec3(0.01, angle > 0 ? -0.01 : 0.01, 0));
	        //System.out.println("old direction: LEFT" + "| new direction: " + (angle < 0 ? "UP" : "DOWN") + " | face to hit: " + potentialElevatorFaceToHit);
		}
	}
	
	private void detectCollisions() {
	    BoundingBox bbFalcon = new BoundingBox();
        for (IMesh mesh : falcon) {
            bbFalcon.add(mesh.getBounds());
        }
        
        boolean isOutOfMap = !bbBuilding.contains2D(bbFalcon);
        
        if(bbElevator.intersects2D(bbFalcon)) {
            if(potentialElevatorFaceToHit != null && potentialElevatorFaceToHit.equals(ElevatorFace.FRONT)) {
                position = position.add(new Vec3(0, 2, 3.05));
                falcon.forEach(mesh -> {
                    mesh.setPosition(position);
                });                
                cam.setPosition(position.add(new Vec3(-direction.x*100, -direction.y*100, 0.5)));
            }
            else if(potentialElevatorFaceToHit != null && potentialElevatorFaceToHit.equals(ElevatorFace.BACK)) {
                position = position.add(new Vec3(0, -2, -3.05));
                falcon.forEach(mesh -> {
                    mesh.setPosition(position);
                });
                cam.setPosition(position.add(new Vec3(-direction.x*100, -direction.y*100, 0.5)));
            }
        }
	}
	
	public void animationTick(double time, double interval) {
		Mat4 tr = Mat4.multiply(scale, rotation);
		position = position.add(direction);
		falcon.forEach(mesh -> {
			mesh.setTransform(tr);
			mesh.setPosition(position);
		});
		
		detectCollisions();

		this.cam = getCamera(getCurrentView());
		
		if(hasTurned) {
		    hasTurned = false;
		    ticksToIgnoreCamMove = 100;
		}
		
		if(ticksToIgnoreCamMove == 0) {
		    if(fixCameraPos)
	            cam.setPosition(new Vec3(0, 0, 5));
	        else {
	            cam.setPosition(position.add(new Vec3(-direction.x*100, -direction.y*100, 0.5)));
	        }
		} else {
		    Vec3 oldCamPos = cam.getPosition();
		    Vec3 futureCamPos = position.add(new Vec3(-direction.x*100, -direction.y*100, 0.5));
		    Vec3 difference = futureCamPos.subtract(oldCamPos);
		    difference = difference.scale((1/(float) ticksToIgnoreCamMove--));
		    
		    cam.setPosition(oldCamPos.add(difference));
		}
		
		cam.setTarget(position.add(new Vec3(0, 0, 0.25)));
	}
}
