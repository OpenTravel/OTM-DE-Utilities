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

package org.opentravel.dex.controllers.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexRepositoryItemReplacedEvent;
import org.opentravel.dex.events.DexRepositoryNamespaceSelectionEvent;
import org.opentravel.dex.events.DexRepositorySelectionEvent;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.ListSubnamespacesTask;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Controller for posting a repository to display its namespaces in a tree view.
 * 
 * @author dmh
 *
 */
public class RepositoryNamespacesTreeController extends DexIncludedControllerBase<Repository>
    implements TaskResultHandlerI {
    private static Log log = LogFactory.getLog( RepositoryNamespacesTreeController.class );

    @FXML
    protected TreeView<NamespacesDAO> repositoryNamespacesTree;

    protected TreeView<NamespacesDAO> tree;
    protected TreeItem<NamespacesDAO> root;
    private Map<String,TreeItem<NamespacesDAO>> namespaceMap = new TreeMap<>();

    private RepositorySearchController filterController = null;
    private Map<String,RepositoryItem> currentFilter = null;

    private List<NamespacesDAO> namespaceList;

    // All event types listened to by this controller's handlers
    private static final EventType[] publishedEvents = {DexRepositoryNamespaceSelectionEvent.REPOSITORY_NS_SELECTED};
    private static final EventType[] subscribedEvents =
        {DexRepositorySelectionEvent.REPOSITORY_SELECTED, DexRepositoryItemReplacedEvent.REPOSITORY_ITEM_REPLACED};

    public RepositoryNamespacesTreeController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void clear() {
        if (tree != null && tree.getRoot() != null)
            tree.getRoot().getChildren().clear();
    }

    @Override
    public void configure(DexMainController main) {
        super.configure( main );
        eventPublisherNode = tree;
    }

    @Override
    public void checkNodes() {
        if (!(repositoryNamespacesTree instanceof TreeView))
            throw new IllegalStateException( "Repository namespace tree not injected by FXML." );
    }

    private ReadOnlyObjectProperty<TreeItem<NamespacesDAO>> getSelectable() {
        return tree.getSelectionModel().selectedItemProperty();
    }

    @Override
    public void initialize() {
        log.debug( "Initializing repository namespace tree controller." );

        if (repositoryNamespacesTree == null)
            throw new IllegalArgumentException( "Repository namespaces tree view is null." );
        this.tree = repositoryNamespacesTree;

        root = new TreeItem<>();
        root.setExpanded( true ); // Startout fully expanded
        // Set up the TreeTable
        tree.setRoot( root );
        tree.setShowRoot( false );
        tree.setEditable( true );

        getSelectable().addListener( (v, old, newValue) -> namespaceSelectionListener( newValue ) );

    }

    private void namespaceSelectionListener(TreeItem<NamespacesDAO> item) {
        if (item == null)
            return;
        log.debug( "Namespace selected: " + item.getValue() );
        tree.fireEvent( new DexRepositoryNamespaceSelectionEvent( this, item.getValue() ) );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( "event handler" );
        if (event instanceof DexRepositorySelectionEvent)
            handleEvent( (DexRepositorySelectionEvent) event );
        if (event instanceof DexRepositoryItemReplacedEvent)
            handleEvent( (DexRepositoryItemReplacedEvent) event );
    }

    private void handleEvent(DexRepositorySelectionEvent event) {
        event.getRepository();
        // log.debug( "Repository selection changed." );
        clear();
        try {
            post( event.getRepository() );
        } catch (Exception e) {
            log.warn( "Error posting repository: " + e.getLocalizedMessage() );
            mainController.postError( e, "Error displaying repository." );
        }
    }

    private void handleEvent(DexRepositoryItemReplacedEvent event) {
        // log.debug( "Repository item replaced handler" );
        RepositoryItem oldItem = event.getOldItem();
        RepositoryItem newItem = event.getNewItem();
        if (oldItem == null || newItem == null || oldItem == newItem)
            return;

        repositoryNamespacesTree.getRoot().getChildren().clear(); // protected on refresh, force re-read of repo
        refresh();
    }

    /**
     * Get all the namespacesDAOs in the passed branch (or root if passed null item)
     * 
     * @param item
     */
    private void getNamespaces(TreeItem<NamespacesDAO> item) {
        if (item == null)
            item = root;
        if (!item.getChildren().isEmpty()) {
            for (TreeItem<NamespacesDAO> subItem : item.getChildren()) {
                getNamespaces( subItem );
            }
        } else {
            namespaceList.add( item.getValue() );
        }
    }

    @Override
    public void post(Repository repository) throws Exception {
        if (repository == null)
            return;
        if (postedData == repository) {
            // log.debug( "Skipping posting repository namespaces - Just apply filters." );
            updateTree();
            return;
        }
        super.post( repository ); // clear view and hold onto repo
        // log.debug( "Post namespaces in the repository: " + repository.getDisplayName() );
        root = repositoryNamespacesTree.getRoot();

        mainController.postStatus( "Loading root namespaces" );
        // currentFilter = parentController.getRepositorySearchFilter();

        // Get the root namespaces in real time
        try {
            for (String rootNS : repository.listRootNamespaces()) {
                TreeItem<NamespacesDAO> item = new TreeItem<>( new NamespacesDAO( rootNS, null, repository ) );
                item.setExpanded( false );
                root.getChildren().add( item );
                namespaceMap.put( rootNS, item );
                // Get sub-namespaces in background thread
                new ListSubnamespacesTask( item.getValue(), this::handleTaskComplete,
                    mainController.getStatusController() ).go();
            }
        } catch (RepositoryException e) {
            log.debug( "Error: " + e.getLocalizedMessage() );
            mainController.postError( e, "Error listing namespaces." );
        }
    }

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        if (event.getTarget() instanceof ListSubnamespacesTask) {
            log.debug( "Handling sub-namespace task results" );
            String fullPath;
            // String childNS;
            String parentNS;
            if (event == null || !(event.getTarget() instanceof ListSubnamespacesTask)) {
                log.error( "Invalid event returned." );
                return;
            }
            ListSubnamespacesTask task = (ListSubnamespacesTask) event.getTarget();
            if (task == null) {
                log.error( "Missing task." );
                return;
            }
            if (task.getErrorException() != null) {
                mainController.postError( task.getErrorException(), "Error listing namespaces." );
                return;
            }
            Map<String,NamespacesDAO> map = ((ListSubnamespacesTask) event.getTarget()).getMap();
            for (Entry<String,NamespacesDAO> nsEntry : map.entrySet()) {
                // un-marshal the entry
                fullPath = nsEntry.getValue().getFullPath();
                parentNS = nsEntry.getValue().getBasePath();
                if (parentNS == null || namespaceMap.get( parentNS ) == null) {
                    // log.debug("Skipping.");
                    continue;
                }

                TreeItem<NamespacesDAO> item = new TreeItem<>( nsEntry.getValue() );
                item.setExpanded( false );
                namespaceMap.put( fullPath, item );

                // null parent is a root already in the tree
                TreeItem<NamespacesDAO> parent = namespaceMap.get( parentNS );
                if ((parent != null) && (currentFilter == null || currentFilter.containsKey( fullPath )))
                    parent.getChildren().add( item );
                // log.debug("Added " + childNS + " to " + parentNS);
            }

        }
    }

    /**
     * Update the entire tree. Use global namespaceMap and apply filters if set.
     */
    private void updateTree() {
        // Build a new tree
        TreeItem<NamespacesDAO> filteredRoot = new TreeItem<>();

        // Set new tree structure
        for (Entry<String,TreeItem<NamespacesDAO>> entry : namespaceMap.entrySet()) {
            String parentNS = entry.getValue().getValue().getBasePath();
            TreeItem<NamespacesDAO> parent = filteredRoot;
            if (parentNS != null)
                parent = namespaceMap.get( parentNS );
            if (parent == null) {
                if (!filteredRoot.getChildren().contains( entry.getValue() ))
                    filteredRoot.getChildren().add( entry.getValue() );
            } else if (isSelected( entry.getValue() )) {
                if (!parent.getChildren().contains( entry.getValue() ))
                    parent.getChildren().add( entry.getValue() );
                addParent( parent, filteredRoot );
            }
        }
        tree.setRoot( filteredRoot );
    }

    private void addParent(TreeItem<NamespacesDAO> parent, TreeItem<NamespacesDAO> root) {
        // Get it's parent and add if needed
        if (parent.getValue() != null) {
            if (parent.getValue().getBasePath() == null) {
                if (!root.getChildren().contains( parent ))
                    root.getChildren().add( parent );
            } else {
                TreeItem<NamespacesDAO> ancestor = namespaceMap.get( parent.getValue().getBasePath() );
                if (ancestor != null && !ancestor.getChildren().contains( parent )) {
                    ancestor.getChildren().add( parent );
                    addParent( ancestor, root ); // Recurse
                }
            }
        }
    }

    /**
     * Filter the tree items based on full path.
     * 
     * @param item
     * @return true if there is a filter and the full path is selected or if there is no filter
     */
    private boolean isSelected(TreeItem<NamespacesDAO> item) {
        return filterController == null || filterController.isSelected( item.getValue().getFullPath() );
    }

    /**
     * Provide this controller a filter.
     * 
     * @param repositorySearchController
     */
    public void setFilter(RepositorySearchController repositorySearchController) {
        filterController = repositorySearchController;
    }
}
