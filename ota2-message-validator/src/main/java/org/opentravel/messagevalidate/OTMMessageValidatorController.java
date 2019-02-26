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

package org.opentravel.messagevalidate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.OtmApplicationException;
import org.opentravel.application.common.StatusType;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the OTM-Diff application.
 */
public class OTMMessageValidatorController extends AbstractMainWindowController {
	
	public static final String FXML_FILE = "/ota2-message-validator.fxml";
	
    private static final Logger log = LoggerFactory.getLogger( OTMMessageValidatorController.class );
    
	@FXML private TextField projectFilename;
	@FXML private TextField messageFilename;
	@FXML private TextArea validationOutput;
	@FXML private Button projectButton;
	@FXML private Button messageButton;
	@FXML private Button validateButton;
	@FXML private Label statusBarLabel;
	
	private File projectFile;
	private File messageFile;
	private File codegenFolder;
	
	private UserSettings userSettings;
	
	/**
	 * Called when the user clicks the button select the OTM project file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleSelectProjectFile(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select OTM Project",
				userSettings.getProjectFolder(),
				OTP_EXTENSION_FILTER, ALL_EXTENSION_FILTER );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Loading Project: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws OtmApplicationException {
					try {
						projectFile = selectedFile;
						setFilenameText( selectedFile.getName(), projectFilename );
						generateSchemas();
						
                    } catch (Exception e) {
                        throw new OtmApplicationException( e.getMessage(), e );
                        
					} finally {
						userSettings.setProjectFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button select the OTM project file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleSelectMessageFile(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select Message to Validate",
				userSettings.getMessageFolder(),
				XML_EXTENSION_FILTER, JSON_EXTENSION_FILTER );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Validating Message File: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws OtmApplicationException {
					try {
						messageFile = selectedFile;
						setFilenameText( selectedFile.getName(), messageFilename );
						handleValidateMessage( null );
						
                    } catch (Exception e) {
                        throw new OtmApplicationException( e.getMessage(), e );
                        
					} finally {
						userSettings.setMessageFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button select the OTM project file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleValidateMessage(ActionEvent event) {
		Runnable r = new BackgroundTask( "Validating Message...", StatusType.INFO ) {
			public void execute() throws OtmApplicationException {
			    try {
	                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
	                
	                new MessageValidator( codegenFolder, new PrintStream( bytesOut ) )
	                        .validate( messageFile );
	                validationOutput.setText( new String( bytesOut.toByteArray() ) );
			        
                } catch (Exception e) {
                    throw new OtmApplicationException( e.getMessage(), e );
			    }
			}
		};
		
		new Thread( r ).start();
	}
	
	/**
	 * Generates XML and JSON schemas on the local file system from the selected
	 * OTM project file.
	 * 
	 * @throws SchemaCompilerException  thrown if an error occurs or the model
	 *									contains validation errors
	 */
    private void generateSchemas() throws SchemaCompilerException {
        CompileAllCompilerTask compilerTask = new CompileAllCompilerTask();
        ValidationFindings findings;
        
        CompilerExtensionRegistry.setActiveExtension( "OTA2" );
        
        codegenFolder = getOutputFolder();
        codegenFolder.mkdirs();
        compilerTask.applyTaskOptions( new ValidationCompileOptions( codegenFolder ) );
        findings = compilerTask.compileOutput( projectFile );
        
        if (findings.hasFinding( FindingType.ERROR )) {
            if (log.isErrorEnabled()) {
                log.error( "ERROR MESSAGES:" );
                
                for (String message : findings.getValidationMessages( FindingType.ERROR,
                        FindingMessageFormat.IDENTIFIED_FORMAT )) {
                    log.error( String.format( "  %s", message ) );
                }
            }
            throw new SchemaCompilerException( "Errors in OTM model (see console for DETAILS)." );
        }
    }
	
	/**
	 * Returns the location of the schema generation output folder.  If the folder
	 * currently exists, it and its contents will be deleted by this method.
	 * 
	 * @return File
	 */
	private File getOutputFolder() {
		File projectFolder = projectFile.getParentFile();
		String folderName = projectFile.getName();
		int dotIdx = folderName.lastIndexOf( '.' );
		File outputFolder;
		
		if (dotIdx >= 0) {
			folderName = folderName.substring( 0, dotIdx );
		}
		folderName += "_ValidatorOutput";
		outputFolder = new File( projectFolder, folderName );
		deleteFolderContents( outputFolder );
		return outputFolder;
	}
	
	/**
	 * Recursively deletes the given folder and all of its contents.
	 * 
	 * @param folderLocation  the location of the folder to be deleted
	 */
	private void deleteFolderContents(File folderLocation) {
		if (folderLocation.exists()) {
			if (folderLocation.isDirectory()) {
				for (File folderMember : folderLocation.listFiles()) {
					deleteFolderContents( folderMember );
				}
			}
			FileUtils.delete( folderLocation );
		}
	}
	
	/**
	 * Updates the value of the specified filename text field.
	 * 
	 * @param filenameValue  the value to assign to the filename text field
	 * @param textField  the text field to which the value will be assigned
	 */
	private void setFilenameText(String filenameValue, TextField textField) {
		Platform.runLater( () -> textField.setText( filenameValue ) );
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String, org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater( () -> {
            statusBarLabel.setText( message );
            projectFilename.disableProperty().set( disableControls );
            projectButton.disableProperty().set( disableControls );
            messageFilename.disableProperty().set( disableControls );
            messageButton.disableProperty().set( disableControls );
            validateButton.disableProperty().set( disableControls );
		});
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#updateControlStates()
	 */
	@Override
	protected void updateControlStates() {
		Platform.runLater( () -> {
            boolean projectSelected = (projectFile != null) && projectFile.exists();
            boolean messageSelected = (messageFile != null) && messageFile.exists();
            
            validateButton.disableProperty().set( !projectSelected || !messageSelected );
		});
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller.
	 *
	 * @param primaryStage  the primary stage for this controller
	 */
	@Override
	protected void initialize(Stage primaryStage) {
		super.initialize( primaryStage );
		this.userSettings = UserSettings.load();
		updateControlStates();
	}
	
}
