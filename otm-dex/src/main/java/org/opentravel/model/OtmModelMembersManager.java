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

package org.opentravel.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.DexFilter;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * junit: {@link TestOtmModelMembersManager}
 * 
 * @author dmh
 *
 */
public class OtmModelMembersManager {
    private static Logger log = LogManager.getLogger( OtmModelMembersManager.class );

    // All members - Library Members are TLLibraryMembers and contextual facets
    public static final int MEMBERCOUNT = 2666; // 2000 / .075 +1;
    private Map<LibraryMember,OtmLibraryMember> members = new HashMap<>( MEMBERCOUNT );
    private Map<LibraryMember,OtmLibraryMember> syncedMembers = Collections.synchronizedMap( members );

    OtmModelManager modelMgr = null;

    public OtmModelMembersManager(OtmModelManager modelManager) {
        this.modelMgr = modelManager;

        if (modelMgr == null)
            throw new IllegalArgumentException( "Namespace handler must have model manager argument." );
    }

    /**
     * Simply add the member to the members map if it is not already in the map. See
     * {@link OtmLibrary#add(OtmLibraryMember)} to add to both TL library and manager.
     * 
     * @param member
     */
    public void add(OtmLibraryMember member) {
        if (member != null && member.getTL() instanceof LibraryMember && !contains( member.getTlLM() ))
            members.put( member.getTlLM(), member );
    }

    protected void clear() {
        members.clear();
    }

    /**
     * @return true if the TL Library Member exists as a key in the members map.
     */
    public boolean contains(LibraryMember tlMember) {
        return tlMember != null && members.containsKey( tlMember );
    }

    /**
     * @return true if the member exists as a value in the members map.
     */
    public boolean contains(OtmLibraryMember member) {
        return member != null && members.containsValue( member );
        // return member != null && member.getTL() instanceof LibraryMember && members.containsKey( member.getTlLM() );
    }

    /**
     * Examine all members. Return list of members that use the passed member as a base type. Excludes
     * OtmContextualFacets.
     * 
     * @param member
     * @return
     */
    public List<OtmLibraryMember> findSubtypesOf(OtmLibraryMember member) {
        // Changed 11/5/2019 - why copy list? The list is not changing.
        List<OtmLibraryMember> values = new ArrayList<>( getMembers() );
        List<OtmLibraryMember> subTypes = new ArrayList<>();
        // Contextual facets use base type to define injection point
        for (OtmLibraryMember m : values) {
            if (m.getBaseType() == member && !(m instanceof OtmContextualFacet))
                subTypes.add( m );
        }
        // if (!users.isEmpty())
        // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        return subTypes;
    }

    /**
     * Examine all member's usedTypes list. Return list of owners that have a of a descendant type user that is assigned
     * to provider.
     * 
     * @param provider
     * @return
     */
    public List<OtmLibraryMember> findUsersOf(OtmTypeProvider provider) {
        // Changed 11/5/2019 - why copy list? The list is not changing.
        // List<OtmLibraryMember> values = new ArrayList<>( getMembers() );
        List<OtmLibraryMember> users = new ArrayList<>();
        for (OtmLibraryMember m : getMembers()) {
            if (m.getUsedTypes().contains( provider ))
                users.add( m );
        }
        // if (!users.isEmpty())
        // log.debug("Found " + users.size() + " users of " + p.getNameWithPrefix());
        return users;
    }

    /**
     * Return a library member with the same name that is in the latest version of the libraries with the same base
     * namespace
     * 
     * @param member
     * @return
     */
    public OtmLibraryMember getLatestMember(OtmLibraryMember member) {
        for (OtmLibraryMember c : getMembers()) {
            if (c.getLibrary().getBaseNS().equals( member.getLibrary().getBaseNS() )
                && c.getName().equals( member.getName() ) && c.isLatestVersion())
                return c;
        }
        return null;
    }

