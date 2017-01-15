package tron;

import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.DoubleLinkedList;
import tron.helper.ElevatorFace;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.formats.IModelReader.Options;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;

public class Player {
	private static final float MAX_SPEED = 2.5f;
	private static final float MIN_SPEED = 0.5f;
	private static final float SPEED_STEP = 0.05f;
	
    private final List<IMesh> playerObj = new ArrayList<>();
    private final Mat4 scale;
    
    private Vec3 position;
    private Vec3 direction;
    private Mat4 rotation;
    private float speed = 1.25f;
    private ElevatorFace possibleFaceToHit;
    private BoundingBox boundingBox;
    private double playerLength;
    
    private int floorLevel = 0;
    private boolean isDestroyed = false;
    
    private ICamera playerCamera;
    private boolean hasTurned = false;
    private boolean hasLevelChanged = false;
    private boolean camIsFixed = false;
    private int ticksToIgnoreCamMove = 0;
    
    private RGBA beamColor; 
    public DoubleLinkedList<Tail> tail;
    
    public Player(URL playerObj, Vec3 position, Vec3 direction, Mat4 rotation, ElevatorFace possibleFaceToHit, Mat4 scale, RGBA beamColor) throws IOException {
    	new ObjReader(playerObj, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> this.playerObj.add(mesh));
        Mat4 tr = Mat4.multiply(scale, rotation);
        this.playerObj.forEach(mesh -> {
            mesh.setTransform(tr);
        });
        
        this.position = position;
        this.direction = direction;
        this.rotation = rotation;
        this.scale = scale;
        this.possibleFaceToHit = possibleFaceToHit;
        
        calculateBB();
        this.playerLength = boundingBox.getExtentY();
        this.beamColor = beamColor;
        tail = new DoubleLinkedList<Tail>();
    }
    
    public void move(double deltaTime, BoundingBox bbBuilding, BoundingBox bbElevator) {
        if(!isDestroyed) {
            Mat4 tr = Mat4.multiply(scale, rotation);
            position = position.add(direction.scale((float) deltaTime*speed));
            
            playerObj.forEach(mesh -> {
                mesh.setTransform(tr);
                mesh.setPosition(position);
            });
            
            calculateBB();
            updateCamera(bbBuilding, bbElevator);
        }
    }
    
    public void speedUp() {
    	speed = Math.min(speed+SPEED_STEP, MAX_SPEED);
    }
    public void speedDown() {
    	speed = Math.max(speed-SPEED_STEP, MIN_SPEED);
    }
    
    public void turn(int angle) {
        rotation = Mat4.multiply(rotation, Mat4.rotate(angle, Vec3.Z));
        if(direction.y > 0) {
            possibleFaceToHit = angle < 0 ? ElevatorFace.LEFT : ElevatorFace.RIGHT;
            direction = direction.add(new Vec3(angle > 0 ? -1 : 1, -1, 0));
            //System.out.println("old direction: UP" + "| new direction: " + (angle < 0 ? "RIGHT" : "LEFT") + " | face to hit: " + potentialElevatorFaceToHit);
        } else if(direction.y < 0) {
            possibleFaceToHit = angle < 0 ? ElevatorFace.RIGHT : ElevatorFace.LEFT;
            direction = direction.add(new Vec3(angle > 0 ? 1 : -1, 1, 0));
            //System.out.println("old direction: DOWN" + "| new direction: " + (angle < 0 ? "LEFT" : "RIGHT") + " | face to hit: " + potentialElevatorFaceToHit);
        } else if(direction.x > 0) {
            possibleFaceToHit = angle < 0 ? ElevatorFace.BACK : ElevatorFace.FRONT;
            direction = direction.add(new Vec3(-1, angle > 0 ? 1 : -1, 0));
            //System.out.println("old direction: RIGHT" + " | new direction: " + (angle < 0 ? "DOWN" : "UP") + " | face to hit: " + potentialElevatorFaceToHit);
        } else {
            possibleFaceToHit = angle < 0 ? ElevatorFace.FRONT : ElevatorFace.BACK;
            direction = direction.add(new Vec3(1, angle > 0 ? -1 : 1, 0));
            //System.out.println("old direction: LEFT" + "| new direction: " + (angle < 0 ? "UP" : "DOWN") + " | face to hit: " + potentialElevatorFaceToHit);
        }
    }
    
    public void changeLevel(double elevatorWidth, boolean isGoingUp) {
        Vec3 shiftVector;
        if(isGoingUp) {
            shiftVector = new Vec3(0, elevatorWidth + playerLength, TronController.FLOOR_HEIGHT);
            floorLevel++;
        } else {
            shiftVector = new Vec3(0, elevatorWidth - playerLength, -TronController.FLOOR_HEIGHT);
            floorLevel--;
        }
        
        position = position.add(shiftVector);
        playerObj.forEach(mesh -> {
            mesh.setPosition(position);
        });
        
        ticksToIgnoreCamMove = 0;
        hasLevelChanged = true;
    }
    
    private void calculateBB() {
        BoundingBox bb = new BoundingBox();
        
        for (IMesh mesh : playerObj) {
            bb.add(mesh.getBounds());
        }
        
        boundingBox = bb;
    }
    
