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

package org.opentravel.release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the available facets and the current facet selection for a particular
 * OTM entity type.  Only those OTM entity types which allow for substitution group
 * selections will allow for different facet selections.
 */
public class FacetSelection {
	
    private static final Logger log = LoggerFactory.getLogger( FacetSelection.class );
    
	private NamedEntity entityType;
	private TLFacetOwner facetOwner;
	private String selectedFacetName;
	private List<String> facetNames;
	private Map<String,TLFacet> facetsByName = new HashMap<>();
	
	/**
	 * Constructor that initializes the facet selection instance using the information
	 * provided from an OTM release.
	 * 
	 * @param facetOwner  the OTM entity type of the facet owner (or alias)
	 * @param facetSelectionName  the qualified name of the preferred facet selection
	 * @param symbolTable  the symbol table to use when resolving names
	 * @throws IllegalArgumentException  thrown if the given facet owner / selection pairing is not valid
	 */
	private FacetSelection(NamedEntity facetOwner, QName facetSelectionName, SymbolTable symbolTable) {
		initialize( facetOwner );
		
		if (facetSelectionName != null) {
			NamedEntity facetSelection = (NamedEntity) symbolTable.getEntity(
					facetSelectionName.getNamespaceURI(), facetSelectionName.getLocalPart() );
			
			if (facetSelection == null) {
				throw new IllegalArgumentException("The named entity cannot be resolved: " + facetSelection);
			}
			if (!(facetSelection instanceof TLFacet)) {
				throw new IllegalArgumentException("The selection is not a valid facet: " + facetSelection);
			}
			if (!isValidFacetReference( (TLFacet) facetSelection, (TLFacetOwner) facetOwner )) {
				throw new IllegalArgumentException("The selection is not a facet of the specified owner.");
			}
			setSelectedFacet( Utils.getDisplayName( facetSelection, false ) );
		}
	}
	
	/**
	 * Returns a list of <code>FacetSelection</code> objects for all substitutable entities
	 * in the given model.  Any facets that already have preferred selections will by populated
	 * with the appropriate values.
	 * 
	 * @param facetSelections  the list of pre-selected facets from the OTM release
	 * @param model  the model from which to resolve facet selections
	 * @return List<FacetSelection>
	 */
	public static List<FacetSelection> buildFacetSelections(Map<QName,QName> facetSelections, TLModel model) {
		SymbolTable symbolTable = SymbolTableFactory.newSymbolTableFromModel( model );
		List<FacetSelection> selectionList = new ArrayList<>();
		Map<QName,NamedEntity> facetOwnerMap = new HashMap<>();
		List<QName> ownerNames = new ArrayList<>();
		
		// Search the model for all possible substitution groups
		findSubstitutionGroups( model, facetOwnerMap, ownerNames );
		
		// Construct a facet selection for all substitution groups with multiple choices
		for (QName ownerName : ownerNames) {
		    try {
	            NamedEntity facetOwner = facetOwnerMap.get( ownerName );
	            QName facetSelection = facetSelections.get( ownerName );
	            FacetSelection selection = new FacetSelection( facetOwner, facetSelection, symbolTable );
	            
	            if (selection.getFacetNames().size() > 1) {
	                selectionList.add( selection );
	            }
		        
		    } catch (IllegalArgumentException e) {
		        log.warn( String.format( "Error building facet selection for owner '%s'", ownerName ), e );
		    }
		}
		
		return selectionList;
	}

    /**
     * Searches the given model to find all of the possible substitution groups.
     * 
     * @param model  the model to search
     * @param facetOwnerMap  the map of owner names to substitution group facets being constructed
     * @param ownerNames  the list of owner names with substitution groups being constructed
     */
    private static void findSubstitutionGroups(TLModel model, Map<QName,NamedEntity> facetOwnerMap,
            List<QName> ownerNames) {
        for (TLLibrary library : model.getUserDefinedLibraries()) {
			for (TLBusinessObject businessObject : library.getBusinessObjectTypes())  {
			    if (businessObject != null) {
	                facetOwnerMap.put( getQName( businessObject ), businessObject );
	                businessObject.getCustomFacets().forEach( f -> facetOwnerMap.put( getQName( f ), f ) );
	                businessObject.getQueryFacets().forEach( f -> facetOwnerMap.put( getQName( f ), f ) );
	                businessObject.getUpdateFacets().forEach( f -> facetOwnerMap.put( getQName( f ), f ) );
			    }
			}
			for (TLChoiceObject choiceObject : library.getChoiceObjectTypes())  {
			    if (choiceObject != null) {
	                facetOwnerMap.put( getQName( choiceObject ), choiceObject );
	                choiceObject.getChoiceFacets().forEach( f -> facetOwnerMap.put( getQName( f ), f ) );
			    }
			}
			for (NamedEntity coreObject : library.getCoreObjectTypes())  {
                facetOwnerMap.put( getQName( coreObject ), coreObject );
			}
		}
		ownerNames.addAll( facetOwnerMap.keySet() );
		Collections.sort( ownerNames, new QNameComparator() );
    }
	
	/**
	 * Returns a map of qualified names representing the given list of facet selections.
	 * 
	 * @param facetSelections  the list of facet selections to convert
	 * @return Map<QName,QName>
	 */
	public static Map<QName,QName> buildFacetSelectionMap(List<FacetSelection> facetSelections) {
		Map<QName,QName> selectionMap = new HashMap<>();
		
		for (FacetSelection selection : facetSelections) {
			TLFacet selectedFacet = selection.getSelectedFacet();
			
			if (selectedFacet != null) {
				QName facetOwnerName = getQName( selection.getEntityType() );
				QName selectedFacetName = getQName( selectedFacet );
				
				selectionMap.put( facetOwnerName, selectedFacetName );
			}
		}
		return selectionMap;
	}
	
