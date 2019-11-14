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
import org.opentravel.dex.controllers.DexMainControllerBase;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller base class for dialog pop-up menus.
 * 
 * @author dmh
 *
 */
public abstract class DexPopupControllerBase implements DexPopupController {
    private static Log log = LogFactory.getLog( DexPopupControllerBase.class );

    public enum Results {
        OK, CANCEL, ERROR;
    }

    protected static String helpText = "";
    protected String title = "";

    protected Results result = Results.OK;
    // Each sub-type must define its own stage and pass it in setStage().
    protected Stage popupStage;
    protected MenuItem launchedFromMenuItem = null;

    @Override
    public void checkNodes() {}

    @Override
    public void clear() {
        // Only implement if needed for this controller
    }


    /**
     * Set the passed stage x and y position over the primary stage
     * 
     * @param dialogStage
     */
    public static void positionStage(Stage dialogStage) {
        // Put the new stage on top of the primary stage
        Stage primaryStage = DexMainControllerBase.getStageStatic();
        if (primaryStage != null) {
            // // Calculate the center position of the parent Stage
            double centerXPosition = primaryStage.getX() + primaryStage.getWidth() / 2d;
            double centerYPosition = primaryStage.getY() + primaryStage.getHeight() / 2d;
            dialogStage.setX( centerXPosition - 400 );
            dialogStage.setY( centerYPosition - 150 );
        }
    }

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
     * Un-select the menu item if known
     */
    public void doClose(WindowEvent e) {
        if (launchedFromMenuItem != null)
            launchedFromMenuItem.setDisable( false );
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

        popupStage.setOnHidden( this::doClose );
    }

    @Override
    public void show(String message) {
        setup( message );
        popupStage.show();
    }

    public void hide() {
        popupStage.hide();
    }

    @Override
    public Results showAndWait(String message) {
        setup( message );
        popupStage.showAndWait();
        return result;
    }

}
