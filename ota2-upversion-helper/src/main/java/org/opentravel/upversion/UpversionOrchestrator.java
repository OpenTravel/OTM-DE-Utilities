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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.application.common.ProgressMonitor;
import org.opentravel.ns.ota2.project_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.project_v01_00.ProjectType;
import org.opentravel.ns.ota2.project_v01_00.UnmanagedProjectItemType;
import org.opentravel.schemacompiler.ic.ImportManagementIntegrityChecker;
import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.MajorVersionHelper;
import org.opentravel.schemacompiler.version.VersionChain;
import org.opentravel.schemacompiler.version.VersionChainFactory;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.visitor.ModelElementVisitor;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Orchestrates the creation of new versions of one or more libraries.  In addition
 * to creating the new version, attribute and element type assignments are moved and
 * contextual facets created for the new version as necessary.
 */
public class UpversionOrchestrator {
	
	public static final String DEFAULT_PROJECT_ID       = "http://www.opentravel.org";
	public static final String DEFAULT_PROJECT_NAME     = "NewVersions";
	public static final String DEFAULT_PROJECT_FILENAME = "new-versions.otp";
	
	private static VersionSchemeFactory vsFactory = VersionSchemeFactory.getInstance();
	
	private RepositoryManager repositoryManager;
	private List<RepositoryItem> oldVersions;
	private File outputFolder;
	private String projectId = DEFAULT_PROJECT_ID;
	private String projectFilename = DEFAULT_PROJECT_FILENAME;
	private String projectName = DEFAULT_PROJECT_NAME;
	private Set<String> newVersionFilenames = new HashSet<>();
	private ProgressMonitor monitor;
	
	/**
	 * Assigns the repository manager instance to use during processing.  If not
	 * assigned, the default instance will be used.
	 *
	 * @param repositoryManager  the field value to assign
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
		return this;
	}

	/**
	 * Assigns the output folder location where the new-version project and
	 * libraries will be saved.  Any existing OTM libraries or projects in this
	 * folder will be deleted during the up-versioning process.
	 *
	 * @param outputFolder  the output folder location to assign
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
		return this;
	}

	/**
	 * Assigns the project ID of the new-version project.
	 *
	 * @param projectId  the project ID to assign
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setProjectId(String projectId) {
		this.projectId = (projectId == null) ? DEFAULT_PROJECT_ID : projectId;
		return this;
	}

	/**
	 * Assigns the filename of the new-version project.
	 *
	 * @param projectFilename  the project filename to assign
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setProjectFilename(String projectFilename) {
		this.projectFilename = (projectFilename == null) ? DEFAULT_PROJECT_FILENAME : projectFilename;
		return this;
	}

	/**
	 * Assigns the name of the new-version project to create.
	 *
	 * @param projectName  the project name to assign
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setProjectName(String projectName) {
		this.projectName = (projectName == null) ? DEFAULT_PROJECT_NAME : projectName;
		return this;
	}

	/**
	 * Assigns the list of repository items for all of the old-version libraries
	 * to be up-versioned.
	 *
	 * @param oldVersions  the list of old-version repository items
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setOldVersions(List<RepositoryItem> oldVersions) {
		this.oldVersions = oldVersions;
		return this;
	}

	/**
	 * Assigns the progress monitor that will report on task percent-complete.
	 *
	 * @param monitor  progress monitor that will report on task percent-complete (may be null)
	 * @return UpversionOrchestrator
	 */
	public UpversionOrchestrator setProgressMonitor(ProgressMonitor monitor) {
		this.monitor = monitor;
		return this;
	}
	
	/**
	 * Creates new major versions of each library and returns the new versions.
	 * 
	 * @return List<TLLibrary>
	 * @throws SchemaCompilerException  thrown if an error occurs while creating the new versions
	 */
	public List<TLLibrary> createNewVersions() throws SchemaCompilerException {
		// Validate the state of the orchestrator to make sure we can proceed
		if (repositoryManager == null) {
			repositoryManager = RepositoryManager.getDefault();
		}
		if (outputFolder == null) {
			throw new SchemaCompilerException("Output folder location for new-version files not assigned.");
		}
		if ((oldVersions == null) || oldVersions.isEmpty()) {
			throw new SchemaCompilerException("Old library versions not provided for up-version processing.");
		}
		purgeOldLibraries();
		
		// Run the up-version orchestration process and save the new-version libraries
		if (monitor != null) {
			monitor.taskStarted( (oldVersions.size() * 3) + 2L );
		}
		List<TLLibrary> oldVersionLibraries = loadOldVersions();
		validateOldVersionLibraries( oldVersionLibraries );
		UpversionRegistry registry = buildNewLibraryVersions( oldVersionLibraries );
		
		updateTypeReferences( registry );
		saveLibraries( registry.getAllNewVersions() );
		createNewVersionProjectFile( registry.getAllNewVersions() );
		
		if (monitor != null) {
			monitor.taskCompleted();
		}
		return new ArrayList<>( registry.getAllNewVersions() );
	}
	
