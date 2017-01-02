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
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.IView.ViewType;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.Vec4;
import ch.fhnw.util.math.geometry.BoundingBox;

public class TronGame {
	private IMesh mesh;
	
	public static void main(String[] args) {
		new TronGame();
	}

	private TronGame() {
		Platform.get().init();
		
		final List<IMesh> falcon = new ArrayList<>();
		final List<IMesh> building = new ArrayList<>();
		final List<IMesh> elevator = new ArrayList<>();
		
		try {
			final URL objFalcon = getClass().getResource("/models/WpnCaptainBlueFalcon.obj");
			new ObjReader(objFalcon, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> falcon.add(mesh));
			
			final URL objBuilding = getClass().getResource("/models/building.obj");
			new ObjReader(objBuilding, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> building.add(mesh));
			
			final URL objElevator = getClass().getResource("/models/elevator.obj");
            new ObjReader(objElevator, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> elevator.add(mesh));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BoundingBox bbBuilding = getBoundingBoxOfObj(building);
        BoundingBox bbElevator = getBoundingBoxOfObj(elevator);
		
		TronController controller = new TronController(falcon, bbBuilding, bbElevator);
		
		controller.run(time -> {
			new DefaultView(controller, 0, 40, 1200, 460, IView.INTERACTIVE_VIEW, "TronGame");
		});
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		controller.run(time -> {
			new DefaultView(controller, 0, 540, 1200, 460, new IView.Config(ViewType.INTERACTIVE_VIEW, 0, new IView.ViewFlag[0]), "TronGame");
	
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);
			
			//scene.add3DObject(MeshUtilities.createGroundPlane(10f)); // -10 bis +10
			
			scene.add3DObject(new DirectionalLight(new Vec3(10, -10, 10), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(-10, 10, 10), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(10, -10, 10), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(10, 10, 10), RGB.BLACK, RGB.WHITE));
			
			scene.add3DObjects(building);
			scene.add3DObjects(elevator);
			scene.add3DObjects(falcon);
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
