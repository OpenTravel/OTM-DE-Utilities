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
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.application.common.OTA2ApplicationProvider;
import org.opentravel.application.common.OTA2ApplicationSpec;
import org.opentravel.application.common.OTA2LauncherTabSpec;
import org.opentravel.application.common.OtmApplicationException;
import org.opentravel.application.common.OtmApplicationRuntimeException;
import org.opentravel.application.common.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the Utility Launcher.
 */
public class LauncherController extends AbstractMainWindowController {

	public static final String FXML_FILE = "/ota2-launcher.fxml";

	private static final Logger log = LoggerFactory.getLogger(LauncherController.class);

	private static final String APP_CLASS_KEY = "appClass";
	private static final String APP_PROCESS_KEY = "appProcess";
	private static final String MSG_ALREADY_RUNNING_TITLE = "alert.alreadyRunning.title";
	private static final String MSG_ALREADY_RUNNING_MESSAGE = "alert.alreadyRunning.message";
	private static final String MSG_LAUNCH_TITLE = "task.launch.title";
	private static final String MSG_LAUNCH_ERROR = "task.launch.error";

	@FXML
	private TabPane tabPane;
	@FXML
	private ImageView statusBarIcon;
	@FXML
	private Label statusBarLabel;

	private List<Button> launchButtons = new ArrayList<>();
	private boolean launchHeadless = false;

	/**
	 * Called when the user clicks the menu to edit the global proxy settings
	 * 
	 * @param event
	 *            the action event that triggered this method call
	 */
	@FXML
	public void editProxySettings(ActionEvent event) {
		ProxySettingsDialogController controller = null;
		try {
			FXMLLoader loader = new FXMLLoader(
					LauncherController.class.getResource(ProxySettingsDialogController.FXML_FILE));
			BorderPane page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene(page);

			dialogStage.setTitle("Network Proxy Settings");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(getPrimaryStage());
			dialogStage.setScene(scene);

			controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.showAndWait();

		} catch (IOException e) {
			log.error("Error launching the proxy settings dialog.", e);
		}
	}

	/**
	 * Called when the user clicks one of the utility application buttons to launch it.
	 * 
	 * @param event
	 *            the action event that triggered this method call
	 */
	@SuppressWarnings("unchecked")
	@FXML
	public void launchUtilityApp(ActionEvent event) {
		Button sourceButton = (Button) event.getSource();
		Class<? extends AbstractOTMApplication> appClass = (Class<? extends AbstractOTMApplication>) sourceButton
				.getProperties().get(APP_CLASS_KEY);
		Process appProcess = (Process) sourceButton.getProperties().get(APP_PROCESS_KEY);

		if ((appProcess != null) && appProcess.isAlive()) {
			Alert alert = new Alert(AlertType.INFORMATION);

			alert.setTitle(MessageBuilder.formatMessage(MSG_ALREADY_RUNNING_TITLE));
			alert.setHeaderText(MessageBuilder.getDisplayName(appClass));
			alert.setContentText(MessageBuilder.formatMessage(MSG_ALREADY_RUNNING_MESSAGE));
			alert.showAndWait();

		} else {
			String statusMessage = MessageBuilder.formatMessage(MSG_LAUNCH_TITLE,
					MessageBuilder.getDisplayName(appClass));
			Runnable r = new BackgroundTask(statusMessage, StatusType.INFO) {
				@Override
				public void execute() throws OtmApplicationException {
					launchApplicationProcess(sourceButton, appClass);
				}
			};

			sourceButton.getProperties().remove(APP_PROCESS_KEY);
			new Thread(r).start();
		}
	}