	/**
	 * Returns true if the given folder contains existing files that will be purged
	 * during up-version processing.
	 * 
	 * @param folder  the folder to check for existing files
	 * @return boolean
	 */
	public static boolean hasExistingFiles(File folder) {
		boolean existingFiles = false;
		
		for (File folderItem : folder.listFiles()) {
			if (folderItem.isFile()) {
				existingFiles = true;
				break;
			}
		}
		return existingFiles;
	}
	
	/**
	 * Loads the old-version libraries from the list of repository items
	 * provided by the caller.
	 * 
	 * @return List<TLLibrary>
	 * @throws SchemaCompilerException  thrown if one or more of the old-version
	 *									libraries cannot be loaded
	 */
	private List<TLLibrary> loadOldVersions() throws SchemaCompilerException {
		try {
			ProjectManager projectManager = new ProjectManager( new TLModel(), false, repositoryManager );
			Project oldVersionProject = projectManager.newProject(
					File.createTempFile( "old", ".otp" ), DEFAULT_PROJECT_ID, "OldVersions", null );
			List<TLLibrary> oldVersionLibraries = new ArrayList<>();
			
			for (RepositoryItem item : oldVersions) {
				Repository repository = item.getRepository();
				ProjectItem pItem;
				
				if (repository instanceof RemoteRepository) {
					((RemoteRepository) repository).downloadContent( item, true );
				}
				pItem = projectManager.addManagedProjectItem( item, oldVersionProject );
				oldVersionLibraries.add( (TLLibrary) pItem.getContent() );
				reportWorkUnitCompleted();
			}
			projectManager.saveProject( oldVersionProject );
			return oldVersionLibraries;
			
		} catch (IOException e) {
			throw new SchemaCompilerException( "Unable to create project for old-version libraries.", e );
		}
	}
	
