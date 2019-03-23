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
import java.util.List;

import org.opentravel.application.common.ProgressMonitor;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;

/**
 * Orchestrator that performs on-demand validation of a collection of OTM
 * libraries.
 */
public class ValidationOrchestrator {
	
	private RepositoryManager repositoryManager;
	private List<RepositoryItem> repositoryItems = new ArrayList<>();
	private ProgressMonitor monitor;
	
	/**
	 * Assigns the repository manager instance to use during processing.  If not
	 * assigned, the default instance will be used.
	 *
	 * @param repositoryManager  the field value to assign
	 * @return ValidationOrchestrator
	 */
	public ValidationOrchestrator setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
		return this;
	}

	/**
	 * Assigns the value of the 'repositoryItems' field.
	 *
	 * @param repositoryItems  the field value to assign
	 * @return ValidationOrchestrator
	 */
	public ValidationOrchestrator setRepositoryItems(List<RepositoryItem> repositoryItems) {
		this.repositoryItems = repositoryItems;
		return this;
	}

	/**
	 * Assigns the progress monitor that will report on task percent-complete.
	 *
	 * @param monitor  progress monitor that will report on task percent-complete (may be null)
	 * @return ValidationOrchestrator
	 */
	public ValidationOrchestrator setProgressMonitor(ProgressMonitor monitor) {
		this.monitor = monitor;
		return this;
	}
	
	/**
	 * Performs validation of the libraries in the list of repository items.
	 * 
	 * @return ValidationFindings
	 * @throws SchemaCompilerException  thrown if an error occurs while loading or validating the model
	 */
	public ValidationFindings runValidation() throws SchemaCompilerException {
		ValidationFindings findings;
		
		if (repositoryManager == null) {
			repositoryManager = RepositoryManager.getDefault();
		}
		if ((repositoryItems == null) || repositoryItems.isEmpty()) {
			throw new SchemaCompilerException("No libraries to validate.");
		}
		if (monitor != null) {
			monitor.taskStarted( repositoryItems.size() + 1L );
		}
		
		findings = TLModelCompileValidator.validateModel( loadModel() );
		reportWorkUnitCompleted();
		return findings;
	}
	
	/**
	 * Loads the libraries from the list of repository items provided by the caller.
	 * 
	 * @return TLModel
	 * @throws SchemaCompilerException  thrown if one or more of the libraries cannot be loaded
	 */
	private TLModel loadModel() throws SchemaCompilerException {
		try {
			ProjectManager projectManager = new ProjectManager( new TLModel(), false, repositoryManager );
			Project project = projectManager.newProject(
					File.createTempFile( "vld", ".otp" ), "http://www.opentravel.org", "OldVersions", null );
			
			for (RepositoryItem item : repositoryItems) {
				Repository repository = item.getRepository();
				
				if (repository instanceof RemoteRepository) {
					((RemoteRepository) repository).downloadContent( item, true );
				}
				projectManager.addManagedProjectItem( item, project );
				reportWorkUnitCompleted();
			}
			project.getProjectFile().deleteOnExit();
			return projectManager.getModel();
			
		} catch (IOException e) {
			throw new SchemaCompilerException( "Unable to create project for old-version libraries.", e );
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
