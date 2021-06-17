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
import org.opentravel.common.DexLibraryException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMemberFactory;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.version.VersionChain;
import org.opentravel.schemacompiler.version.VersionChainFactory;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Create new OtmLibraries using a static methods in this factory. OtmLibraries are facades for the TL Model's abstract
 * library classes. After the facade is created, all the members are created using the member factory.
 * <p>
 * New Otm libraries can be created:
 * <ol>
 * <li>From an AbstractLibrary. These can result in either built-in or local OtmLibraries.
 * <li>From an ProjectItem. These can result in managed (major or minor) OtmLibraries.
 * <li>From a local library. This results in a major, managed OtmLibrary.
 * </ol>
 * <p>
 * In Dex, users can open libraries by:
 * <ul>
 * <li>Opening a project. All contained and referenced libraries are opened.
 * <li>Opening a library.
 * <li>Adding repository item to project.
 * <li>Creating new library.
 * <li>Publishing a library into a repository.
 * <li>Creating new version.
 * </ul>
 * <p>
 * Utilities are provided that are used to determine which type of library facade to create.
 * 
 * @author dmh
 *
 */
public class OtmLibraryFactory {
    private static Log log = LogFactory.getLog( OtmLibraryFactory.class );

    private OtmLibraryFactory() {
        // NO-OP - only static methods
    }

    public static OtmMajorLibrary newLibrary(OtmLocalLibrary unmanagedLib) {
        OtmMajorLibrary otmLibrary = new OtmMajorLibrary( unmanagedLib );
        unmanagedLib.getModelManager().changeToManaged( (OtmLocalLibrary) unmanagedLib, otmLibrary );
        return otmLibrary;
    }

    // TODO or delete
    // You can only add unmanaged, local libraries
    // public static OtmLibrary newLibrary(ProjectItem pi, OtmProject otmProject) throws DexLibraryException {
    // OtmLibrary newLib = newLibrary( pi, otmProject.getModelManager() );
    //
    // if (newLib instanceof OtmLocalLibrary)
    // otmProject.add( (OtmLocalLibrary) newLib );
    // else {
    // ProjectManager pm = otmProject.getModelManager().getProjectManager();
    // ProjectItem newPI = pm.addManagedProjectItem( pi, otmProject.getTL() );
    // }
    // return newLib;
    // }

    /**
     * Create new library from the content of the PI.
     * <p>
     * Caller must model all the members in the TL library using
     * {@linkplain OtmLibraryFactory#modelMembers(AbstractLibrary, OtmModelManager)}
     * 
     * @param pi
     * @param mgr
     * @return new local, minor or major library
     * @throws DexLibraryException
     */
    public static OtmLibrary newLibrary(ProjectItem pi, OtmModelManager mgr) throws DexLibraryException {
        if (pi == null || mgr == null)
            throw new DexLibraryException( "Missing factory parameters." );

        // DEX does not use the built-in project. Model them using AbstractLibrary.
        if (pi.getContent() instanceof BuiltInLibrary)
            return newLibrary( pi.getContent(), mgr );

        if (!(pi.getContent() instanceof TLLibrary))
            throw new DexLibraryException( "Missing library content from project item." );

        TLLibrary tlLib = (TLLibrary) pi.getContent();
        int minorVN = getMinorVersionNumber( tlLib );

        addIfMissing( tlLib, mgr.getTlModel() );

        // Return variable
        OtmLibrary otmLibrary = null;

        if (isLocal( pi )) {
            otmLibrary = new OtmLocalLibrary( tlLib, mgr );
        } else if (minorVN <= 0) {
            otmLibrary = new OtmMajorLibrary( tlLib, mgr );
        } else {
            otmLibrary = new OtmMinorLibrary( tlLib, mgr );
        }

        otmLibrary.add( pi );
        // modelMembers( tlLib, mgr );
        return otmLibrary;
    }

    /**
     * Return true if this PI is local. No repository or state is UNMANAGED.
     * 
     * @param pi
     * @return
     */
    public static boolean isLocal(ProjectItem pi) {
        return pi == null || pi.getRepository() == null || pi.getState() == RepositoryItemState.UNMANAGED;
    }

    /**
     * Create a new OTM library with modeled content for the abstract TL library. Can create {@link OtmBuiltInLibrary}
     * or {@link OtmLocalLibrary}.
     * <p>
     * If the library is found in the TLProjectManager to have a PI, that is used by
     * {@link OtmLibraryFactory#newLibrary(ProjectItem, OtmModelManager)}
     * <p>
     * Models all the members in the TL library using
     * {@link OtmLibraryMemberFactory#create(org.opentravel.schemacompiler.model.LibraryMember, OtmModelManager)}.
     * 
     * @param absLibrary
     * @param mgr
     * @return new Library
     * @throws DexLibraryException
     */
    public static OtmLibrary newLibrary(AbstractLibrary absLibrary, OtmModelManager mgr) throws DexLibraryException {
        if (absLibrary == null || mgr == null)
            throw new DexLibraryException( "Missing factory parameters." );

        // Use PI if it has one.
        if (mgr.getProjectItem( absLibrary ) != null) {
            return newLibrary( mgr.getProjectItem( absLibrary ), mgr );
        }

        addIfMissing( absLibrary, mgr.getTlModel() );

        // Return variable
        OtmLibrary otmLibrary = null;

        if (absLibrary instanceof BuiltInLibrary) {
            otmLibrary = new OtmBuiltInLibrary( (BuiltInLibrary) absLibrary, mgr );
        } else if (absLibrary instanceof XSDLibrary) {
            // NO-OP
        } else if (absLibrary instanceof TLLibrary) {
            otmLibrary = new OtmLocalLibrary( (TLLibrary) absLibrary, mgr );
        }

        // modelMembers( absLibrary, mgr );

        return otmLibrary;
    }

