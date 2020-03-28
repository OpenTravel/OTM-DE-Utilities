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

import org.opentravel.application.common.OtmApplicationRuntimeException;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleValueGenerator;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utility class that handles the construction of the upgraded DOM tree structure by comparing items from the OTM model
 * and attempting to match them with the original DOM example tree.
 */
public class UpgradeTreeBuilder {

    private static final Logger log = LoggerFactory.getLogger( UpgradeTreeBuilder.class );

    private Document upgradeDocument;
    private String defaultNamespace;
    private Map<String,String> namespaceMappings;
    private ExampleValueGenerator exampleValueGenerator;
    private ExampleGeneratorOptions exampleOptions;
    private Deque<UpgradeNodeContext> elementStack = new ArrayDeque<>();

    /**
     * Constructor that specifies the options to use when generating unmatched sections of the upgraded example tree.
     * 
     * @param exampleOptions the example options to use when generating new content
     */
    public UpgradeTreeBuilder(ExampleGeneratorOptions exampleOptions) {
        this( newDocument(), exampleOptions );
    }

    /**
     * Constructor that specifies the options to use when generating unmatched sections of the upgraded example tree.
     * 
     * @param upgradeDocument the upgraded example document
     * @param exampleOptions the example options to use when generating new content
     */
    public UpgradeTreeBuilder(Document upgradeDocument, ExampleGeneratorOptions exampleOptions) {
        this.upgradeDocument = upgradeDocument;
        this.exampleValueGenerator = ExampleValueGenerator.getInstance( null );
        this.exampleOptions = exampleOptions;
    }

    /**
     * Constructs the upgraded DOM tree along with the <code>TreeItem</code> structure that will be displayed in the
     * visual interface.
     * 
     * @param otmEntity the OTM entity for which to construct the upgraded DOM tree
     * @param originalRoot the root element of the original DOM tree
     * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
     */
    public TreeItem<DOMTreeUpgradeNode> buildUpgradeDOMTree(NamedEntity otmEntity, Element originalRoot) {
        ExampleMatchType matchType = getMatchType( otmEntity, originalRoot );

        initNamespaceMappings( otmEntity );
        clearReferenceFlags( originalRoot );

        if (ExampleMatchType.isMatch( matchType )) {
            markReferenced( originalRoot );
        }

        Element rootElement = createDomElement( otmEntity );
        DOMTreeUpgradeNode node = new DOMTreeUpgradeNode( otmEntity, otmEntity, rootElement, matchType );
        TreeItem<DOMTreeUpgradeNode> treeItem = new TreeItem<>( node );
        UpgradeModelVisitor visitor = new UMVisitor();
        UpgradeModelNavigator navigator =
            new UpgradeModelNavigator( visitor, otmEntity.getOwningModel(), exampleOptions );

        upgradeDocument.appendChild( rootElement );
        treeItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
        elementStack.push(
            new UpgradeNodeContext( treeItem, originalRoot, otmEntity, !ExampleMatchType.isMatch( matchType ) ) );
        navigator.navigate( otmEntity );
        configureNamespaceDeclarations();

        return treeItem;
    }

    /**
     * Replaces the given original tree item with new content by building a branch from the original DOM node provided.
     * 
     * @param origTreeItem the upgrade tree item to be replaced
     * @param originalNode the original DOM tree node from which to build new upgrade content
     * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
     * @throws ExampleUpgradeException thrown if an error occurs during the example generation process
     */
    public TreeItem<DOMTreeUpgradeNode> replaceUpgradeDOMBranch(TreeItem<DOMTreeUpgradeNode> origTreeItem,
        Node originalNode) throws ExampleUpgradeException {
        TreeItem<DOMTreeUpgradeNode> rootTreeItem = origTreeItem;
        TreeItem<DOMTreeUpgradeNode> parentTreeItem = origTreeItem.getParent();
        TreeItem<DOMTreeUpgradeNode> upgradeTreeItem;

        while (rootTreeItem.getParent() != null) {
            rootTreeItem = rootTreeItem.getParent();
        }
        initNamespaceMappings( rootTreeItem.getValue().getOtmEntity() );

        if (origTreeItem.getValue().getOtmEntity() != null) { // Complex OTM type
            upgradeTreeItem = upgradeComplexTypeItem( origTreeItem, originalNode );

        } else { // Simple OTM type (or complex with simple content)
            upgradeTreeItem = upgradeSimpleTypeItem( origTreeItem, originalNode );
        }

        // If this is not the root, we need to replace the old original tree item (and
        // DOM node) with the one we just generated.
        if (parentTreeItem != null) {
            replaceTreeItem( origTreeItem, parentTreeItem, upgradeTreeItem );
        }
        configureNamespaceDeclarations();
        markReferenced( originalNode );

        return upgradeTreeItem;
    }

    /**
     * Upgrades the content of a tree item associated with an OTM complex type.
     * 
     * @param origTreeItem the tree item that contains the original value
     * @param originalNode the DOM node that represents the original value
     * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
     * @throws ExampleUpgradeException thrown if an error occurs during the upgrade processing
     */
    private TreeItem<DOMTreeUpgradeNode> upgradeComplexTypeItem(TreeItem<DOMTreeUpgradeNode> origTreeItem,
        Node originalNode) throws ExampleUpgradeException {
        NamedEntity declaredType = origTreeItem.getValue().getDeclaredEntity();
        NamedEntity elementType = origTreeItem.getValue().getOtmEntity();
        boolean isAutoGen = (originalNode == null);
        TreeItem<DOMTreeUpgradeNode> upgradeTreeItem;

        // If the OTM entity is the root of a substitution group, use the DOM element to
        // resolve to the correct facet.
        if (declaredType != null) {
            elementType = resolveElementFacet( originalNode, declaredType, elementType, isAutoGen );
        }

        if (isAutoGen || (originalNode instanceof Element)) {
            Element upgradeElement = createDomElement( elementType );
            ExampleMatchType matchType =
                isAutoGen ? ExampleMatchType.NONE : getMatchType( elementType, (Element) originalNode );
            DOMTreeUpgradeNode node = new DOMTreeUpgradeNode( elementType, declaredType, upgradeElement, matchType );
            UpgradeModelVisitor visitor = new UMVisitor();
            UpgradeModelNavigator navigator =
                new UpgradeModelNavigator( visitor, elementType.getOwningModel(), exampleOptions );

            upgradeTreeItem = new TreeItem<>( node );
            upgradeTreeItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
            elementStack
                .push( new UpgradeNodeContext( upgradeTreeItem, (Element) originalNode, elementType, isAutoGen ) );
            navigator.navigate( elementType );

            if (!isAutoGen) {
                node.setMatchType( ExampleMatchType.MANUAL );
            }

        } else {
            throw new ExampleUpgradeException(
                "Cannot process complex content using an attribute from the original document" );
        }
        return upgradeTreeItem;
    }

    /**
     * Use the DOM element to resolve to the correct facet to use when upgrading the tree item.
     * 
     * @param originalNode the DOM node that represents the original value
     * @param declaredType the entity type originally declared in the owning OTM element
     * @param elementType the OTM complex entity type associated with this node (may be different than the declared
     *        type)
     * @param autoGen flag indicating whether the DOM node content was auto-generated
     * @return NamedEntity
     */
    private NamedEntity resolveElementFacet(Node originalNode, NamedEntity declaredType, NamedEntity elementType,
        boolean isAutoGen) {
        if (isAutoGen) {
            elementType = getPreferredFacet( declaredType );

        } else {
            List<NamedEntity> candidateTypes = new ArrayList<>();

            if (declaredType instanceof TLComplexTypeBase) {
                candidateTypes.addAll( FacetCodegenUtils.getAvailableFacets( (TLComplexTypeBase) declaredType ) );

            } else if (isFacetOwnerAlias( declaredType )) {
                candidateTypes.addAll( FacetCodegenUtils.getAvailableFacetAliases( (TLAlias) declaredType ) );

            } else {
                candidateTypes.add( declaredType );
            }

            for (NamedEntity candidateType : candidateTypes) {
                if (ExampleMatchType.isMatch( getMatchType( candidateType, (Element) originalNode ) )) {
                    elementType = candidateType;
                    break;
                }
            }
        }
        return elementType;
    }

