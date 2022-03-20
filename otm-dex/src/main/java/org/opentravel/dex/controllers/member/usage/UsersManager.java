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

package org.opentravel.dex.controllers.member.usage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmFacets.OtmEmptyTableFacet;
import org.opentravel.model.otmFacets.OtmNamespaceFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.control.TreeItem;

/**
 * Manage all the users of a OtmLibraryMember per namespace as needed for display.
 *
 * @author dmh
 * @param <T>
 *
 */
public class UsersManager {
    private static Logger log = LogManager.getLogger( UsersManager.class );

    protected OtmLibraryMember subject;
    private String nsPrefix;
    private OtmModelManager modelMgr;

    private TreeItem<MemberAndUsersDAO> nsItem = null;
    private TreeItem<MemberAndUsersDAO> assignedTypes;
    private TreeItem<MemberAndUsersDAO> contributed;
    private TreeItem<MemberAndUsersDAO> baseTypes;
    private TreeItem<MemberAndUsersDAO> resourceTypes;

    /**
     * @param user - used to create namespace facet
     * @param member - the subject used as a assigned/base/resource type by the managed users
     */
    public UsersManager(OtmLibraryMember user, OtmLibraryMember member, TreeItem<MemberAndUsersDAO> parent) {
        this.subject = member;
        nsItem = new TreeItem<>( new MemberAndUsersDAO( new OtmNamespaceFacet( user ) ) );
        parent.getChildren().add( nsItem );
        nsItem.setExpanded( true );
    }

    protected void addContributed(OtmLibraryMember user) {
        contributed = add( user, contributed, "Contributors to " + subject.getName() );
    }

    protected void addAssigned(OtmLibraryMember user) {
        assignedTypes = add( user, assignedTypes, "Members that use " + subject.getName() + " as Assigned Type" );
    }

    protected void addBase(OtmLibraryMember user) {
        baseTypes = add( user, baseTypes, subject.getName() + " is extended by" );
    }

    protected void addResource(OtmLibraryMember user) {
        resourceTypes = add( user, resourceTypes, "Used in Resource" );
    }

    protected TreeItem<MemberAndUsersDAO> add(OtmLibraryMember user, TreeItem<MemberAndUsersDAO> uRoot, String label) {
        TreeItem<MemberAndUsersDAO> item = new TreeItem<>( new MemberAndUsersDAO( user ) );
        if (uRoot == null)
            uRoot = createDividerItem( label, nsItem );
        uRoot.getChildren().add( item );
        return uRoot;
    }

    protected TreeItem<MemberAndUsersDAO> getNsRoot() {
        return nsItem;
    }

    private TreeItem<MemberAndUsersDAO> createDividerItem(String label, TreeItem<MemberAndUsersDAO> parent) {
        TreeItem<MemberAndUsersDAO> item =
            new TreeItem<>( new MemberAndUsersDAO( new OtmEmptyTableFacet( label, modelMgr ) ) );
        parent.getChildren().add( item );
        item.setExpanded( true );
        // parent.getChildren().sort( null );
        return item;
    }

}
