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

package org.opentravel.objecteditor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexStyleSheetHandler;
import org.opentravel.dex.controllers.DexMainControllerBase;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.MenuBarWithProjectController;
import org.opentravel.dex.controllers.graphics.GraphicsTabController;
import org.opentravel.dex.controllers.library.LibrariesTabController;
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.controllers.member.MemberTreeTableController;
import org.opentravel.dex.controllers.member.properties.MemberPropertiesTabController;
import org.opentravel.dex.controllers.member.usage.WhereUsedTabController;
import org.opentravel.dex.controllers.repository.RepositoryTabController;
import org.opentravel.dex.controllers.resources.ResourcesTabController;
import org.opentravel.dex.controllers.search.SearchTabController;

import java.awt.Dimension;

import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Main controller for OtmObjectEditorLayout.fxml.
 * 
 * @author dmh
 *
 */
public class ObjectEditorController extends DexMainControllerBase {
    private static Log log = LogFactory.getLog( ObjectEditorController.class );

    @FXML
    private MenuBarWithProjectController menuBarWithProjectController;
    @FXML
    private DexStatusController dexStatusController;
    @FXML
    private MemberFilterController memberFilterController;
    @FXML
    private MemberTreeTableController memberTreeTableController;
    @FXML
    private MemberPropertiesTabController memberPropertiesTabController;
    @FXML
    private LibrariesTabController librariesTabController;
    @FXML
    private WhereUsedTabController whereUsedTabController;
    @FXML
    private RepositoryTabController repositoryTabController;
    @FXML
    private ResourcesTabController resourcesTabController;
    @FXML
    private SearchTabController searchTabController;
    @FXML
    private GraphicsTabController graphicsTabController;

    @Override
    public void checkNodes() {
        if (!(repositoryTabController instanceof RepositoryTabController))
            throw new IllegalStateException( "Repository tab not injected by FXML." );
        if (!(resourcesTabController instanceof ResourcesTabController))
            throw new IllegalStateException( "Resource tab not injected by FXML." );
        if (!(memberPropertiesTabController instanceof MemberPropertiesTabController))
            throw new IllegalStateException( "Member properties tab not injected by FXML." );
        if (!(librariesTabController instanceof LibrariesTabController))
            throw new IllegalStateException( "Libraries tab not injected by FXML." );
        if (!(whereUsedTabController instanceof WhereUsedTabController))
            throw new IllegalStateException( "Where used tab not injected by FXML." );
        // Other controllers not checked...to do if needed
    }

    @Override
    public void initialize() {
        log.debug( "Object Editor Controller - Initialize." );
        checkNodes();
    }

    /**
     * Set up this FX controller
     * 
     * @param stage
     */
    @Override
    public void setStage(Stage stage) {
        super.setStage( stage );
        // log.debug( "Object Editor Controller - Setting Stage" );

        // Get the user preferences, Override with the instance from the abstract main controller
        if (getUserSettings() instanceof UserSettings)
            userSettings = getUserSettings();
        else
            userSettings = UserSettings.load();

        DexStyleSheetHandler styleSheetHandler = new DexStyleSheetHandler( getUserSettings() );
        styleSheetHandler.apply( stage );

        // Set the stage size based on user preferences
        Dimension size = userSettings.getWindowSize();
        stage.setHeight( size.height );
        stage.setWidth( size.width );

        stage.widthProperty().addListener( (observable, oldValue, newValue) -> {
            // log.debug("Width changed!! - new = " + newValue);
            userSettings.setWindowSize( new Dimension( newValue.intValue(), userSettings.getWindowSize().height ) );
            userSettings.save();
        } );
        stage.heightProperty().addListener( (ob, ov, newValue) -> {
            // log.debug("Height changed!! - new = " + newValue);
            userSettings.setWindowSize( new Dimension( userSettings.getWindowSize().width, newValue.intValue() ) );
            userSettings.save();
        } );

        // set initial position
        stage.setX( userSettings.getWindowPosition().getX() );
        stage.setY( userSettings.getWindowPosition().getY() );

        // included controllers - controllers without a parent tab or window
        // * Add to controller list
        // * set their event manager
        // * retain for base class getter
        //
        int viewGroupId = 1; // unique value assigned to all view groups.

        addIncludedController( menuBarWithProjectController, eventManager, viewGroupId++ );
        menuBarWithProjectController.showCombo( true );
        menuBarController = menuBarWithProjectController; // Make available to base class

        // Setup status controller
        addIncludedController( dexStatusController, eventManager, viewGroupId++ );
        statusController = dexStatusController; // Make available to base class

        // Tab controllers - They will include the controllers in that tab.
        // Tab controllers are not in the included controllers list
        repositoryTabController.configure( this, viewGroupId++ ); // TODO - this is slow!
        resourcesTabController.configure( this, viewGroupId++ );
        librariesTabController.configure( this, viewGroupId++ );
        whereUsedTabController.configure( this, viewGroupId++ );
        searchTabController.configure( this, viewGroupId++ );

        graphicsTabController.configure( this, viewGroupId++ );

        // Add menu items for tab controllers that can also be launched in separate views windows
        menuBarWithProjectController.addViewItem( librariesTabController );
        menuBarWithProjectController.addViewItem( memberPropertiesTabController );
        menuBarWithProjectController.addViewItem( repositoryTabController );
        menuBarWithProjectController.addViewItem( resourcesTabController );
        menuBarWithProjectController.addViewItem( searchTabController );
        menuBarWithProjectController.addViewItem( graphicsTabController );

        // Include controllers that are not in tabs
        addIncludedController( memberFilterController, eventManager, viewGroupId++ );
        memberTreeTableController.setFilter( memberFilterController );
        addIncludedController( memberTreeTableController, eventManager, viewGroupId++ );

        memberPropertiesTabController.configure( this, viewGroupId );

        // Now that all controller's event requirements are known
        eventManager.configureEventHandlers();

        // FIXME - remove configure() and addIncludedController without viewGroupId
        // FIXME - pass viewGroupId to windows

        setMainController( this );

        // Catch close requests (works after popup)
        // TODO - how to catch windows close and not close from appExit?
        // stage.setOnHiding( event -> menuBarWithProjectController.appExit( event ) );
        // stage.setOnHiding( event -> log.debug( "Trying to close application." ) );

        // Try opening the last repository they used
        // Repository lastRepo = getRepositoryManager().getRepository( userSettings.getLastRepositoryId() );
        // if (lastRepo != null)
        // menuBarWithProjectController.fireEvent( new DexRepositorySelectionEvent( lastRepo ) );
    }

    public MemberFilterController getMemberFilterController() {
        return memberFilterController;
    }
}