	/**
	 * Deletes all files from the output folder that end with an '.otp' or '.otm'
	 * extension.  If any sub-folders exist, they are not purged by this method.
	 */
	private void purgeOldLibraries() {
		if (outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		
		for (File f : outputFolder.listFiles()) {
			String filename = f.getName().toLowerCase();
			
			if (f.isFile() && (filename.endsWith(".otp") || filename.endsWith(".otm"))) {
			    FileUtils.delete( f );
			}
		}
	}
	
	/**
	 * Construct the new major version libraries for each of the old versions
	 * passed to this orchestrator.
	 * 
	 * @param oldVersionLibraries  the list of old-version libraries to be up-versioned
	 * @return UpversionRegistry
	 * @throws SchemaCompilerException  thrown if any of the new library versions cannot be created
	 */
	private UpversionRegistry buildNewLibraryVersions(List<TLLibrary> oldVersionLibraries)
			throws SchemaCompilerException {
		MajorVersionHelper helper = new MajorVersionHelper();
		UpversionRegistry registry = new UpversionRegistry();
		
		for (TLLibrary oldVersion : oldVersionLibraries) {
			File libraryFile = getNewVersionFile( oldVersion );
			
			if (libraryFile.exists()) {
                FileUtils.delete( libraryFile );
			}
			TLLibrary newVersion = helper.createNewMajorVersion( oldVersion, libraryFile );
			
			registry.addLibraryVersionMapping( oldVersion, newVersion );
			reportWorkUnitCompleted();
		}
		return registry;
	}
	
	/**
	 * Returns the file where the new library version will be stored.
	 * 
	 * @param oldVersion  the old library version
	 * @return File
	 * @throws VersionSchemeException  throwns if the version scheme of the library is invalid
	 */
	private File getNewVersionFile(TLLibrary oldVersion) throws VersionSchemeException {
		VersionScheme vScheme = vsFactory.getVersionScheme( oldVersion.getVersionScheme() );
		String oldVersionId = vScheme.getVersionIdentifier( oldVersion.getNamespace() );
		String oldMajorVersionId = vScheme.getMajorVersion( oldVersionId );
		String newVersionId = vScheme.incrementMajorVersion( oldMajorVersionId );
		String newVersionNS = vScheme.setVersionIdentifier( oldVersion.getNamespace(), newVersionId );
		String newVersionPrefix = vScheme.getPrefix( oldVersion.getPrefix(), newVersionId );
		String newVersionFilename = newVersionPrefix + "-" +
				vScheme.getDefaultFileHint( newVersionNS, oldVersion.getName() );
		String finalFilename = newVersionFilename;
		char suffix = 'a';
		
		while (newVersionFilenames.contains( finalFilename )) {
			finalFilename = newVersionFilename.replace( ".otm", "-" + suffix + ".otm" );
			suffix++;
		}
		return new File( outputFolder, newVersionFilename );
	}
	
	/**
	 * Updates all type references in the new version libraries that currently point to
	 * the old version.
	 * 
	 * @param registry  the upversion registry that contains the mappings of old and new library versions
	 */
	private void updateTypeReferences(UpversionRegistry registry) {
		if (!registry.getAllNewVersions().isEmpty()) {
			TLModel model = registry.getAllNewVersions().iterator().next().getOwningModel();
			ModelIntegrityChecker ic = new ModelIntegrityChecker();
			ModelElementVisitor visitor = new UpversionReferenceVisitor( registry );
			ModelNavigator navigator = new ModelNavigator( visitor );
			
			ModelReferenceResolver.resolveReferences( model );
			model.addListener( ic );
			registry.getAllNewVersions().forEach( navigator::navigateLibrary );
			model.removeListener( ic );
			registry.getAllNewVersions().forEach(
					ImportManagementIntegrityChecker::verifyReferencedLibraries );
			reportWorkUnitCompleted();
		}
	}
	
	/**
	 * Saves each of the libraries in the collection provided.
	 * 
	 * @param librariesToSave  the list of libraries to be saved
	 * @throws SchemaCompilerException  thrown if an error occurs while saving
	 */
	private void saveLibraries(Collection<TLLibrary> librariesToSave) throws SchemaCompilerException {
		new LibraryModelSaver().saveLibraries( new ArrayList<>( librariesToSave ) );
		
		for (TLLibrary library : librariesToSave) {
			File libraryFile = URLUtils.toFile( library.getLibraryUrl() );
			String backupFilename = libraryFile.getName().replace( ".otm", ".bak" );
			File backupFile = new File( libraryFile.getParentFile(), backupFilename );
			
			if (backupFile.exists()) {
			    FileUtils.delete( backupFile );
			}
			reportWorkUnitCompleted();
		}
	}
	
	/**
	 * Constructs an OTM project file in the output directory for all of the new version
	 * libraries in the collection provided.
	 * 
	 * @param newVersionLibraries  the collection of new version libraries
	 * @throws SchemaCompilerException  thrown if an error occurs while saving the new OTP file
	 */
	private void createNewVersionProjectFile(Collection<TLLibrary> newVersionLibraries)
			throws SchemaCompilerException {
		File projectFile = new File( outputFolder, projectFilename );
		ObjectFactory objectFactory = new ObjectFactory();
		ProjectType jaxbProject = new ProjectType();
		
		jaxbProject.setProjectId( projectId );
		jaxbProject.setName( projectName );
		
		for (TLLibrary library : newVersionLibraries) {
			UnmanagedProjectItemType projectItem = new UnmanagedProjectItemType();
			File libraryFile = URLUtils.toFile( library.getLibraryUrl() );
			
			projectItem.setFileLocation( libraryFile.getName() );
			jaxbProject.getProjectItemBase().add(
					objectFactory.createUnmanagedProjectItem( projectItem ) );
		}
		new ProjectFileUtils().saveProjectFile( jaxbProject, projectFile );
		reportWorkUnitCompleted();
	}
	
	/**
	 * Verifies that each library in the old version list is the latest version of that library
	 * in the model.
	 * 
	 * @param oldVersionLibraries  the list of old-version libraries to validate
	 * @throws SchemaCompilerException  thrown if one or more of the old version libraries is not valid
	 */
	private void validateOldVersionLibraries(List<TLLibrary> oldVersionLibraries) throws SchemaCompilerException {
		if (oldVersionLibraries.isEmpty()) {
			throw new SchemaCompilerException("No old-version libraries specified.");
		}
		TLModel model = oldVersionLibraries.get( 0 ).getOwningModel();
		VersionChainFactory chainFactory = new VersionChainFactory( model );
		StringBuilder errorDetails = new StringBuilder();
		boolean hasError = false;
		
		for (TLLibrary library : oldVersionLibraries) {
			VersionChain<TLLibrary> libraryChain = chainFactory.getVersionChain( library );
			
			if (libraryChain.getNextVersion( library ) != null) {
				if (errorDetails.length() != 0) errorDetails.append(", ");
				errorDetails.append( URLUtils.toFile( library.getLibraryUrl() ).getName() );
				hasError = true;
			}
		}
		
		if (hasError) {
			throw new SchemaCompilerException(
					"The following library(ies) are not the latest version in the model: " +
							errorDetails.toString());
		}
	}
	
	/**
	 * If a progress monitor is assigned, this method will report a single unit of work as
	 * completed.
	 */
	private void reportWorkUnitCompleted() {
		if (monitor != null) {
			monitor.progress( 1 );
		}
	}
	
}
