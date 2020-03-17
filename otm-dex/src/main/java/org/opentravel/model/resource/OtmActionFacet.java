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

package org.opentravel.model.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmFacets.OtmFacet;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLReferenceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionFacet extends OtmResourceChildBase<TLActionFacet> implements OtmResourceChild, OtmTypeUser {
    private static Log log = LogFactory.getLog( OtmActionFacet.class );

    private static final String TOOLTIP =
        "            Action facets describe the message payload for RESTful action requests and responses.  In addition to their own payload, they provide basic information about how the resource's business object should be referenced in the message.";

    private static final int DEFAULT_REPEAT_COUNT = 1000;
    private final OtmActionFacetFieldManager fieldsMgr = new OtmActionFacetFieldManager( this );

    public enum BuildTemplate {
        REQUEST, RESPONSE, LIST
    }
    // For actions - GET, POST
    // Queries - launch a query facet selection dialog
    // Name is the name of the facet w/o object base name

    public OtmActionFacet(String name, OtmResource parent) {
        super( new TLActionFacet(), parent );
        if (parent != null)
            parent.getTL().addActionFacet( getTL() );
        setName( name );
    }

    /**
     * Add this action facet to the OtmResource. Do not change the TL object.
     * 
     * @param tla
     * @param parent
     */
    public OtmActionFacet(TLActionFacet tla, OtmResource parent) {
        super( tla, parent );

        if (parent != null && tla.getOwningResource() == null)
            parent.getTL().addActionFacet( getTL() );
    }


    public void build(BuildTemplate template) {
        if (getOwningMember() != null) {
            String nameBase = "";
            if (getOwningMember().getSubject() != null)
                nameBase = getOwningMember().getSubject().getName();
            switch (template) {
                case REQUEST:
                    setName( nameBase + "Request" );
                    setReferenceType( TLReferenceType.REQUIRED );
                    setBasePayload( getOwningMember().getDefaultRequestPayload() );
                    break;
                case RESPONSE:
                    setName( nameBase + "Response" );
                    setReferenceType( TLReferenceType.OPTIONAL );
                    setBasePayload( getOwningMember().getDefaultResponsePayload() );
                    break;
                case LIST:
                    setName( nameBase + "List" );
                    setReferenceType( TLReferenceType.OPTIONAL );
                    setRepeatCount( DEFAULT_REPEAT_COUNT );
                    setBasePayload( getOwningMember().getDefaultResponsePayload() );
                    break;
            }
            isValid( true );
        }
    }

    /**
     * 
     * @return base payload if any or else the reference facet
     */
    public OtmObject getWrappedObject() {
        return getBasePayload() != null ? getBasePayload() : getReferenceFacet();
    }

    /**
     * Could be Choice or Core
     * 
     * @return core, choice or null
     */
    public OtmLibraryMember getBasePayload() {
        OtmObject obj = OtmModelElement.get( (TLModelElement) getTL().getBasePayload() );
        return obj instanceof OtmLibraryMember ? (OtmLibraryMember) obj : null;
    }


    // @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new ArrayList<>();
        ch.add( OtmModelElement.get( (TLModelElement) getTL().getBasePayload() ) );
        return ch;
    }

    @Override
    public List<DexEditField> getFields() {
        return fieldsMgr.getFields();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    /**
     * Get the facet from the reference facet name
     * 
     * @return OtmFacet or null
     */
    public OtmFacet<? extends TLFacet> getReferenceFacet() {
        TLBusinessObject referencedBO = null;
        TLFacet tlFacet = null;
        if (getOwningMember() != null && getOwningMember().getSubject() != null)
            referencedBO = getOwningMember().getSubject().getTL();
        if (referencedBO != null)
            tlFacet = ResourceCodegenUtils.getReferencedFacet( referencedBO, getReferenceFacetName() );
        OtmObject obj = OtmModelElement.get( tlFacet );
        if (obj instanceof OtmContextualFacet)
            obj = ((OtmContextualFacet) obj).getWhereContributed();

        return obj instanceof OtmFacet ? (OtmFacet<?>) obj : null;
    }

    public int getRepeatCount() {
        return getTL().getReferenceRepeat();
    }

    public void setRepeatCount(int value) {
        getTL().setReferenceRepeat( value );
    }

    public String getReferenceFacetName() {
        return getTL().getReferenceFacetName() != null ? getTL().getReferenceFacetName() : OtmResource.SUBGROUP;
    }

    public TLReferenceType getReferenceType() {
        return getTL().getReferenceType() != null ? getTL().getReferenceType() : TLReferenceType.NONE;
    }

    public ObservableList<String> getReferenceTypeCandidates() {
        return fieldsMgr.getReferenceTypeCandidates();
    }

    public String getReferenceTypeString() {
        return getReferenceType() != null ? getReferenceType().toString() : "";
    }

    /**
     * 
     * @return
     */
    public OtmObject getRequestPayload() {
        // For a request, the "payloadType" indicates which action facet.
        // This action facet defines the root element.
        OtmObject rqPayload = null;
        String rfName = getReferenceFacetName();
        OtmBusinessObject subject = getOwningMember().getSubject();
        OtmLibraryMember base = getBasePayload(); // creates wrapper object

        // Early Exit Conditions
        // Don't care about reference type - that is only for responses
        // if (getReferenceType() == TLReferenceType.NONE)
        // return null; // User set to have No request payload
        if (subject == null)
            return rqPayload; // Error, resource is not complete
        if (rfName.equals( OtmResource.SUBGROUP ) || rfName.isEmpty())
            return subject; // The whole object is the substitution group

        rqPayload = getReferenceFacet();
        return rqPayload;
    }

    /**
     * Apply the rules to determine what type of object will be in the response
     * 
     * @return
     */
    public OtmObject getResponsePayload(OtmActionResponse response) {
        TLActionResponse source = response.getTL();
        NamedEntity plt = ResourceCodegenUtils.getPayloadType( source.getPayloadType() );
        return plt != null ? OtmModelElement.get( (TLModelElement) plt ) : this;
    }

    @Override
    public TLActionFacet getTL() {
        return (TLActionFacet) tlObject;
    }

    @Override
    public String getName() {
        return getTL().getName(); // Override the localName used by supertype
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    public void setBasePayload(OtmObject basePayload) {
        // Must be core or choice object
        if (basePayload != null && basePayload.getTL() instanceof NamedEntity)
            getTL().setBasePayload( (NamedEntity) basePayload.getTL() );
        else
            getTL().setBasePayload( null );
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        // isValid( true );
        return getName();
    }

    public String setReferenceFacetName(String name) {
        // getTL().setReferenceFacetName( name );
        log.debug( "Set reference facet to " + name );
        if (name.equals( OtmResource.SUBGROUP ))
            getTL().setReferenceFacetName( null );
        else
            for (OtmObject f : getOwningMember().getSubjectFacets()) {
                if (name.equals( f.getName() )) {
                    if (f instanceof OtmFacet)
                        setReferenceFacet( (OtmFacet<?>) f );
                    else if (f instanceof OtmContextualFacet)
                        setReferenceFacet( (OtmContextualFacet) f );
                }
            }
        return getReferenceFacetName();
    }

    /**
     * Set the tlReferenceFacetName to the identity name of the facet. If set to null, then the object is used instead
     * of facet (substitution group)
     * 
     * @param facet
     */
    public void setReferenceFacet(OtmObject facet) {
        TLFacet tlf = null;
        if (facet instanceof OtmFacet)
            tlf = ((OtmFacet<?>) facet).getTL();
        else if (facet instanceof OtmContextualFacet)
            tlf = ((OtmContextualFacet) facet).getTL();

        String name = "";
        if (tlf == null)
            name = null;
        else
            name = ResourceCodegenUtils.getActionFacetReferenceName( tlf );
        getTL().setReferenceFacetName( name );
        log.debug( "Setting reference facet name to: " + name );
    }

    // // FIXED - there has to be a better way to code this to have same code run in both cases
    // public void setReferenceFacet(OtmContextualFacet facet) {
    // String name = "";
    // if (facet == null)
    // name = null;
    // else
    // name = ResourceCodegenUtils.getActionFacetReferenceName( facet.getTL() );
    // getTL().setReferenceFacetName( name );
    // log.debug( "Setting reference facet name to: " + name );
    // }

    public TLReferenceType setReferenceType(TLReferenceType type) {
        if (type == null)
            type = TLReferenceType.NONE;
        getTL().setReferenceType( type );
        // log.debug( "Set reference type to " + getReferenceType() );
        return getReferenceType();
    }

    /**
     * @param value
     */
    public TLReferenceType setReferenceTypeString(String value) {
        TLReferenceType type = null;
        for (TLReferenceType t : TLReferenceType.values())
            if (t.toString().equals( value ))
                type = t;
        return setReferenceType( type );
    }



    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#assignedTypeProperty()
     */
    @Override
    public StringProperty assignedTypeProperty() {
        return null;
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#getAssignedTLType()
     */
    @Override
    public NamedEntity getAssignedTLType() {
        return null;
    }


    /**
     * @see org.opentravel.model.OtmTypeUser#getAssignedType()
     */
    @Override
    public OtmTypeProvider getAssignedType() {
        return (OtmTypeProvider) getBasePayload();
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#getTlAssignedTypeName()
     */
    @Override
    public String getTlAssignedTypeName() {
        return null;
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#setAssignedTLType(org.opentravel.schemacompiler.model.NamedEntity)
     */
    @Override
    public NamedEntity setAssignedTLType(NamedEntity type) {
        return null;
    }


    /**
     * @see org.opentravel.model.OtmTypeUser#setAssignedType(org.opentravel.model.OtmTypeProvider)
     */
    @Override
    public OtmTypeProvider setAssignedType(OtmTypeProvider type) {
        setBasePayload( type );
        return getAssignedType();
    }


    /**
     * Not-implemented
     * 
     * @see org.opentravel.model.OtmTypeUser#setTLTypeName(java.lang.String)
     */
    @Override
    public void setTLTypeName(String name) {}

}
