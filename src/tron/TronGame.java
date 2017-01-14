package tron;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.formats.IModelReader.Options;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.light.DirectionalLight;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import tron.helper.ElevatorFace;

public class TronGame {	
	public static void main(String[] args) {
		new TronGame();
	}
	public static TronController controller = null;
	
	private TronGame() {
		Platform.get().init();
		
		final List<IMesh> building = new ArrayList<>();
		final List<IMesh> elevator = new ArrayList<>();
		final List<Player> players = new ArrayList<>();
		
		try {
		    Player player1 = new Player(
		            getClass().getResource("/models/WpnCaptainBlueFalcon.obj"),
		            new Vec3(0, -2, 0),
		            new Vec3(0, -1, 0),
		            Mat4.rotate(0, Vec3.Z),
		            ElevatorFace.BACK,
		            Mat4.scale(0.005f),
		            RGBA.BLUE
            );
		    
		    Player player2 = new Player(
		            getClass().getResource("/models/WpnCaptainBlueFalcon.obj"),
                    new Vec3(0, 2, 0),
                    new Vec3(0, 1, 0),
                    Mat4.rotate(180, Vec3.Z),
                    ElevatorFace.FRONT,
                    Mat4.scale(0.005f),
                    RGBA.DARK_GRAY
            );
		    
		    players.add(player1);
		    players.add(player2);
						
			final URL objBuilding = getClass().getResource("/models/building.obj");
			new ObjReader(objBuilding, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> building.add(mesh));
			
			final URL objElevator = getClass().getResource("/models/elevator.obj");
            new ObjReader(objElevator, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> elevator.add(mesh));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final BoundingBox bbBuilding = getBoundingBoxOfObj(building);
        final BoundingBox bbElevator = getBoundingBoxOfObj(elevator);		
        controller = new TronController(players, bbBuilding, bbElevator);
		
		controller.run(time -> {
			new DefaultView(controller, 0, 40, 1200, 460, IView.INTERACTIVE_VIEW, "TronGame");
		});
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		controller.run(time -> {
			new DefaultView(controller, 0, 540, 1200, 460, IView.INTERACTIVE_VIEW, "TronGame");
			
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);
						
			scene.add3DObject(new DirectionalLight(new Vec3(10, -10, 10), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(-10, 10, 10), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(10, -10, 10), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(10, 10, 10), RGB.BLACK, RGB.WHITE));
			
			scene.add3DObjects(building);
			scene.add3DObjects(elevator);
			players.forEach(player -> scene.add3DObjects(player.getPlayerObj()));
			
			for(int i = 0; i < players.size(); i++) {
			    players.get(i).setPlayerCamera(controller.getCamera(controller.getViews().get(i)));
			}
		});
		
		controller.animate((time, interval) -> {
		    if(controller.getCurrentView() != null)
		        controller.animationTick(time, interval);
		});
		
		Platform.get().run();
	}
	
	private BoundingBox getBoundingBoxOfObj(List<IMesh> obj) {
	    BoundingBox bb = new BoundingBox();
	    
	    for (IMesh mesh : obj) {
            bb.add(mesh.getBounds());
        }
	    
	    return bb;
	}
}
