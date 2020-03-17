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

package org.opentravel.dex.actions;

import org.opentravel.model.OtmObject;
import org.opentravel.model.resource.OtmParameter;
import org.opentravel.model.resource.OtmParameterGroup;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ObservableValue;

public class SetParameterGroupFacetAction extends DexStringAction {
    // private static Log log = LogFactory.getLog( SetAbstractAction.class );

    /**
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmParameterGroup)
            return subject.isEditable();
        return false;
    }

    private List<OtmParameter> children;

    protected SetParameterGroupFacetAction() {
        // actionType = DexActions.SETPARAMETERGROUPFACET;
    }

    protected String get() {
        return getSubject().getReferenceFacetName();
    }

    @Override
    public OtmParameterGroup getSubject() {
        return (OtmParameterGroup) otm;
    }

    protected void set(String value) {
        OtmObject result = getSubject().setReferenceFacetString( value );
        if (result == null)
            postWarning( "Could not set reference facet to: " + value );
    }

    /**
     * {@inheritDoc} Also save parameter children.
     */
    @Override
    public String doIt(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        // Save then delete existing parameter children
        this.children = new ArrayList<>();
        getSubject().getChildren().forEach( c -> {
            if (c instanceof OtmParameter)
                children.add( (OtmParameter) c );
        } );
        children.forEach( c -> getSubject().delete( c ) );

        return super.doIt( observable, oldValue, newValue );
    }

    /**
     * {@inheritDoc} Also remove added parameters and restore parameter children.
     */
    @Override
    public String undoIt() {
        // Remove added parameters and then restore saved children
        List<OtmObject> kids = new ArrayList<>( getSubject().getChildren() );
        kids.forEach( c -> getSubject().delete( c ) );
        children.forEach( c -> getSubject().add( c.getTL() ) );

        return super.undoIt();
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (!(subject instanceof OtmParameterGroup))
            return false;
        otm = subject;
        return true;
    }

    @Override
    public String toString() {
        return "Parameter group reference facet set to " + get();
    }

}