	/**
	 * Spawns an external Java process for the selected application.
	 * 
	 * @param sourceButton
	 *            the button that was clicked by the user to launch an application
	 * @param appClass
	 *            the JavaFX application class for the utility being launched
	 * @throws OtmApplicationException
	 *             thrown if an error occurs while launching the application
	 */
	private void launchApplicationProcess(Button sourceButton, Class<? extends AbstractOTMApplication> appClass)
			throws OtmApplicationException {
		try {
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
			String classpath = System.getProperty("java.class.path");
			UserSettings settings = UserSettings.load();
			List<String> cmds = new ArrayList<>(Arrays.asList(javaBin, "-cp", classpath));
			ProcessBuilder builder;
			Process newProcess;

			// Configure proxy settings (if necessary)
			if (!settings.isUseProxy()) {
				cmds.add("-Dhttp.proxyHost=" + settings.getProxyHost());

				if (settings.getProxyPort() != null) {
					cmds.add("-Dhttp.proxyPort=" + settings.getProxyPort());
				}
				if (!StringUtils.isEmpty(settings.getNonProxyHosts())) {
					cmds.add("-Dhttp.nonProxyHosts=" + settings.getNonProxyHosts());
				}
			}

			// Configure for headless/hidden operation (if necessary)
			if (isLaunchHeadless()) {
				cmds.add("-Dotm.utilities.disableDisplay=true");
			}

			// Build and execute the command to start the new sub-process
			cmds.add(appClass.getCanonicalName());
			builder = new ProcessBuilder(cmds);
			builder.redirectErrorStream(true);
			builder.redirectOutput(Redirect.to(getLogFile(appClass)));
			newProcess = builder.start();
			sourceButton.getProperties().put(APP_PROCESS_KEY, newProcess);

			// Wait five seconds before exiting to give the app time to finish launching
			sleep(5000);

			// Report an error if the process failed to start
			if (!newProcess.isAlive()) {
				sourceButton.getProperties().put(APP_PROCESS_KEY, null);
				throw new OtmApplicationRuntimeException(
						MessageBuilder.formatMessage(MSG_LAUNCH_ERROR, MessageBuilder.getDisplayName(appClass)));
			}

		} catch (Exception e) {
			throw new OtmApplicationException(e.getMessage(), e);
		}
	}

