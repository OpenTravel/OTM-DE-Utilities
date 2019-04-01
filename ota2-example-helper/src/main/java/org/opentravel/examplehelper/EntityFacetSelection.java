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

package org.opentravel.examplehelper;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the available facets and the current facet selection for a particular OTM entity type. Only those OTM
 * entity types which allow for substitution group selections will allow for different facet selections.
 */
public class EntityFacetSelection {

    private NamedEntity entityType;
    private TLFacetOwner facetOwner;
    private String selectedFacetName;
    private List<String> facetNames;
    private Map<String,TLFacet> facetsByName = new HashMap<>();
    private FacetSelections owningFacetSelections;

    /**
     * Constructor that specifies the entity type for which facet selections can be made.
     * 
     * @param entityType the OTM entity type
     */
    public EntityFacetSelection(NamedEntity entityType) {
        NamedEntity realEntityType = entityType;
        List<TLFacet> facetList = null;

        this.entityType = entityType;
        this.facetNames = new ArrayList<>();

        if (realEntityType instanceof TLAlias) {
            realEntityType = ((TLAlias) realEntityType).getOwningEntity();
        }

        if (realEntityType instanceof TLContextualFacet) {
            this.facetOwner = (TLFacetOwner) realEntityType;

        } else if (realEntityType instanceof TLFacet) {
            // If the entity is a facet, then it will be the only member of the facet list
            TLFacet facet = (TLFacet) realEntityType;
            String facetName = HelperUtils.getDisplayName( facet, false );

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
        if (facetOwner instanceof TLContextualFacet) {
            facetList = FacetCodegenUtils.getAvailableFacets( (TLContextualFacet) facetOwner );

        } else if (facetOwner instanceof TLComplexTypeBase) {
            facetList = FacetCodegenUtils.getAvailableFacets( (TLComplexTypeBase) facetOwner );

        } else if (facetOwner instanceof TLOperation) {
            facetList = FacetCodegenUtils.getAvailableFacets( (TLOperation) facetOwner );
        }

        // If facets were found, add them to the configuration for this selection instance
        initSelectableFacets( facetList );
    }

    /**
     * Initializes the list of selectable facets from the list provided.
     * 
     * @param facetList the list of facets from which the user can select
     */
    private void initSelectableFacets(List<TLFacet> facetList) {
        if ((facetOwner != null) && (facetList != null) && !facetList.isEmpty()) {
            for (TLFacet facet : facetList) {
                String facetName = HelperUtils.getDisplayName( facet, false );

                if ((facet.getFacetType() == TLFacetType.SUMMARY) || (facet.getFacetType() == TLFacetType.SHARED)) {
                    this.selectedFacetName = facetName;
                }
                facetsByName.put( facetName, facet );
                facetNames.add( facetName );
            }
            if (this.selectedFacetName == null) {
                this.selectedFacetName = facetNames.get( 0 );
            }
        }
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
     * If the 'entityType' field is a <code>FacetOwner</code> and more than a single facet selection is available, this
     * method will return that 'entityType' as the facet owner.
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
     * Returns the facet that is currently selected by the user (null if no selection is available).
     * 
     * @return TLFacet
     */
    public TLFacet getSelectedFacet() {
        return facetsByName.get( selectedFacetName );
    }

    /**
     * Assigns the name of the selected facet.
     *
     * @param facetName the facet name to assign
     */
    public void setSelectedFacet(String facetName) {
        this.selectedFacetName = facetName;

        if (owningFacetSelections != null) {
            owningFacetSelections.notifySelectionChanged( this );
        }
    }

    /**
     * Returns the list of available facet names for this selection.
     * 
     * @return List&lt;String&gt;
     */
    public List<String> getFacetNames() {
        return facetNames;
    }

    /**
     * Returns the facet with the specified name or null if no such facet exists.
     * 
     * @param facetName the name of the facet to return
     * @return TLFacet
     */
    public TLFacet getFacet(String facetName) {
        return facetsByName.get( facetName );
    }

    /**
     * Returns the name of the given facet within this selection instance or null if the given facet is not known to
     * this instance.
     * 
     * @param facet the facet for which to return a name
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
     * Assigns the group of facet selections to which this instance belongs.
     * 
     * @param owningFacetSelections the owning group of facet selections
     */
    protected void setOwningFacetSelections(FacetSelections owningFacetSelections) {
        this.owningFacetSelections = owningFacetSelections;
    }

}
