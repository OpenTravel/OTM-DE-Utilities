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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.DexMainControllerBase;

import javafx.fxml.FXML;
import javafx.geometry.Dimension2D;
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
    public enum Results {
        OK, CANCEL, ERROR;
    }

    private static Logger log = LogManager.getLogger( DexPopupControllerBase.class );
    protected static String helpText = "";

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

    // Each sub-type must define its own stage and pass it in setStage().
    protected Stage popupStage;
    protected String title = "";
    protected Results result = Results.OK;
    protected MenuItem launchedFromMenuItem = null;

    @Override
    public void checkNodes() {}


    @Override
    public void clear() {
        // Only implement if needed for this controller
    }

    /**
     * Clear, close stage and set result to Cancel
     */
    public void doCancel() {
        clear();
        if (popupStage != null)
            popupStage.close();
        result = Results.CANCEL;
    }

    /**
     * Un-select the menu item if known
     */
    public void doClose(WindowEvent e) {
        if (launchedFromMenuItem != null)
            launchedFromMenuItem.setDisable( false );
    }

    /**
     * Clear, close stage and set result to OK
     */
    public void doOK() {
        clear();
        popupStage.close();
        result = Results.OK;
    }

    /**
     * 
     * @return result of OK or Cancel
     */
    public Results getResult() {
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void hide() {
        popupStage.hide();
    }

    /**
     * Is run when the associated .fxml file is loaded.
     */
    @Override
    @FXML
    public void initialize() {
        // log.debug( "Initialize injection point." );
    }

    /**
     * 
     * @return true if and only if the doOK() method was used to close the dialog.
     */
    public boolean okResult() {
        return result.equals( Results.OK );
    }

    protected void postHelp(String helpText, TextFlow helpControl) {
        if (helpControl != null)
            helpControl.getChildren().add( new Text( helpText ) );
    }

    @Override
    public void refresh() {
        // NO-OP
    }

    /**
     * Provides the base class access to the controller's stage. Sets stage, stage title, and Checks the fx nodes.
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

    /**
     * Note: caller must save dimensions in user settings.
     * 
     * @param title
     * @param popupStage
     * @param dimension
     */
    protected void setStage(String title, Stage popupStage, Dimension2D dimension) {
        setStage( title, popupStage );
        if (dimension != null) {
            popupStage.setHeight( dimension.getHeight() );
            popupStage.setWidth( dimension.getWidth() );
        }
    }

    @Override
    public void setTitle(String title) {
        this.title = " ";
        if (title != null)
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

    @Override
    public void show(String message) {
        setup( message );
        popupStage.show();
    }

    @Override
    public Results showAndWait(String message) {
        setup( message );
        if (popupStage == null)
            log.warn( "Could not show pop-up window, null stage." );
        else {
            popupStage.requestFocus();
            popupStage.showAndWait();
        }
        return result;
    }

}
