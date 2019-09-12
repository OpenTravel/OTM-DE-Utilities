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

import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmChoiceObject;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationOpen;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmServiceObject;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum LibraryMemberType {
    BUSINESS("Business", OtmBusinessObject.class),
    CHOICE("Choice", OtmChoiceObject.class),
    CORE("Core", OtmCore.class),
    RESOURCE("Resource", OtmResource.class),
    SERVICE("Service", OtmServiceObject.class),
    SIMPLE("Simple", OtmSimpleObject.class),
    ENUMERATIONOPEN("Open Enumeration", OtmEnumerationOpen.class),
    ENUMERATIONCLOSED("Closed Enumeration", OtmEnumerationClosed.class),
    VWA("Value With Attributes", OtmValueWithAttributes.class);

    private final String label;
    private Class<? extends OtmLibraryMember> memberClass;

    public Class<? extends OtmLibraryMember> memberClass() {
        return memberClass;
    }

    public String label() {
        return label;
    }

    private LibraryMemberType(String label, Class<? extends OtmLibraryMember> objectClass) {
        this.label = label;
        this.memberClass = objectClass;
    }


    public static OtmLibraryMember buildMember(LibraryMemberType memberType, String name, OtmModelManager mgr)
        throws ExceptionInInitializerError, InstantiationException, IllegalAccessException, NoSuchMethodException,
        SecurityException, IllegalArgumentException, InvocationTargetException {

        OtmLibraryMember member = null;
        if (memberType != null && name != null && mgr != null) {
            Constructor<? extends OtmLibraryMember> constructor;
            constructor = memberType.memberClass.getDeclaredConstructor( String.class, OtmModelManager.class );
            if (constructor != null)
                member = constructor.newInstance( name, mgr );
        }
        // if (memberType != null && memberType.memberClass != null)
        // member = memberType.memberClass().newInstance();
        return member;
    }
}

