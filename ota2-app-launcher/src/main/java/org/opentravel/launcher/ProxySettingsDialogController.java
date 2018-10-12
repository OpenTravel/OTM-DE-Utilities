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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 * Controller class for the dialog used to edit the network proxy settings.
 */
public class ProxySettingsDialogController {
	
	public static final String FXML_FILE = "/proxy-settings.fxml";
	
	private static final String CSS_ERROR_STYLE             = "-fx-background-color: pink";
	private static final String MSG_PROXY_HOST_REQUIRED     = "validation.proxyHost.required";
	private static final String MSG_PROXY_HOST_INVALID      = "validation.proxyHost.invalid";
	private static final String MSG_PROXY_PORT_REQUIRED     = "validation.proxyPort.required";
	private static final String MSG_PROXY_PORT_INVALID      = "validation.proxyPort.invalid";
	private static final String MSG_NON_PROXY_HOSTS_INVALID = "validation.nonProxyHosts.invalid";
	
	private static final Pattern hostnamePattern = Pattern.compile( "^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*$" );
	private static final Pattern nphostPattern = Pattern.compile( "^(?:[a-zA-Z0-9]+|\\*)(?:\\.[a-zA-Z0-9]+)*(?:\\.\\*)?$" );
	
	@FXML private CheckBox useProxyCB;
	@FXML private TextField proxyHostText;
	@FXML private TextField proxyPortText;
	@FXML private TextField nonProxyHostsText;
	@FXML private Button okButton;
	
	private Stage dialogStage;
	private boolean okSelected = false;
	
	/**
	 * Called when the user clicks the Ok button to confirm their updates.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOk(ActionEvent event) {
		UserSettings settings = UserSettings.load();
		
		settings.setUseProxy( useProxyCB.isSelected() );
		settings.setProxyHost( proxyHostText.getText() );
		settings.setProxyPort( StringUtils.isEmpty( proxyPortText.getText() ) ?
				null : Integer.parseInt( proxyPortText.getText() ) );
		settings.setNonProxyHosts( nonProxyHostsText.getText() );
		settings.save();
		
		dialogStage.close();
		okSelected = true;
	}
	
	/**
	 * Called when the user clicks the Ok button to cancel their updates.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectCancel(ActionEvent event) {
		dialogStage.close();
	}
	
	/**
	 * Called when the user updates the values in one of the visual controls
	 * to perform validation.
	 */
	private void validateFields() {
		boolean useProxy = useProxyCB.isSelected();
		boolean isValid = true;
		
		proxyHostText.setDisable( !useProxy );
		proxyPortText.setDisable( !useProxy );
		nonProxyHostsText.setDisable( !useProxy );
		
		if (useProxy) {
			isValid &= applyValidationStatus( proxyHostText, validateProxyHost( proxyHostText.getText() ) );
			isValid &= applyValidationStatus( proxyPortText, validateProxyPort( proxyPortText.getText() ) );
			isValid &= applyValidationStatus( nonProxyHostsText, validateNonProxyHosts( nonProxyHostsText.getText() ) );
		}
		okButton.setDisable( !isValid );
	}
	
	/**
	 * Assigns the stage for the dialog.
	 *
	 * @param dialogStage  the dialog stage to assign
	 */
	public void setDialogStage(Stage dialogStage) {
		UserSettings settings = UserSettings.load();
		
		// Initialize field values and perform validation
		useProxyCB.setSelected( settings.isUseProxy() );
		proxyHostText.setText( settings.getProxyHost() );
		proxyPortText.setText( (settings.getProxyPort() == null) ? "" : settings.getProxyPort().toString() );
		nonProxyHostsText.setText( settings.getNonProxyHosts() );
		this.dialogStage = dialogStage;
		validateFields();
		
		// Register listeners for change events
		useProxyCB.selectedProperty().addListener( ( c, o, n ) -> validateFields() );
		proxyHostText.textProperty().addListener( ( c, o, n ) -> validateFields() );
		proxyPortText.textProperty().addListener( ( c, o, n ) -> validateFields() );
		nonProxyHostsText.textProperty().addListener( ( c, o, n ) -> validateFields() );
	}

	/**
	 * Returns true if the user clicked the Ok button to close the dialog.
	 *
	 * @return boolean
	 */
	public boolean isOkSelected() {
		return okSelected;
	}

	/**
	 * @see javafx.stage.Stage#showAndWait()
	 */
	public void showAndWait() {
		dialogStage.showAndWait();
	}
	
	/**
	 * If the given error message is null, the specified control will be assigned
	 * to a non-error visual status.  If non-null, an error highlighting will be
	 * applied and the tooltip set to the error message.  This method returns true
	 * if the control's content is valid; false if an error exists.
	 * 
	 * @param control  the control to which a validation status should be applied
	 * @param errorMessage  the error message to be displayed
	 * @return boolean
	 */
	private boolean applyValidationStatus(Control control, String errorMessage) {
		boolean isValid = (errorMessage == null);
		
		if (isValid) {
			control.setStyle( null );
			control.setTooltip( null );
			
		} else {
			control.setStyle( CSS_ERROR_STYLE );
			control.setTooltip( new Tooltip( errorMessage ) );
		}
		return isValid;
	}
	
	/**
	 * Checks the given hostname value and returns an error string or null
	 * if the hostname is valid.
	 * 
	 * @param hostname  the hostname string to verify
	 * @return String
	 */
	private String validateProxyHost(String hostname) {
		String errorMessage = null;
		
		if (StringUtils.isEmpty( hostname )) {
			errorMessage = MessageBuilder.formatMessage( MSG_PROXY_HOST_REQUIRED );
			
		} else if (!hostnamePattern.matcher( hostname ).matches()) {
			errorMessage = MessageBuilder.formatMessage( MSG_PROXY_HOST_INVALID );
		}
		return errorMessage;
	}
	
	/**
	 * Checks the given port number string value and returns an error string
	 * or null if the string is valid.
	 * 
	 * @param port  the port number string to verify
	 * @return String
	 */
	private String validateProxyPort(String port) {
		String errorMessage = null;
		
		try {
			if (StringUtils.isEmpty( port )) {
				errorMessage = MessageBuilder.formatMessage( MSG_PROXY_PORT_REQUIRED );
				
			} else {
				if (Integer.parseInt( port ) < 0) {
					throw new NumberFormatException();
				}
			}
			
		} catch (NumberFormatException e) {
			errorMessage = MessageBuilder.formatMessage( MSG_PROXY_PORT_INVALID );
		}
		return errorMessage;
	}
	
	/**
	 * Checks the given non-proxy hosts string value and returns an error
	 * string or null if the string is valid.
	 * 
	 * @param nonProxyHosts  the non-proxy hosts string to verify
	 * @return String
	 */
	private String validateNonProxyHosts(String nonProxyHosts) {
		String errorMessage = null;
		
		if (!StringUtils.isEmpty( nonProxyHosts )) {
			String[] nphList = nonProxyHosts.split( "\\|" );
			
			for (String npHost : nphList) {
				// Ignore leading or trailing wildcards
				if (npHost.startsWith( "*." )) {
					npHost = npHost.replace( "*.", "a." );
					
				} else if (npHost.endsWith( ".*" )) {
					npHost = npHost.replace( ".*", ".com" );
				}
				
				if (!nphostPattern.matcher( npHost ).matches()) {
					errorMessage = MessageBuilder.formatMessage( MSG_NON_PROXY_HOSTS_INVALID );
					break;
				}
			}
		}
		return errorMessage;
	}
	
}
