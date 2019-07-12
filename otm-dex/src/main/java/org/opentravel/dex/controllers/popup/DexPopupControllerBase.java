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

package org.opentravel.dex.controllers.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Controller base class for dialog pop-up menus.
 * 
 * @author dmh
 *
 */
public abstract class DexPopupControllerBase implements DexPopupController {
    private static Log log = LogFactory.getLog( DexPopupControllerBase.class );

    public enum Results {
        OK, CANCEL;
    }

    // Define LAYOUT_FILE
    // public static final String LAYOUT_FILE = "/UnlockLibraryDialog.fxml";

    // @FXML declarations

    protected static String helpText = "";
    protected String title = "";

    protected Results result = Results.OK;
    // Each sub-type must define its own stage and pass it in setStage().
    protected Stage popupStage;

    @Override
    public void checkNodes() {}

    @Override
    public void clear() {
        // Only implement if needed for this controller
    }

    // UNCOMMENT - needs to be static to simplify creating the popup dialog
    //
    // /**
    // * Initialize this controller using the passed FXML loader.
    // * <p>
    // * Note: This approach using a static stage and main controller hides the complexity from calling controller.
    // * Otherwise, this code must migrate into the calling controller.
    // */
    // public static DexPopupControllerBase init() {
    // FXMLLoader loader = new FXMLLoader(DexPopupControllerBase.class.getResource(LAYOUT_FILE));
    // DexPopupControllerBase controller = null;
    // try {
    // // Load the fxml file initialize controller it declares.
    // Pane pane = loader.load();
    // // Create scene and stage
    // Stage dialogStage = new Stage();
    // dialogStage.setScene(new Scene(pane));
    // dialogStage.initModality(Modality.APPLICATION_MODAL);
    // popupStage = dialogStage;
    //
    // // get the controller from it.
    // controller = loader.getController();
    // if (!(controller instanceof DexPopupControllerBase))
    // throw new IllegalStateException("Error creating dialog box controller.");
    // } catch (IOException e1) {
    // log.error("Error loading dialog box. " + e1.getLocalizedMessage());
    // throw new IllegalStateException(
    // "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString());
    // }
    // return controller;
    // }

    public void doCancel() {
        clear();
        popupStage.close();
        result = Results.CANCEL;
    }

    public void doOK() {
        clear();
        popupStage.close();
        result = Results.OK;
    }

    /**
     * 
     * @return true if and only if the doOK() method was used to close the dialog.
     */
    public boolean okResult() {
        return result.equals( Results.OK );
    }

    /**
     * 
     * @return result of OK or Cancel
     */
    public Results getResult() {
        return result;
    }

    /**
     * Is run when the associated .fxml file is loaded.
     */
    @Override
    @FXML
    public void initialize() {
        log.debug( "Initialize injection point." );
    }

    @Override
    public void refresh() {
        // NO-OP
    }

    protected String getTitle() {
        return title;
    }

    protected void postHelp(String helpText, TextFlow helpControl) {
        if (helpControl != null)
            helpControl.getChildren().add( new Text( helpText ) );
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Base class will call setup() before <i>showing</i> this dialog.
     * <p>
     * Sub-types <b>must</b> setStage with their own stage. e.g. <code>super.setStage(dialogTitle, dialogStage);</code>
     * <p>
     * Sub-types should:
     * <ul>
     * <li>Set action handlers <code> dialogButtonCancel.setOnAction(e -> doCancel());</code>
     * <li>Provide Help text <code> postHelp(helpText, dialogHelp); </code>
     * <li>Post message in the window <code> dialogText.setText(message);</code>
     * </ul>
     * 
     * @param message
     */
    protected abstract void setup(String message);

    /**
     * Provides the base class access to the controller's stage. Sets stage, stage title and Checks the fx nodes.
     * 
     * @param title
     * @param popupStage
     */
    protected void setStage(String title, Stage popupStage) {
        if (popupStage == null)
            throw new IllegalStateException( "Missing stage." );
        checkNodes();
        setTitle( title );
        popupStage.setTitle( title );
        this.popupStage = popupStage;
    }

    @Override
    public void show(String message) {
        setup( message );
        popupStage.show();
    }

    @Override
    public Results showAndWait(String message) {
        setup( message );
        popupStage.showAndWait();
        return result;
    }

}
