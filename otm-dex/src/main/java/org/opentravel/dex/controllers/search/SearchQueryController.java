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

package org.opentravel.dex.controllers.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexRepositorySelectionEvent;
import org.opentravel.dex.events.DexSearchResultsEvent;
import org.opentravel.dex.repository.RepositorySearchCriteria;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.SearchRepositoryTask;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositorySearchResult;

import java.util.List;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Manage the repository search panel
 * 
 * {@link RemoteRepository#getEntityWhereUsed(org.opentravel.schemacompiler.model.NamedEntity, boolean)}
 *
 * @author dmh
 *
 */
public class SearchQueryController extends DexIncludedControllerBase<OtmLibraryMember> implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( SearchQueryController.class );

    @FXML
    private VBox searchQueryVBox;
    @FXML
    private ChoiceBox<String> statusChoice;
    @FXML
    private TextField searchTerm;
    @FXML
    private Button doSearch;
    @FXML
    private Button clearSearch;
    @FXML
    private RadioButton latestOnlyRadio;
    @FXML
    private RadioButton lockedRadio;

    private Repository currentRepository;
    private OtmLibraryMember currentMember = null;

    private static final EventType[] subscribedEvents =
        {DexRepositorySelectionEvent.REPOSITORY_SELECTED, DexMemberSelectionEvent.MEMBER_SELECTED};
    private static final EventType[] publishedEvents = {DexSearchResultsEvent.SEARCH_RESULTS};

    @Override
    public void checkNodes() {
        if (searchQueryVBox == null)
            throw new IllegalStateException( "Null query event publisher node in search controller." );
        if (searchTerm == null)
            throw new IllegalStateException( "Null search term in repository search controller." );
        if (doSearch == null)
            throw new IllegalStateException( "Null search button in repository search controller." );
        if (clearSearch == null)
            throw new IllegalStateException( "Null clear search button in repository search controller." );
        if (latestOnlyRadio == null || lockedRadio == null)
            throw new IllegalStateException( "Null node in search controller." );

        log.debug( "FXML Nodes checked OK." );
    }

    public SearchQueryController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    @FXML
    public void initialize() {
        log.debug( "Repository Selection Controller initialized." );
    }

    /**
     * @param repository
     * 
     */
    public void setRepository(Repository repository) {
        currentRepository = repository;
    }

    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        eventPublisherNode = searchQueryVBox;
        log.debug( "Search Stage set." );
    }

    @FXML
    public void clearSearch(ActionEvent event) {
        searchTerm.setText( "" );
    }

    @FXML
    public void doSearch(ActionEvent e) {
        runSearch( e );
    }

    private void runSearch(ActionEvent event) {
        DexStatusController statusController = null;
        if (mainController != null) {
            currentRepository = mainController.getSelectedRepository();
            statusController = mainController.getStatusController();
        }
        RepositorySearchCriteria criteria = new RepositorySearchCriteria( currentRepository, searchTerm.getText() );
        criteria.setSubject( currentMember );
        criteria.setLatestVersionsOnly( latestOnlyRadio.isSelected() );
        criteria.setLockedOnly( lockedRadio.isSelected() );

        if (currentRepository != null)
            new SearchRepositoryTask( criteria, this::handleTaskComplete, statusController ).go();
    }

    private void handleEvent(DexRepositorySelectionEvent event) {
        // TODO
    }

    private void handleEvent(DexMemberSelectionEvent event) {
        try {
            post( event.getMember() );
        } catch (Exception e) {
            // NO-OP
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        log.debug( event.getEventType() + " event received." );
        if (event instanceof DexRepositorySelectionEvent)
            handleEvent( (DexRepositorySelectionEvent) event );
        else if (event instanceof DexMemberSelectionEvent)
            handleEvent( (DexMemberSelectionEvent) event );
        else
            refresh();
    }

    /**
     * Respond to search task complete by putting results from task into a new SearchResultsDAO and firing that data
     * access object in an event.
     */
    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        log.debug( "Search returned." );
        if (event != null && event.getTarget() instanceof SearchRepositoryTask) {
            List<RepositorySearchResult> repoResults = ((SearchRepositoryTask) event.getTarget()).getResults();
            SearchResultsDAO results = new SearchResultsDAO( currentRepository, repoResults );
            fireEvent( new DexSearchResultsEvent( results ) );
        }
    }

    @Override
    public void post(OtmLibraryMember member) throws Exception {
        currentMember = member;
        searchTerm.setText( member.getName() );
    }
}