	/**
	 * Returns the type of OTM entity to which this facet selection applies.
	 *
	 * @return NamedEntity
	 */
	public NamedEntity getEntityType() {
		return entityType;
	}
	
	/**
	 * If the 'entityType' field is a <code>FacetOwner</code> and more than a single facet
	 * selection is available, this method will return that 'entityType' as the facet owner.
	 *
	 * @return TLFacetOwner
	 */
	public TLFacetOwner getFacetOwner() {
		return facetOwner;
	}

	/**
	 * Returns the name of the currently selected facet.
	 *
	 * @return String
	 */
	public String getSelectedFacetName() {
		return selectedFacetName;
	}
	
	/**
	 * Returns the facet that is currently selected by the user (null if
	 * no selection is available).
	 * 
	 * @return TLFacet
	 */
	public TLFacet getSelectedFacet() {
		return facetsByName.get( selectedFacetName );
	}

	/**
	 * Assigns the name of the selected facet.
	 *
	 * @param facetName  the facet name to assign
	 */
	public void setSelectedFacet(String facetName) {
		this.selectedFacetName = facetName;
	}

	/**
	 * Returns the list of available facet names for this selection.
	 * 
	 * @return List<String>
	 */
	public List<String> getFacetNames() {
		return facetNames;
	}
	
	/**
	 * Returns the facet with the specified name or null if no such facet
	 * exists.
	 * 
	 * @return TLFacet
	 */
	public TLFacet getFacet(String facetName) {
		return facetsByName.get( facetName );
	}
	
	/**
	 * Returns the name of the given facet within this selection instance or
	 * null if the given facet is not known to this instance.
	 * 
	 * @param facet  the facet for which to return a name
	 * @return String
	 */
	public String getFacetName(TLFacet facet) {
		String facetName = null;
		
		for (String fn : facetNames) {
			if (facet == facetsByName.get( fn )) {
				facetName = fn;
				break;
			}
		}
		return facetName;
	}
	
	/**
	 * Initializes the entity type and the available list of facet names for that type.
	 * 
	 * @param entityType  the OTM entity type of the facet owner (or alias)
	 */
	private void initialize(NamedEntity entityType) {
		NamedEntity realEntityType = entityType;
		List<TLFacet> facetList = null;
		
		this.entityType = entityType;
		this.facetNames = new ArrayList<>();
		
		if (realEntityType instanceof TLAlias) {
			realEntityType = ((TLAlias) realEntityType).getOwningEntity();
		}
		
		if (realEntityType instanceof TLContextualFacet) {
			this.facetOwner = (TLFacetOwner) realEntityType;
			facetList = FacetCodegenUtils.getAvailableFacets( (TLContextualFacet) facetOwner );
			
		} else if (realEntityType instanceof TLFacet) {
			// If the entity is a non-contextual facet, then it will be the only member
			// of the facet list
			TLFacet facet = (TLFacet) realEntityType;
			String facetName = Utils.getDisplayName( facet, false );
			
			facetList = Arrays.asList( facet );
			facetNames = Arrays.asList( facetName );
			facetsByName.put( facetName, facet );
			
		} else if (realEntityType instanceof TLActionFacet) {
			NamedEntity basePayload = ((TLActionFacet) realEntityType).getBasePayload();
			
			if (basePayload instanceof TLFacetOwner) {
				this.facetOwner = (TLFacetOwner) basePayload;
			}
			
		} else if (realEntityType instanceof TLFacetOwner) {
			this.facetOwner = (TLFacetOwner) realEntityType;
		}
		
		// Retrieve the facet list based upon the entity type
		if (facetOwner instanceof TLComplexTypeBase) {
			facetList = FacetCodegenUtils.getAvailableFacets( (TLComplexTypeBase) facetOwner );
			
		} else if (facetOwner instanceof TLOperation) {
			facetList = FacetCodegenUtils.getAvailableFacets( (TLOperation) facetOwner );
		}
		
		// If facets were found, add them to the configuration for this selection instance
		if ((facetOwner != null) && (facetList != null) && !facetList.isEmpty()) {
			for (TLFacet facet : facetList) {
				String facetName = Utils.getDisplayName( facet, false );
				
				facetsByName.put( facetName, facet );
				facetNames.add( facetName );
			}
		}
	}
	
	/**
	 * Returns the qualified name of the given entity.
	 * 
	 * @param entity  the entity for which to return a qualified name
	 * @return QName
	 */
	private static QName getQName(NamedEntity entity) {
		return (entity == null) ? null : new QName( entity.getNamespace(), entity.getLocalName() );
	}
	
	/**
	 * Returns true if the given facet is owned by the given owner.
	 * 
	 * @param facet  the facet selection to test
	 * @param owner  the facet owner to be verified
	 * @return boolean
	 */
	private boolean isValidFacetReference(TLFacet facet, TLFacetOwner owner) {
		boolean isValid = (facet.getOwningEntity() == owner);
		
		if (!isValid && (facet instanceof TLContextualFacet)) {
			while (!isValid) {
				TLFacetOwner ctxOwner = facet.getOwningEntity();
				
				if (ctxOwner instanceof TLContextualFacet) {
					facet = (TLContextualFacet) ctxOwner;
				}
				isValid = (facet.getOwningEntity() == owner);
			}
		}
		return isValid;
	}
	
}