    private static void addIfMissing(AbstractLibrary absLibrary, TLModel tlModel) {
        if (tlModel != null && !tlModel.getAllLibraries().contains( absLibrary ))
            try {
                tlModel.addLibrary( absLibrary );
            } catch (IllegalArgumentException e) {
                // This is found during testing loading the built-in. just ignore it.
                log.debug( "TL Model already contained abstract library." + e.getLocalizedMessage() );
            }
    }

    /**
     * For each TL Named Member, use the {@linkplain OtmLibraryMemberFactory#create(LibraryMember, OtmModelManager)} to
     * create and add its OTM facade.
     * <p>
     * <b>Note: </b>Must be done after new library is returned to allow constructor access to the OtmLibrary.
     * 
     * @param aLib
     * @param mgr
     */
    public static void modelMembers(AbstractLibrary aLib, OtmModelManager mgr) {
        aLib.getNamedMembers().forEach( nm -> OtmLibraryMemberFactory.create( nm, mgr ) );
    }

    /**
     * @param absLib
     * @return the minor version number. On error/exception, return -1.
     */
    public static int getMinorVersionNumber(AbstractLibrary absLib) {
        int vn = -1;
        if (absLib instanceof BuiltInLibrary || absLib.getNamespace() == null)
            return vn;

        try {
            String versionScheme = absLib.getVersionScheme();
            VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
            String versionId = vScheme.getVersionIdentifier( absLib.getNamespace() );
            vn = Integer.valueOf( vScheme.getMinorVersion( versionId ) );
        } catch (NumberFormatException e) {
            log.debug( "Error converting version string." + e.getCause() );
            return vn;
        } catch (VersionSchemeException e) {
            log.debug( "Error determining version. " + e.getCause() );
            return vn;
        }

        return vn;
    }

    /**
     * @param namespace
     * @return the major version number
     * @throws VersionSchemeException
     */
    public static int getMajorVersionNumber(AbstractLibrary absLib) {
        int vn = -1;
        if (absLib instanceof BuiltInLibrary || absLib.getNamespace() == null)
            return vn;
        try {
            String versionScheme = absLib.getVersionScheme();
            VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( versionScheme );
            String versionId = vScheme.getVersionIdentifier( absLib.getNamespace() );
            vn = Integer.valueOf( vScheme.getMajorVersion( versionId ) );
        } catch (NumberFormatException e) {
            log.debug( "Error converting version string." + e.getCause() );
            return vn;
        } catch (VersionSchemeException e) {
            log.debug( "Error determining version. " + e.getCause() );
            return vn;
        }

        return vn;
    }

    /**
     * Check to assure absLibrary is unmanaged. It must be a TLLibrary and the TL Version chain factory will not return
     * a version chain for it.
     * <p>
     * This is <b>NOT</b> the test used in the factory's newLibrary method.
     * 
     * @param absLibrary
     * @return
     */
    @Deprecated
    public static boolean isUnmanaged(AbstractLibrary absLibrary, TLModel tlModel) {
        return getTLVersionChain( absLibrary, tlModel ) == null;
    }

    @Deprecated
    public static VersionChain<TLLibrary> getTLVersionChain(AbstractLibrary absLibrary, TLModel tlModel) {
        if (!(absLibrary instanceof TLLibrary)) {
            log.error( "Tried to get a version chain with a library that is not a TLLibrary." );
            return null;
        }
        VersionChainFactory vcf;
        try {
            vcf = getVersionChainFactory( tlModel );
        } catch (DexLibraryException e) {
            log.error( "Exception gettting version chain factory: " + e.getLocalizedMessage() );
            return null;
        }
        return vcf.getVersionChain( (TLLibrary) absLibrary );
    }


    /**
     * Use the TL Model to attempt to get a version chain factory.
     * 
     * @return the factory or null if factory throws exception
     * @throws DexLibraryException
     */
    @Deprecated
    public static VersionChainFactory getVersionChainFactory(TLModel tlModel) throws DexLibraryException {
        VersionChainFactory versionChainFactory = null;
        try {
            versionChainFactory = new VersionChainFactory( tlModel );
        } catch (Exception e) {
            throw new DexLibraryException( "Factory can not access version chain factory." );
        }
        return versionChainFactory;
    }

}
