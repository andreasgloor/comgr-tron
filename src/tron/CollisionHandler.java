package tron;

import java.util.List;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.ElevatorFace;

public class CollisionHandler {
    
    private final BoundingBox bbBuilding;
    private final BoundingBox bbElevator;
    private final List<Player> players;
    private final List<BonusItem> bonusItems;
    
    public CollisionHandler(BoundingBox bbBuilding, BoundingBox bbElevator, List<BonusItem> bonusItems, List<Player> players) {
        this.bbBuilding = bbBuilding;
        this.bbElevator = bbElevator;
        this.players = players;
        this.bonusItems = bonusItems;
    }
    
    public void detectPlayerCollisions() {     
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                if(players.get(i).getBoundingBox().intersects2D(players.get(j).getBoundingBox())) {
                    players.get(i).setDestroyed(true);
                    players.get(j).setDestroyed(true);
                }                
            }
        }
    }
    
    public void detectTailCollisions() {
    	// Detect Collision 
    	for (int i = 0; i < players.size(); i++) {
    		
    		for(int j = 0; j < players.size(); j++) {
    			if (j == i) continue;
    			List<BoundingBox> boundingBoxes = players.get(j).getTailBoundingBoxes();
    			for(int x = 0; x < boundingBoxes.size(); x++) {
    				if(players.get(i).getBoundingBox().intersects2D(boundingBoxes.get(x))){
        				players.get(i).setDestroyed(true);
        				System.out.println("Tail Collision");
        			}
    			}
    			
    		} 
    	}
    }
    
    public void detectBonusItemCollisions() {
    	for (int i = 0; i < players.size(); i++) {
    		for(int j = 0; j < bonusItems.size(); j++) {
    			if(players.get(i).getBoundingBox().intersects2D(bonusItems.get(j).getBoundingBox())) {
    				BonusItem item = bonusItems.get(j);
    				Player player = players.get(i);
    				switch (item.getType()) {
    					case RemoveTail:
    						player.removeTail();
    						break;
    					case ExtendTail:
    						player.extendTailSize();
    						break;
    					case ShrinkTail:
    						player.shrinkTailSize();
    						break;
    				}
    			}
    		}
    	}
    }
    
    public void detectSceneCollisions(Player player) {
        if(!player.isDestroyed()) {
            player.setDestroyed(!bbBuilding.contains2D(player.getBoundingBox()));
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
