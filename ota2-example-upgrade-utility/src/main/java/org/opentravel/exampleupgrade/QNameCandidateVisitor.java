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

package org.opentravel.exampleupgrade;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.diff.impl.QNameComparator;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Visitor that constructs the mappings of global element names to matching OTM entities.
 */
public class QNameCandidateVisitor extends ModelElementVisitorAdapter {

    private static final FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory( null );
    private static final QNameComparator qnComparator = new QNameComparator();

    private Set<QName> visitedNames = new HashSet<>();
    private Map<QName,List<OTMObjectChoice>> baseFamilyMatches = new HashMap<>();
    private Map<String,List<OTMObjectChoice>> allElementsByBaseNS = new HashMap<>();

    /**
     * Returns the map entities that are assigned to the same base namespace and are within the same base name family
     * (i.e. substitution group).
     *
     * @return Map&lt;QName,List&lt;OTMObjectChoice&gt;&gt;
     */
    public Map<QName,List<OTMObjectChoice>> getFamilyMatches() {
        Map<QName,List<OTMObjectChoice>> familyMatches = new HashMap<>();

        for (Entry<QName,List<OTMObjectChoice>> entry : baseFamilyMatches.entrySet()) {
            List<OTMObjectChoice> familyChoices = entry.getValue();

            Collections.sort( familyChoices,
                (w1, w2) -> qnComparator.compare( w1.getOtmObjectName(), w2.getOtmObjectName() ) );

            for (OTMObjectChoice familyChoice : familyChoices) {
                QName choiceName = familyChoice.getOtmObjectName();
                String choiceBaseNS = HelperUtils.getBaseNamespace( choiceName.getNamespaceURI() );
                QName choiceBaseName = new QName( choiceBaseNS, choiceName.getLocalPart() );

                familyMatches.put( choiceBaseName, familyChoices );
            }
        }
        return familyMatches;
    }

    /**
     * Returns the map of all elements by base namespace.
     *
     * @return Map&lt;String,List&lt;OTMObjectChoice&gt;&gt;
     */
    public Map<String,List<OTMObjectChoice>> getAllElementsByBaseNS() {
        allElementsByBaseNS.values().forEach( list -> Collections.sort( list,
            (w1, w2) -> qnComparator.compare( w1.getOtmObjectName(), w2.getOtmObjectName() ) ) );
        return allElementsByBaseNS;
    }

    /**
     * Processes the named entity by creating entries in the appropriate maps being constructed by this visitor.
     * 
     * @param entity the OTM entity to be processed
     * @param entityName the qualified name of the entity's XML element
     * @param baseObjectLocalName the local name of the base object that owns the given entity
     */
    private void processEntity(NamedEntity entity, QName entityName, String baseObjectLocalName) {
        if ((entityName != null) && !visitedNames.contains( entityName )) {
            String baseNS = ((TLLibrary) entity.getOwningLibrary()).getBaseNamespace();
            QName baseNSName = new QName( baseNS, baseObjectLocalName );
            OTMObjectChoice objectChoice = new OTMObjectChoice( entity, entityName );

            baseFamilyMatches.computeIfAbsent( baseNSName,
                nsName -> baseFamilyMatches.put( nsName, new ArrayList<>() ) );
            List<OTMObjectChoice> baseNSMatchList = baseFamilyMatches.get( baseNSName );

            allElementsByBaseNS.computeIfAbsent( baseNS, ns -> allElementsByBaseNS.put( ns, new ArrayList<>() ) );
            List<OTMObjectChoice> allElementsBaseNSList = allElementsByBaseNS.get( baseNS );

            baseNSMatchList.add( objectChoice );
            allElementsBaseNSList.add( objectChoice );
            visitedNames.add( entityName );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public boolean visitFacet(TLFacet facet) {
        if (facetDelegateFactory.getDelegate( facet ).hasContent()) {
            TLFacetOwner facetOwner = facet.getOwningEntity();

            processEntity( facet, XsdCodegenUtils.getGlobalElementName( facet ), facetOwner.getLocalName() );
            processEntity( facet, XsdCodegenUtils.getSubstitutableElementName( facet ), facetOwner.getLocalName() );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
     */
    @Override
    public boolean visitContextualFacet(TLContextualFacet facet) {
        if (facetDelegateFactory.getDelegate( facet ).hasContent()) {
            QName nonsubElementName = XsdCodegenUtils.getGlobalElementName( facet );
            QName subElementName = XsdCodegenUtils.getSubstitutableElementName( facet );
            TLFacetOwner facetOwner = facet.getOwningEntity();
            TLFacetType facetType = facet.getFacetType();

            if ((facetType == TLFacetType.QUERY) || (facetType == TLFacetType.UPDATE)) {
                if (facetOwner instanceof TLContextualFacet) {
                    while (((TLContextualFacet) facetOwner).getOwningEntity() instanceof TLContextualFacet) {
                        facetOwner = ((TLContextualFacet) facetOwner).getOwningEntity();
                    }
                } else {
                    facetOwner = facet;
                }

            } else {
                facetOwner = FacetCodegenUtils.getTopLevelOwner( facet );
            }

            processEntity( facet, nonsubElementName, facetOwner.getLocalName() );
            processEntity( facet, subElementName, facetOwner.getLocalName() );
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public boolean visitAlias(TLAlias alias) {
        if (alias.getOwningEntity() instanceof TLFacet) {
            QName nonsubElementName = XsdCodegenUtils.getGlobalElementName( alias );
            QName subElementName = XsdCodegenUtils.getSubstitutableElementName( alias );
            TLAlias ownerAlias = findOwnerAlias( alias );

            processEntity( alias, nonsubElementName, ownerAlias.getLocalName() );
            processEntity( alias, subElementName, ownerAlias.getLocalName() );
        }
        return true;
    }

    /**
     * Returns the owning alias of the given alias.
     * 
     * @param alias the alias for which to return the owner
     * @return TLAlias
     */
    private TLAlias findOwnerAlias(TLAlias alias) {
        TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias( alias );
        TLFacetType facetType = ((TLFacet) alias.getOwningEntity()).getFacetType();

        if ((facetType == TLFacetType.QUERY) || (facetType == TLFacetType.UPDATE)
            || (facetType == TLFacetType.CHOICE)) {
            if (ownerAlias.getOwningEntity() instanceof TLContextualFacet) {
                TLAlias nextOwner = AliasCodegenUtils.getOwnerAlias( alias );

                while ((nextOwner != null) && (nextOwner.getOwningEntity() instanceof TLContextualFacet)) {
                    ownerAlias = nextOwner;
                    nextOwner = AliasCodegenUtils.getOwnerAlias( ownerAlias );
                }
            } else {
                ownerAlias = alias;
            }

        } else {
            while (ownerAlias.getOwningEntity() instanceof TLFacet) {
                ownerAlias = AliasCodegenUtils.getOwnerAlias( alias );
            }
        }
        return ownerAlias;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
     */
    @Override
    public boolean visitActionFacet(TLActionFacet facet) {
        NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( facet );

        if ((payloadType instanceof TLActionFacet) && !ResourceCodegenUtils.isTemplateActionFacet( facet )) {
            QName facetName = XsdCodegenUtils.getGlobalElementName( facet );
            processEntity( facet, facetName, facetName.getLocalPart() );
        }
        return true;
    }

}