    /**
     * Upgrades the content of a tree item associated with an OTM simple type (or a complex with simple content).
     * 
     * @param origTreeItem the tree item that contains the original value
     * @param originalNode the DOM node that represents the original value
     * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
     * @throws ExampleUpgradeException thrown if an error occurs during the upgrade processing
     */
    private TreeItem<DOMTreeUpgradeNode> upgradeSimpleTypeItem(TreeItem<DOMTreeUpgradeNode> origTreeItem,
        Node originalNode) throws ExampleUpgradeException {
        TLMemberField<?> otmField = origTreeItem.getValue().getOtmField();
        NamedEntity otmEntity = (NamedEntity) otmField.getOwner();
        Element dummyElement = createDummyElement( otmEntity );
        DOMTreeUpgradeNode dummyNode =
            new DOMTreeUpgradeNode( otmEntity, otmEntity, dummyElement, ExampleMatchType.MANUAL );
        TreeItem<DOMTreeUpgradeNode> dummyItem = new TreeItem<>( dummyNode );
        UpgradeModelVisitor visitor = new UMVisitor();
        UpgradeModelNavigator navigator =
            new UpgradeModelNavigator( visitor, otmField.getOwningModel(), exampleOptions );
        Element parentElement = (originalNode == null) ? null : (Element) originalNode.getParentNode();
        TreeItem<DOMTreeUpgradeNode> upgradeTreeItem;

        elementStack.push( new UpgradeNodeContext( dummyItem, parentElement, otmEntity, true ) );

        // Assign the manual value from the original node
        if (originalNode instanceof Attr) {
            elementStack.peek().setManualValue( ((Attr) originalNode).getValue() );

        } else if (originalNode != null) {
            elementStack.peek().setManualValue( HelperUtils.getElementTextValue( (Element) originalNode ) );
        }

        if (otmField instanceof TLAttribute) {
            navigator.navigateAttribute( (TLAttribute) otmField );

        } else if (otmField instanceof TLIndicator) {
            navigator.navigateIndicator( (TLIndicator) otmField );

        } else { // must be an element
            navigator.navigateElement( (TLProperty) otmField );
        }

        if (!dummyItem.getChildren().isEmpty()) {
            upgradeTreeItem = dummyItem.getChildren().get( 0 );
            upgradeTreeItem.getValue().setMatchType( ExampleMatchType.MANUAL );

        } else {
            throw new ExampleUpgradeException(
                "An unknown error occurred while attempting to process content from original document." );
        }
        return upgradeTreeItem;
    }

    /**
     * Replaces the original tree item with the upgraded one that was generated.
     * 
     * @param origTreeItem the tree item to be replaced
     * @param parentTreeItem the parent tree item under which the replacement will be performed
     * @param upgradeTreeItem the tree item that should replace the original
     */
    private void replaceTreeItem(TreeItem<DOMTreeUpgradeNode> origTreeItem, TreeItem<DOMTreeUpgradeNode> parentTreeItem,
        TreeItem<DOMTreeUpgradeNode> upgradeTreeItem) {
        Node origNode = origTreeItem.getValue().getDomNode();
        Node parentNode = parentTreeItem.getValue().getDomNode();
        Node upgradeNode = upgradeTreeItem.getValue().getDomNode();
        int childTreeIdx = parentTreeItem.getChildren().indexOf( origTreeItem );

        parentTreeItem.getChildren().remove( origTreeItem );
        parentTreeItem.getChildren().add( childTreeIdx, upgradeTreeItem );

        if (upgradeNode instanceof Element) {
            if (origNode.getParentNode() == parentNode) { // Simple replacement of the old node
                parentNode.replaceChild( upgradeNode, origNode );

            } else { // New DOM node for the document
                // In this case, we need to insert the node into the proper position within
                // its parent.
                Node nextSiblingNode = null;

                for (int i = childTreeIdx + 1; i < parentTreeItem.getChildren().size(); i++) {
                    TreeItem<DOMTreeUpgradeNode> siblingItem = parentTreeItem.getChildren().get( i );

                    if (siblingItem.getValue().getDomNode().getParentNode() != null) {
                        nextSiblingNode = siblingItem.getValue().getDomNode();
                        break;
                    }
                }
                parentNode.insertBefore( upgradeNode, nextSiblingNode );
            }

        } else { // must be an attribute node
            ((Element) parentNode).setAttribute( upgradeNode.getNodeName(), upgradeNode.getNodeValue() );
        }
    }

    /**
     * Replaces the given original tree item with new content by building a branch from the original DOM node provided.
     * 
     * @param treeItem the upgrade tree item to be replaced
     * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
     */
    public TreeItem<DOMTreeUpgradeNode> clearUpgradeDOMBranch(TreeItem<DOMTreeUpgradeNode> treeItem) {
        TreeItem<DOMTreeUpgradeNode> parentTreeItem = treeItem.getParent();
        TreeItem<DOMTreeUpgradeNode> newTreeItem;
        Node origDomNode = treeItem.getValue().getDomNode();
        DOMTreeUpgradeNode newUpgradeNode;
        Node newDomNode;
        Image nodeIcon;

        // Build the placeholder DOM node and remove the original node from its parent
        if (origDomNode instanceof Element) {
            newDomNode = upgradeDocument.createElementNS( origDomNode.getNamespaceURI(), origDomNode.getNodeName() );
            nodeIcon = AbstractDOMTreeNode.elementIcon;
        } else {
            newDomNode = upgradeDocument.createAttribute( origDomNode.getNodeName() );
            nodeIcon = AbstractDOMTreeNode.attributeIcon;
        }
        if (origDomNode.getParentNode() != null) {
            origDomNode.getParentNode().removeChild( origDomNode );
        }

        // Create the new tree item and replace it in the original tree
        int childIndex = parentTreeItem.getChildren().indexOf( treeItem );

        if (treeItem.getValue().getOtmEntity() != null) {
            newUpgradeNode = new DOMTreeUpgradeNode( treeItem.getValue().getOtmEntity(),
                treeItem.getValue().getDeclaredEntity(), newDomNode, ExampleMatchType.MISSING );
        } else {
            newUpgradeNode =
                new DOMTreeUpgradeNode( treeItem.getValue().getOtmField(), newDomNode, ExampleMatchType.MISSING );
        }
        newTreeItem = new TreeItem<>( newUpgradeNode );
        newTreeItem.setGraphic( new ImageView( nodeIcon ) );
        parentTreeItem.getChildren().add( childIndex, newTreeItem );
        parentTreeItem.getChildren().remove( treeItem );

        return newTreeItem;
    }

    /**
     * Returns the type of match between the given OTM entity and the DOM element provided.
     * 
     * @param otmEntity the OTM entity with which to compare the DOM element
     * @param domElement the DOM element to be compared
     * @return ExampleMatchType
     */
    private ExampleMatchType getMatchType(NamedEntity otmEntity, Element domElement) {
        QName entityName = XsdCodegenUtils.getGlobalElementName( otmEntity );
        ExampleMatchType matchType = ExampleMatchType.NONE;

        if ((entityName != null) && (domElement != null)) {
            // First, attempt to match against the non-substitutable element name
            if (entityName.getLocalPart().equals( domElement.getLocalName() )) {
                matchType = getMatchType( entityName.getNamespaceURI(), domElement.getNamespaceURI() );
            }

            // If no match was found yet, check the non-substitutable element name (if one exists for the entity)
            if (!ExampleMatchType.isMatch( matchType )) {
                QName substitutableName = getSubstitutableElementName( otmEntity );

                if ((substitutableName != null)
                    && (substitutableName.getLocalPart().equals( domElement.getLocalName() ))) {
                    matchType = getMatchType( substitutableName.getNamespaceURI(), domElement.getNamespaceURI() );

                    if (matchType == ExampleMatchType.EXACT) {
                        matchType = ExampleMatchType.EXACT_SUBSTITUTABLE;

                    } else if (matchType == ExampleMatchType.PARTIAL) {
                        matchType = ExampleMatchType.PARTIAL_SUBSTITUTABLE;
                    }
                }
            }
        }
        return matchType;
    }