    /**
     * Get the member with matching prefix and name
     * 
     * @param nameWithPrefix formatted as prefix + ":" + name
     * @return member if found or null
     */
    public OtmLibraryMember getMember(String nameWithPrefix) {
        for (OtmLibraryMember candidate : getMembers())
            if (candidate.getNameWithPrefix().equals( nameWithPrefix ))
                return candidate;
        return null;
    }

    /**
     * Retrieve the OtmLibraryMember facade for this member from the map.
     * <p>
     * For better performance, use {@linkplain OtmModelElement#get(TLModelElement)} which uses the listener.
     * 
     * @param tlMember
     * @return
     */
    public OtmLibraryMember getMember(TLModelElement tlMember) {
        if (tlMember instanceof LibraryMember)
            return members.get( (tlMember) );
        // OtmModelElement.get( tlObject );
        return null;
    }

    /**
     * Synchronized access to members.values()
     * 
     * @return all the library members being managed in a unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers() {
        // return Collections.unmodifiableCollection( members.values() );
        return Collections.unmodifiableCollection( syncedMembers.values() );
    }

    /**
     * Notes: using the commented out sync'ed code causes TestInheritance#testInheritedCustomFacets() to time out.
     * getMembers() uses the synchronized member list.
     * <p>
     * 
     * @param filter DexFilter to use to select members. If null, all members are selected.
     * @return all the filter selected library members in an unmodifiableCollection
     */
    public Collection<OtmLibraryMember> getMembers(DexFilter<OtmLibraryMember> filter) {
        // log.debug( "Starting to get filtered members." );
        if (filter == null)
            return getMembers();
        // List<OtmLibraryMember> selected = Collections.synchronizedList( new ArrayList<>() );
        // synchronized (selected) {
        // getMembers().forEach( m -> {
        // if (filter.isSelected( m ))
        // selected.add( m );
        // } );
        // }
        // 5/26/2021
        List<OtmLibraryMember> selected = new ArrayList<>();
        getMembers().forEach( m -> {
            if (filter.isSelected( m ))
                selected.add( m );
        } );
        // log.debug( "Got " + selected.size() + " filtered members." );
        return Collections.unmodifiableCollection( selected );
    }

    /**
     * @return new list with all the library members in that library
     */
    public List<OtmLibraryMember> getMembers(OtmLibrary library) {
        List<OtmLibraryMember> libraryMembers = new ArrayList<>();
        getMembers().forEach( m -> {
            if (m.getLibrary() == library)
                libraryMembers.add( m );
        } );
        return libraryMembers;
    }

    /**
     * @param name
     * @return list of members with matching names
     */
    public List<OtmLibraryMember> getMembers(OtmLibraryMember m) {
        List<OtmLibraryMember> matches = new ArrayList<>();
        for (OtmLibraryMember candidate : getMembers())
            if (m != candidate && candidate.getName().equals( m.getName() ))
                matches.add( candidate );
        return matches;
    }

    /**
     * @return new collection of all contextual facets in the model.
     */
    public Collection<OtmLibraryMember> getMembersContextualFacets() {
        // Use the filter "instanceof OtmContextualFacet" on the getMembers()
        return getMembers( d -> d instanceof OtmContextualFacet );
    }


    /**
     * Get the OtmXsdSimple from the library if any.
     * 
     * @param name
     * @param lib
     * @return
     */
    public OtmXsdSimple getXsdMember(String name, OtmLibrary lib) {
        LibraryMember member = null;
        if (lib != null)
            member = lib.getTL().getNamedMember( name );
        OtmObject otm = OtmModelElement.get( (TLModelElement) member );
        return otm instanceof OtmXsdSimple ? (OtmXsdSimple) otm : null;
    }

    /**
     * Simply remove the member from the map. To delete a member use {@link OtmLibrary#delete(OtmLibraryMember)}
     * 
     * @param member
     */
    public void remove(OtmLibraryMember member) {
        if (member != null && member.getTL() instanceof LibraryMember && contains( member.getTlLM() ))
            members.remove( member.getTlLM(), member );
    }

}
