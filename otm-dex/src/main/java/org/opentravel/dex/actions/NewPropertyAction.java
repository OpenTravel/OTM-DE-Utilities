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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmProperties.OtmProperty;
import org.opentravel.model.otmProperties.OtmPropertyType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * This action creates a new property in the subject property owner.
 */
public class NewPropertyAction extends DexRunAction {
    private static Log log = LogFactory.getLog( NewPropertyAction.class );

    /**
     * Any OTM object that uses the intended model manager.
     * 
     * @param subject
     * @return
     */
    public static boolean isEnabled(OtmObject subject) {
        return subject instanceof OtmPropertyOwner && subject.isEditable();
    }

    private OtmProperty newProperty = null;

    public NewPropertyAction() {
        // Constructor for reflection
    }

    /**
     * {@inheritDoc} Create a new property of passed type to the subject property owner.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#doIt(java.lang.Object)
     */
    @Override
    public Object doIt(Object data) {
        if (otm != null && otm.getModelManager() != null && data instanceof OtmPropertyType)
            // Build and hold onto for undo
            newProperty = OtmPropertyType.build( (OtmPropertyType) data, getSubject() );

        log.debug( "Added new member " + get() );
        return get();
    }

    /**
     * Return the new member or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmProperty get() {
        return newProperty;
    }

    @Override
    public OtmPropertyOwner getSubject() {
        return (OtmPropertyOwner) otm;
    }

    @Override
    public ValidationFindings getVetoFindings() {
        return null;
    }

    @Override
    public boolean isValid() {
        return newProperty != null ? newProperty.isValid() : false;
    }

    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmPropertyOwner)
            otm = subject;
        return otm != null;
    }

    @Override
    public String toString() {
        String name = "";
        if (newProperty != null)
            if (newProperty.getName() != null)
                name = newProperty.getName();
            else
                name = OtmPropertyType.getType( newProperty ).label();
        return "Created new property: " + name;
    }

    @Override
    public OtmProperty undoIt() {
        if (newProperty != null) {
            newProperty.getParent().delete( newProperty );
            newProperty = null;
        }
        log.debug( "Undo new property." );
        return newProperty;
    }
}
