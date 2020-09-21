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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmProject;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for selecting a project dialog box pop-up menu.
 * <p>
 * 
 * @author dmh
 *
 */
public class SelectProjectDialogController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( SelectProjectDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/SelectProjectDialog.fxml";

    @FXML
    BorderPane selectProjectDialog;
    @FXML
    Label dialogTitleLabel;
    @FXML
    TextFlow dialogHelp;
    @FXML
    ListView<OtmProject> projectList;
    @FXML
    Button dialogButtonCancel;
    @FXML
    Button dialogButtonOK;

    private OtmModelManager modelManager;

    private List<OtmProject> projects = null;

    protected static Stage dialogStage;

    private static String helpText = "Select the project.";
    private static String dialogTitle = "Project Selection Dialog";

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );

        if (selectProjectDialog == null || dialogTitleLabel == null || dialogHelp == null || projectList == null
            || dialogButtonCancel == null || dialogButtonOK == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @return dialog box controller or null
     */
    public static SelectProjectDialogController init() {
        FXMLLoader loader = new FXMLLoader( SelectProjectDialogController.class.getResource( LAYOUT_FILE ) );
        SelectProjectDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof SelectProjectDialogController))
                throw new IllegalStateException( "Error creating unlock dialog controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );

        }
        positionStage( dialogStage );
        return controller;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // checkNodes();

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        projectList.setOnMouseClicked( this::doSelect );
        postHelp( helpText, dialogHelp );

        // Add items to the project list widget
        if (projects == null && modelManager != null)
            projects = modelManager.getProjects();
        if (projects != null)
            projects.forEach( p -> projectList.getItems().add( p ) );
    }

    /**
     * Select and exit on double click
     * 
     * @param e
     */
    private void doSelect(MouseEvent e) {
        if (e != null && e.getClickCount() > 1)
            doOK();
    }

    /**
     * Must be called before show()-ing the dialog.
     * 
     * @param modelManager
     */
    public void setManager(OtmModelManager modelManager) {
        this.modelManager = modelManager;
    }

    /**
     * If set, this list is used instead of model manager user projects
     * 
     * @param projectList
     */
    public void setProjectList(List<OtmProject> projectList) {
        this.projects = projectList;
    }


    /**
     * 
     * @return selected project or null
     */
    public OtmProject getSelection() {
        if (!okResult() || projectList == null)
            return null;
        return projectList.getSelectionModel().getSelectedItem();
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }
}
