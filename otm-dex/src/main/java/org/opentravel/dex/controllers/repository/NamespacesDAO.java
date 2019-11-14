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
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.repository.GetRepositoryItemsTask;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.image.ImageView;

/**
 * Data Access Object (DAO) for displaying repository namespaces in a tree view.
 * 
 * @author dmh
 *
 */
public class NamespacesDAO implements DexDAO<String>, TaskResultHandlerI {
    private static Log log = LogFactory.getLog( NamespacesDAO.class );

    // Namespaces - for root namespaces it will be like: http://www.opentravel.org/OTM
    // for sub-namespaces it will be just the sub-ns: e.g. hospitality
    protected String ns;

    // BasePath is the path of parent or null if root namespace
    // e.g. http://www.opentravel.org/OTM/product
    protected String basePath;

    private Repository repository;
    //
    private String decoration = "";
    private String permission = "Unknown";

    private List<RepositoryItem> allItems = null;
    private List<RepositoryItem> latestItems = null;

    // If the controller is set, refresh it when get repo items task is done.
    private DexIncludedController<NamespacesDAO> controller = null;

    public NamespacesDAO(String ns, String basePath, Repository repo) {
        this.ns = ns;
        this.basePath = basePath;
        this.setRepository( repo );

        // task to retrieve items to allow filter by item type (Draft, etc) or Locked
        new GetRepositoryItemsTask( this, this::handleTaskComplete, null ).go();
    }

    public void refresh(DexIncludedController<NamespacesDAO> controller) {
        this.controller = controller;
        new GetRepositoryItemsTask( this, this::handleTaskComplete, null ).go();
    }

    public boolean contains(RepositoryItem item) {
        if (allItems.contains( item ))
            return true;
        if (latestItems.contains( item ))
            return true;
        return false;
    }

    public StringProperty fullPathProperty() {
        return new ReadOnlyStringWrapper( basePath != null ? basePath + "/" + ns : ns );
    }

    /**
     * @return the namespace without base path
     */
    public String get() {
        return ns;
    }

    public List<RepositoryItem> getAllItems() {
        return allItems;
    }

    public List<RepositoryItem> getAllItems(TLLibraryStatus includeStatus, boolean lockedOnly) {
        List<RepositoryItem> selected = new ArrayList<>();
        for (RepositoryItem item : allItems) {
            if (item.getStatus().compareTo( includeStatus ) < 0)
                continue;
            if (item.getLibraryName() == null && lockedOnly)
                continue;
            selected.add( item );
        }
        return selected;
    }

    public String getBasePath() {
        return basePath;
    }

    /**
     * Get the unique name (key) for this namespace. If not a root namespace, the parent's path will be added.
     * 
     * @return
     */
    public String getFullPath() {
        return basePath != null ? basePath + "/" + ns : ns;
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return null;
    }

    public List<RepositoryItem> getLatestItems() {
        return latestItems;
    }

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }

    @Override
    public String getValue() {
        return ns;
    }
    //
    // public ImageView getIcon() {
    // return images.getView(element.getIconType());
    // }
    //

    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        if (event.getTarget() instanceof GetRepositoryItemsTask) {
            permission = ((GetRepositoryItemsTask) event.getTarget()).getPermission();
            allItems = ((GetRepositoryItemsTask) event.getTarget()).getAllItems();
            latestItems = ((GetRepositoryItemsTask) event.getTarget()).getLatestItems();
            if (allItems != null) {
                int locked = 0;
                for (RepositoryItem item : allItems)
                    if (item.getLockedByUser() != null)
                        locked++;
                decoration = "   ( " + allItems.size() + "/" + locked + " )";
            }
        }
        if (controller != null)
            try {
                controller.post( this );
            } catch (Exception e) {
                // No-op
            }
    }

    public StringProperty nsProperty() {
        return new SimpleStringProperty( ns );
    }

    public StringProperty permissionProperty() {
        return new ReadOnlyStringWrapper( permission );
        // return new ReadOnlyStringWrapper(permission);
    }

    public void replace(RepositoryItem oldItem, RepositoryItem newItem) {
        if (allItems.contains( oldItem )) {
            allItems.remove( oldItem );
            allItems.add( newItem );
        }
        if (latestItems.contains( oldItem )) {
            latestItems.remove( oldItem );
            latestItems.add( newItem );
        }
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Used by tree item for displayed value.
     */
    @Override
    public String toString() {
        return ns + decoration;
    }
}
