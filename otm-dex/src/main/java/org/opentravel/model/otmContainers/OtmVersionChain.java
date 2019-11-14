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

package org.opentravel.model.otmContainers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmModelElement;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.util.List;

/**
 * OTM Version Chain. Utilities for accessing libraries with the same name, namespace and major version number.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmVersionChain {
    private static Log log = LogFactory.getLog( OtmVersionChain.class );

    List<OtmLibrary> libraries;
    String baseNSwithName;

    public OtmVersionChain(OtmLibrary library) {
        libraries = library.getModelManager().getVersionChain( library );
        baseNSwithName = library.getNameWithBasenamespace();

        // Verify - comment out when not debugging/testing
        for (OtmLibrary lib : libraries) {
            assert lib.getNameWithBasenamespace().equals( baseNSwithName );
            try {
                assert lib.getMajorVersion() == library.getMajorVersion();
            } catch (VersionSchemeException e) {
                log.debug( "Version Scheme exception creating version chain. " + e.getLocalizedMessage() );
                assert false; // Error
            }
        }
    }

    public OtmLibrary getEditable() {
        for (OtmLibrary lib : libraries)
            if (lib.isEditable())
                return lib;
        return null;
    }

    public OtmLibrary getMajor() {
        for (OtmLibrary lib : libraries)
            if (!lib.isMinorVersion())
                return lib;
        return null;
    }

    /**
     * Return true if any library in the chain is editable.
     * 
     * @return
     */
    public boolean isChainEditable() {
        for (OtmLibrary lib : libraries)
            if (lib.isEditable())
                return true;
        return false;
    }

    public boolean isLatestVersion(OtmLibraryMember member) {
        int vn = member.getLibrary().getMinorVersion();
        for (OtmLibrary lib : libraries) {
            if (lib.getMinorVersion() > vn && lib.getTL().getNamedMember( member.getName() ) != null)
                return false;
        }
        return true;
    }

    /**
     * Is the major version of these libraries the latest in the model?
     * 
     * @param member
     * @return
     */
    public boolean isLatestChain() {
        return getLatestVersion() != null ? getLatestVersion().isLatestVersion() : false;
    }

    public OtmLibrary getLatestVersion() {
        OtmLibrary latest = null;
        if (libraries != null && !libraries.isEmpty()) {
            latest = libraries.get( 0 );
            for (OtmLibrary lib : libraries)
                if (lib.getMinorVersion() > latest.getMinorVersion())
                    latest = lib;
        }
        return latest;
    }

    public OtmLibraryMember getLatestVersion(OtmLibraryMember member) {
        OtmLibraryMember latest = null;
        int vn = member.getLibrary().getMinorVersion();
        for (OtmLibrary lib : libraries) {
            if (lib.getMinorVersion() > vn && lib.getTL().getNamedMember( member.getName() ) != null) {
                LibraryMember tlMember = lib.getTL().getNamedMember( member.getName() );
                member = (OtmLibraryMember) OtmModelElement.get( (TLModelElement) tlMember );
            }
        }
        return member;
    }

    public boolean contains(OtmLibrary lib) {
        return libraries.contains( lib );
    }

    /**
     * @param member
     * @return
     */
    public boolean isNewToChain(OtmLibraryMember member) {
        for (OtmLibrary lib : libraries)
            if (lib != member.getLibrary() && lib.contains( member ))
                return false;
        return true;
    }
}
