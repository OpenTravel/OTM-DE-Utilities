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
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

/**
 * OTM Object for Resource objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmActionFacet extends OtmResourceChildBase<TLActionFacet> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmActionFacet.class );

    public OtmActionFacet(TLActionFacet tla, OtmResource parent) {
        super( tla, parent );

        // tla.getReferenceFacetName(); -
        // tla.getBasePayloadName();
        // tla.getReferenceRepeat();
        // tla.getReferenceType();
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.FACET;
    }

    public OtmActionFacet(String name, OtmResource parent) {
        super( new TLActionFacet(), parent );
        setName( name );
    }

    @Override
    public String setName(String name) {
        getTL().setName( name );
        isValid( true );
        return getName();
    }

    @Override
    public TLActionFacet getTL() {
        return (TLActionFacet) tlObject;
    }


    // @Override
    public Collection<OtmObject> getChildrenHierarchy() {
        Collection<OtmObject> ch = new ArrayList<>();
        ch.add( OtmModelElement.get( (TLModelElement) getTL().getBasePayload() ) );
        return ch;
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( "Base Payload", new Button(), 1 ) );
        fields.add( new DexEditField( "Reference Type", new ComboBox<String>(), 1 ) );
        fields.add( new DexEditField( "Reference Facet", new ComboBox<String>(), 1 ) );
        fields.add( new DexEditField( "Repeat Count", new Spinner<Integer>( 0, 10000, 0 ), 1 ) );
        return fields;
    }

}