    /**
     * Returns the match type between the two namespace URI's provided. A partial match will result if the two base
     * namespaces are the same, but versions are different.
     * 
     * @param ns1 the first namespace URI
     * @param ns2 the second namespace URI
     * @return ExampleMatchType
     */
    private ExampleMatchType getMatchType(String ns1, String ns2) {
        ExampleMatchType matchType = ExampleMatchType.NONE;

        if (ns1.equals( ns2 )) {
            matchType = ExampleMatchType.EXACT;

        } else {
            String baseNS1 = HelperUtils.getBaseNamespace( ns1 );
            String baseNS2 = HelperUtils.getBaseNamespace( ns2 );

            if (baseNS1.equals( baseNS2 )) {
                matchType = ExampleMatchType.PARTIAL;
            }
        }
        return matchType;
    }

    /**
     * Returns the type of match between the given OTM entity and the DOM element provided.
     * 
     * @param otmEntity the OTM entity with which to compare the DOM element
     * @param domElement the DOM element to be compared
     * @return ExampleMatchType
     */
    private ExampleMatchType getElementMatchType(Element originalElement, Element upgradeElement) {
        QName entityName = new QName( upgradeElement.getNamespaceURI(), upgradeElement.getLocalName() );
        ExampleMatchType matchType = ExampleMatchType.NONE;

        if (entityName.getLocalPart().equals( originalElement.getLocalName() )) {
            matchType = getMatchType( entityName.getNamespaceURI(), originalElement.getNamespaceURI() );
        }
        return matchType;
    }

    /**
     * Creates a new DOM element for the given entity.
     * 
     * @param entity the OTM entity for which to create a DOM element
     * @return Element
     */
    private Element createDomElement(NamedEntity entity) {
        QName entityName = getSubstitutableElementName( entity );
        Element domElement;

        if (entityName == null) {
            entityName = XsdCodegenUtils.getGlobalElementName( entity );
        }

        if (entityName != null) {
            domElement = upgradeDocument.createElementNS( entityName.getNamespaceURI(),
                qualifiedElementName( entityName.getNamespaceURI(), entityName.getLocalPart() ) );

        } else {
            throw new IllegalArgumentException( "The OTM entity does not define a global XML element." );
        }
        return domElement;
    }

    /**
     * Creates a DOM element for the given entity or a dummy element if the OTM entity is null or invalid.
     * 
     * @param entity the OTM entity for which to create a dummy element
     * @return Element
     */
    private Element createDummyElement(NamedEntity entity) {
        Element dummyElement;
        try {
            dummyElement = createDomElement( entity );

        } catch (Exception e) {
            dummyElement = upgradeDocument.createElementNS( "http://otm.dummy.com", "Dummy" );
        }
        return dummyElement;
    }

