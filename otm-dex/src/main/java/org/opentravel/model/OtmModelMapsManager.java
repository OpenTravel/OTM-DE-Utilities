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
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.concurrent.WorkerStateEvent;

/**
 * Manage access to library provider and user maps.
 * 
 * @author dmh
 *
 */
public class OtmModelMapsManager implements TaskResultHandlerI {
    private static Logger log = LogManager.getLogger( OtmModelMapsManager.class );

    // TODO - make this into a handler
    // // TODO - make this into a structure
    protected Map<OtmLibrary,List<OtmLibraryMember>> providerMap = null;
    protected Map<OtmLibrary,List<OtmLibraryMember>> usersMap = null;
    private OtmModelManager modelMgr;

    /**
     */
    public OtmModelMapsManager(OtmModelManager modelMgr) {
        this.modelMgr = modelMgr;
    }

    /**
     * Add entries for each library in the users list.
     * <p>
     * Exposed only for testing.
     * 
     * @param usersMap
     * @param lib
     * @param users
     */
    protected void addToUsersMap(Map<OtmLibrary,List<OtmLibraryMember>> usersMap, OtmLibrary lib,
        OtmLibraryMember member) {

        for (OtmLibraryMember user : member.getWhereUsed()) {
            if (user == null || user.getLibrary() == null || user.getLibrary() == lib)
                continue; // skip

            // Get the member list for this library
            List<OtmLibraryMember> memberList = usersMap.get( user.getLibrary() );
            if (memberList == null) {
                memberList = new ArrayList<>();
                usersMap.put( user.getLibrary(), memberList );
            }
            if (!memberList.contains( user ))
                memberList.add( user );
        }
        // // Done - test using the retrieved list instead of retrieving it again
        // users.forEach( u -> {
        // if (u != null && u.getLibrary() != null && u.getLibrary() != lib) {
        // // Get the member list for this library
        // List<OtmLibraryMember> memberList = usersMap.get( u.getLibrary() );
        // // ToDo - test using the retrieved list instead of retrieving it again
        // if (memberList != null) {
        // // Already had entry, just add User
        // if (!memberList.contains( u.getOwningMember() ))
        // usersMap.get( u.getLibrary() ).add( u.getOwningMember() );
        // } else {
        // // Create new entry in map with new list
        // memberList = new ArrayList<>();
        // memberList.add( u.getOwningMember() );
        // usersMap.put( u.getLibrary(), memberList );
        // }
        // }
        // } );
    }

    /**
     * Get a users map.
     * <p>
     * The keys are each library that uses types from this library.
     * <p>
     * The values are an array of this library's members that use the key library types.
     * <p>
     * Each member is examined using getWhereUsed()
     * 
     * @return new map.
     */
    public Map<OtmLibrary,List<OtmLibraryMember>> getUsersMap(OtmLibrary lib, boolean sort) {
        // log.debug( "Starting getting users map for " + lib );
        usersMap = new HashMap<>();
        for (OtmLibraryMember m : lib.getMembers()) {
            // List<OtmLibraryMember> users = m.getWhereUsed();
            addToUsersMap( usersMap, lib, m );
        }
        return usersMap;
    }

