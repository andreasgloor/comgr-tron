package tron;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.formats.IModelReader.Options;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.DoubleLinkedList;

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
	private Vec3 direction2 = new Vec3(0, 1, 0);
	private Vec3 position = new Vec3(0, -1, 0);
	private Vec3 position2 = new Vec3(0, 1, 0);
	private final Mat4 scale = Mat4.scale(0.005f);
	private Mat4 rotation = Mat4.rotate(0, Vec3.Z);
	private Mat4 rotation2 = Mat4.rotate(180, Vec3.Z);
	private final List<IMesh> falcon, falcon2;
	private final BoundingBox bbBuilding, bbElevator;	
	private ICamera cam, cam2;
	
	private boolean fixCameraPos = false;
	private boolean hasTurned = false;
	private boolean hasTurned2 = false;
	private boolean hasLevelChanged = false;
	private boolean hasLevelChanged2 = false;
	private int ticksToIgnoreCamMove = 0;
	private int ticksToIgnoreCamMove2 = 0;
    private double time_last = 0;
	
	private enum ElevatorFace { FRONT, BACK, LEFT, RIGHT };
	private ElevatorFace potentialElevatorFaceToHit = null;
	
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
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.LEFT : ElevatorFace.RIGHT;
			direction = direction.add(new Vec3(angle > 0 ? -1 : 1, -1, 0));
			//System.out.println("old direction: UP" + "| new direction: " + (angle < 0 ? "RIGHT" : "LEFT") + " | face to hit: " + potentialElevatorFaceToHit);
		} else if(direction.y < 0) {
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.RIGHT : ElevatorFace.LEFT;
		    direction = direction.add(new Vec3(angle > 0 ? 1 : -1, 1, 0));
		    //System.out.println("old direction: DOWN" + "| new direction: " + (angle < 0 ? "LEFT" : "RIGHT") + " | face to hit: " + potentialElevatorFaceToHit);
		} else if(direction.x > 0) {
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.BACK : ElevatorFace.FRONT;
			direction = direction.add(new Vec3(-1, angle > 0 ? 1 : -1, 0));
	        //System.out.println("old direction: RIGHT" + " | new direction: " + (angle < 0 ? "DOWN" : "UP") + " | face to hit: " + potentialElevatorFaceToHit);
		} else {
		    potentialElevatorFaceToHit = angle < 0 ? ElevatorFace.FRONT : ElevatorFace.BACK;
			direction = direction.add(new Vec3(1, angle > 0 ? -1 : 1, 0));
	        //System.out.println("old direction: LEFT" + "| new direction: " + (angle < 0 ? "UP" : "DOWN") + " | face to hit: " + potentialElevatorFaceToHit);
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
        
        if(bbElevator.intersects2D(bbFalcon)) {
            if(potentialElevatorFaceToHit != null && potentialElevatorFaceToHit.equals(ElevatorFace.FRONT)) {
                position = position.add(new Vec3(0, 1, 3.05));
                falcon.forEach(mesh -> {
                    mesh.setPosition(position);
                });                
                cam.setPosition(position.add(new Vec3(-direction.x*100, -direction.y*100, 0.5)));
            }
        }
	}
	
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
		
		cam = getCamera(getViews().get(0));
		cam2 = getCamera(getViews().get(1));
				
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
	
	
	private Map<Integer, DoubleLinkedList<Tail>> tails = new HashMap<Integer, DoubleLinkedList<Tail>>();
	
	/**
	 * Initialize Tails
	 */
	private void InitTails() {
		for (int i = 0; i < 2; i++ ) {
			tails.put(i, new DoubleLinkedList<Tail>());
		}
	}
	
	/**
	 * Calculate
	 * @param position of falcon
	 */
	public void CalculateFalconTail(Vec3 position, int falconId) {
		DoubleLinkedList<Tail> falconTail = tails.get(falconId);
		//System.out.println("hasTurned: " + hasTurned + " hasLevelChagnged " + hasLevelChanged + " isEmpty " + tails.isEmpty());
		if(hasTurned || hasLevelChanged || tails.isEmpty()) {
			// Create new Tail 
			falconTail.addFirst(new Tail(position, position));
		} else {
			  Tail currentTail = (Tail) falconTail.getFirst();
			  currentTail.setEnd(position);
		}
		
		// Resize Tail at the End
		Tail lastTail = (Tail) falconTail.getLast();
		Vec3 tailDistance = lastTail.getStart().subtract(lastTail.getEnd());
		if((tailDistance.x == 0 && tailDistance.y < 40 && falconTail.Length() > 1) || (tailDistance.y == 0 && tailDistance.x < 40 && falconTail.Length() > 1)) {
			falconTail.removeLast();
		} else {
			Vec3 dist = new Vec3(tailDistance.x*0.8,tailDistance.y*0.8, tailDistance.z*0.8);
			lastTail.setStart(lastTail.getEnd().add(dist));
		}
		//System.out.println("new start" + ((Tail)tails.getLast()).getStart() + " end: " + ((Tail)tails.getLast()).getEnd());
		// RepaintTail(falconId);
	}
	
	/**
	 * Resize / Transform Tail
	 */
	private void RepaintTail(int falconId) {
		DoubleLinkedList<Tail> falconTail = tails.get(falconId);
		if(falconTail.Length() == 1) {
			Tail first = (Tail)falconTail.getFirst();
			first.getMesh().setTransform(Mat4.scale(1,1,1));
			
		} else {
			Tail first = (Tail)falconTail.getFirst(); 
			first.getMesh().setTransform(Mat4.scale(1,1,1));
			Tail last = (Tail)falconTail.getLast(); 
			last.getMesh().setTransform(Mat4.scale(1,1,1));
		}
	}
	
	private void Test() {
		List<IMesh> beam = new ArrayList<>();
		final URL beamblue = getClass().getResource("/models/beam.obj");
        try {
			new ObjReader(beamblue, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> beam.add(mesh));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        getScene().add3DObjects(beam);
		
		beam.forEach(mesh -> mesh.setTransform(Mat4.scale(10000, 1, 1)));
		beam.forEach(mesh -> mesh.setPosition(new Vec3(3,3,0)));
		beam.forEach(mesh -> mesh.getGeometry().modify((id, colors) -> {
			for(int i = 0; i < colors.length; ++i) {
				colors[i][3] = 0.2f;
			}
		}));
		
	
	}
	
}