    private void updateCamera(BoundingBox bbBuilding, BoundingBox bbElevator) {
       if(hasTurned) {
            //hasTurned = false;
            ticksToIgnoreCamMove = 25;
        }
        
        if(camIsFixed) {
            playerCamera.setPosition(new Vec3(0, 0, 5));
        } else {
            if(ticksToIgnoreCamMove == 0) {
                playerCamera.setPosition(position.add(new Vec3(-direction.x, -direction.y, 0.5)));
            } else {
                Vec3 oldCamPos = playerCamera.getPosition();
                Vec3 futureCamPos = position.add(new Vec3(-direction.x, -direction.y, 0.5));
                Vec3 difference = futureCamPos.subtract(oldCamPos);
                difference = difference.scale((1/(float) ticksToIgnoreCamMove--));
                playerCamera.setPosition(oldCamPos.add(difference));            
            }
        }
        
        playerCamera.setTarget(position.add(new Vec3(0, 0, 0.25)));
    }
    
    public void CalculateFalconTail() {
    	if(hasTurned || hasLevelChanged || tail.isEmpty()) {
			// Create new Tail 
			tail.addFirst(new Tail(position, position));
		} else {
			  Tail currentTail = (Tail) tail.getFirst();
			  currentTail.setEnd(position);
		}
    	
    	/* reset flags */
    	if(hasTurned) hasTurned = false;
    	if(hasLevelChanged) hasLevelChanged = false;

		// Resize Tail at the End
    	// TODO: Change function
		/* Tail lastTail = (Tail) tail.getLast();
		Vec3 tailDistance = lastTail.getStart().subtract(lastTail.getEnd());
		if((tailDistance.x == 0 && tailDistance.y < 40 && tail.Length() > 1) || (tailDistance.y == 0 && tailDistance.x < 40 && tail.Length() > 1)) {
			tail.removeLast();
			System.out.println("remove");
		} else {
			Vec3 dist = new Vec3(tailDistance.x*0.8,tailDistance.y*0.8, tailDistance.z*0.8);
			lastTail.setStart(lastTail.getEnd().add(dist));
		}*/

		RepaintTail();
    }
    
    private final URL falconbeam = getClass().getResource("/models/beam.obj");
    private Vec3 beamSize = null;
	private void RepaintTail() {
		// First Tail
		Tail first = (Tail)tail.getFirst();
		IMesh beam = null;
		if (first.getMesh() == null) {
			try {
				beam = new ObjReader(falconbeam, Options.CONVERT_TO_Z_UP).getMeshes(new ColorMaterial(getBeamColor())).get(0);
				beamSize = beam.getBounds().getMax().subtract(beam.getBounds().getMin());
				beam.setPosition(new Vec3(first.getStart().x,first.getStart().y,0));	
				first.setMesh(beam);
				TronGame.controller.getScene().add3DObjects(first.getMesh());
			} catch (IOException e) { 
				// Ignore 
			}
		} else {
			beam = first.getMesh();

			Vec3 diff = first.getEnd().subtract(first.getStart());
			float distance = first.getStart().distance(first.getEnd());
			Vec3 factors = new Vec3(distance/beamSize.x,distance/beamSize.y,1);
			beam.setTransform(Mat4.scale(0.01f + factors.x*(diff.x/distance), 0.01f + (diff.y/distance)*factors.y,1));
	      	Vec3 dist = beam.getBounds().getMax().subtract(beam.getBounds().getMin());
			beam.setPosition(new Vec3(first.getEnd().x - (dist.x/2)*direction.x, first.getEnd().y- (dist.y/2)*direction.y, first.getEnd().z));
			first.updateBoundingBox();
		}

		// Last Tail
		if(tail.Length() > 1) {
			Tail last = (Tail)tail.getLast();
			// Todo: repaint last tail
			// last.getStart(),last.getEnd();
		}
		
		
	}

    /****** GETTER AND SETTER ******/
    public List<IMesh> getPlayerObj() {
        return playerObj;
    }
    
    public Vec3 getPosition() {
        return position;
    }

    public Vec3 getDirection() {
        return direction;
    }

    public ElevatorFace getPossibleFaceToHit() {
        return possibleFaceToHit;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public void setDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }
    
    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void setPlayerCamera(ICamera playerCamera) {
        this.playerCamera = playerCamera;
    }

    public ICamera getPlayerCamera() {
        return playerCamera;
    }

    public boolean hasTurned() {
        return hasTurned;
    }

    public void setTurned(boolean hasTurned) {
        this.hasTurned = hasTurned;
    }

    public void setCamFixed(boolean camIsFixed) {
        this.camIsFixed = camIsFixed;
    }
    
    public RGBA getBeamColor() {
    	return this.beamColor;
    }
    
    public List<BoundingBox> getTailBoundingBoxes(){
    	List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		List<Object> nodes = tail.getAll();
		for(int j = 0; j < nodes.size(); j++) {
			boundingBoxes.add(((Tail)nodes.get(j)).getBoundingBox());
		}
		return boundingBoxes;
    }
	
    
}