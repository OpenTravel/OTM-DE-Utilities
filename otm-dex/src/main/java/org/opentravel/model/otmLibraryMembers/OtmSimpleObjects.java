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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeProvider;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;

import java.util.Collections;
import java.util.List;

/**
 * Abstract OTM Object Node for "Simple" Library Members. Some simple objects contain children. Simple objects can be
 * assigned to attributes. When serialized, simple objects produce a single field (single parse token).
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmSimpleObjects<T extends TLLibraryMember> extends OtmLibraryMemberBase<TLLibraryMember>
    implements OtmObject, OtmTypeProvider {
    private static Log log = LogFactory.getLog( OtmSimpleObjects.class );

    public OtmSimpleObjects(T tl, OtmModelManager mgr) {
        super( tl, mgr );

        // FIXME - handle these
        XSDComplexType xc;
        XSDSimpleType xs;
        XSDElement xe;

    }

    @Override
    public OtmObject add(OtmObject child) {
        return null; // has no children to add
    }

    @Override
    public String getName() {
        return getTL().getLocalName();
    }

    /**
     * @return this
     */
    @Override
    public OtmSimpleObjects<?> getOwningMember() {
        return this;
    }

    @Override
    public TLLibraryMember getTL() {
        return (TLLibraryMember) tlObject;
    }

    // @Override
    // public boolean isExpanded() {
    // return true;
    // }

    @Override
    public boolean isNameControlled() {
        return false;
    };

    @Override
    public List<OtmObject> getInheritedChildren() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public void modelInheritedChildren() {}

}