	/**
	 * Causes the current thread to sleep for the specified number of milliseconds.
	 * 
	 * @param durationMillis
	 *            the duration (in millis) to sleep
	 */
	private void sleep(long durationMillis) {
		try {
			Thread.sleep(durationMillis);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Returns the running process associated with the specified launch button (should be used for testing purposes
	 * only). If no such process exists, null will be returned.
	 * 
	 * @param launchButtonTitle
	 *            the title of the launch button for which the process will be returned
	 * @return Process
	 */
	protected Process getProcess(String launchButtonTitle) {
		Process process = null;

		for (Button launchButton : launchButtons) {
			if (launchButton.getText().equals(launchButtonTitle)) {
				process = (Process) launchButton.getProperties().get(APP_PROCESS_KEY);
				break;
			}
		}
		return process;
	}

	/**
	 * Called when the user clicks the menu to display the about-application dialog.
	 * 
	 * @param event
	 *            the action event that triggered this method call
	 */
	@FXML
	public void aboutApplication(ActionEvent event) {
		AboutDialogController.createAboutDialog(getPrimaryStage()).showAndWait();
	}

	/**
	 * Called when the user clicks the menu to exit the application.
	 * 
	 * @param event
	 *            the action event that triggered this method call
	 */
	@FXML
	public void exitApplication(ActionEvent event) {
		getPrimaryStage().close();
	}

	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String,
	 *      org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater(() -> {
			statusBarLabel.setText(message);
			statusBarIcon.setImage((statusType == null) ? null : statusType.getIcon());
		});
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
		Map<OTA2LauncherTabSpec, SortedSet<OTA2ApplicationSpec>> appsByTab = getApplicationsByTab();

		super.initialize(primaryStage);

		for (Entry<OTA2LauncherTabSpec, SortedSet<OTA2ApplicationSpec>> entry : appsByTab.entrySet()) {
			OTA2LauncherTabSpec tabSpec = entry.getKey();
			TilePane buttonPane = newTab(tabSpec.getName());

			for (OTA2ApplicationSpec appSpec : entry.getValue()) {
				Button launchButton = newAppIcon(appSpec.getApplicationClass(), appSpec.getLaunchIcon());

				buttonPane.getChildren().add(launchButton);
				launchButtons.add(launchButton);
			}
		}
	}

	/**
	 * Returns the list of <code>OTA2ApplicationSpec</code>s organized by tab.
	 * 
	 * @return Map&lt;OTA2LauncherTabSpec,SortedSet&lt;OTA2ApplicationSpec&gt;&gt;
	 */
	private Map<OTA2LauncherTabSpec, SortedSet<OTA2ApplicationSpec>> getApplicationsByTab() {
		ServiceLoader<OTA2ApplicationProvider> loader = ServiceLoader.load(OTA2ApplicationProvider.class);
		Map<OTA2LauncherTabSpec, SortedSet<OTA2ApplicationSpec>> appsByTab = new TreeMap<>();

		for (OTA2ApplicationProvider provider : loader) {
			OTA2ApplicationSpec spec = provider.getApplicationSpec();
			SortedSet<OTA2ApplicationSpec> appSpecs = appsByTab.get(spec.getLauncherTab());

			if (appSpecs == null) {
				appSpecs = new TreeSet<>();
				appsByTab.put(spec.getLauncherTab(), appSpecs);
			}
			appSpecs.add(spec);
		}
		return appsByTab;
	}

	/**
	 * Creates a new launcher tab with the specified name and returns the content pane for the tab.
	 * 
	 * @param name
	 *            the name of the tab to create
	 * @return TilePane
	 */
	private TilePane newTab(String name) {
		Tab tab = new Tab(name);
		StackPane tabContent = new StackPane();
		TilePane buttonPane = new TilePane();

		buttonPane.setHgap(25.0);
		buttonPane.setVgap(25.0);
		buttonPane.setPrefRows(1);
		buttonPane.setPrefColumns(1);
		buttonPane.setPadding(new Insets(25.0, 25.0, 25.0, 25.0));

		tabContent.getChildren().add(buttonPane);
		tab.setContent(tabContent);
		tabPane.getTabs().add(tab);

		return buttonPane;
	}

	/**
	 * Constructs a new utility application icon that will launch the given application class when the button is
	 * clicked.
	 * 
	 * @param appClass
	 *            the JavaFX application class to use when launching the utility
	 * @param image
	 *            the image icon to display on the button
	 * @return Button
	 */
	private Button newAppIcon(Class<? extends AbstractOTMApplication> appClass, Image image) {
		ImageView buttonImg = new ImageView();
		Button appButton = new Button();

		buttonImg.setImage(image);
		appButton.setText(MessageBuilder.getDisplayName(appClass));
		appButton.setGraphic(buttonImg);
		appButton.setContentDisplay(ContentDisplay.TOP);
		appButton.setOnAction(this::launchUtilityApp);
		appButton.getProperties().put(APP_CLASS_KEY, appClass);
		return appButton;
	}

	/**
	 * Returns the file to which the given application class's log output should be directed.
	 * 
	 * @param appClass
	 *            the utility application class for which to return a log file
	 * @return File
	 */
	private File getLogFile(Class<?> appClass) {
		return new File(getLogFolder(), appClass.getSimpleName() + ".log");
	}

	/**
	 * Returns the folder location for utility application log files.
	 * 
	 * @return File
	 */
	private File getLogFolder() {
		String currentFolder = System.getProperty("user.dir");
		File targetFolder = new File(currentFolder + "/target");
		File rootFolder = targetFolder.exists() ? targetFolder : new File(currentFolder);
		File logFolder = new File(rootFolder + "/logs");

		logFolder.mkdirs();
		return logFolder;
	}

	/**
	 * Returns the flag indicating whether apps should be launched in headless/hidden mode (for testing purposes only).
	 *
	 * @return boolean
	 */
	protected boolean isLaunchHeadless() {
		return launchHeadless;
	}

	/**
	 * Assigns the flag indicating whether apps should be launched in headless/hidden mode (for testing purposes only).
	 *
	 * @param launchHeadless
	 *            the boolean value to assign
	 */
	protected void setLaunchHeadless(boolean launchHeadless) {
		this.launchHeadless = launchHeadless;
	}

}
