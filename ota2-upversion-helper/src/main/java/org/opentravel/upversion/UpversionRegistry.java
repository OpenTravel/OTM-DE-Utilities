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

package org.opentravel.upversion;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.TLLibrarySymbolTablePopulator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a mapping correlation between the old and new version libraries of a model.
 */
public class UpversionRegistry {

    private TLLibrarySymbolTablePopulator symbolTablePopulator = new TLLibrarySymbolTablePopulator();
    private Map<TLLibrary,TLLibrary> libraryMap = new HashMap<>();
    private Map<TLLibrary,SymbolTable> newVersionSymbolTables = new HashMap<>();

    /**
     * Adds a new library version mapping to this registry.
     * 
     * @param oldVersion the old version of the library
     * @param newVersion the new version of the library
     */
    public void addLibraryVersionMapping(TLLibrary oldVersion, TLLibrary newVersion) {
        if ((oldVersion == null) || (newVersion == null)) {
            throw new IllegalArgumentException( "Neither the old or new library version can be null." );
        }
        if (!oldVersion.getBaseNamespace().equals( newVersion.getBaseNamespace() )) {
            throw new IllegalArgumentException(
                "The old and new library versions must both belong to the same base namespace." );
        }
        if (!oldVersion.getName().equals( newVersion.getName() )) {
            throw new IllegalArgumentException( "The old and new library versions must both have the same name." );
        }
        SymbolTable symbolTable = new SymbolTable();

        libraryMap.put( oldVersion, newVersion );
        newVersionSymbolTables.put( newVersion, symbolTable );
        symbolTablePopulator.populateSymbols( newVersion, symbolTable );
    }

    /**
     * Returns all of the old library versions that have been registered.
     * 
     * @return Collection&lt;TLLibrary&gt;
     */
    public Collection<TLLibrary> getAllOldVersions() {
        return Collections.unmodifiableCollection( libraryMap.keySet() );
    }

    /**
     * Returns all of the new library versions that have been registered.
     * 
     * @return Collection&lt;TLLibrary&gt;
     */
    public Collection<TLLibrary> getAllNewVersions() {
        return Collections.unmodifiableCollection( libraryMap.values() );
    }

    /**
     * If the given library has been registered as an old version, returns the corresponding new version.
     * 
     * @param oldVersion the old version of the library
     * @return TLLibrary
     */
    public TLLibrary getNewVersion(TLLibrary oldVersion) {
        return libraryMap.get( oldVersion );
    }

    /**
     * If the owning library of the given entity has been registered as an old version, returns the corresponding entity
     * from the new library version.
     * 
     * @param oldVersion the old version of the entity
     * @return NamedEntity
     */
    public NamedEntity getNewVersion(NamedEntity oldVersion) {
        AbstractLibrary oldLibraryVersion = (oldVersion == null) ? null : oldVersion.getOwningLibrary();
        TLLibrary newLibraryVersion =
            (oldLibraryVersion instanceof TLLibrary) ? getNewVersion( (TLLibrary) oldLibraryVersion ) : null;
        SymbolTable newVersionST = newVersionSymbolTables.get( newLibraryVersion );

        return ((newVersionST == null) || (newLibraryVersion == null)) ? null
            : (NamedEntity) newVersionST.getEntity( newLibraryVersion.getNamespace(), oldVersion.getLocalName() );
    }

}