    /**
     * Returns the substitutable element name for the entity or null if one does not exist.
     * 
     * @param otmEntity the entity for which to return the substitutable element name
     * @return QName
     */
    private QName getSubstitutableElementName(NamedEntity otmEntity) {
        QName substitutableName = null;

        if (otmEntity instanceof TLFacet) {
            substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) otmEntity );

        } else if (isFacetAlias( otmEntity )) {
            substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) otmEntity );
        }
        return substitutableName;
    }

    /**
     * Returns true if the given OTM entity is an alias of a <code>TLFacet</code> object.
     * 
     * @param otmEntity the OTM entity to check
     * @return boolean
     */
    private boolean isFacetAlias(NamedEntity otmEntity) {
        return (otmEntity instanceof TLAlias) && (((TLAlias) otmEntity).getOwningEntity() instanceof TLFacet);
    }

    /**
     * Returns true if the given OTM entity is an alias of a <code>TLComplexTypeBase</code> object.
     * 
     * @param otmEntity the OTM entity to check
     * @return boolean
     */
    private boolean isFacetOwnerAlias(NamedEntity otmEntity) {
        return (otmEntity instanceof TLAlias) && (((TLAlias) otmEntity).getOwningEntity() instanceof TLComplexTypeBase);
    }

    /**
     * For an OTM parent (facet owner), the "preferred" or default facet will be returned.
     * 
     * @param otmParentEntity the OTM parent entity that owns the facet to be returned
     * @return NamedEntity
     */
    private NamedEntity getPreferredFacet(NamedEntity otmParentEntity) {
        TLComplexTypeBase owner = null;
        TLAlias ownerAlias = null;
        TLFacet preferredFacet;

        if (otmParentEntity instanceof TLAlias) {
            ownerAlias = (TLAlias) otmParentEntity;
            otmParentEntity = ownerAlias.getOwningEntity();
        }
        if (otmParentEntity instanceof TLComplexTypeBase) {
            owner = (TLComplexTypeBase) otmParentEntity;
        } else {
            throw new IllegalArgumentException( "Entity must be an OTM complex type or an alias of one." );
        }

        preferredFacet = exampleOptions.getPreferredFacet( (TLFacetOwner) owner );

        // Apply default lookups if no preferred facet selection is specified by the user
        if (preferredFacet == null) {
            if (owner instanceof TLBusinessObject) {
                preferredFacet = ((TLBusinessObject) owner).getSummaryFacet();

            } else if (owner instanceof TLChoiceObject) {
                TLChoiceObject choice = (TLChoiceObject) owner;

                preferredFacet =
                    choice.getChoiceFacets().isEmpty() ? choice.getSharedFacet() : choice.getChoiceFacets().get( 0 );

            } else if (owner instanceof TLCoreObject) {
                preferredFacet = ((TLCoreObject) owner).getSummaryFacet();

            } else {
                throw new IllegalArgumentException( "Unknown complex type: " + owner.getClass().getSimpleName() );
            }
        }

        if (ownerAlias != null) {
            return AliasCodegenUtils.getFacetAlias( ownerAlias, preferredFacet.getFacetType() );

        } else {
            return preferredFacet;
        }
    }

    /**
     * Initializes the namespace mappings that will be used throughout the upgraded XML document.
     * 
     * @param otmEntity the OTM entity that will serve as the root of the XML document
     */
    private void initNamespaceMappings(NamedEntity otmEntity) {
        TLModel model = otmEntity.getOwningModel();

        this.defaultNamespace = otmEntity.getNamespace();
        this.namespaceMappings = new HashMap<>();

        for (AbstractLibrary library : model.getAllLibraries()) {
            String ns = library.getNamespace();

            if (!namespaceMappings.containsKey( ns )) {
                String prefix = library.getPrefix();
                int count = 0;

                // Make sure this prefix is unique to the entire model
                while (namespaceMappings.containsValue( prefix )) {
                    prefix = library.getPrefix() + (++count);
                }
                namespaceMappings.put( ns, prefix );
            }
        }
    }

    /**
     * Returns the qualified name of a DOM element with the given namespace and local name.
     * 
     * @param namespace the namespace URI of the element
     * @param localName the local name of the element
     * @return String
     */
    private String qualifiedElementName(String namespace, String localName) {
        String prefix = defaultNamespace.equals( namespace ) ? null : namespaceMappings.get( namespace );
        String qName;

        if (prefix != null) {
            qName = prefix + ":" + localName;
        } else {
            qName = localName;
        }
        return qName;
    }

    /**
     * Adds the necessary namespace declarations to the root element of the upgrade DOM document tree.
     */
    private void configureNamespaceDeclarations() {
        Element rootElement = upgradeDocument.getDocumentElement();
        Set<String> usedNamespaces = new HashSet<>();

        // Start by deleting all existing namespace declarations
        NamedNodeMap attrs = rootElement.getAttributes();
        Set<String> existingAttrs = new HashSet<>();
        int attrCount = attrs.getLength();

        for (int i = 0; i < attrCount; i++) {
            Attr domAttr = (Attr) attrs.item( i );

            if (domAttr.getNodeName().equals( "xmlns" ) || domAttr.getNodeName().contains( ":" )) {
                existingAttrs.add( domAttr.getNodeName() );
            }
        }
        for (String nsAttr : existingAttrs) {
            rootElement.removeAttribute( nsAttr );
        }

        // Now create namespace declarations for every namespace used in the document
        findNamespaceURIs( rootElement, usedNamespaces );

        for (String ns : usedNamespaces) {
            if (!ns.equals( defaultNamespace )) {
                rootElement.setAttribute( "xmlns:" + namespaceMappings.get( ns ), ns );
            }
        }
    }

    /**
     * Recursively searches the given element, recording all namespace URI's that are encountered.
     * 
     * @param element the DOM element to search
     * @param usedNamespaces the list of namespace URI's being assembled
     */
    private void findNamespaceURIs(Element element, Set<String> usedNamespaces) {
        Node domChild = element.getFirstChild();
        String ns = element.getNamespaceURI();

        if (ns != null) {
            usedNamespaces.add( ns );
        }

        while (domChild != null) {
            if (domChild.getNodeType() == Node.ELEMENT_NODE) {
                findNamespaceURIs( (Element) domChild, usedNamespaces );
            }
            domChild = domChild.getNextSibling();
        }
    }

    /**
     * Clears all of the 'isReferenced' flags from the elements and attributes of the original DOM tree.
     * 
     * @param originalDomNode the root of the DOM tree for which to clear all flags
     */
    private void clearReferenceFlags(Node originalDomNode) {
        if (originalDomNode != null) {
            Node domChild = originalDomNode.getFirstChild();

            originalDomNode.setUserData( DOMTreeOriginalNode.IS_REFERENCED_KEY, null, null );

            while (domChild != null) {
                if ((domChild.getNodeType() == Node.ELEMENT_NODE) || (domChild.getNodeType() == Node.ATTRIBUTE_NODE)) {
                    clearReferenceFlags( domChild );
                }
                domChild = domChild.getNextSibling();
            }
        }
    }

    /**
     * Marks the given original DOM node as referenced by the upgrade tree.
     * 
     * @param originalDomNode the original DOM node to mark as referenced
     */
    private void markReferenced(Node originalDomNode) {
        if (originalDomNode != null) {
            originalDomNode.setUserData( DOMTreeOriginalNode.IS_REFERENCED_KEY, Boolean.TRUE, null );
        }
    }

    /**
     * Returns a new DOM document instance.
     * 
     * @return Document
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        } catch (ParserConfigurationException e) {
            throw new OtmApplicationRuntimeException( "Unable to create DOM document instance.", e );
        }
    }

    /**
     * Context that captures the pairing between the upgrade tree node and the original DOM element (if any).
     */
    private class UpgradeNodeContext {

        private TreeItem<DOMTreeUpgradeNode> upgradeItem;
        private Element originalElement;
        private Node nextOriginalChild;
        private NamedEntity otmElementType;
        private boolean autogenNode = false;
        private String manualValue;

        /**
         * Full constructor.
         * 
         * @param upgradeItem the tree item for the upgrade tree
         * @param originalElement the original DOM element from which the upgrade item was created (may be null)
         * @param otmElementType the actual type of the OTM element
         * @param autoGenNode flag value indicating whethe the node's context was created from an auto-generated example
         */
        public UpgradeNodeContext(TreeItem<DOMTreeUpgradeNode> upgradeItem, Element originalElement,
            NamedEntity otmElementType, boolean autoGenNode) {
            this.upgradeItem = upgradeItem;
            this.originalElement = originalElement;
            this.otmElementType = otmElementType;
            this.autogenNode = autoGenNode;
            this.nextOriginalChild = (originalElement == null) ? null : originalElement.getFirstChild();

            while ((nextOriginalChild != null) && (nextOriginalChild.getNodeType() != Node.ELEMENT_NODE)) {
                nextOriginalChild = nextOriginalChild.getNextSibling();
            }
        }

        /**
         * Returns the tree item for the upgrade tree.
         *
         * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
         */
        public TreeItem<DOMTreeUpgradeNode> getUpgradeItem() {
            return upgradeItem;
        }

        /**
         * Returns the original DOM element from which the upgrade item was created (may be null).
         *
         * @return Element
         */
        public Element getOriginalElement() {
            return originalElement;
        }

        /**
         * Returns the actual type of the OTM element (may be different than the model element due to substitution group
         * selections in the original DOM document).
         *
         * @return NamedEntity
         */
        public NamedEntity getOtmElementType() {
            return otmElementType;
        }

        /**
         * Returns true if the node's context was created from an auto-generated example.
         *
         * @return boolean
         */
        public boolean isAutogenNode() {
            return autogenNode;
        }

        /**
         * Returns the manually-assigned value for the next attribute or simple element.
         *
         * @return String
         */
        public String getManualValue() {
            return manualValue;
        }

        /**
         * Assigns the manually-assigned value for the next attribute or simple element.
         *
         * @param manualValue the element/attribute value to assign
         */
        public void setManualValue(String manualValue) {
            this.manualValue = manualValue;
        }

        /**
         * Returns the next DOM child element under the original child element and advances to the subsequent child.
         *
         * @return Element
         */
        public Element nextOriginalChild() {
            Element nextChild = (Element) nextOriginalChild;
            advanceToNextOriginalChild();

            return nextChild;
        }

        /**
         * Returns the next DOM child element under the original child element without advancing to the subsequent
         * child.
         *
         * @return Element
         */
        public Element peekNextOriginalChild() {
            return (Element) nextOriginalChild;
        }

        /**
         * Returns the next original child in the list which matches (either partial or exact) the upgraded element
         * provided. If a matching element is found, it will be assigned as the next-original-child.
         * 
         * @param upgradeElement the upgraded element for which to return a matching child
         * @return Element
         */
        public Element findNextOriginalChild(Element upgradeElement) {
            List<Element> elementList = getElementSiblings( peekNextOriginalChild() );
            Element foundElement = null;

            for (Element nextChild : elementList) {
                if (ExampleMatchType.isMatch( getElementMatchType( nextChild, upgradeElement ) )) {
                    nextOriginalChild = foundElement = nextChild;
                    break;
                }
            }
            return foundElement;
        }

        /**
         * Returns the next original child in the list which matches (either partial or exact) the OTM entity provided.
         * If a matching element is found, it will be assigned as the next-original-child.
         * 
         * @param upgradeElement the upgraded element for which to return a matching child
         * @return Element
         */
        public Element findNextOriginalChild(NamedEntity otmEntity) {
            List<Element> elementList = getElementSiblings( peekNextOriginalChild() );
            Element foundElement = null;

            for (Element nextChild : elementList) {
                if (ExampleMatchType.isMatch( getMatchType( otmEntity, nextChild ) )) {
                    nextOriginalChild = foundElement = nextChild;
                    break;
                }
            }
            return foundElement;
        }

        /**
         * Returns a list of all sibling elements (including the one provided). The given element is guranteed to be the
         * first element in the list followed by all subsequent sibling. Any prior sibling elements, will be appended to
         * the end of the resulting list.
         * 
         * @param startingElement the starting element in the list of siblings
         * @return List&lt;Element&gt;
         */
        private List<Element> getElementSiblings(Element startingElement) {
            List<Element> siblingList = new ArrayList<>();
            Node parentNode = (startingElement == null) ? null : startingElement.getParentNode();
            Node currentNode = startingElement;

            while (currentNode != null) {
                if (currentNode instanceof Element) {
                    siblingList.add( (Element) currentNode );
                }
                currentNode = currentNode.getNextSibling();
            }
            currentNode = (parentNode == null) ? null : parentNode.getFirstChild();

            while ((currentNode != null) && (currentNode != startingElement)) {
                if (currentNode instanceof Element) {
                    siblingList.add( (Element) currentNode );
                }
                currentNode = currentNode.getNextSibling();
            }
            return siblingList;
        }

        /**
         * Advances to the next DOM element child of the original DOM element. If the end of the child list has been
         * reached, this method will return null.
         */
        private void advanceToNextOriginalChild() {
            while (nextOriginalChild != null) {
                nextOriginalChild = nextOriginalChild.getNextSibling();

                if ((nextOriginalChild != null) && (nextOriginalChild.getNodeType() == Node.ELEMENT_NODE)) {
                    break;
                }
            }
            if (nextOriginalChild == null) {
                nextOriginalChild = (originalElement == null) ? null : originalElement.getFirstChild();

                while ((nextOriginalChild != null) && (nextOriginalChild.getNodeType() != Node.ELEMENT_NODE)) {
                    nextOriginalChild = nextOriginalChild.getNextSibling();
                }
            }
        }

    }

    /**
     * Implementation of the <code>UpgradeModelVisitor</code> that invokes callbacks to the private methods of this
     * class.
     */
    private class UMVisitor extends UpgradeModelVisitor {

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#getResolvedElementType()
         */
        @Override
        public NamedEntity getResolvedElementType() {
            return elementStack.peek().getOtmElementType();
        }

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#canRepeat(org.opentravel.schemacompiler.model.TLProperty,
         *      org.opentravel.schemacompiler.model.NamedEntity)
         */
        @Override
        public boolean canRepeat(TLProperty otmElement, NamedEntity resolvedElementType) {
            Element nextOriginalChild = elementStack.peek().peekNextOriginalChild();
            boolean repeatAllowed = false;

            if (nextOriginalChild != null) {
                boolean hasGlobalElement = (XsdCodegenUtils.getGlobalElementName( resolvedElementType ) != null);
                ExampleMatchType matchType;

                if (hasGlobalElement) {
                    matchType = getMatchType( resolvedElementType, nextOriginalChild );

                } else { // check for same element name
                    matchType = getFieldMatchType( otmElement, nextOriginalChild );
                }
                repeatAllowed = ExampleMatchType.isMatch( matchType );
            }
            return repeatAllowed;
        }

        /**
         * Returns the type of match between the given OTM entity and the DOM element provided.
         * 
         * @param otmField the OTM entity with which to compare the DOM element
         * @param domElement the DOM element to be compared
         * @return ExampleMatchType
         */
        private ExampleMatchType getFieldMatchType(TLMemberField<?> otmField, Element domElement) {
            ExampleMatchType matchType;

            if (otmField.getName().equals( domElement.getLocalName() )) {
                matchType = ExampleMatchType.EXACT;
            } else {
                matchType = ExampleMatchType.NONE;
            }
            return matchType;
        }

        /**
         * Constructs a new item for the upgrade tree structure using the given OTM attribute.
         * 
         * @param otmAttribute the OTM attribute from which to create the tree item
         */
        private void buildAttributeTreeItem(TLAttribute otmAttribute) {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "buildAttributeTreeItem() : %s", otmAttribute.getName() ) );
            }
            TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
            Element originalElement = elementStack.peek().getOriginalElement();
            Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();
            Attr originalAttr =
                (originalElement == null) ? null : originalElement.getAttributeNode( otmAttribute.getName() );
            ExampleMatchType matchType = null;
            Attr upgradeAttr;

            if (originalAttr == null) {
                originalAttr = findManualAttributeMatch();
            }

            if (originalAttr != null) {
                markReferenced( originalAttr );
                upgradeElement.setAttribute( otmAttribute.getName(), originalAttr.getValue() );
                upgradeAttr = upgradeElement.getAttributeNode( otmAttribute.getName() );
                matchType = ExampleMatchType.EXACT;

            } else if (otmAttribute.isMandatory() || elementStack.peek().isAutogenNode()) {
                upgradeElement.setAttribute( otmAttribute.getName(), exampleValueGenerator
                    .getExampleValue( otmAttribute, currentElementItem.getValue().getOtmEntity() ) );
                upgradeAttr = upgradeElement.getAttributeNode( otmAttribute.getName() );
                matchType = ExampleMatchType.NONE;

            } else {
                matchType = ExampleMatchType.MISSING;
                upgradeAttr = upgradeDocument.createAttribute( otmAttribute.getName() );
            }

            TreeItem<DOMTreeUpgradeNode> attributeItem =
                new TreeItem<>( new DOMTreeUpgradeNode( otmAttribute, upgradeAttr, matchType ) );

            attributeItem.setGraphic( new ImageView( AbstractDOMTreeNode.attributeIcon ) );
            currentElementItem.getChildren().add( attributeItem );
        }

        /**
         * Constructs a new item for the upgrade tree structure using the given OTM indicator.
         * 
         * @param otmIndicator the OTM indicator from which to create the tree item
         */
        private void buildIndicatorTreeItem(TLIndicator otmIndicator) {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "buildIndicatorTreeItem() : %s", otmIndicator.getName() ) );
            }

            if (otmIndicator.isPublishAsElement()) {
                buildIndicatorElementTreeItem( otmIndicator );

            } else { // publish as attribute
                buildIndicatorAttributeTreeItem( otmIndicator );
            }
        }

        /**
         * Constructs a new item for the upgrade tree structure using the given OTM indicator-element.
         * 
         * @param otmIndicator the OTM indicator from which to create the tree item
         */
        private void buildIndicatorElementTreeItem(TLIndicator otmIndicator) {
            String indicatorName = otmIndicator.getName() + (otmIndicator.getName().endsWith( "Ind" ) ? "" : "Ind");
            TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
            Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();

            String ns = otmIndicator.getOwningLibrary().getNamespace();
            Element indicatorUpgradeElement =
                upgradeDocument.createElementNS( ns, qualifiedElementName( ns, indicatorName ) );
            Element originalIndicatorElement = elementStack.peek().findNextOriginalChild( indicatorUpgradeElement );
            TreeItem<DOMTreeUpgradeNode> elementItem;
            ExampleMatchType matchType;

            if (originalIndicatorElement == null) {
                originalIndicatorElement = findManualElementMatch();
            }

            if ((originalIndicatorElement != null) || elementStack.peek().isAutogenNode()) {
                String indicatorValue = HelperUtils.getElementTextValue( originalIndicatorElement );

                if (indicatorValue == null) {
                    indicatorValue = "false";
                }
                markReferenced( originalIndicatorElement );
                indicatorUpgradeElement.appendChild( upgradeDocument.createTextNode( indicatorValue ) );
                upgradeElement.appendChild( indicatorUpgradeElement );
                matchType = ExampleMatchType.EXACT;

            } else {
                matchType = ExampleMatchType.MISSING;
            }

            elementItem = new TreeItem<>( new DOMTreeUpgradeNode( otmIndicator, indicatorUpgradeElement, matchType ) );
            elementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
            currentElementItem.getChildren().add( elementItem );
        }

        /**
         * Constructs a new item for the upgrade tree structure using the given OTM indicator-attribute.
         * 
         * @param otmIndicator the OTM indicator from which to create the tree item
         */
        private void buildIndicatorAttributeTreeItem(TLIndicator otmIndicator) {
            String indicatorName = otmIndicator.getName() + (otmIndicator.getName().endsWith( "Ind" ) ? "" : "Ind");
            TreeItem<DOMTreeUpgradeNode> currentElementItem = elementStack.peek().getUpgradeItem();
            Element originalElement = elementStack.peek().getOriginalElement();
            Element upgradeElement = (Element) currentElementItem.getValue().getDomNode();

            Attr originalAttr = (originalElement == null) ? null : originalElement.getAttributeNode( indicatorName );
            TreeItem<DOMTreeUpgradeNode> attributeItem;
            ExampleMatchType matchType;
            Attr upgradeAttr;

            if (originalAttr == null) {
                originalAttr = findManualAttributeMatch();
            }

            if ((originalAttr != null) || elementStack.peek().isAutogenNode()) {
                markReferenced( originalAttr );
                upgradeElement.setAttribute( indicatorName, (originalAttr == null) ? "true" : originalAttr.getValue() );
                upgradeAttr = upgradeElement.getAttributeNode( indicatorName );
                matchType = ExampleMatchType.EXACT;

            } else {
                upgradeAttr = upgradeDocument.createAttribute( indicatorName );
                matchType = ExampleMatchType.MISSING;
            }

            attributeItem = new TreeItem<>( new DOMTreeUpgradeNode( otmIndicator, upgradeAttr, matchType ) );
            attributeItem.setGraphic( new ImageView( AbstractDOMTreeNode.attributeIcon ) );
            currentElementItem.getChildren().add( attributeItem );
        }

        /**
         * Encapsulates all of the data items required to build a tree item associated with an OTM element.
         */
        private class ElementTreeItemInfo {

            private TLProperty otmElement;
            private TreeItem<DOMTreeUpgradeNode> currentElementItem;
            private Element upgradeElement;
            private Element simpleChildUpgradeElement;
            private Element originalChildElement;
            private NamedEntity declaredType;
            private NamedEntity elementType;
            private boolean autoGen;

            /**
             * Constructor that specifies the OTM element for which a tree item is being constructed.
             * 
             * @param otmElement the OTM element instance
             */
            public ElementTreeItemInfo(TLProperty otmElement) {
                String upgradeElementNS = otmElement.getOwningLibrary().getNamespace();

                this.otmElement = otmElement;
                this.currentElementItem = elementStack.peek().getUpgradeItem();
                this.upgradeElement = (Element) getCurrentElementItem().getValue().getDomNode();
                this.simpleChildUpgradeElement = upgradeDocument.createElementNS( upgradeElementNS,
                    qualifiedElementName( upgradeElementNS, otmElement.getName() ) );
                this.originalChildElement = elementStack.peek().findNextOriginalChild( getSimpleChildUpgradeElement() );
                this.declaredType = otmElement.getType();
                this.autoGen = false;
            }

            /**
             * Returns the OTM element for which a tree item is being generated.
             *
             * @return TLProperty
             */
            public TLProperty getOtmElement() {
                return otmElement;
            }

            /**
             * Returns the tree item that is being upgraded from the top of the stack.
             *
             * @return TreeItem&lt;DOMTreeUpgradeNode&gt;
             */
            public TreeItem<DOMTreeUpgradeNode> getCurrentElementItem() {
                return currentElementItem;
            }

            /**
             * Returns the current DOM element value from the top of the stack.
             *
             * @return Element
             */
            public Element getUpgradeElement() {
                return upgradeElement;
            }

            /**
             * Returns a DOM element for which a simple-type upgrade value can be constructed.
             *
             * @return Element
             */
            public Element getSimpleChildUpgradeElement() {
                return simpleChildUpgradeElement;
            }

            /**
             * Returns the next element in the DOM tree that matches the OTM element.
             *
             * @return Element
             */
            public Element getOriginalChildElement() {
                return originalChildElement;
            }

            /**
             * Assigns the next element in the DOM tree that matches the OTM element.
             *
             * @param originalChildElement the field value to assign
             */
            public void setOriginalChildElement(Element originalChildElement) {
                this.originalChildElement = originalChildElement;
            }

            /**
             * Returns the declared type of the OTM element.
             *
             * @return NamedEntity
             */
            public NamedEntity getDeclaredType() {
                return declaredType;
            }

            /**
             * Returns the actual type of the OTM element.
             *
             * @return NamedEntity
             */
            public NamedEntity getElementType() {
                return elementType;
            }

            /**
             * Assigns the actual type of the OTM element.
             *
             * @param elementType the OTM entity type to assign
             */
            public void setElementType(NamedEntity elementType) {
                this.elementType = elementType;
            }

            /**
             * Returns the flag indicating whether the DOM element value is auto-generated.
             *
             * @return boolean
             */
            public boolean isAutoGen() {
                return autoGen;
            }

            /**
             * Assigns the flag indicating whether the DOM element value is auto-generated.
             *
             * @param autoGen the flag value to assign
             */
            public void setAutoGen(boolean autoGen) {
                this.autoGen = autoGen;
            }

        }

        /**
         * Constructs a tree item for the given OTM element. If the element is not generated for the upgrade tree, this
         * method will return false; true otherwise.
         * 
         * @param otmElement the OTM element for which to construct a tree item
         * @return boolean
         */
        private boolean buildElementTreeItem(TLProperty otmElement) {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "buildElementTreeItem() : %s", otmElement.getName() ) );
            }
            ElementTreeItemInfo itemInfo = new ElementTreeItemInfo( otmElement );
            boolean navigateChildren = true;

            // Find the next child element that matches the name/type of the OTM element
            findNextChildElement( itemInfo );

            // Now we can process the element; differently depending on whether it is a globally-
            // defined or a simple type (or VWA)
            QName elementName = XsdCodegenUtils.getGlobalElementName( itemInfo.getElementType() );

            if ((elementName == null) || itemInfo.getOtmElement().isReference()
                || ((itemInfo.getElementType() instanceof TLListFacet)
                    && (((TLListFacet) itemInfo.getElementType()).getItemFacet() instanceof TLSimpleFacet))) {
                // Type is a simple value, simple list facet, open/closed enumeration, or VWA
                navigateChildren = buildSimpleElementTreeItem( itemInfo );

            } else { // Must be a global element (not a list facet)
                navigateChildren = buildComplexElementTreeItem( itemInfo, elementName );
            }
            return navigateChildren;
        }

        /**
         * Finds the next child element that matches the name/type of the OTM element.
         * 
         * @param itemInfo the OTM property information being used to construct the new tree item
         */
        private void findNextChildElement(ElementTreeItemInfo itemInfo) {
            if (itemInfo.getOriginalChildElement() != null) {
                itemInfo.setElementType( itemInfo.getOtmElement().getType() );

            } else if (elementStack.peek().isAutogenNode()) {
                if ((itemInfo.getDeclaredType() instanceof TLComplexTypeBase)
                    || isFacetOwnerAlias( itemInfo.getDeclaredType() )) {
                    itemInfo.setElementType( getPreferredFacet( itemInfo.getDeclaredType() ) );
                } else {
                    itemInfo.setElementType( itemInfo.getDeclaredType() );
                }

            } else {
                resolveElementType( itemInfo );
            }
        }

        /**
         * Resolves the actual type of the OTM element for which the tree item is being constructed.
         * 
         * @param itemInfo the OTM property information being used to construct the new tree item
         */
        private void resolveElementType(ElementTreeItemInfo itemInfo) {
            List<NamedEntity> candidateTypes = new ArrayList<>();
            NamedEntity otmElementType = itemInfo.getOtmElement().getType();

            if (otmElementType instanceof TLComplexTypeBase) {
                candidateTypes.addAll( FacetCodegenUtils.getAvailableFacets( (TLComplexTypeBase) otmElementType ) );

            } else if (isFacetOwnerAlias( otmElementType )) {
                candidateTypes.addAll( FacetCodegenUtils.getAvailableFacetAliases( (TLAlias) otmElementType ) );

            } else {
                candidateTypes.add( otmElementType );
            }
            itemInfo.setElementType( candidateTypes.get( 0 ) ); // Default, just in case we cannot find a match

            for (NamedEntity candidateType : candidateTypes) {
                itemInfo.setOriginalChildElement( elementStack.peek().findNextOriginalChild( candidateType ) );

                if (itemInfo.getOriginalChildElement() != null) {
                    itemInfo.setElementType( candidateType );
                    break;
                }
            }
        }

        /**
         * Generates upgraded content for the OTM property using the information provided. This method returns true if
         * the tree construction algorithm should continue traversing child elements.
         * 
         * @param itemInfo the OTM property information being used to construct the new tree item
         * @return boolean
         */
        private boolean buildSimpleElementTreeItem(ElementTreeItemInfo itemInfo) {
            TreeItem<DOMTreeUpgradeNode> childElementItem;
            ExampleMatchType matchType;
            boolean navigateChildren = true;
            String elementValue = null;

            if (itemInfo.getOriginalChildElement() == null) {
                itemInfo.setOriginalChildElement( findManualElementMatch() );
            }

            if (itemInfo.getOriginalChildElement() != null) {
                matchType = getFieldMatchType( itemInfo.getOtmElement(), itemInfo.getOriginalChildElement() );
                elementValue = HelperUtils.getElementTextValue( itemInfo.getOriginalChildElement() );
                markReferenced( itemInfo.getOriginalChildElement() );

                if (elementValue == null) {
                    elementValue = exampleValueGenerator.getExampleValue( itemInfo.getOtmElement(),
                        itemInfo.getCurrentElementItem().getValue().getOtmEntity() );
                }

            } else if (itemInfo.getOtmElement().isMandatory() || elementStack.peek().isAutogenNode()) {
                // Only create if a required element is missing from the original document
                matchType = ExampleMatchType.NONE;
                elementValue = exampleValueGenerator.getExampleValue( itemInfo.getOtmElement(),
                    itemInfo.getCurrentElementItem().getValue().getOtmEntity() );
                itemInfo.setAutoGen( true );

            } else {
                matchType = ExampleMatchType.MISSING;
                childElementItem = new TreeItem<>( new DOMTreeUpgradeNode( itemInfo.getOtmElement(),
                    itemInfo.getSimpleChildUpgradeElement(), matchType ) );
                childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
                itemInfo.getCurrentElementItem().getChildren().add( childElementItem );
                navigateChildren = false;
            }

            if (navigateChildren) {
                itemInfo.getSimpleChildUpgradeElement().appendChild( upgradeDocument.createTextNode( elementValue ) );
                itemInfo.getUpgradeElement().appendChild( itemInfo.getSimpleChildUpgradeElement() );
                childElementItem = new TreeItem<>( new DOMTreeUpgradeNode( itemInfo.getOtmElement(),
                    itemInfo.getSimpleChildUpgradeElement(), matchType ) );
                childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
                itemInfo.getCurrentElementItem().getChildren().add( childElementItem );
                elementStack.peek().nextOriginalChild();
                elementStack.push( new UpgradeNodeContext( childElementItem, itemInfo.getOriginalChildElement(),
                    itemInfo.getElementType(), itemInfo.isAutoGen() ) );
            }
            return navigateChildren;
        }

        /**
         * Generates upgraded content for the OTM property using the information provided. This method returns true if
         * the tree construction algorithm should continue traversing child elements.
         * 
         * @param itemInfo the OTM property information being used to construct the new tree item
         * @param elementName the qualified name of the element to be generated
         * @return boolean
         */
        private boolean buildComplexElementTreeItem(ElementTreeItemInfo itemInfo, QName elementName) {
            TreeItem<DOMTreeUpgradeNode> childElementItem;
            ExampleMatchType matchType = getMatchType( itemInfo.getElementType(), itemInfo.getOriginalChildElement() );
            boolean navigateChildren = true;
            Element childUpgradeElement;

            if (ExampleMatchType.isMatch( matchType )) { // Found a match in the original document
                if ((matchType == ExampleMatchType.EXACT_SUBSTITUTABLE)
                    || (matchType == ExampleMatchType.PARTIAL_SUBSTITUTABLE)) {
                    // Handle special case for substitutable element matches
                    QName substitutableName = getSubstitutableElementName( itemInfo.getElementType() );

                    if (substitutableName != null) {
                        childUpgradeElement =
                            upgradeDocument.createElementNS( substitutableName.getNamespaceURI(), qualifiedElementName(
                                substitutableName.getNamespaceURI(), substitutableName.getLocalPart() ) );

                    } else {
                        childUpgradeElement = upgradeDocument.createElementNS( elementName.getNamespaceURI(),
                            qualifiedElementName( elementName.getNamespaceURI(), elementName.getLocalPart() ) );
                    }

                } else {
                    childUpgradeElement = upgradeDocument.createElementNS( elementName.getNamespaceURI(),
                        qualifiedElementName( elementName.getNamespaceURI(), elementName.getLocalPart() ) );
                }
                itemInfo.getUpgradeElement().appendChild( childUpgradeElement );
                markReferenced( itemInfo.getOriginalChildElement() );

            } else {
                // Find out if the match type is NONE or MISSING
                if (!itemInfo.getOtmElement().isMandatory() && !elementStack.peek().isAutogenNode()) {
                    matchType = ExampleMatchType.MISSING;
                    navigateChildren = false;
                }

                // No match from original document, so we must auto-generate the element
                childUpgradeElement = autoGenerateComplexElement( itemInfo, elementName, matchType );
            }

            childElementItem = new TreeItem<>( new DOMTreeUpgradeNode( itemInfo.getElementType(),
                itemInfo.getOtmElement().getType(), childUpgradeElement, matchType ) );
            childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
            itemInfo.getCurrentElementItem().getChildren().add( childElementItem );

            if (ExampleMatchType.isMatch( matchType )) {
                elementStack.peek().nextOriginalChild();
            }

            if (navigateChildren) {
                elementStack.push( new UpgradeNodeContext( childElementItem, itemInfo.getOriginalChildElement(),
                    itemInfo.getElementType(), itemInfo.isAutoGen() ) );
            }
            return navigateChildren;
        }

        /**
         * Auto-generates content for a new complex element using the information provided.
         * 
         * @param itemInfo the OTM property information being used to construct the new tree item
         * @param elementName the qualified name of the element to be generated
         * @param matchType the type of match that was identified in the original DOM element
         * @return Element
         */
        private Element autoGenerateComplexElement(ElementTreeItemInfo itemInfo, QName elementName,
            ExampleMatchType matchType) {
            Element childUpgradeElement;
            if (itemInfo.getDeclaredType() instanceof TLComplexTypeBase) {
                QName substitutableName;

                itemInfo.setElementType( getPreferredFacet( itemInfo.getDeclaredType() ) );
                substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) itemInfo.getElementType() );
                childUpgradeElement = upgradeDocument.createElementNS( substitutableName.getNamespaceURI(),
                    qualifiedElementName( substitutableName.getNamespaceURI(), substitutableName.getLocalPart() ) );

            } else if (isFacetOwnerAlias( itemInfo.getDeclaredType() )) {
                QName substitutableName;

                itemInfo.setElementType( getPreferredFacet( itemInfo.getDeclaredType() ) );
                substitutableName = XsdCodegenUtils.getSubstitutableElementName( (TLAlias) itemInfo.getElementType() );
                childUpgradeElement = upgradeDocument.createElementNS( substitutableName.getNamespaceURI(),
                    qualifiedElementName( substitutableName.getNamespaceURI(), substitutableName.getLocalPart() ) );

            } else {
                childUpgradeElement = upgradeDocument.createElementNS( elementName.getNamespaceURI(),
                    qualifiedElementName( elementName.getNamespaceURI(), elementName.getLocalPart() ) );
            }

            if (matchType == ExampleMatchType.NONE) {
                itemInfo.getUpgradeElement().appendChild( childUpgradeElement );
            }
            itemInfo.setAutoGen( true );
            return childUpgradeElement;
        }

        /**
         * Completes the processing of the given OTM element.
         * 
         * @param otmElement the OTM element being navigated
         */
        private void finishElementTreeItem(TLProperty otmElement) {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "finishElementTreeItem() : name=%s / type=%s", otmElement.getName(),
                    otmElement.getType().getLocalName() ) );
            }
            elementStack.pop();
        }

        /**
         * Constructs a tree item for an extension point group of the given type.
         * 
         * @param extensionPointType the facet type of the extension point group
         */
        private boolean buildExtensionPointGroupTreeItemStart(TLFacetType extensionPointType) {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "buildExtensionPointGroupTreeItemStart() : %s", extensionPointType ) );
            }
            boolean processChildren = false;
            QName elementName;

            switch (extensionPointType) {
                case SUMMARY:
                    elementName = SchemaDependency.getExtensionPointElement().toQName();
                    break;
                case SHARED:
                    elementName = SchemaDependency.getExtensionPointElement().toQName();
                    break;
                default:
                    elementName = SchemaDependency.getExtensionPointElement().toQName();
                    break;
            }
            Element childUpgradeElement = upgradeDocument.createElementNS( elementName.getNamespaceURI(),
                qualifiedElementName( elementName.getNamespaceURI(), elementName.getLocalPart() ) );
            Element originalChildElement = elementStack.peek().findNextOriginalChild( childUpgradeElement );

            if (originalChildElement != null) {
                TreeItem<DOMTreeUpgradeNode> childElementItem =
                    new TreeItem<>( new DOMTreeUpgradeNode( null, null, childUpgradeElement, ExampleMatchType.EXACT ) );

                childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
                elementStack.push( new UpgradeNodeContext( childElementItem, originalChildElement, null, false ) );
                markReferenced( originalChildElement );
                processChildren = true;
            }
            return processChildren;
        }

        /**
         * Completes the tree item for an extension point group of the given type. The extension point group element is
         * only added to the parent DOM element if one or more extension point child elements were actually created.
         * 
         * @param extensionPointType the facet type of the extension point group
         */
        private void buildExtensionPointGroupTreeItemEnd(TLFacetType extensionPointType) {
            if (log.isDebugEnabled()) {
                log.debug( String.format( "buildExtensionPointGroupTreeItemEnd() : %s", extensionPointType ) );
            }
            TreeItem<DOMTreeUpgradeNode> extensionPointElementItem = elementStack.pop().getUpgradeItem();
            TreeItem<DOMTreeUpgradeNode> parentElementItem = elementStack.peek().getUpgradeItem();

            if (!extensionPointElementItem.getChildren().isEmpty()) {
                Element extensionPointElement = (Element) extensionPointElementItem.getValue().getDomNode();
                Element parentElement = (Element) parentElementItem.getValue().getDomNode();

                parentElement.appendChild( extensionPointElement );
                parentElementItem.getChildren().add( extensionPointElementItem );
            }
        }

        /**
         * Constructs a tree item for the given OTM extension point facet. If the extension point facet is not generated
         * for the upgrade tree, this method will return false; true otherwise.
         * 
         * @param otmExtensionPoint the OTM extension point facet for which to construct a tree item
         * @return boolean
         */
        private boolean buildExtensionPointFacetTreeItem(TLExtensionPointFacet otmExtensionPoint) {
            if (log.isDebugEnabled()) {
                log.debug(
                    String.format( "buildExtensionPointFacetTreeItem() : %s", otmExtensionPoint.getLocalName() ) );
            }
            TreeItem<DOMTreeUpgradeNode> extensionPointElementItem = elementStack.peek().getUpgradeItem();
            QName elementName = XsdCodegenUtils.getGlobalElementName( otmExtensionPoint );
            Element childUpgradeElement = upgradeDocument.createElementNS( elementName.getNamespaceURI(),
                qualifiedElementName( elementName.getNamespaceURI(), elementName.getLocalPart() ) );
            Node originalChildNode = elementStack.peek().getOriginalElement().getFirstChild();
            boolean isAutoGen = elementStack.peek().isAutogenNode();
            ExampleMatchType matchType = null;
            boolean processChildren = false;
            Element originalChild = null;


            // Since extension points are unordered, we need to search the entire list instead
            // of treating it as a sequence (as with normal elements)
            while (originalChildNode != null) {
                if (originalChildNode.getNodeType() == Node.ELEMENT_NODE) {
                    matchType = getElementMatchType( childUpgradeElement, (Element) originalChildNode );

                    if (ExampleMatchType.isMatch( matchType )) {
                        originalChild = (Element) originalChildNode;
                        break;
                    }
                }
                originalChildNode = originalChildNode.getNextSibling();
            }

            // Only create a new element if we find a match in the original
            // document (or we are auto-generating)
            if ((originalChild != null) || isAutoGen) {
                TreeItem<DOMTreeUpgradeNode> childElementItem = new TreeItem<>(
                    new DOMTreeUpgradeNode( otmExtensionPoint, otmExtensionPoint, childUpgradeElement, matchType ) );

                childElementItem.setGraphic( new ImageView( AbstractDOMTreeNode.elementIcon ) );
                extensionPointElementItem.getValue().getDomNode().appendChild( childUpgradeElement );
                extensionPointElementItem.getChildren().add( childElementItem );
                elementStack
                    .push( new UpgradeNodeContext( childElementItem, originalChild, otmExtensionPoint, isAutoGen ) );
                markReferenced( originalChild );
                processChildren = true;
            }
            return processChildren;
        }

        /**
         * Returns a dummy attribute that will provide the manually-assigned value or null if no manual value was
         * assigned.
         * 
         * @return Attr
         */
        private Attr findManualAttributeMatch() {
            String manualValue = elementStack.peek().getManualValue();
            Attr manualAttr = null;

            if (manualValue != null) {
                manualAttr = upgradeDocument.createAttribute( "dummy" );
                manualAttr.setValue( manualValue );
                elementStack.peek().setManualValue( null );
            }
            return manualAttr;
        }

        /**
         * Returns a dummy simple element that will provide the manually-assigned value or null if no manual value was
         * assigned.
         * 
         * @return Element
         */
        private Element findManualElementMatch() {
            String manualValue = elementStack.peek().getManualValue();
            Element manualElement = null;

            if (manualValue != null) {
                manualElement = upgradeDocument.createElement( "dummy" );
                manualElement.appendChild( upgradeDocument.createTextNode( manualValue ) );
                elementStack.peek().setManualValue( null );
            }
            return manualElement;
        }

        /**
         * Completes the processing of the given OTM extension point facet.
         * 
         * @param otmExtensionPoint the OTM extension point facet being navigated
         */
        private void finishExtensionPointFacetTreeItem(TLExtensionPointFacet otmExtensionPoint) {
            if (log.isDebugEnabled()) {
                log.debug(
                    String.format( "finishExtensionPointFacetTreeItem() : %s", otmExtensionPoint.getLocalName() ) );
            }
            elementStack.pop();
        }

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#isAutoGenerationEnabled()
         */
        @Override
        public boolean isAutoGenerationEnabled() {
            return elementStack.peek().isAutogenNode();
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
         */
        @Override
        public boolean visitAttribute(TLAttribute attribute) {
            buildAttributeTreeItem( attribute );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitIndicator(org.opentravel.schemacompiler.model.TLIndicator)
         */
        @Override
        public boolean visitIndicator(TLIndicator indicator) {
            buildIndicatorTreeItem( indicator );
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElement(TLProperty element) {
            return buildElementTreeItem( element );
        }

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitElementEnd(org.opentravel.schemacompiler.model.TLProperty)
         */
        @Override
        public boolean visitElementEnd(TLProperty element) {
            finishElementTreeItem( element );
            return true;
        }

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitExtensionPointGroupStart(org.opentravel.schemacompiler.model.TLFacetType)
         */
        @Override
        public boolean visitExtensionPointGroupStart(TLFacetType extensionPointType) {
            return buildExtensionPointGroupTreeItemStart( extensionPointType );
        }

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitExtensionPointGroupEnd(org.opentravel.schemacompiler.model.TLFacetType)
         */
        @Override
        public void visitExtensionPointGroupEnd(TLFacetType extensionPointType) {
            buildExtensionPointGroupTreeItemEnd( extensionPointType );
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
         */
        @Override
        public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
            return buildExtensionPointFacetTreeItem( extensionPointFacet );
        }

        /**
         * @see org.opentravel.exampleupgrade.UpgradeModelVisitor#visitExtensionPointEnd(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
         */
        @Override
        public boolean visitExtensionPointEnd(TLExtensionPointFacet extensionPointFacet) {
            finishExtensionPointFacetTreeItem( extensionPointFacet );
            return true;
        }

    }

}
