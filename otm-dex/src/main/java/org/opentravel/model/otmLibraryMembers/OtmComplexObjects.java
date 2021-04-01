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
import org.opentravel.model.OtmChildrenOwner;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmDetailFacet;
import org.opentravel.model.otmFacets.OtmSummaryFacet;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;

/**
 * Abstract OTM Object for Complex Library Members.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmComplexObjects<T extends TLComplexTypeBase> extends OtmLibraryMemberBase<TLLibraryMember>
    implements OtmObject, OtmTypeProvider, OtmChildrenOwner {
    private static Log log = LogFactory.getLog( OtmComplexObjects.class );

    /**
     * Construct complex library member. Set its model manager, TL object and add a listener.
     */
    public OtmComplexObjects(T tl, OtmModelManager mgr) {
        super( tl, mgr );
    }

    @Override
    public T getTL() {
        return (T) tlObject;
    }


    /**
     * @see org.opentravel.model.OtmChildrenOwner#add(org.opentravel.model.OtmObject)
     */
    @Override
    public OtmObject add(OtmObject child) {
        if (!getChildren().contains( child )) {
            children.add( child );
            return child;
        }
        log.debug(
            "Could not add: " + getName() + " already contained " + child.getObjectTypeName() + " " + child.getName() );
        return null;
    }

    @Override
    public String getName() {
        return getTL().getLocalName();
    }

    /**
     * {@inheritDoc} Wraps name from TL extension's extends entity name.
     * 
     * @see org.opentravel.model.otmLibraryMembers.OtmLibraryMemberBase#baseTypeProperty()
     */
    @Override
    public StringProperty baseTypeProperty() {
        if (getTL().getExtension() != null && getTL().getExtension().getExtendsEntity() instanceof TLModelElement) {
            // log.debug("Extension found on " + this);
            OtmObject ex = OtmModelElement.get( (TLModelElement) getTL().getExtension().getExtendsEntity() );
            if (ex != null)
                return new ReadOnlyStringWrapper( ex.getNameWithPrefix() );
            // return new ReadOnlyStringWrapper( getTL().getExtension().getExtendsEntityName() );
        }
        return super.baseTypeProperty();
    }

    /**
     * @return this
     */
    @Override
    public OtmComplexObjects<T> getOwningMember() {
        return this;
    }

    @Override
    public boolean isNameControlled() {
        return true;
    }

    @Override
    public String setName(String name) {
        // sub-type must first: getTL().setName( name );

        // Clear the name property for all facets
        for (OtmObject child : getChildren())
            child.clearNameProperty();
        // Clear the name property for all users of this object
        for (OtmLibraryMember lm : getWhereUsed())
            for (OtmTypeUser user : lm.getDescendantsTypeUsers())
                user.clearNameProperty();
        isValid( true );
        return getName();
    }

    /**
     * @return
     */
    public OtmSummaryFacet getSummary() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmSummaryFacet)
                return (OtmSummaryFacet) c;
        return null;
    }

    /**
     * @return
     */
    public OtmDetailFacet getDetail() {
        for (OtmObject c : getChildren())
            if (c instanceof OtmDetailFacet)
                return (OtmDetailFacet) c;
        return null;
    }

}
