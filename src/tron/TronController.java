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
	public static final double FLOOR_HEIGHT = 6.1;
	
	private final List<Player> players;
	private final BoundingBox bbBuilding;
	private final BoundingBox bbElevator;
	private final CollisionHandler collisionHandler;
	
	private boolean fixCameraPos = false;
	private boolean hasLevelChanged = false;
    private double time_last = 0;
	
	public TronController(List<Player> players, BoundingBox bbBuilding, BoundingBox bbElevator) {
	    this.players = players;
	    this.bbBuilding = bbBuilding;
	    this.bbElevator = bbElevator;
		this.collisionHandler = new CollisionHandler(bbBuilding, bbElevator);
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
		case GLFW.GLFW_KEY_D:
		    players.get(1).turn(-90);
		    players.get(1).setTurned(true);
			break;
		case GLFW.GLFW_KEY_A:
		    players.get(1).turn(90);
		    players.get(1).setTurned(true);
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
		
		double dt = (time - time_last) * MOVE_PER_SECOND;
		time_last = time;

		players.forEach(player -> player.move(dt, bbBuilding, bbElevator));
		collisionHandler.detectPlayerCollisions(players);
	    players.forEach(player -> collisionHandler.detectSceneCollisions(player));
		
		InitTails();
		CalculateFalconTail(players.get(0).getPosition(), 0);
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
		if(players.get(0).hasTurned() || hasLevelChanged || falconTail.isEmpty()) {
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
		
		RepaintTail(falconId);
	}
	
	/**
	 * Resize / Transform Tail
	 */
	private void RepaintTail(int falconId) {
//		System.out.println("repaintTail");
		
		DoubleLinkedList<Tail> falconTail = tails.get(falconId);
		if(falconTail.Length() == 1) {
			
//			System.out.println("new falcon");
			Tail t = (Tail)falconTail.getFirst();
			PaintTail(t.getStart(),t.getEnd());
			
			//Tail first = (Tail)falconTail.getFirst();
			// first.getMesh().setTransform(Mat4.scale(1,1,1));
			
		} else {
			System.out.println("move falcon" );
			/*Tail first = (Tail)falconTail.getFirst(); 
			first.getMesh().setTransform(Mat4.scale(1,1,1));
			Tail last = (Tail)falconTail.getLast(); 
			last.getMesh().setTransform(Mat4.scale(1,1,1));*/
		}
	}
	
	private void PaintTail(Vec3 start, Vec3 end) {
		List<IMesh> beam = new ArrayList<>();
		final URL beamblue = getClass().getResource("/models/beam.obj");
        try {
			new ObjReader(beamblue, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> beam.add(mesh));
		} catch (IOException e) {
			// ignore
		}
		
        getScene().add3DObjects(beam);
		
		beam.forEach(mesh -> mesh.setTransform(Mat4.scale(10, 10, 1)));
		beam.forEach(mesh -> mesh.setPosition(new Vec3(start.x,start.y,0)));	
	
	}
	
}
