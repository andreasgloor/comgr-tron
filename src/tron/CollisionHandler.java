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
        player.setDestroyed(!bbBuilding.contains2D(player.getBoundingBox()));
        
        if(!player.isDestroyed()) {
            handleElevatorCollision(player);
        }
    }
    
    private void handleElevatorCollision(Player player) {
        if(bbElevator.intersects2D(player.getBoundingBox())) {            
            if(player.getPossibleFaceToHit().equals(ElevatorFace.FRONT) && player.getFloorLevel() <= 2) {
                player.changeLevel(bbElevator.getMaxX() - bbElevator.getMinX(), true);
                System.out.println(bbElevator.getMaxX() - bbElevator.getMinX());
            } else if(player.getPossibleFaceToHit().equals(ElevatorFace.BACK) && player.getFloorLevel() >= 1) {
                player.changeLevel((bbElevator.getMaxX() - bbElevator.getMinX()) * (-1), false);
                System.out.println((bbElevator.getMaxX() - bbElevator.getMinX()) * (-1));
            } else {
               player.setDestroyed(true); 
            }
        }
    }
}
