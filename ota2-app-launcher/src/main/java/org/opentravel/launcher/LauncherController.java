/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.launcher;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.application.common.StatusType;
import org.opentravel.diffutil.OTMDiffApplication;
import org.opentravel.examplehelper.ExampleHelperApplication;
import org.opentravel.exampleupgrade.ExampleUpgradeApplication;
import org.opentravel.messagevalidate.OTMMessageValidatorApplication;
import org.opentravel.modelcheck.ModelCheckApplication;
import org.opentravel.release.OTMReleaseApplication;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the Utility Launcher.
 */
public class LauncherController extends AbstractMainWindowController {

	public static final String FXML_FILE = "/ota2-launcher.fxml";
	
	private static final String APP_CLASS_KEY   = "appClass";
	private static final String APP_PROCESS_KEY = "appProcess";
	private static final String MSG_ALREADY_RUNNING_TITLE   = "alert.alreadyRunning.title";
	private static final String MSG_ALREADY_RUNNING_MESSAGE = "alert.alreadyRunning.message";
	private static final String MSG_LAUNCH_TITLE            = "task.launch.title";
	private static final String MSG_LAUNCH_ERROR            = "task.launch.error";
	
	@FXML private TilePane releasedAppsPane;
	@FXML private TilePane experimentalAppsPane;
	@FXML private ImageView statusBarIcon;
	@FXML private Label statusBarLabel;
	
	/**
	 * Called when the user clicks the menu to edit the global proxy settings
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void editProxySettings(ActionEvent event) {
		ProxySettingsDialogController controller = null;
		try {
			FXMLLoader loader = new FXMLLoader( LauncherController.class.getResource(
					ProxySettingsDialogController.FXML_FILE ) );
			BorderPane page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( "Network Proxy Settings" );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.initOwner( getPrimaryStage() );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.setDialogStage( dialogStage );
			controller.showAndWait();
			
		} catch (IOException e) {
			e.printStackTrace( System.out );
		}
	}
	
	/**
	 * Called when the user clicks one of the utility application buttons
	 * to launch it.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@SuppressWarnings("unchecked")
	@FXML
	public void launchUtilityApp(ActionEvent event) {
		Button sourceButton = (Button) event.getSource();
		Class<? extends AbstractOTMApplication> appClass = (Class<? extends AbstractOTMApplication>)
				sourceButton.getProperties().get( APP_CLASS_KEY );
		Process appProcess = (Process) sourceButton.getProperties().get( APP_PROCESS_KEY );
		
		if ((appProcess != null) && appProcess.isAlive()) {
			Alert alert = new Alert( AlertType.INFORMATION );
			
			alert.setTitle( MessageBuilder.formatMessage( MSG_ALREADY_RUNNING_TITLE ) );
			alert.setHeaderText( MessageBuilder.getDisplayName( appClass ) );
			alert.setContentText( MessageBuilder.formatMessage( MSG_ALREADY_RUNNING_MESSAGE ) );
			alert.showAndWait();
			
		} else {
			String statusMessage = MessageBuilder.formatMessage( MSG_LAUNCH_TITLE, MessageBuilder.getDisplayName( appClass ) );
			Runnable r = new BackgroundTask( statusMessage, StatusType.INFO ) {
				public void execute() throws Throwable {
					String javaHome = System.getProperty( "java.home" );
					String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
					String classpath = System.getProperty( "java.class.path" );
					UserSettings settings = UserSettings.load();
					List<String> cmds = new ArrayList<>( Arrays.asList( javaBin, "-cp", classpath ) );
					ProcessBuilder builder;
					Process newProcess;
					
					// Configure proxy settings (if necessary)
					if (!StringUtils.isEmpty( settings.getProxyHost() )) {
						cmds.add( "-Dhttp.proxyHost=" + settings.getProxyHost() );
						
						if (settings.getProxyPort() != null) {
							cmds.add( "-Dhttp.proxyPort=" + settings.getProxyPort() );
						}
						if (!StringUtils.isEmpty( settings.getNonProxyHosts() )) {
							cmds.add( "-Dhttp.nonProxyHosts=" + settings.getNonProxyHosts() );
						}
					}
					
					// Build and execute the command to start the new sub-process
					cmds.add( appClass.getCanonicalName() );
					builder = new ProcessBuilder( cmds );
					builder.redirectErrorStream( true );
					builder.redirectOutput( Redirect.to( getLogFile( appClass ) ) );
					newProcess = builder.start();
					
					// Wait a second before checking the status
					try {
						Thread.sleep( 1000 );
					} catch (InterruptedException e) {}
					
					// Finish up by saving the running process or reporting an error
					if (newProcess.isAlive()) {
						sourceButton.getProperties().put( APP_PROCESS_KEY, newProcess );
						
					} else {
						throw new RuntimeException( MessageBuilder.formatMessage(
								MSG_LAUNCH_ERROR, MessageBuilder.getDisplayName( appClass ) ) );
					}
				}
			};
			
			sourceButton.getProperties().remove( APP_PROCESS_KEY );
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the menu to display the about-application
	 * dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void aboutApplication(ActionEvent event) {
		AboutDialogController.createAboutDialog( getPrimaryStage() ).showAndWait();
	}
	
	/**
	 * Called when the user clicks the menu to exit the application.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void exitApplication(ActionEvent event) {
		getPrimaryStage().close();
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String, org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater( () -> {
			statusBarLabel.setText( message );
			statusBarIcon.setImage( (statusType == null) ? null : statusType.getIcon() );
		} );
	}

	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#updateControlStates()
	 */
	@Override
	protected void updateControlStates() {
		// No action required
	}

	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#initialize(javafx.stage.Stage)
	 */
	@Override
	protected void initialize(Stage primaryStage) {
		super.initialize( primaryStage );
		
		releasedAppsPane.getChildren().add( newAppIcon( ExampleHelperApplication.class, Images.exampleHelperIcon ) );
		releasedAppsPane.getChildren().add( newAppIcon( OTMDiffApplication.class, Images.diffUtilityIcon ) );
		releasedAppsPane.getChildren().add( newAppIcon( OTMReleaseApplication.class, Images.releaseEditorIcon ) );
		experimentalAppsPane.getChildren().add( newAppIcon( OTMMessageValidatorApplication.class, Images.messageValidatorIcon ) );
		experimentalAppsPane.getChildren().add( newAppIcon( ModelCheckApplication.class, Images.messageValidatorIcon ) );
		experimentalAppsPane.getChildren().add( newAppIcon( ExampleUpgradeApplication.class, Images.exampleUpgradeIcon ) );
	}
	
