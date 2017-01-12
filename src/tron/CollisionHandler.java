package tron;

import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.ElevatorFace;

public class CollisionHandler {
    
    private BoundingBox bbBuilding;
    private BoundingBox bbElevator;
    
    public CollisionHandler(BoundingBox bbBuilding, BoundingBox bbElevator) {
        this.bbBuilding = bbBuilding;
        this.bbElevator = bbElevator;
    }
    
    public void detectCollisions(Player player) {
//      boolean isOutOfMap = !bbBuilding.contains2D(bbPlayer);
        
        handleElevatorCollision(player);
    }
    
    private void handleElevatorCollision(Player player) {
        if(bbElevator.intersects2D(player.getBoundingBox())) {            
            if(player.getPossibleFaceToHit().equals(ElevatorFace.FRONT) && player.getFloorLevel() <= 2) {
                player.changeLevel(bbElevator.getMaxX() - bbElevator.getMinX(), true);
            } else if(player.getPossibleFaceToHit().equals(ElevatorFace.BACK) && player.getFloorLevel() >= 1) {
                //TODO: Going down too fast.
                player.changeLevel((bbElevator.getMaxX() - bbElevator.getMinX()) * (-1), false);
            } else {
               player.setDestroyed(true); 
            }
        }
    }
}
