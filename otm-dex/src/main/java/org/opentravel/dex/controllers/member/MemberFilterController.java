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
import org.opentravel.dex.controllers.DexFilter;
import org.opentravel.dex.controllers.DexFilterWidget;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.filters.ButtonFilterWidget;
import org.opentravel.dex.controllers.member.filters.ButtonToggleFilterWidget;
import org.opentravel.dex.controllers.member.filters.FilterWidget;
import org.opentravel.dex.controllers.member.filters.LibraryFilterWidget;
import org.opentravel.dex.controllers.member.filters.MinorVersionFilterWidget;
import org.opentravel.dex.controllers.member.filters.NameFilterWidget;
import org.opentravel.dex.controllers.member.filters.ObjectTypeFilterWidget;
import org.opentravel.dex.controllers.popup.DexPopupController;
import org.opentravel.dex.events.DexFilterChangeEvent;
import org.opentravel.dex.events.DexLibrarySelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberType;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
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
public class MemberFilterController extends DexIncludedControllerBase<Void> implements DexFilter<OtmLibraryMember> {
    private static Log log = LogFactory.getLog( MemberFilterController.class );

    // All event types fired by this controller.
    private static final EventType[] publishedEvents =
        {DexFilterChangeEvent.FILTER_CHANGED, DexLibrarySelectionEvent.LIBRARY_SELECTED};
    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexModelChangeEvent.MODEL_CHANGED, DexLibrarySelectionEvent.LIBRARY_SELECTED};

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private HBox memberFilter;
    @FXML
    private ChoiceBox<String> librarySelector;
    @FXML
    TextField memberNameFilter;
    @FXML
    private ComboBox<String> memberTypeCombo;
    @FXML
    private RadioButton latestButton;
    @FXML
    private RadioButton editableButton;
    @FXML
    private RadioButton errorsButton;
    @FXML
    private RadioButton builtInsButton;

    // Possible alternate target for change events
    private DexPopupController popupController = null;
    // Filter data
    private ButtonFilterWidget builtInFilter;
    // private OtmTypeProvider minorVersionMatch = null;
    private List<DexFilterWidget<OtmLibraryMember>> filters = new ArrayList<>();

    private boolean ignoreClear = false;
    private OtmModelManager modelMgr;

    private FilterWidget latestButtonFilter;

    private FilterWidget editableButtonFilter;

    private FilterWidget errorsButtonFilter;

    private MemberTreeTableController parentController;

    public MemberFilterController() {
        super( subscribedEvents, publishedEvents );
        // log.debug("Member Filter Controller constructor.");
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
            filters.forEach( DexFilterWidget::clear );
        }
    }

    @Override
    public void configure(DexMainController mainController, int viewGroupId) {
        super.configure( mainController, viewGroupId );
        configure( mainController.getModelManager() );
        eventPublisherNode = memberFilter;
    }

    public void configure(OtmModelManager modelManager) {
        modelMgr = modelManager;
        configureFilters();
    }

    public void configure(OtmModelManager modelManager, DexPopupController popupController) {
        configure( modelManager );
        this.popupController = popupController;
    }

    private void configureFilters() {
        filters.add( new LibraryFilterWidget( this, librarySelector ) );
        filters.add( new ObjectTypeFilterWidget( this, memberTypeCombo ) );
        filters.add( new NameFilterWidget( this, memberNameFilter ) );

        // 12/15/2020 - started making filters on demand to improve performance, but it proved to be insignificant
        // performance hit.
        latestButtonFilter =
            new ButtonFilterWidget( this, latestButton ).setSelector( OtmLibraryMember::isLatestVersion );
        latestButton.setOnAction( e -> setLatestFilter() );
        // filters.add( new ButtonFilterWidget( this, latestButton ).setSelector( OtmLibraryMember::isLatestVersion ) );

        editableButtonFilter =
            new ButtonFilterWidget( this, editableButton ).setSelector( OtmLibraryMember::isEditableMinor );
        editableButton.setOnAction( e -> setEditableFilter() );
        // filters.add( new ButtonFilterWidget( this, editableButton ).setSelector( OtmLibraryMember::isEditableMinor )
        // );

        errorsButtonFilter = new ButtonFilterWidget( this, errorsButton ).setSelector( m -> !m.isValid( false ) );
        errorsButton.setOnAction( e -> setErrorsFilter() );
        // filters.add( new ButtonFilterWidget( this, errorsButton ).setSelector( m -> !m.isValid( false ) ) );

        if (popupController != null)
            filters.add( new MinorVersionFilterWidget( this ) );

        this.builtInFilter = new ButtonToggleFilterWidget( this, builtInsButton );
        builtInFilter.set( false );
        builtInFilter.setSelector( m -> m.getLibrary().isBuiltIn() );
        filters.add( builtInFilter );
    }

    private void setErrorsFilter() {
        if (errorsButton.isSelected())
            filters.add( errorsButtonFilter );
        else {
            filters.remove( errorsButtonFilter );
        }
        log.debug( "Set Errors Filter." );
        fireFilterChangeEvent();
    }

    private void setEditableFilter() {
        if (editableButton.isSelected())
            filters.add( editableButtonFilter );
        else {
            filters.remove( editableButtonFilter );
        }
        fireFilterChangeEvent();
    }

    private void setLatestFilter() {
        if (latestButton.isSelected())
            filters.add( latestButtonFilter );
        else {
            filters.remove( latestButtonFilter );
        }
        fireFilterChangeEvent();
    }

    /**
     * Make and fire a filter event. Set ignore clear in case event handler tries to clear() this controller.
     */
    public void fireFilterChangeEvent() {
        // log.debug( "Letting others know about filter change." );
        // For performance, let the parent or pop-up controller know
        if (parentController != null)
            parentController.refresh();
        else if (popupController != null) {
            popupController.refresh();
        }
        // Let everyone else know
        // 12/15/2020 - running events in background solved responsiveness issue.
        Platform.runLater( () -> {
            ignoreClear = true; // Set just in case event handler does a clear
            eventPublisherNode.fireEvent( new DexFilterChangeEvent( this, memberFilter ) );
            ignoreClear = false;
        } );
    }

    @Override
    public OtmModelManager getModelManager() {
        return modelMgr;
    }

    private ObjectTypeFilterWidget getTypeFilter() {
        for (DexFilterWidget<?> w : filters)
            if (w instanceof ObjectTypeFilterWidget)
                return (ObjectTypeFilterWidget) w;
        return null;
    }

    private MinorVersionFilterWidget getMinorVersionFilter() {
        for (DexFilterWidget<?> w : filters)
            if (w instanceof MinorVersionFilterWidget)
                return (MinorVersionFilterWidget) w;
        return null;
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (event instanceof DexLibrarySelectionEvent)
            handleLibrarySelectionEvent( (DexLibrarySelectionEvent) event );
        else if (event instanceof DexModelChangeEvent)
            handleModelChange( (DexModelChangeEvent) event );
    }

    private void handleModelChange(DexModelChangeEvent e) {
        // modelMgr = e.getModelManager();
        // filters.forEach( DexFilterWidget::refresh );
    }

    @Override
    public void initialize() {
        checkNodes();

        // Get focus after the scene is set
        Platform.runLater( () -> memberNameFilter.requestFocus() );
    }

    /**
     * 
     * @param member to test
     * @return true if the object passes the selection filters (should be displayed)
     */
    @Override
    public boolean isSelected(OtmLibraryMember member) {
        if (member == null || member.getLibrary() == null) {
            log.warn( "Filter passed invalid member." );
            return true;
        }

        for (DexFilterWidget<OtmLibraryMember> w : filters)
            if (!w.isSelected( member ))
                return false;

        // log.debug( member.getName() + " passed filter." );
        // No filters applied OR passed all filters
        return true;
    }

    public void handleLibrarySelectionEvent(DexLibrarySelectionEvent event) {
        filters.forEach( w -> w.selectionHandler( event ) );
    }

    @Override
    public void refresh() {
        if (mainController != null)
            modelMgr = mainController.getModelManager();
        filters.forEach( DexFilterWidget::refresh );
    }

    public void setController(MemberTreeTableController parent) {
        this.parentController = parent;
    }

    /**
     * Set the built-in button
     *
     * @param show
     */
    public void setBuiltIns(boolean show) {
        if (builtInFilter != null)
            builtInFilter.set( show );
    }

    /**
     * Only select the same type of object with same name and in the same version chain.
     * 
     * @param type
     */
    public void setMinorVersionFilter(OtmTypeProvider type) {
        MinorVersionFilterWidget w = getMinorVersionFilter();
        if (w == null) {
            w = new MinorVersionFilterWidget( this );
            filters.add( w );
        }

        if (w != null)
            w.set( type );
        // minorVersionMatch = type;
    }

    /**
     * Set the type filter to the member's type.
     *
     * @see ObjectTypeFilterWidget#set(OtmLibraryMemberType)
     * @param member
     */
    public void setTypeFilter(OtmLibraryMember member) {
        ObjectTypeFilterWidget w = getTypeFilter();
        if (w != null)
            w.set( OtmLibraryMemberType.get( member ) );
    }

    /**
     * Set the type filter value.
     * 
     * @see ObjectTypeFilterWidget#set(OtmLibraryMemberType)
     * @param value
     */
    public void setTypeFilterValue(OtmLibraryMemberType value) {
        ObjectTypeFilterWidget w = getTypeFilter();
        if (w != null)
            w.set( value );
    }
}
