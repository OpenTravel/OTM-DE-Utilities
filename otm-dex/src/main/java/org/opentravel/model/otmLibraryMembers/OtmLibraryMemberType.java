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

import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmFacets.OtmChoiceFacet;
import org.opentravel.model.otmFacets.OtmCustomFacet;
import org.opentravel.model.otmFacets.OtmQueryFacet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// TODO - contextual facets
public enum OtmLibraryMemberType {
    BUSINESS("Business", OtmBusinessObject.class),
    CHOICE("Choice", OtmChoiceObject.class),
    CORE("Core", OtmCore.class),
    VWA("Value With Attributes", OtmValueWithAttributes.class),
    SIMPLE("Simple", OtmSimpleObject.class),
    ENUMERATIONOPEN("Open Enumeration", OtmEnumerationOpen.class),
    ENUMERATIONCLOSED("Closed Enumeration", OtmEnumerationClosed.class),
    RESOURCE("Resource", OtmResource.class),
    CHOICEFACET("Choice Facet", OtmChoiceFacet.class),
    CUSTOM("Custom Facet", OtmCustomFacet.class),
    QUERY("Query Facet", OtmQueryFacet.class),
    EXTENSIONPOINTFACET("Extension Point", OtmExtensionPointFacet.class),
    SERVICE("Service", OtmServiceObject.class);

    private final String label;
    private Class<? extends OtmLibraryMember> memberClass;

    public Class<? extends OtmLibraryMember> memberClass() {
        return memberClass;
    }

    public String label() {
        return label;
    }

    private OtmLibraryMemberType(String label, Class<? extends OtmLibraryMember> objectClass) {
        this.label = label;
        this.memberClass = objectClass;
    }

    public static String getLabel(OtmLibraryMember member) {
        for (OtmLibraryMemberType type : values())
            if (type.memberClass() == member.getClass())
                return type.label();
        return "";
    }

    public static OtmLibraryMemberType getClass(String label) {
        for (OtmLibraryMemberType type : values())
            if (type.label.equals( label ))
                return type;
        return null;
    }

    public static OtmLibraryMemberType get(OtmLibraryMember member) {
        if (member == null)
            return null;
        for (OtmLibraryMemberType type : values())
            if (type.memberClass() == member.getClass())
                return type;
        return null;
    }

    public static boolean is(OtmLibraryMember member, OtmLibraryMemberType type) {
        return type.memberClass() == member.getClass();
    }

    /**
     * Build a library member. Set its model manager, TL object and adds a listener. Does <b>not</b> set library or add
     * to model manager.
     * 
     * @param memberType
     * @param name
     * @param mgr
     * @return the library member or null
     * 
     *         <p>
     *         Throws reflection related exceptions.
     * @throws ExceptionInInitializerError
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static OtmLibraryMember buildMember(OtmLibraryMemberType memberType, String name, OtmModelManager mgr)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException, NoSuchMethodException,
        InvocationTargetException {

        OtmLibraryMember member = null;
        if (memberType != null && name != null && mgr != null) {
            Constructor<? extends OtmLibraryMember> constructor =
                memberType.memberClass.getDeclaredConstructor( String.class, OtmModelManager.class );
            if (constructor != null)
                member = constructor.newInstance( name, mgr );
        }
        return member;
    }
}

