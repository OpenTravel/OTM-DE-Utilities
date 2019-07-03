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

package org.opentravel.model.otmContainers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryPermission;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * OTM Object for libraries. Does <b>NOT</b> provide access to members.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmLibrary {
    private static Log log = LogFactory.getLog( OtmLibrary.class );

    protected OtmModelManager mgr;
    protected List<ProjectItem> projectItems = new ArrayList<>();
    protected AbstractLibrary tlLib;

    protected ValidationFindings findings;

    public OtmLibrary(ProjectItem pi, OtmModelManager mgr) {
        this.mgr = mgr;
        projectItems.add( pi );
        tlLib = pi.getContent();
    }

    public OtmLibrary(AbstractLibrary tl, OtmModelManager mgr) {
        this.mgr = mgr;
        tlLib = tl;
    }

    protected OtmLibrary(OtmModelManager mgr) {
        this.mgr = mgr;
    }

    /**
     * Add the project item to the list maintained by the library. Libraries can be members of multiple, open projects.
     * 
     * @param pi
     */
    public void add(ProjectItem pi) {
        if (pi.getContent() == null
            || (!(pi.getNamespace().equals( getTL().getNamespace() ) && pi.getContent().getName().equals( getName() ))))

            // if (pi.getContent() != tlLib)
            throw new IllegalArgumentException( "Can not add project item with wrong library." );
        projectItems.add( pi );
        log.debug( "Added project item to " + this.getName() + ". Now has " + projectItems.size() + " items." );
    }

    public boolean contains(AbstractLibrary aLib) {
        if (tlLib == aLib)
            return true;
        for (ProjectItem pi : projectItems)
            if (pi.getContent() == aLib)
                return true;
        return false;
    }

    public AbstractLibrary getTL() {
        return tlLib;
    }

    public String getFullName() {
        return getTL() != null ? getTL().getNamespace() + "/" + getTL().getName() : null;
    }

    public OtmModelManager getModelManager() {
        return mgr;
    }

    public OtmProject getManagingProject() {
        return mgr.getManagingProject( this );
    }

    public String getName() {
        return getTL() != null ? getTL().getName() : "";
    }

    public String getPrefix() {
        return getTL().getPrefix();
    }

    public Icons getIconType() {
        return ImageManager.Icons.LIBRARY;
    }

    /**
     * A library is editable if any associated project item state is Managed_WIP -OR- unmanaged.
     * 
     * @return
     */
    public boolean isEditable() {
        return getState() == RepositoryItemState.MANAGED_WIP || getState() == RepositoryItemState.UNMANAGED;
    }

    /**
     * @return actual status of TL Libraries otherwise DRAFT
     */
    public TLLibraryStatus getStatus() {
        if (tlLib instanceof TLLibrary)
            return ((TLLibrary) tlLib).getStatus();
        else
            return TLLibraryStatus.FINAL;
    }

    public List<OtmLibrary> getIncludes() {
        List<OtmLibrary> libs = new ArrayList<>();
        for (TLInclude include : tlLib.getIncludes()) {
            if (include.getOwningLibrary() != null)
                libs.add( mgr.get( include.getOwningLibrary() ) );
        }
        return libs;
    }

    public String getStateName() {
        return projectItems.isEmpty() ? "" : getState().toString();
    }

    /**
     * Examine all project items and return the state that grants the user the most rights.
     * 
     * @return
     */
    public RepositoryItemState getState() {
        RepositoryItemState state = RepositoryItemState.MANAGED_UNLOCKED; // the weakest state
        if (projectItems != null)
            for (ProjectItem pi : projectItems) {
                // log.debug("state = " + pi.getState());
                switch (pi.getState()) {
                    case MANAGED_UNLOCKED:
                        break;
                    case BUILT_IN:
                    case UNMANAGED:
                        // These are true regardless of user or user actions
                        return pi.getState();

                    case MANAGED_LOCKED:
                        if (state != RepositoryItemState.MANAGED_WIP)
                            state = pi.getState();
                        break;

                    case MANAGED_WIP:
                        // This gives user most rights and is therefore always used as state
                        return pi.getState();
                }
            }
        return state;
    }

    public String getNameWithBasenamespace() {
        return getBaseNamespace() + "/" + getName();
    }

    public String getLockedBy() {
        for (ProjectItem pi : projectItems)
            if (pi.getLockedByUser() != null)
                return pi.getLockedByUser();
        return "";
    }

    public String getBaseNamespace() {
        return projectItems.isEmpty() ? "" : projectItems.get( 0 ).getBaseNamespace();
    }

    public boolean isLatestVersion() {
        return mgr.isLatest( this );
    }

    /**
     * Get the name(s) of the project(s) that contain this library.
     * 
     * @return new array of string containing the project names
     */
    public List<String> getProjectNames() {
        List<String> names = new ArrayList<>();
        if (projectItems != null)
            for (ProjectItem pi : projectItems) {
                for (Project p : pi.memberOfProjects())
                    if (!names.contains( p.getName() ))
                        names.add( p.getName() );
            }
        names.sort( null );
        return names;
    }

    public void validate() {
        findings = TLModelCompileValidator.validateModelElement( getTL(), true );
    }

    /**
     * @return
     */
    public String getVersion() {
        return getTL().getVersion();
    }

    /**
     * @return
     */
    public boolean canBeLocked() {
        if (getStatus() == TLLibraryStatus.DRAFT && getState() == RepositoryItemState.MANAGED_UNLOCKED
            && getManagingProject() != null && getManagingProject().getPermission() != null)
            return getManagingProject().getPermission().equals( RepositoryPermission.WRITE );
        return false;
    }

    public boolean canBeUnlocked() {
        // TODO - check to see if this is the user that locked it
        return getState() == RepositoryItemState.MANAGED_LOCKED || getState() == RepositoryItemState.MANAGED_WIP;
    }
}
