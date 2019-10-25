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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmFacets.OtmAbstractFacet;
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmFacets.OtmSharedFacet;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * OTM Object Node for business objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmChoiceObject extends OtmComplexObjects<TLChoiceObject> {
    private static Log log = LogFactory.getLog( OtmChoiceObject.class );

    public OtmChoiceObject(TLChoiceObject tlo, OtmModelManager mgr) {
        super( tlo, mgr );
    }

    public OtmChoiceObject(String name, OtmModelManager mgr) {
        super( new TLChoiceObject(), mgr );
        setName( name );
    }

    @Override
    public TLChoiceObject getTL() {
        return (TLChoiceObject) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.CHOICE;
    }

    /**
     * Add the contextual facet library member to this business object. A contributed facet is created and added to the
     * children.
     * 
     * @param cf contextual facet that is a library member
     * @return new contributed facet
     */
    public OtmContributedFacet add(OtmContextualFacet cf) {
        if (cf instanceof OtmChoiceFacet)
            getTL().addChoiceFacet( cf.getTL() );
        else
            return null;

        // Creating the contributed facet will link the contributed and contributor via the TL facet.
        OtmContributedFacet contrib = new OtmContributedFacet( cf.getTL(), this );
        super.add( contrib );
        return contrib;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Does</b> add contextual facets to the TL object
     * 
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmAbstractFacet<?> add(OtmObject child) {
        OtmAbstractFacet<?> result = null;
        result = super.add( child );
        if (child instanceof OtmContextualFacet)
            child = ((OtmContextualFacet) child).getWhereContributed();
        if (child instanceof OtmContributedFacet) {
            ((OtmContributedFacet) child).getTL().setOwningEntity( getTL() );
        }
        return result;
    }

    // @Override
    // public OtmObject setBaseType(OtmObject baseObj) {
    // if (baseObj instanceof OtmChoiceObject) {
    // TLExtension tlExt = getTL().getExtension();
    // if (tlExt == null)
    // tlExt = new TLExtension();
    // tlExt.setExtendsEntity( ((OtmChoiceObject) baseObj).getTL() );
    // getTL().setExtension( tlExt );
    // }
    // return getBaseType();
    // }


    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public void delete(OtmObject child) {
        if (child instanceof OtmContributedFacet)
            child = ((OtmContributedFacet) child).getContributor();
        if (child == null)
            return;
        if (child.getTL() instanceof TLContextualFacet)
            getTL().removeChoiceFacet( (TLContextualFacet) child.getTL() );
        remove( child );
    }

    @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new ArrayList<>();
        children.forEach( c -> {
            if (c instanceof OtmSharedFacet)
                ch.add( c );
            if (c instanceof OtmAlias)
                ch.add( c );
        } );
        return ch;
    }

    /**
     * @return
     */
    public OtmSharedFacet getShared() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmSharedFacet)
                return (OtmSharedFacet) c;
        return null;
    }

}
