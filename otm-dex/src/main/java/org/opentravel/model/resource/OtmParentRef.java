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
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

/**
 * OTM Object for Resource Action objects.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmParentRef extends OtmResourceChildBase<TLResourceParentRef> implements OtmResourceChild {
    private static Log log = LogFactory.getLog( OtmParentRef.class );

    public OtmParentRef(TLResourceParentRef tla, OtmResource parent) {
        super( tla, parent );
    }

    @Override
    public String getName() {
        return "Parent: " + getParentResource().getNameWithPrefix();
    }

    @Override
    public TLResourceParentRef getTL() {
        return (TLResourceParentRef) tlObject;
    }

    @Override
    public Icons getIconType() {
        return ImageManager.Icons.RESOURCE_PARENTREF;
    }

    private ObservableList<String> getParentCandidates() {
        ObservableList<String> candidates = FXCollections.observableArrayList();
        getOwningMember().getModelManager().getResources( true )
            .forEach( r -> candidates.add( r.getNameWithPrefix() ) );
        candidates.remove( getOwningMember().getNameWithPrefix() );
        return candidates;
    }

    public OtmResource getParentResource() {
        return (OtmResource) OtmModelElement.get( getTL().getParentResource() );
    }

    private Node getParentNode() {
        String selection = "";
        if (getParentResource() != null)
            selection = getParentResource().getNameWithPrefix();
        ComboBox<String> box = DexEditField.makeComboBox( getParentCandidates(), selection, this );
        return box;
    }

    private Node getParameterGroupNode() {
        ObservableList<String> groups = FXCollections.observableArrayList();
        if (getParentResource() != null)
            getParentResource().getParameterGroups().forEach( pg -> groups.add( pg.getName() ) );
        ComboBox<String> box = DexEditField.makeComboBox( groups, getTL().getParentParamGroupName(), this );
        return box;
    }

    private Node getPathNode() {
        TextField field = DexEditField.makeTextField( getTL().getPathTemplate(), this );
        return field;
    }

    @Override
    public List<DexEditField> getFields() {
        List<DexEditField> fields = new ArrayList<>();
        fields.add( new DexEditField( 0, 0, PARENT_LABEL, PARENT_TOOLTIP, getParentNode() ) );
        fields.add( new DexEditField( 1, 0, PARAM_GROUP_LABEL, PARAM_GROUP_TOOLTIP, getParameterGroupNode() ) );
        fields.add( new DexEditField( 2, 0, PATH_LABEL, PATH_LABEL, getPathNode() ) );
        return fields;
    }

    public Tooltip getTooltip() {
        return new Tooltip( TOOLTIP );
    }

    private static final String TOOLTIP = "Specifies a parent reference for a REST resource.";

    private static final String PARAM_GROUP_LABEL = "Parameter Group";
    private static final String PARAM_GROUP_TOOLTIP =
        "The name of the parameter group on the the parent resource with which this parent reference is associated.  The referenced group must be an ID parameter group.";
    private static final String PATH_LABEL = "Path Template";
    private static final String PATH_TOOLTIP =
        "Specifies the path template for the parent resource. This path will be pre-pended to all action path templates when the child resource is treated as a sub-resource (non-first class).";
    private static final String PARENT_LABEL = "Parent";
    private static final String PARENT_TOOLTIP = "Specifies a parent reference for a REST resource.";


}
