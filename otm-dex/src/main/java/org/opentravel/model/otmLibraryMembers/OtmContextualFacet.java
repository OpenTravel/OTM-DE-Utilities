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

package org.opentravel.model.otmLibraryMembers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.Collections;
import java.util.List;

/**
 * Abstract OTM Node for Custom Facets library members.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmContextualFacet extends OtmLibraryMemberBase<TLContextualFacet>
    implements OtmObject, OtmTypeProvider, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmContextualFacet.class );

    // The contributed facet that is child of a library member.
    private OtmContributedFacet whereContributed = null;

    public OtmContextualFacet(TLContextualFacet tl, OtmModelManager manager) {
        super( tl, manager );
        // lazy evaluated modelChildren();
    }

    /**
     * NOTE: detection of "ghost" inherited facets depends on Contributor will not have ghost set as where contributed.
     * 
     * @return the non-ghost contributed facet where this facet is used
     */
    public OtmContributedFacet getWhereContributed() {
        if (whereContributed == null) {
            OtmObject o = OtmModelElement.get( (TLModelElement) getTL().getOwningEntity() );
            if (o instanceof OtmContributedFacet)
                o = ((OtmContributedFacet) o).getContributor();
            if (o instanceof OtmChildrenOwner)
                for (OtmObject c : ((OtmChildrenOwner) o).getChildren())
                    if (c instanceof OtmContributedFacet && c.getName().equals( this.getName() )) {
                        whereContributed = (OtmContributedFacet) c;
                        ((OtmContributedFacet) c).setContributor( this );
                    }
        }
        return whereContributed;
    }

    @Override
    public List<OtmObject> getChildren() {
        // children.clear();
        if (children != null && children.isEmpty())
            modelChildren();
        // if (getWhereContributed() != null)
        // children.addAll(getWhereContributed().getChildren());
        // FIXME - what about children that are other contextual facets?
        //
        // Properties that are children will be on the contributed facet since that is a facet
        return children;
    }

    @Override
    public void modelChildren() {
        super.modelChildren();
        if (getWhereContributed() != null)
            children.addAll( getWhereContributed().getChildren() );
    }

    @Override
    public TLContextualFacet getTL() {
        return (TLContextualFacet) tlObject;
    }

    @Override
    public String getName() {
        return getTL().getLocalName();
        // return this.getClass().getSimpleName();
    }

    @Override
    public boolean isExpanded() {
        return true;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET_CONTEXTUAL;
    }

    @Override
    public OtmContextualFacet getOwningMember() {
        return this;
    }

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public void modelInheritedChildren() {
        // TODO Auto-generated method stub
    }

}