    /** ************************************* Provider *************************************/
    /**
     * Get a provider map.
     * <p>
     * The keys are each provider library -- libraries containing types assigned to type-users in this library.
     * <p>
     * The values are an array of this library's members that use the provided the types. Each value is a member that
     * uses types from the provider library.
     * 
     * @return new map.
     */
    public Map<OtmLibrary,List<OtmLibraryMember>> getProvidersMap(OtmLibrary library, boolean sort) {
        // log.debug( "Starting getting provider map for " + library );
        providerMap = new TreeMap<>();

        // keys = Get all the libraries that provide types to library parameter
        // Values = all the type users in target library that use types from different libraries
        List<OtmTypeUser> usersInTargetLibrary = new ArrayList<>();
        for (OtmLibraryMember m : library.getMembers()) {
            if (m instanceof OtmTypeUser)
                usersInTargetLibrary.add( (OtmTypeUser) m );
            usersInTargetLibrary.addAll( m.getDescendantsTypeUsers() );
        }
        // Add each user to the entry found using assigned type's library.
        // TODO - pass in the library to filter out user's with types from the library
        usersInTargetLibrary.forEach( u -> addToMap( u, library, providerMap ) );

        // Add all the users of base types from other libraries.
        for (OtmLibraryMember m : library.getMembers()) {
            if (m.getBaseType() != null && m.getBaseType().getLibrary() != library)
                addToMap( m.getBaseType().getLibrary(), m, providerMap );
        }

        if (sort)
            providerMap.values().forEach( l -> l.sort( null ) );
        // log.debug( "Done getting provider map for " + this );
        return providerMap;


        // // If the member is a type user, add it
        // for (OtmLibraryMember m : library.getMembers()) {
        // if (m instanceof OtmTypeUser)
        // addToMap( (OtmTypeUser) m, library, providerMap );
        //
        // // If the member has type users, add all the libraries a property uses
        // Collection<OtmTypeUser> users = new ArrayList<>( m.getDescendantsTypeUsers() );
        // for (OtmTypeUser u : users) {
        // // Check for owners being contextual facets. Skip these.
        // if (u.getOwningMember() != m && u.getOwningMember() instanceof OtmContextualFacet)
        // continue;
        // // For each user, get the provider's owner and add to map
        // addToMap( u, library, providerMap );
        // }
        // }

        // providerMap = new TreeMap<>();
        // for (OtmLibraryMember m : library.getMembers()) {
        // // If the member is a type user, add it
        // if (m instanceof OtmTypeUser)
        // addToMap( (OtmTypeUser) m, library, providerMap );
        // // Testing delay happens even when this for loop is commented out
        // // If the member has type users, add all the libraries a property uses
        // Collection<OtmTypeUser> users = new ArrayList<>( m.getDescendantsTypeUsers() );
        // for (OtmTypeUser u : users) {
        // // Check for owners being contextual facets. Skip these.
        // if (u.getOwningMember() != m && u.getOwningMember() instanceof OtmContextualFacet)
        // continue;
        // // For each user, get the provider's owner and add to map
        // addToMap( u, library, providerMap );
        // }
        // }
        // if (sort)
        // providerMap.values().forEach( l -> l.sort( null ) );
        // log.debug( "Done getting provider map for " + this );
        // return providerMap;
    }

    /**
     * Add this user to the entry found using assigned type's library.
     * 
     * @param user - assigned type's owning library is key, owning member is member
     * @param lib - the library the map is being built for
     * @param map
     */
    private void addToMap(OtmTypeUser user, OtmLibrary lib, Map<OtmLibrary,List<OtmLibraryMember>> map) {
        if (user != null && user.getAssignedType() != null && user.getAssignedType().getLibrary() != null) {
            OtmLibrary key = user.getAssignedType().getLibrary();
            // Ignore built-in
            if (!key.isBuiltIn() && key != lib)
                addToMap( key, user.getOwningMember(), map );
        }
    }

    /**
     * 
     * @param key - map key to access list of members
     * @param member - member to add if unique
     * @param map
     */
    private void addToMap(OtmLibrary key, OtmLibraryMember member, Map<OtmLibrary,List<OtmLibraryMember>> map) {
        List<OtmLibraryMember> values = map.get( key );
        if (values == null)
            values = new ArrayList<>();
        if (!values.contains( member )) {
            values.add( member );
        }
        map.put( key, values ); // replaces old values
    }



    // /**
    // * Add user's owner to the list associated with its library in the map.
    // * <p>
    // * Map Entry = user's assignedType's library : list of < user's owning members>
    // *
    // * @param user whose owning member will be added to the list for the assigned type's library
    // * @param map of libraries and list of user-owners
    // */
    // private void addToMap(OtmTypeUser user, OtmLibrary library, Map<OtmLibrary,List<OtmLibraryMember>> map) {
    // log.debug( "Adding " + user + " to map." );
    //
    // if (user != null && map != null && user.getOwningMember() != null && user.getAssignedType() != null) {
    // // if (user.getName() != null && user.getName().equals( "Newcore" ))
    // // log.debug( "HERE" );
    //
    // // Determine what library map key to use
    // OtmLibrary assignedLibrary = null;
    // OtmLibraryMember assignedMember = user.getAssignedType().getOwningMember();
    // if (assignedMember != null)
    // assignedLibrary = assignedMember.getLibrary();
    //
    // // Library key compare is just on name, not name with version
    // if (assignedLibrary != null && assignedLibrary != library) {
    //
    // // Get the list from the map for the library of the assigned type
    // List<OtmLibraryMember> mList = map.get( assignedLibrary );
    // if (mList != null) {
    // // Add the user's owner to the list
    // if (!mList.contains( user.getOwningMember() ))
    // mList.add( user.getOwningMember() );
    // } else {
    // // Create new entry in the map and add the library and list containing user's owner
    // mList = new ArrayList<>();
    // mList.add( user.getOwningMember() );
    // map.put( assignedLibrary, mList );
    // }
    // }
    // }
    // }


    /**
     * @see org.opentravel.dex.tasks.TaskResultHandlerI#handleTaskComplete(javafx.concurrent.WorkerStateEvent)
     */
    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        // TODO Auto-generated method stub
    }
}

