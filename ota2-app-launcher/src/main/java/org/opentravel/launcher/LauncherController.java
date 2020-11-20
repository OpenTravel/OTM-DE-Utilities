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

import org.apache.commons.lang3.StringUtils;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.OTA2ApplicationProvider;
import org.opentravel.application.common.OTA2ApplicationSpec;
import org.opentravel.application.common.OTA2LauncherTabSpec;
import org.opentravel.application.common.OtmApplicationException;
import org.opentravel.application.common.OtmApplicationRuntimeException;
import org.opentravel.application.common.StatusType;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
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

    private static final Logger log = LoggerFactory.getLogger( LauncherController.class );

    private static final String APP_CLASS_KEY = "appClass";
    private static final String APP_LIBFOLDER_KEY = "appLibraryFolderPath";
    private static final String APP_LOGFILE_KEY = "appLogFile";
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
     * @param event the action event that triggered this method call
     */
    @FXML
    public void editProxySettings(ActionEvent event) {
        ProxySettingsDialogController controller = null;
        try {
            FXMLLoader loader =
                new FXMLLoader( LauncherController.class.getResource( ProxySettingsDialogController.FXML_FILE ) );
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
            log.error( "Error launching the proxy settings dialog.", e );
        }
    }

    /**
     * Called when the user clicks one of the utility application buttons to launch it.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void launchUtilityApp(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String appClassname = (String) sourceButton.getProperties().get( APP_CLASS_KEY );
        String appLibraryFolderPath = (String) sourceButton.getProperties().get( APP_LIBFOLDER_KEY );
        String appLogFilename = (String) sourceButton.getProperties().get( APP_LOGFILE_KEY );
        Process appProcess = (Process) sourceButton.getProperties().get( APP_PROCESS_KEY );

        if ((appProcess != null) && appProcess.isAlive()) {
            Alert alert = new Alert( AlertType.INFORMATION );

            alert.setTitle( MessageBuilder.formatMessage( MSG_ALREADY_RUNNING_TITLE ) );
            alert.setHeaderText( sourceButton.getText() );
            alert.setContentText( MessageBuilder.formatMessage( MSG_ALREADY_RUNNING_MESSAGE ) );
            alert.showAndWait();

        } else {
            String statusMessage = MessageBuilder.formatMessage( MSG_LAUNCH_TITLE, sourceButton.getText() );
            Runnable r = new BackgroundTask( statusMessage, StatusType.INFO ) {
                @Override
                public void execute() throws OtmApplicationException {
                    launchApplicationProcess( sourceButton, appClassname, sourceButton.getText(), appLibraryFolderPath,
                        appLogFilename );
                }
            };

            sourceButton.getProperties().remove( APP_PROCESS_KEY );
            new Thread( r ).start();
        }
    }

    /**
     * Spawns an external Java process for the selected application.
     * 
     * @param sourceButton the button that was clicked by the user to launch an application
     * @param appClassname the fully-qualified JavaFX application class name for the utility being launched
     * @param appDisplayName the display name for the application being launched
     * @param appLibraryFolderPath folder path for the application's library jars (may be null for native apps)
     * @param logFilename the name of the application's log file
     * @throws OtmApplicationException thrown if an error occurs while launching the application
     */
    private void launchApplicationProcess(Button sourceButton, String appClassname, String appDisplayName,
        String appLibraryFolderPath, String logFilename) throws OtmApplicationException {
        try {
            File logFile = new File( getLogFolder(), logFilename );
            String javaHome = System.getProperty( "java.home" );
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = getClasspath( appLibraryFolderPath );
            UserSettings settings = UserSettings.load();
            List<String> cmds = new ArrayList<>( Arrays.asList( javaBin, "-cp", classpath ) );
            ProcessBuilder builder;
            Process newProcess;

            // Configure proxy settings (if necessary)
            if (settings.isUseProxy()) {
                cmds.add( "-Dhttp.proxyHost=" + settings.getProxyHost() );

                if (settings.getProxyPort() != null) {
                    cmds.add( "-Dhttp.proxyPort=" + settings.getProxyPort() );
                }
                if (!StringUtils.isEmpty( settings.getNonProxyHosts() )) {
                    cmds.add( "-Dhttp.nonProxyHosts=" + settings.getNonProxyHosts() );
                }
            }

            // Configure for headless/hidden operation (if necessary)
            if (isLaunchHeadless()) {
                cmds.add( "-Dotm.utilities.disableDisplay=true" );
            }

            // Build and execute the command to start the new sub-process
            for (String appCmd : appClassname.split( "\\s+" )) {
                cmds.add( appCmd );
            }
            builder = new ProcessBuilder( cmds );
            builder.redirectErrorStream( true );
            builder.redirectOutput( Redirect.appendTo( logFile ) );
            newProcess = builder.start();
            sourceButton.getProperties().put( APP_PROCESS_KEY, newProcess );

            // Wait five seconds before exiting to give the app time to finish launching
            sleep( 5000 );

            // Report an error if the process failed to start
            if (!newProcess.isAlive()) {
                sourceButton.getProperties().put( APP_PROCESS_KEY, null );
                throw new OtmApplicationRuntimeException(
                    MessageBuilder.formatMessage( MSG_LAUNCH_ERROR, appDisplayName ) );
            }

        } catch (Exception e) {
            throw new OtmApplicationException( e.getMessage(), e );
        }
    }

    /**
     * Returns the classpath to use when launching an application. If the given library folder path is null, the current
     * system classpath will be returned. If non-null, the classpath will include all jar files in that folder.
     * 
     * @param libraryFolderPath the library folder path for the application jars (may be null)
     * @return String
     * @throws FileNotFoundException thrown if the specified library folder path does not exist
     */
    private String getClasspath(String libraryFolderPath) throws FileNotFoundException {
        String localCp = System.getProperty( "java.class.path" );
        String cp;

        if (libraryFolderPath != null) {
            File libFolder = new File( System.getProperty( "user.dir" ) + libraryFolderPath );
            String javaHome = System.getProperty( "java.home" );
            StringBuilder cpb = new StringBuilder();

            if (!libFolder.exists()) {
                throw new FileNotFoundException(
                    String.format( "The application's library folder \"%s\" does not exist.", libraryFolderPath ) );
            }

            for (String lcpPart : localCp.split( File.pathSeparator )) {
                if (lcpPart.startsWith( javaHome )) {
                    cpb.append( lcpPart ).append( File.pathSeparatorChar );
                }
            }
            cpb.append( libFolder.getAbsolutePath() + File.separator + "*" );
            cp = cpb.toString();

        } else {
            cp = localCp;
        }
        return cp;
    }

    /**
     * Causes the current thread to sleep for the specified number of milliseconds.
     * 
     * @param durationMillis the duration (in millis) to sleep
     */
    private void sleep(long durationMillis) {
        try {
            Thread.sleep( durationMillis );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the running process associated with the specified launch button (should be used for testing purposes
     * only). If no such process exists, null will be returned.
     * 
     * @param launchButtonTitle the title of the launch button for which the process will be returned
     * @return Process
     */
    protected Process getProcess(String launchButtonTitle) {
        Process process = null;

        for (Button launchButton : launchButtons) {
            if (launchButton.getText().equals( launchButtonTitle )) {
                process = (Process) launchButton.getProperties().get( APP_PROCESS_KEY );
                break;
            }
        }
        return process;
    }

    /**
     * Called when the user clicks the menu to display the about-application dialog.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void aboutApplication(ActionEvent event) {
        AboutDialogController.createAboutDialog( getPrimaryStage() ).showAndWait();
    }

    /**
     * Called when the user clicks the menu to exit the application.
     * 
     * @param event the action event that triggered this method call
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
        Map<OTA2LauncherTabSpec,SortedSet<OTA2ApplicationSpec>> appsByTab = getApplicationsByTab();

        super.initialize( primaryStage );

        for (Entry<OTA2LauncherTabSpec,SortedSet<OTA2ApplicationSpec>> entry : appsByTab.entrySet()) {
            OTA2LauncherTabSpec tabSpec = entry.getKey();
            TilePane buttonPane = newTab( tabSpec.getName() );

            for (OTA2ApplicationSpec appSpec : entry.getValue()) {
                Button launchButton = newAppIcon( appSpec );

                buttonPane.getChildren().add( launchButton );
                launchButtons.add( launchButton );
            }
        }
    }

    /**
     * Returns the list of <code>OTA2ApplicationSpec</code>s organized by tab.
     * 
     * @return Map&lt;OTA2LauncherTabSpec,SortedSet&lt;OTA2ApplicationSpec&gt;&gt;
     */
    private Map<OTA2LauncherTabSpec,SortedSet<OTA2ApplicationSpec>> getApplicationsByTab() {
        ServiceLoader<OTA2ApplicationProvider> loader = ServiceLoader.load( OTA2ApplicationProvider.class );
        Map<OTA2LauncherTabSpec,SortedSet<OTA2ApplicationSpec>> appsByTab = new TreeMap<>();
        boolean isAdmin = isAdministrator();

        for (OTA2ApplicationProvider provider : loader) {
            OTA2ApplicationSpec spec = provider.getApplicationSpec();

            if (!spec.isDisabled() && (!spec.isAdminApp() || isAdmin)) {
                SortedSet<OTA2ApplicationSpec> appSpecs = appsByTab.get( spec.getLauncherTab() );

                if (appSpecs == null) {
                    appSpecs = new TreeSet<>();
                    appsByTab.put( spec.getLauncherTab(), appSpecs );
                }
                appSpecs.add( spec );
            }
        }
        return appsByTab;
    }

    /**
     * Creates a new launcher tab with the specified name and returns the content pane for the tab.
     * 
     * @param name the name of the tab to create
     * @return TilePane
     */
    private TilePane newTab(String name) {
        Tab tab = new Tab( name );
        StackPane tabContent = new StackPane();
        TilePane buttonPane = new TilePane();

        buttonPane.setHgap( 25.0 );
        buttonPane.setVgap( 25.0 );
        buttonPane.setPrefRows( 1 );
        buttonPane.setPrefColumns( 1 );
        buttonPane.setPadding( new Insets( 25.0, 25.0, 25.0, 25.0 ) );

        tabContent.getChildren().add( buttonPane );
        tab.setContent( tabContent );
        tabPane.getTabs().add( tab );

        return buttonPane;
    }

    /**
     * Constructs a new utility application icon that will launch the given application class when the button is
     * clicked.
     * 
     * @param appSpec the application spec for the application to be launched
     * @return Button
     */
    private Button newAppIcon(OTA2ApplicationSpec appSpec) {
        String logFilename = appSpec.getLogFilename();
        ImageView buttonImg = new ImageView();
        Button appButton = new Button();

        if (logFilename == null) {
            logFilename = getSimpleClassname( appSpec.getApplicationClassname() ) + ".log";
        }
        buttonImg.setImage( appSpec.getLaunchIcon() );
        appButton.setText( appSpec.getName() );
        appButton.setGraphic( buttonImg );
        appButton.setContentDisplay( ContentDisplay.TOP );
        appButton.setOnAction( this::launchUtilityApp );
        appButton.getProperties().put( APP_CLASS_KEY, appSpec.getApplicationClassname() );
        appButton.getProperties().put( APP_LIBFOLDER_KEY, appSpec.getLibraryFolderPath() );
        appButton.getProperties().put( APP_LOGFILE_KEY, logFilename );
        return appButton;
    }

    /**
     * Returns the simple name of the class with the given qualified name.
     * 
     * @param classname the fully-qualified name of the Java class
     * @return String
     */
    private String getSimpleClassname(String classname) {
        int lastIdx = classname.lastIndexOf( '.' );

        return (lastIdx < 0) ? classname : classname.substring( lastIdx + 1 );
    }

    /**
     * Returns the folder location for utility application log files.
     * 
     * @return File
     */
    private File getLogFolder() {
        String currentFolder = System.getProperty( "user.dir" );
        File targetFolder = new File( currentFolder + "/target" );
        File rootFolder = targetFolder.exists() ? targetFolder : new File( currentFolder );
        File logFolder = new File( rootFolder + "/logs" );

        logFolder.mkdirs();
        return logFolder;
    }

    /**
     * Returns true if the current user has administrator access to at least one remote OTM repository.
     * 
     * @return boolean
     */
    private boolean isAdministrator() {
        boolean isAdmin = false;
        try {
            List<RemoteRepository> repos = RepositoryManager.getDefault().listRemoteRepositories();

            for (RemoteRepository repo : repos) {
                isAdmin = isAdministrator( repo );

                if (isAdmin) {
                    break;
                }
            }

        } catch (RepositoryException e) {
            // No action - return false
        }
        return isAdmin;
    }

    /**
     * Returns true if the user is an administrator for the given remote repository.
     * 
     * @param repo the remote repository to check
     * @return boolean
     */
    private boolean isAdministrator(RemoteRepository repo) {
        boolean isAdmin = false;
        try {
            isAdmin = repo.isAdministrator();

        } catch (Exception e) {
            // Ignore error and continue
        }
        return isAdmin;
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
     * @param launchHeadless the boolean value to assign
     */
    protected void setLaunchHeadless(boolean launchHeadless) {
        this.launchHeadless = launchHeadless;
    }

}
