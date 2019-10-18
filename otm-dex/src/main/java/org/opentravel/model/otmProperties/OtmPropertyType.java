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

package org.opentravel.model.otmProperties;

import org.opentravel.model.OtmPropertyOwner;
import org.opentravel.model.otmFacets.OtmAbstractDisplayFacet;
import org.opentravel.model.otmLibraryMembers.OtmEnumeration;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public enum OtmPropertyType {
    ATTRIBUTE("Attribute", OtmAttribute.class),
    INDICATOR("Indicator", OtmIndicator.class),
    ID("XML ID", OtmIdAttribute.class),
    IDREFATTRIBUTE("ID Reference Attribute", OtmIdReferenceAttribute.class),

    ELEMENT("Element", OtmElement.class),
    INDICATORELEMENT("Indicator Element", OtmIndicatorElement.class),
    IDREFELEMENT("ID Reference Element", OtmIdReferenceElement.class),

    ENUMVALUE("Enumeration Value", OtmEnumerationValue.class);

    private final String label;
    private Class<? extends OtmProperty> propertyClass;

    public String label() {
        return label;
    }

    public Class<? extends OtmProperty> propertyClass() {
        return propertyClass;
    }

    private OtmPropertyType(String label, Class<? extends OtmProperty> objectClass) {
        this.label = label;
        this.propertyClass = objectClass;
    }

    /**
     * Get the type associated with the property
     * 
     * @return
     */
    public static OtmPropertyType getType(OtmProperty p) {
        return p != null ? getType( p.getClass() ) : null;
    }

    public static OtmPropertyType getType(Class<?> propertyClass) {
        for (OtmPropertyType type : values())
            if (type.propertyClass == propertyClass)
                return type;
        return null;
    }

    public static OtmPropertyType getType(String label) {
        for (OtmPropertyType type : values())
            if (type.label == label)
                return type;
        return null;
    }

    /**
     * Get a list of JavaFX menu items for the properties with the property type in the user data of the item. Includes
     * separators without a type in the user data.
     * 
     * @return
     */
    public static List<MenuItem> menuItems() {
        List<MenuItem> items = new ArrayList<>();
        MenuItem item;
        for (OtmPropertyType type : values()) {
            item = new MenuItem( type.label() );
            item.setUserData( type );
            items.add( item );
            if (type == IDREFATTRIBUTE || type == IDREFELEMENT)
                items.add( new SeparatorMenuItem() );
        }
        return items;
    }

    public static void enableMenuItems(Menu menu, OtmPropertyOwner owner) {
        for (MenuItem item : menu.getItems()) {
            if (item.getUserData() instanceof OtmPropertyType) {
                OtmPropertyType type = (OtmPropertyType) item.getUserData();
                // Is the item enabled for the property owner?
                switch (type) {
                    case ENUMVALUE:
                        item.setDisable( !(owner instanceof OtmEnumeration) );
                        break;

                    case ATTRIBUTE:
                    case ID:
                    case IDREFATTRIBUTE:
                    case INDICATOR:
                        item.setDisable( owner instanceof OtmEnumeration );
                        break;

                    case ELEMENT:
                    case IDREFELEMENT:
                    case INDICATORELEMENT:
                        item.setDisable( !(owner.getTL() instanceof TLPropertyOwner) );
                        break;
                    default:
                        item.setDisable( true );
                }
            }
        }

    }

    /**
     * Build a new property of the given type. Add to the owner (both facade and TL).
     * 
     * @param propertyType
     * @param owner
     * @return
     */
    public static OtmProperty build(OtmPropertyType propertyType, OtmPropertyOwner owner) {
        OtmProperty property = null;
        TLModelElement tl = buildTL( propertyType );
        if (owner instanceof OtmAbstractDisplayFacet)
            owner = ((OtmAbstractDisplayFacet) owner).getParent();
        if (tl != null && owner != null)
            if (propertyType.equals( ID ))
                // There is no way for the factory to know the intent, use specialized method
                property = OtmPropertyFactory.createID( tl, owner );
            else
                property = OtmPropertyFactory.create( tl, owner );
        return property;
    }

    public static TLModelElement buildTL(OtmPropertyType type) {
        TLModelElement tl = null;
        switch (type) {
            case ATTRIBUTE:
            case ID:
                tl = new TLAttribute();
                break;
            case ELEMENT:
                tl = new TLProperty();
                break;
            case IDREFATTRIBUTE:
                tl = new TLAttribute();
                ((TLAttribute) tl).setReference( true );
                break;
            case IDREFELEMENT:
                tl = new TLProperty();
                ((TLProperty) tl).setReference( true );
                break;
            case INDICATOR:
                tl = new TLIndicator();
                ((TLIndicator) tl).setPublishAsElement( false );
                break;
            case INDICATORELEMENT:
                tl = new TLIndicator();
                ((TLIndicator) tl).setPublishAsElement( true );
                break;
            case ENUMVALUE:
                tl = new TLEnumValue();
            default:
        }
        return tl;
    }


}

