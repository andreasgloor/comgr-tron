package tron;

import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.ElevatorFace;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.formats.IModelReader.Options;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;

public class Player {
    
    private final List<IMesh> playerObj = new ArrayList<>();
    private final Mat4 scale;
    
    private Vec3 position;
    private Vec3 direction;
    private Mat4 rotation;
    private ElevatorFace possibleFaceToHit;
    private BoundingBox boundingBox;
    
    private int floorLevel = 0;
    private boolean isDestroyed = false;
    
    private ICamera playerCamera;
    private boolean hasTurned = false;
    private boolean camIsFixed = false;
    private int ticksToIgnoreCamMove = 0;
    
    public Player(URL playerObj, Vec3 position, Vec3 direction, Mat4 rotation, ElevatorFace possibleFaceToHit,
                  Mat4 scale) throws IOException {
        new ObjReader(playerObj, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> this.playerObj.add(mesh));
        this.position = position;
        this.direction = direction;
        this.rotation = rotation;
        this.possibleFaceToHit = possibleFaceToHit;
        this.scale = scale;
        calculateBB();
    }
    
    public void move(double deltaTime) {
        if(!isDestroyed) {
            Mat4 tr = Mat4.multiply(scale, rotation);
            position = position.add(direction.scale((float) deltaTime));
            
            playerObj.forEach(mesh -> {
                mesh.setTransform(tr);
                mesh.setPosition(position);
            });
            
            calculateBB();
            updateCamera();
        }
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
        double zShift;
        if(isGoingUp) {
            zShift = TronController.FLOOR_HEIGHT;
            floorLevel++;
        }
        else {
            zShift = -TronController.FLOOR_HEIGHT;
            floorLevel--;
        }
        
        position = position.add(new Vec3(0, elevatorWidth + (boundingBox.getMaxY()-boundingBox.getMinY()), zShift));

        playerObj.forEach(mesh -> {
            mesh.setPosition(position);
        });
        
        playerCamera.setPosition(position.add(new Vec3(-direction.x, -direction.y, 0.5)));
        
    }
    
    private void calculateBB() {
        BoundingBox bb = new BoundingBox();
        
        for (IMesh mesh : playerObj) {
            bb.add(mesh.getBounds());
        }
        
        boundingBox = bb;
    }
    
    private void updateCamera() {
        if(hasTurned) {
            hasTurned = false;
            ticksToIgnoreCamMove = 100;
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

    public List<IMesh> getPlayerObj() {
        return playerObj;
    }
    
    public Vec3 getPosition() {
        return position;
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

    public void setPlayerCamera(ICamera playerCamera) {
        this.playerCamera = playerCamera;
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
}