	/**
	 * Constructs a new utility application icon that will launch the given application class
	 * when the button is clicked.
	 * 
	 * @param appClass  the JavaFX application class to use when launching the utility
	 * @param image  the image icon to display on the button
	 * @return Button
	 */
	private Button newAppIcon(Class<? extends AbstractOTMApplication> appClass, Image image) {
		ImageView buttonImg = new ImageView();
		Button appButton = new Button();
		
		buttonImg.setImage( image );
		appButton.setText( MessageBuilder.getDisplayName( appClass ) );
		appButton.setGraphic( buttonImg );
		appButton.setContentDisplay( ContentDisplay.TOP );
		appButton.setOnAction( this::launchUtilityApp );
		appButton.getProperties().put( APP_CLASS_KEY, appClass );
		return appButton;
	}
	
	/**
	 * Returns the file to which the given application class's log output should
	 * be directed.
	 * 
	 * @param appClass  the utility application class for which to return a log file
	 * @return File
	 */
	private File getLogFile(Class<?> appClass) {
		return new File( getLogFolder(), appClass.getSimpleName() + ".log" );
	}
	
	/**
	 * Returns the folder location for utility application log files.
	 * 
	 * @return File
	 */
	private File getLogFolder() {
		File currentFolder = new File( System.getProperty( "user.dir" ) );
		File targetFolder = new File( currentFolder, "/target" );
		File rootFolder = targetFolder.exists() ? targetFolder : currentFolder;
		File logFolder = new File( rootFolder, "/logs" );
		
		logFolder.mkdirs();
		return logFolder;
	}
	
}
