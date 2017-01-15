package tron;

import java.util.List;
import org.lwjgl.glfw.GLFW;
import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.event.IKeyEvent;
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
	
	public static final double FLOOR_HEIGHT = 6.1;
	
	private final List<Player> players;
	private final BoundingBox bbBuilding;
	private final BoundingBox bbElevator;
	private final CollisionHandler collisionHandler;
	private final List<BonusItem> bonusItems;
	
	private boolean fixCameraPos = false;
    private double time_last = 0;
	
	public TronController(List<Player> players, BoundingBox bbBuilding, BoundingBox bbElevator, List<BonusItem> bonusItems) {
	    this.players = players;
	    this.bbBuilding = bbBuilding;
	    this.bbElevator = bbElevator;
		this.collisionHandler = new CollisionHandler(bbBuilding, bbElevator, bonusItems, players);
		this.bonusItems = bonusItems;
	}

    @Override
	public void keyPressed(IKeyEvent e) {
		switch (e.getKey()) {
		case GLFW.GLFW_KEY_RIGHT:
			players.get(0).turn(-90);
			players.get(0).setTurned(true);
			break;
		case GLFW.GLFW_KEY_LEFT:
		    players.get(0).turn(90);
		    players.get(0).setTurned(true);
			break;
		case GLFW.GLFW_KEY_UP:
			players.get(0).speedUp();
			break;
		case GLFW.GLFW_KEY_DOWN:
			players.get(0).speedDown();
			break;
		case GLFW.GLFW_KEY_D:
		    players.get(1).turn(-90);
		    players.get(1).setTurned(true);
			break;
		case GLFW.GLFW_KEY_A:
		    players.get(1).turn(90);
		    players.get(1).setTurned(true);
			break;
		case GLFW.GLFW_KEY_W:
			players.get(1).speedUp();
			break;
		case GLFW.GLFW_KEY_S:
			players.get(1).speedDown();
			break;
		case GLFW.GLFW_KEY_H:
			printHelp(HELP);
			break;
		case GLFW.GLFW_KEY_TAB:
			fixCameraPos = !fixCameraPos;
            players.forEach(player -> player.setCamFixed(fixCameraPos));
			break;
		default:
			super.keyPressed(e);
		}
	}
	
	public void animationTick(double time, double interval) {	    
	    if(getViews().size() < 2)
	        return;
	    
	    if(time_last == 0){
	        time_last = time;
	        return;
		}
		
		double dt = (time - time_last);
		time_last = time;

		players.forEach(player -> player.move(dt, bbBuilding, bbElevator));
		collisionHandler.detectPlayerCollisions();
		collisionHandler.detectTailCollisions();
		collisionHandler.detectBonusItemCollisions();
	    players.forEach(player -> collisionHandler.detectSceneCollisions(player));
	    players.forEach(player -> player.CalculateFalconTail());
	    
	    bonusItems.forEach(item -> item.animate());
	}	
}
