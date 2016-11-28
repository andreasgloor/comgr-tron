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

public class TronGame {
	private IMesh mesh;
	
	public static void main(String[] args) {
		new TronGame();
	}

	private TronGame() {
		Platform.get().init();
		
		final List<IMesh> falcon = new ArrayList<>();
		try {
			final URL obj = getClass().getResource("/models/WpnCaptainBlueFalcon.obj");
			new ObjReader(obj, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> falcon.add(mesh));
			//falcon.forEach(m -> m.setTransform(Mat4.scale(0.005f)));
			
			/*System.out.println("number of meshes before merging: " + falcon.size());
			final List<IMesh> merged = MeshUtilities.mergeMeshes(falcon);
			System.out.println("number of meshes after merging: " + merged.size());
			scene.add3DObjects(merged);*/
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TronController controller = new TronController(falcon);
		controller.run(time -> {
			new DefaultView(controller, 0, 40, 1200, 460, IView.INTERACTIVE_VIEW, "TronGame");
			//new DefaultView(controller, 0, 540, 1200, 460, new IView.Config(ViewType.INTERACTIVE_VIEW, 0, new IView.ViewFlag[0]), "TronGame");
	
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);
			
			scene.add3DObject(MeshUtilities.createGroundPlane(10f)); // -10 bis +10
			
			scene.add3DObject(new DirectionalLight(new Vec3(-3, -3, 3), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(-3, 3, 3), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(3, -3, 3), RGB.BLACK, RGB.WHITE));
			scene.add3DObject(new DirectionalLight(new Vec3(3, 3, 3), RGB.BLACK, RGB.WHITE));
			
			scene.add3DObjects(falcon);
		});
		controller.animate((time, interval) -> {
			controller.animationTick();
		});
		Platform.get().run();
		
		
		/*Vec3[] params = CAM_PARAMS[e.getKey() - GLFW.GLFW_KEY_1];
			ICamera camera = getCamera(getCurrentView());
			camera.setPosition(params[0]);
			camera.setUp(params[1]);*/
	}
}
