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
import org.opentravel.model.otmProperties.OtmPropertyFactory;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * Make a copy of a member's property.
 */
public class CopyPropertyAction extends DexRunAction {
    private static Log log = LogFactory.getLog( CopyPropertyAction.class );


    /**
     * Any OTM object that uses the intended model manager and an editable library.
     * 
     * @param subject is the property that will be copied. The copy will be added to the subject's parent.
     * @return true if the subject is an editable property
     */
    public static boolean isEnabled(OtmObject subject) {
        if (subject instanceof OtmProperty)
            return subject.isEditable();
        return false;
    }

    /**
     * Any OTM object that uses the intended model manager and an editable library.
     * 
     * @param subject is the property that will be copied. The copy will be added to the subject's parent.
     * @return true if the subject is an editable property
     */
    public static boolean isEnabled(OtmObject subject, OtmObject newParent) {
        if (newParent == null)
            return false;
        if (subject instanceof OtmProperty)
            return newParent instanceof OtmPropertyOwner && newParent.isEditable();
        return false;
    }

    private OtmProperty newProperty = null;

    public CopyPropertyAction() {
        // Constructor for reflection
    }

    /**
     */
    public OtmProperty doIt() {
        LibraryElement newTL = getSubject().getTL().cloneElement();
        if (newTL instanceof TLModelElement)
            newProperty = OtmPropertyFactory.create( (TLModelElement) newTL, getSubject().getParent() );
        return newProperty;
    }

    /**
     * {@inheritDoc} Copy the property and add new property to passed parent.
     * 
     * @return
     */
    @Override
    public Object doIt(Object data) {
        newProperty = null;
        if (getSubject() instanceof OtmProperty && data instanceof OtmPropertyOwner) {
            LibraryElement newTL = getSubject().getTL().cloneElement();
            if (newTL instanceof TLModelElement)
                newProperty = OtmPropertyFactory.create( (TLModelElement) newTL, ((OtmPropertyOwner) data) );
        }
        return newProperty;

        // return doIt();
    }


    /**
     * Return the new property or null if none created.
     * 
     * @see org.opentravel.dex.actions.DexRunAction#get()
     */
    @Override
    public OtmProperty get() {
        return newProperty;
    }

    @Override
    public boolean isValid() {
        return get() != null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.opentravel.dex.actions.DexAction#setSubject(org.opentravel.model.OtmObject)
     */
    @Override
    public boolean setSubject(OtmObject subject) {
        if (subject instanceof OtmProperty)
            otm = subject;
        return otm instanceof OtmProperty;
    }

    @Override
    public OtmProperty getSubject() {
        return (OtmProperty) otm;
    }

    @Override
    public String toString() {
        return "Copied: " + get();
    }

    @Override
    public OtmProperty undoIt() {
        if (get() != null && get().getParent() != null) {
            get().getParent().delete( get() );
            newProperty = null;
        }
        // log.debug( "Undo copy." );
        return get();
    }
}
