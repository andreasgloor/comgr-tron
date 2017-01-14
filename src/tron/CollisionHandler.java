package tron;

import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.ElevatorFace;

public class CollisionHandler {
    
    private final BoundingBox bbBuilding;
    private final BoundingBox bbElevator;
    
    public CollisionHandler(BoundingBox bbBuilding, BoundingBox bbElevator) {
        this.bbBuilding = bbBuilding;
        this.bbElevator = bbElevator;
    }
    
    public void detectCollisions(Player player) {
        player.setDestroyed(!bbBuilding.contains2D(player.getBoundingBox()));
        
        if(!player.isDestroyed()) {
            handleElevatorCollision(player);
            handleCameraCollision(player);
        }
    }
    
    private void handleElevatorCollision(Player player) {
        if(bbElevator.intersects2D(player.getBoundingBox())) {            
            if(player.getPossibleFaceToHit().equals(ElevatorFace.FRONT) && player.getFloorLevel() < 2) 
                player.changeLevel(bbElevator.getExtentY(), true);
            else if(player.getPossibleFaceToHit().equals(ElevatorFace.BACK) && player.getFloorLevel() > 0)
                player.changeLevel(bbElevator.getExtentY() * (-1), false);
            else
               player.setDestroyed(true);
        }
    }
    
    private void handleCameraCollision(Player player) {
        if(!bbBuilding.contains2D(player.getPlayerCamera().getPosition())) {
            float xOutside = 0;
            float yOutside = 0;
            
            switch(player.getPossibleFaceToHit()) {
                case FRONT:
                    yOutside = (player.getPlayerCamera().getPosition().y - bbBuilding.getMinY()) * (-1);
                    break;
                case BACK:
                    yOutside = (player.getPlayerCamera().getPosition().y - bbBuilding.getMaxY()) * (-1);
                    break;
                case LEFT:
                    xOutside = (player.getPlayerCamera().getPosition().x - bbBuilding.getMinX()) * (-1);
                    break;
                case RIGHT:
                    xOutside = (player.getPlayerCamera().getPosition().x - bbBuilding.getMaxX()) * (-1);
                    break;
            }
            
            player.getPlayerCamera().setPosition(player.getPlayerCamera().getPosition().add(new Vec3(xOutside, yOutside, 0)));
        }
        
        if(bbElevator.contains2D(player.getPlayerCamera().getPosition())) {
            float xInside = 0;
            float yInside = 0;
            
            switch(player.getPossibleFaceToHit()) {
                case FRONT:
                    yInside = (player.getPlayerCamera().getPosition().y - bbElevator.getMaxY()) * (-1);
                    break;
                case BACK:
                    yInside = (player.getPlayerCamera().getPosition().y - bbElevator.getMinY()) * (-1);
                    break;
                case LEFT:
                    xInside = (player.getPlayerCamera().getPosition().x - bbElevator.getMaxX()) * (-1);
                    break;
                case RIGHT:
                    xInside = (player.getPlayerCamera().getPosition().x - bbElevator.getMinX()) * (-1);
                    break;
            }
            
            player.getPlayerCamera().setPosition(player.getPlayerCamera().getPosition().add(new Vec3(xInside, yInside, 0)));
        }
    }
}
