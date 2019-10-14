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
import org.opentravel.model.otmFacets.OtmAlias;
import org.opentravel.model.otmFacets.OtmSharedFacet;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLExtension;

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

    @Override
    public OtmObject setBaseType(OtmObject baseObj) {
        if (baseObj instanceof OtmChoiceObject) {
            TLExtension tlExt = getTL().getExtension();
            if (tlExt == null)
                tlExt = new TLExtension();
            tlExt.setExtendsEntity( ((OtmChoiceObject) baseObj).getTL() );
            getTL().setExtension( tlExt );
        }
        return getBaseType();
    }


    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
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
