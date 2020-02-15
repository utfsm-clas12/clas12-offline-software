package cnuphys.bCNUfx.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BaseMDIApplication extends Application {
	
	/**
	 * This is called before the window is created. Use this to load resources,
	 * handle command line arguments, etc.
	 */
	@Override
	public void init() {
		System.out.println("Before the window is created...");
	}

	/**
	 * The main entry point.
	 * @param primaryStage the main window of the application
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		Scene scene = new Scene(new Group(), 500, 250);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("main window");
		
		primaryStage.show();
	}
	
	/**
	 * The last user code called before the application exits
	 */
	@Override
	public void stop() {
		System.out.println("Application has exited.");
	}
	
	/**
	 * This is the javafx way to  quit. Do not call System.exit();
	 */
	public void quit() {
		Platform.exit();
	}
	
	/**
	 * The main program
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}


}
