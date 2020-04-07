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

package org.opentravel.dex.controllers.member;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DexPopupController;
import org.opentravel.dex.events.DexFilterChangeEvent;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;
import org.opentravel.schemacompiler.model.BuiltInLibrary;

import java.util.TreeMap;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Controller for library member filter controller.
 * 
 * @author dmh
 *
 */
public class MemberFilterController extends DexIncludedControllerBase<Void> {
    private static Log log = LogFactory.getLog( MemberFilterController.class );

    // public enum LibraryFilterNodes {
    // Library, Name, Type, State;
    // }

    // Class specific data
    //
    private static final String ALLLIBS = "All Libraries";
    // All event types fired by this controller.
    private static final EventType[] publishedEvents =
        {DexFilterChangeEvent.FILTER_CHANGED, DexLibrarySelectionEvent.LIBRARY_SELECTED};
    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexModelChangeEvent.MODEL_CHANGED, DexLibrarySelectionEvent.LIBRARY_SELECTED};

    // Use type instead
    private OtmLibraryMemberType objectType = null;

    public static final String ALL = "All Objects";

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private HBox memberFilter;

    @FXML
    private ChoiceBox<String> librarySelector;
    @FXML
    private TextField memberNameFilter;

    @FXML
    private ComboBox<String> memberTypeCombo;
    // @FXML
    // private MenuButton memberTypeMenu;
    @FXML
    private RadioButton latestButton;
    @FXML
    private RadioButton editableButton;
    @FXML
    private RadioButton errorsButton;

    @FXML
    private RadioButton builtInsButton;
    private String textFilterValue = null;
    private OtmModelManager modelMgr;

    private TreeMap<String,OtmLibrary> libraryMap2 = new TreeMap<>();

    private String libraryFilter = null;

    private boolean ignoreClear = false;

    private boolean latestVersionOnly = false;

    private boolean editableOnly = false;
    private String classNameFilter = null;
    // Possible alternate target for change events
    private DexPopupController popupController = null;
    private boolean errorsOnly = false;
    private boolean builtIns = false;
    private OtmTypeProvider minorVersionMatch = null;

    public MemberFilterController() {
        super( subscribedEvents, publishedEvents );
        // log.debug("Member Filter Controller constructor.");
    }

    /**
     * Filter on any case of the text in the memberNameFilter
     */
    private void applyTextFilter() {
        textFilterValue = memberNameFilter.getText().toLowerCase();
        fireFilterChangeEvent();
    }

    @Override
    public void checkNodes() {
        if (!(memberFilter instanceof HBox))
            throw new IllegalStateException( "Member Filter not injected by FXML." );
        if (!(librarySelector instanceof ChoiceBox))
            throw new IllegalStateException( "Library selector not injected by FXML." );
        if (!(memberNameFilter instanceof TextField))
            throw new IllegalStateException( "memberNameFilter not injected by FXML." );
        if (!(memberTypeCombo instanceof ComboBox))
            throw new IllegalStateException( "memberTypeCombo not injected by FXML." );
        if (!(latestButton instanceof RadioButton))
            throw new IllegalStateException( "latestButton not injected by FXML." );
        if (!(editableButton instanceof RadioButton))
            throw new IllegalStateException( "editableButton not injected by FXML." );
        if (!(errorsButton instanceof RadioButton))
            throw new IllegalStateException( "errorsButton not injected by FXML." );
        if (!(builtInsButton instanceof RadioButton))
            throw new IllegalStateException( "builtInsButton not injected by FXML." );
    }

    @Override
    public void clear() {
        // When posting updated filter results, do not clear the filters.
        if (!ignoreClear) {
            if (mainController != null)
                modelMgr = mainController.getModelManager();
            configureLibraryChoice();
            libraryFilter = null;

            textFilterValue = null;
            memberNameFilter.setText( "" );
        }
    }

    @Override
    public void configure(DexMainController mainController) {
        super.configure( mainController );
        configure( mainController.getModelManager() );
        eventPublisherNode = memberFilter;
    }

    /**
     * Configure filter.
     * 
     * @param modelManager
     */
    public void configure(OtmModelManager modelManager) {
        modelMgr = modelManager;
        configureLibraryChoice();
    }

    public void configure(OtmModelManager modelManager, DexPopupController popupController) {
        configure( modelManager );
        this.popupController = popupController;
    }

    private void configureLibraryChoice() {
        if (modelMgr == null) {
            log.error( "Needed Model Manager is null." );
            return;
        }
        libraryMap2.clear();
        // libraryMap2.put( ALLLIBS, null );
        for (OtmLibrary lib : modelMgr.getLibraries())
            if (lib.getName() != null && !lib.getName().isEmpty())
                libraryMap2.put( lib.getName(), lib );

        ObservableList<String> libList = FXCollections.observableArrayList( libraryMap2.keySet() );
        libList.add( 0, ALLLIBS );
        // libList.sort( null );
        librarySelector.setItems( libList );
        // librarySelector.getSelectionModel().select( ALLLIBS );
        librarySelector.getSelectionModel().select( 0 );
        librarySelector.setOnAction( e -> setLibraryFilter() );
        // log.debug("Configured library selection combo control.");
    }

    /**
     * Make and fire a filter event. Set ignore clear in case event handler tries to clear() this controller.
     */
    private void fireFilterChangeEvent() {
        if (eventPublisherNode != null) {
            ignoreClear = true; // Set just in case event handler does a clear
            // log.debug( "Ready to fire controller level Filter Change event." );
            eventPublisherNode.fireEvent( new DexFilterChangeEvent( this, memberFilter ) );
            ignoreClear = false;
        } else if (popupController != null) {
            popupController.refresh();
        }
    }

    private OtmLibrary getSelectedLibrary() {
        String key = librarySelector.getSelectionModel().getSelectedItem();
        return libraryMap2.get( key );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (event instanceof DexLibrarySelectionEvent)
            librarySelectionHandler( (DexLibrarySelectionEvent) event );
        else if (event instanceof DexModelChangeEvent)
            handleModelChange( (DexModelChangeEvent) event );
    }

    private void handleModelChange(DexModelChangeEvent e) {
        modelMgr = e.getModelManager();
        clear();
    }

    @Override
    public void initialize() {
        // log.debug("Member Filter Controller - Initialize");
        checkNodes();
        builtInsButton.setSelected( builtIns );

        // Would work for combo
        configMemberTypeChoice();
        errorsButton.setOnAction( e -> setErrorsOnly() );
        memberNameFilter.textProperty().addListener( (v, o, n) -> applyTextFilter() );
        editableButton.setOnAction( e -> setEditableOnly() );
        latestButton.setOnAction( e -> setLatestOnly() );
        builtInsButton.setOnAction( e -> setBuiltIns() );

        // Get focus after the scene is set
        Platform.runLater( () -> memberNameFilter.requestFocus() );
    }

    private void configMemberTypeChoice() {
        ObservableList<String> data = FXCollections.observableArrayList();
        data.add( ALL );
        for (OtmLibraryMemberType type : OtmLibraryMemberType.values()) {
            data.add( type.label() );
        }
        memberTypeCombo.setPromptText( "Object Type" );
        memberTypeCombo.setOnAction( this::setTypeFilter );
        memberTypeCombo.setItems( data );
    }

    /**
     * 
     * @param member to test
     * @return true if the object passes the selection filters (should be displayed)
     */
    public boolean isSelected(OtmLibraryMember member) {
        if (member == null || member.getLibrary() == null) {
            log.warn( "Filter passed invalid member." );
            return true;
        }
        // log.debug( "Is " + member + " selected?" );
        // debugPrint( member );
        if (minorVersionMatch != null
            && !minorVersionMatch.getLibrary().getVersionChain().isLaterVersion( minorVersionMatch, member ))
            return false;
        if (libraryFilter != null && !member.getLibrary().getName().startsWith( libraryFilter ))
            return false;
        if (textFilterValue != null && !member.getName().toLowerCase().startsWith( textFilterValue ))
            return false;
        if (latestVersionOnly && !member.isLatestVersion())
            return false;
        if (editableOnly && !member.isEditableMinor())
            return false;
        if (!isMemberTypeSelected( member ))
            return false;
        if (errorsOnly && member.isValid( false ))
            return false;
        if (!builtIns && member.getLibrary().getTL() instanceof BuiltInLibrary)
            return false;

        // log.debug( member.getName() + " passed filter." );
        // No filters applied OR passed all filters
        return true;
    }

    // private void debugPrint(OtmLibraryMember member) {
    // String n = member.getName();
    // String v = member.getLibrary().getVersion();
    // boolean valid = member.isValid();
    // boolean editable = member.isEditable();
    // boolean isLatest = member.getLibrary().isLatestVersion();
    // if (member.getName().equals( "AcceptableGuarantee" )) {
    // boolean lv = member.getLibrary().isLatestVersion();
    // log.debug( "Is " + member.getName() + "version = " + member.getLibrary().getVersion() + " latest version? "
    // + member.getLibrary().isLatestVersion() );
    // }
    // }

    public void librarySelectionHandler(DexLibrarySelectionEvent event) {
        if (event != null && event.getLibrary() != null && !event.getLibrary().getName().equals( libraryFilter )) {
            libraryFilter = event.getLibrary().getName();
            ignoreClear = true;
            librarySelector.getSelectionModel().select( event.getLibrary().getName() );
            fireFilterChangeEvent();
            // log.debug( "Set Library Filter to: " + libraryFilter );
            ignoreClear = false;
        }
    }

    @Override
    public void refresh() {
        if (mainController != null)
            modelMgr = mainController.getModelManager();
        OtmLibrary prevLib = getSelectedLibrary();
        configureLibraryChoice();
        setLibraryFilter( prevLib );
    }

    private void setBuiltIns() {
        builtIns = builtInsButton.isSelected();
        fireFilterChangeEvent();
    }

    /**
     * Set the built-in button
     * 
     * @param show
     */
    public void setBuiltIns(boolean show) {
        builtInsButton.setSelected( show );
        builtIns = builtInsButton.isSelected();
        fireFilterChangeEvent();
    }

    private void setEditableOnly() {
        // log.debug("Editable set to: " + editableButton.isSelected());
        editableOnly = editableButton.isSelected();
        fireFilterChangeEvent();
    }

    private void setErrorsOnly() {
        errorsOnly = errorsButton.isSelected();
        fireFilterChangeEvent();
    }

    private void setLatestOnly() {
        // log.debug("Latest only set to: " + latestButton.isSelected());
        latestVersionOnly = latestButton.isSelected();
        fireFilterChangeEvent();
    }

    /**
     * Only select the same type of object with same name and in the same version chain.
     * 
     * @param type
     */
    public void setMinorVersionFilter(OtmTypeProvider type) {
        minorVersionMatch = type;
    }

    // Future - use fxControls or other package to get a multiple check box or even check tree to select versions.
    //
    /**
     * Run when a GUI control changes the selected library.
     */
    private void setLibraryFilter() {
        String selection = ALLLIBS;
        if (librarySelector.getSelectionModel().getSelectedItem() != null) {
            selection = librarySelector.getSelectionModel().getSelectedItem();
            if (selection.equals( ALLLIBS )) {
                clear();
                fireFilterChangeEvent();
            } else {
                setLibraryFilter( libraryMap2.get( selection ) );
            }
        }
        librarySelector.setValue( selection );
        memberFilter.fireEvent( new DexLibrarySelectionEvent( libraryMap2.get( selection ) ) );
    }

    private void setLibraryFilter(OtmLibrary lib) {
        ignoreClear = true;
        if (lib != null && !lib.getName().equals( libraryFilter )) {
            libraryFilter = lib.getName();
            librarySelector.getSelectionModel().select( lib.getName() );
            fireFilterChangeEvent();
        }
        // log.debug("Set Library Filter to: " + libraryFilter);
        ignoreClear = false;
    }

    private void setTypeFilter(ActionEvent e) {
        if (memberTypeCombo.getValue() != null) {
            String value = memberTypeCombo.getValue();
            setTypeFilter( value );
        }
    }

    /**
     * Set the type filter to the object type's class simple name
     * 
     * @param value
     */
    public void setTypeFilter(OtmLibraryMember member) {
        if (member == null)
            classNameFilter = null;
        else if (member instanceof OtmLibraryMember)
            setTypeFilterValue( OtmLibraryMemberType.get( member ) );
    }

    public void clearTypeFilter() {
        objectType = null;
        fireFilterChangeEvent();
    }

    public void setTypeFilter(String value) {
        if (value.isEmpty() || value.equals( ALL ))
            clearTypeFilter();
        else
            setTypeFilterValue( OtmLibraryMemberType.getClass( value ) );
    }

    /**
     * Return false if the member fails to pass the class name filter (member object type). If the filter value is null,
     * all member pass through the filter.
     * 
     * @param member
     * @return
     */
    public boolean isMemberTypeSelected(OtmLibraryMember member) {
        if (objectType == null)
            return true; // No filter applied
        if (member == null)
            return false;
        return OtmLibraryMemberType.is( member, objectType );
    }

    /**
     * Set the type filter value. Expected to be the object type's class simple name
     * 
     * @param value
     */
    public void setTypeFilterValue(OtmLibraryMemberType value) {
        objectType = value;
        fireFilterChangeEvent();
        // log.debug("Set Type Filter: " + classNameFilter);
    }
}
