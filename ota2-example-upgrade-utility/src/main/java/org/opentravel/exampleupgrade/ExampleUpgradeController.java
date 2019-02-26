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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.OtmApplicationException;
import org.opentravel.application.common.StatusType;
import org.opentravel.application.common.SyntaxHighlightBuilder;
import org.opentravel.application.common.XmlHighlightBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.visitor.ModelNavigator;
import org.opentravel.schemacompiler.xml.XMLPrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.effect.Lighting;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX controller class for the OTA2 Example Upgrade Utility application.
 */
public class ExampleUpgradeController extends AbstractMainWindowController {
	
	public static final String FXML_FILE = "/ota2-EXAMPLE-upgrade.fxml";
	
	private static final DataFormat DRAG_FORMAT = new DataFormat("application/ota2-original-dom-node");
    private static final Logger log = LoggerFactory.getLogger( ExampleUpgradeController.class );
	
    @FXML private TextField libraryText;
    @FXML private Tooltip libraryTooltip;
    @FXML private Button libraryButton;
    @FXML private TextField exampleText;
    @FXML private Tooltip exampleTooltip;
    @FXML private Button exampleButton;
    @FXML private TextField rootElementPrefixText;
    @FXML private TextField rootElementNSText;
    @FXML private ChoiceBox<OTMObjectChoice> entityChoice;
    @FXML private Button strategyButton;
    @FXML private Button resetButton;
    @FXML private TreeView<DOMTreeOriginalNode> originalTreeView;
    @FXML private TreeView<DOMTreeUpgradeNode> upgradedTreeView;
    @FXML private TabPane tabPane;
    @FXML private AnchorPane previewTab;
    @FXML private AnchorPane originalTab;
    @FXML private AnchorPane autogenTab;
    @FXML private TableView<EntityFacetSelection> facetSelectionTableView;
    @FXML private TableColumn<EntityFacetSelection, String> otmObjectColumn;
    @FXML private TableColumn<EntityFacetSelection, String> facetSelectionColumn;
    @FXML private ChoiceBox<String> bindingStyleChoice;
    @FXML private Spinner<Integer> repeatCountSpinner;
    @FXML private Button saveButton;
    @FXML private Label statusBarLabel;
	private VirtualizedScrollPane<?> previewScrollPane;
	private CodeArea previewPane;
	private ContextMenu upgradeContextMenu;
	
	private File modelFile;
	private File exampleFile;
	private File exampleFolder;
	private TLModel model;
	private SelectionStrategy selectionStrategy = SelectionStrategy.getDefault();
	private Document originalDocument;
	private Document upgradeDocument;
	private boolean upgradeDocumentDirty = false;
	
	private Map<QName,List<OTMObjectChoice>> familyMatches = new HashMap<>();
	private Map<String,List<OTMObjectChoice>> allElementsByBaseNS = new HashMap<>();
	private FacetSelections facetSelections;
	private UserSettings userSettings;
	
	private String dragId;
	private TreeItem<DOMTreeOriginalNode> dragItem;
	
	/**
	 * Called when the user clicks the button to load a new project or library file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectLibrary(ActionEvent event) {
		File initialDirectory = (modelFile != null) ?
				modelFile.getParentFile() : UserSettings.load().getLastModelFile().getParentFile();
		FileChooser chooser = newFileChooser( "Select OTM Library or Project", initialDirectory,
		        OTP_EXTENSION_FILTER, OTR_EXTENSION_FILTER, OTM_EXTENSION_FILTER, ALL_EXTENSION_FILTER );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if ((selectedFile != null) && selectedFile.exists()) {
			Runnable r = new BackgroundTask( "Loading Model: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws OtmApplicationException {
					loadModel( selectedFile );
				}
			};
			
			new Thread( r ).start();
		}
	}
	
    /**
     * Loads a model from the selected file.
     * 
     * @param selectedFile  the OTM library or project that was selected by the user
     * @throws OtmApplicationException  thrown if an error occurs while loading the model
     */
    private void loadModel(File selectedFile) throws OtmApplicationException {
        try {
            ValidationFindings findings;
            TLModel newModel = null;
            
            if (selectedFile.getName().endsWith(".otp")) {
                ProjectManager manager = new ProjectManager( false );
                
                findings = new ValidationFindings();
                manager.loadProject( selectedFile, findings );
                
                newModel = manager.getModel();
                
            } else if (selectedFile.getName().endsWith(".otr")) {
                ReleaseManager releaseManager = new ReleaseManager( RepositoryManager.getDefault() );
                
                findings = new ValidationFindings();
                releaseManager.loadRelease( selectedFile, findings );
                
                if (findings.hasFinding( FindingType.ERROR )) {
                    throw new LibraryLoaderException("Validation errors detected in model (see log for DETAILS)");
                }
                newModel = releaseManager.getModel();
                
            } else { // assume OTM library file
                LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( selectedFile );
                LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<>();
                
                findings = modelLoader.loadLibraryModel( libraryInput );
                newModel = modelLoader.getLibraryModel();
            }
            
            if ((findings == null) || !findings.hasFinding( FindingType.ERROR )) {
                QNameCandidateVisitor visitor = new QNameCandidateVisitor();
                
                model = newModel;
                modelFile = selectedFile;
                userSettings.setLastModelFile( modelFile );
                
                // Scan the model to pre-populate tables with lists of potential entity
                // selections for the EXAMPLE root element.
                new ModelNavigator( visitor ).navigate( model );
                familyMatches = visitor.getFamilyMatches();
                allElementsByBaseNS = visitor.getAllElementsByBaseNS();
                
                populateFacetSelections();
                rebuildEntityChoices();
                
            } else {
                if (log.isWarnEnabled()) {
                    log.warn( String.format( "%s - Error/Warning Messages:", selectedFile.getName() ) );

                    for (String message : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
                        log.warn( String.format( "  %s", message ) );
                    }
                }
                throw new LibraryLoaderException("Validation errors detected in model (see log for DETAILS)");
            }
            
        } catch (Exception e) {
            throw new OtmApplicationException( e.getMessage(), e );
            
        } finally {
            updateControlStates();
            userSettings.save();
        }
    }
    
	/**
	 * Rebuilds the contents of the entity selection maps.
	 */
	private void rebuildEntityChoices() {
		if ((originalDocument == null) || (model == null)) {
		    return;
		}
        QName rootName = HelperUtils.getElementName( originalDocument.getDocumentElement() );
        String rootBaseNS = HelperUtils.getBaseNamespace( rootName.getNamespaceURI() );
        List<OTMObjectChoice> candidateEntities = getCandidateEntities( rootName, rootBaseNS );
        if (candidateEntities == null) candidateEntities = new ArrayList<>();
        List<OTMObjectChoice> selectableObjects = new ArrayList<>();
        OTMObjectChoice exactMatch = null;
        OTMObjectChoice tempExactMatch;
        
        // Build the list of candidate entities
        for (OTMObjectChoice objectChoice : candidateEntities) {
            if (objectChoice.getOtmObjectName().equals( rootName )) {
                exactMatch = objectChoice;
            }
            selectableObjects.add( objectChoice );
        }
        
        if (exactMatch != null) {
            tempExactMatch = exactMatch;
            
        } else {
            tempExactMatch = (!selectableObjects.isEmpty()) ? selectableObjects.get( 0 ) : null;
        }
        
        Platform.runLater( () -> {
            entityChoice.setItems( FXCollections.observableArrayList( selectableObjects ) );
            
            if (tempExactMatch != null) {
                entityChoice.setValue( tempExactMatch );
            }
        });
	}
	
	/**
	 * Constructs the list of facet selections for the Auto-Gen tab.
	 */
	private void populateFacetSelections() {
		this.facetSelections = new FacetSelections();
		
		// Build the facet selections for each facet owner type in the model
		for (TLLibrary library : model.getUserDefinedLibraries()) {
			for (TLBusinessObject entity : library.getBusinessObjectTypes()) {
				facetSelections.addFacetSelection( new EntityFacetSelection( entity ) );
			}
			for (TLChoiceObject entity : library.getChoiceObjectTypes()) {
				facetSelections.addFacetSelection( new EntityFacetSelection( entity ) );
			}
			for (TLCoreObject entity : library.getCoreObjectTypes()) {
				facetSelections.addFacetSelection( new EntityFacetSelection( entity ) );
			}
		}
		
		Platform.runLater( () -> facetSelectionTableView.setItems(
					FXCollections.observableArrayList( facetSelections.getAllFacetSelections() ) ) );
	}
	
	/**
	 * Returns the list of candidate entities to include in the OTM objects
	 * list of the display.
	 * 
	 * @param rootName  the qualified name of the root element of the DOM tree
	 * @param rootBaseNS  the base namespace of the root element
	 * @return List<OTMObjectChoice>
	 */
	private List<OTMObjectChoice> getCandidateEntities(QName rootName, String rootBaseNS) {
		List<OTMObjectChoice> candidates = null;
		
		switch (selectionStrategy.getStrategyType()) {
			case BASE_FAMILY:
				QName baseQName = new QName( rootBaseNS, rootName.getLocalPart() );
				candidates = familyMatches.get( baseQName );
				break;
			case EXAMPLE_NAMESPACE:
				candidates = allElementsByBaseNS.get( rootBaseNS );
				break;
			case USER_NAMESPACE:
				candidates = allElementsByBaseNS.get( selectionStrategy.getUserNamespace() );
				break;
		}
		if (candidates == null) {
			candidates = Collections.emptyList();
		}
		return candidates;
	}
	
	/**
	 * Called when the user clicks the button to load a new EXAMPLE file to be upgraded.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectExampleFile(ActionEvent event) {
		File initialDirectory = (exampleFolder != null) ?
				exampleFolder : UserSettings.load().getLastExampleFolder();
		FileChooser chooser = newFileChooser( "Save Example Output", initialDirectory,
		        XML_EXTENSION_FILTER, JSON_EXTENSION_FILTER );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if ((selectedFile != null) && selectedFile.exists()) {
			Runnable r = new BackgroundTask( "Loading Example Document: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws OtmApplicationException {
					try {
						if (selectedFile.getName().endsWith(".xml")) {
							DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
							dbFactory.setNamespaceAware( true );
							DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
							
							originalDocument = dBuilder.parse( selectedFile );
							rebuildEntityChoices();
							
						} else if (selectedFile.getName().endsWith(".json")) {
							throw new OtmApplicationException("JSON documents not yet supported.");
							
						} else {
							throw new OtmApplicationException("Unknown EXAMPLE file format: " + selectedFile.getName());
						}
						exampleFile = selectedFile;
						exampleFolder = exampleFile.getParentFile();
						userSettings.setLastExampleFolder( exampleFolder );
						
                    } catch (Exception e) {
                        throw new OtmApplicationException( e.getMessage(), e );
                        
					} finally {
						updateControlStates();
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Populates the contents of the visual controls associated with EXAMPLE content.
	 * 
	 * @param newObjectSelected  flag indicating whether the OTM object is a new selection by the user
	 */
	private void populateExampleContent(boolean newObjectSelected) {
		Platform.runLater( () -> {
			try {
				OTMObjectChoice selectedEntity = entityChoice.getValue();
				
				rootElementPrefixText.setText( originalDocument.getDocumentElement().getNodeName() );
				rootElementNSText.setText( HelperUtils.getElementName( originalDocument.getDocumentElement() ).toString() );
				originalTreeView.setRoot( DOMTreeOriginalNode.createTree( originalDocument.getDocumentElement() ) );
				
				if (selectedEntity != null) {
					TreeItem<DOMTreeUpgradeNode> upgradeTree = new UpgradeTreeBuilder( getExampleOptions() )
							.buildUpgradeDOMTree( selectedEntity.getOtmObject(), originalDocument.getDocumentElement() );
					
					upgradedTreeView.setRoot( upgradeTree );
					upgradeDocument = upgradeTree.getValue().getDomNode().getOwnerDocument();
					upgradeDocumentDirty = true;
					setUpgradeExpandedStates( upgradeTree );
					setOriginalExpandedStates( originalTreeView.getRoot() );
					
				} else {
					upgradedTreeView.setRoot( null );
					upgradeDocumentDirty = false;
				}
				updatePreviewPane( newObjectSelected );
				
			} catch (Exception e) {
				previewPane.replaceText( "-- Error Generating Example Output --");
				log.error( "Error Generating Example Output", e );
			}
		});
	}
	
	/**
	 * Updates the contents of the preview pane.
	 * 
	 * @param newObjectSelected  flag indicating whether the OTM object is a new selection by the user
	 */
	private void updatePreviewPane(boolean newObjectSelected) {
		Platform.runLater( () -> {
			try {
				double yScroll = newObjectSelected ? 0.0 : previewScrollPane.estimatedScrollYProperty().getValue();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				OTMObjectChoice selectedEntity = entityChoice.getValue();
				SyntaxHighlightBuilder highlightingBuilder;
				
				if (selectedEntity != null) {
					highlightingBuilder = new XmlHighlightBuilder();
					new XMLPrettyPrinter().formatDocument( upgradeDocument, out );
					
				} else {
					highlightingBuilder = new XmlHighlightBuilder();
				}
				
				previewPane.replaceText( new String( out.toByteArray(), StandardCharsets.UTF_8 ) );
				previewPane.setStyleSpans( 0, highlightingBuilder.computeHighlighting( previewPane.getText() ) );
				previewScrollPane.estimatedScrollYProperty().setValue( yScroll );
				
			} catch (Exception e) {
				previewPane.replaceText( "-- Error Generating Example Output --");
				log.error( "Error Generating Example Output", e );
			}
		});
	}
	
	/**
	 * Called when the user clicks the button to modify the OTM Object selection strategy.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectStrategy(ActionEvent event) {
		try {
			List<String> userNamespaces = new ArrayList<>( allElementsByBaseNS.keySet() );
			FXMLLoader loader = new FXMLLoader( ExampleUpgradeController.class.getResource(
					SelectionStrategyController.FXML_FILE ) );
			SelectionStrategyController controller;
			AnchorPane page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( "OTM Object Selection Strategy" );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.initOwner( getPrimaryStage() );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.setDialogStage( dialogStage );
			Collections.sort( userNamespaces );
			controller.initialize( selectionStrategy, userNamespaces );
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				selectionStrategy = controller.getStrategy();
				rebuildEntityChoices();
			}
			
		} catch (IOException e) {
		    log.error( "Error displaying selection strategy dialog.", e );
		}
	}
	
	/**
	 * Resets all manual updates and restores the upgraded EXAMPLE to its original default
	 * state.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void resetContent(ActionEvent event) {
		Alert confirmDialog = new Alert( AlertType.CONFIRMATION );
		
		confirmDialog.setTitle( "Reset Example Content" );
		confirmDialog.setHeaderText( null );
		confirmDialog.setContentText("Are you sure you want to reset all content to its original state?");
		confirmDialog.showAndWait();
		
		if (confirmDialog.getResult() == ButtonType.OK) {
			populateExampleContent( true );
		}
	}
	
	/**
	 * Called when the user clicks the button to save the current EXAMPLE output to file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void saveExampleOutput(ActionEvent event) {
		UserSettings settings = UserSettings.load();
		FileChooser chooser = newFileChooser( "Save Example Output",
				userSettings.getLastExampleFolder(), XML_EXTENSION_FILTER );
		File targetFile = chooser.showSaveDialog( getPrimaryStage() );
		
		if (targetFile != null) {
			Runnable r = new BackgroundTask( "Saving Report", StatusType.INFO ) {
				protected void execute() throws OtmApplicationException {
					try {
						try (Writer out = new FileWriter( targetFile )) {
							out.write( previewPane.getText() );
						}
						upgradeDocumentDirty = false;
						
                    } catch (Exception e) {
                        throw new OtmApplicationException( e.getMessage(), e );
                        
					} finally {
					    settings.setLastExampleFolder( targetFile.getParentFile() );
					    settings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the 'Show Legend' link to display the legend
	 * for the upgrade tree view.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void showOriginalLegend(ActionEvent event) {
		showLegend( "Original Document Legend", ExampleUpgradeController.class.getResource(
				"/html/original-legend.html" ).toExternalForm() );
	}
	
	/**
	 * Called when the user clicks the 'Show Legend' link to display the legend
	 * for the upgrade tree view.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void showUpgradeLegend(ActionEvent event) {
		showLegend( "Upgraded Document Legend", ExampleUpgradeController.class.getResource(
				"/html/upgrade-legend.html" ).toExternalForm() );
	}
	
	/**
	 * Displays the specified legend documentation.
	 * 
	 * @param TITLE  the TITLE of the dialog box
	 * @param legendUrl  the URL of the legend documentation to display
	 */
	private void showLegend(String title, String legendUrl) {
		try {
			FXMLLoader loader = new FXMLLoader( ExampleUpgradeController.class.getResource(
					LegendController.FXML_FILE ) );
			LegendController controller;
			BorderPane page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( title );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.initOwner( getPrimaryStage() );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.initialize( dialogStage, legendUrl );
			controller.showAndWait();
			
		} catch (IOException e) {
		    log.error( "Error displaying legend dialog.", e );
		}
	}
	/**
	 * Called when the user changes the default binding style.
	 */
	private void handleBindingStyleChange() {
		String selectedStyle = bindingStyleChoice.getValue();
		String currentStyle = CompilerExtensionRegistry.getActiveExtension();

		if ((selectedStyle != null) && !selectedStyle.equals(currentStyle)) {
			CompilerExtensionRegistry.setActiveExtension(selectedStyle);
		}
	}
	
    /**
     * Called when a selection change occurs in the 'upgradedTreeView' control.
     * 
     * @param newValue  the new value that was selected
     */
    private void handleUpgradedItemSelectionChange(TreeItem<DOMTreeUpgradeNode> newValue) {
        DOMTreeUpgradeNode treeNode = (newValue == null) ? null : newValue.getValue();
        NamedEntity selectedEntity = (treeNode == null) ? null : treeNode.getOtmEntity();
        EntityFacetSelection facetSelection;
        
        if (selectedEntity instanceof TLAlias) {
            selectedEntity = ((TLAlias) selectedEntity).getOwningEntity();
        }
        if (selectedEntity instanceof TLFacet) {
            selectedEntity = ((TLFacet) selectedEntity).getOwningEntity();
        }
        facetSelection = (selectedEntity == null) ?
                null : facetSelections.getFacetSelection( selectedEntity );
        
        if (facetSelection != null) {
            facetSelectionTableView.getSelectionModel().select( facetSelection );
            facetSelectionTableView.scrollTo( facetSelection );
        }
    }
    
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#updateControlStates()
	 */
	@Override
	protected void updateControlStates() {
		Platform.runLater( () -> {
			boolean exDisplayDisabled = (originalDocument == null);
			boolean exControlsDisabled = (model == null) || (originalDocument == null);
			
			libraryText.setText( (modelFile == null) ? "" : modelFile.getName() );
			libraryTooltip.setText( (modelFile == null) ? "" : modelFile.getAbsolutePath() );
			exampleText.setText( (exampleFile == null) ? "" : exampleFile.getName() );
			exampleTooltip.setText( (exampleFile == null) ? "" : exampleFile.getAbsolutePath() );
			
			rootElementPrefixText.disableProperty().set( exDisplayDisabled );
			rootElementNSText.disableProperty().set( exDisplayDisabled );
			originalTreeView.disableProperty().set( exDisplayDisabled );
			
			entityChoice.disableProperty().set( exControlsDisabled );
			strategyButton.disableProperty().set( exControlsDisabled );
			resetButton.disableProperty().set( exControlsDisabled );
			saveButton.disableProperty().set( exControlsDisabled );
			upgradedTreeView.disableProperty().set( exControlsDisabled );
			previewPane.disableProperty().set( exControlsDisabled );
		} );
	}
	
	/**
	 * Traverses the given tree and sets the expanded states such that the parents
	 * of any non-matching items are expanded.
	 * 
	 * @param treeItem  the tree item to configure
	 * @return boolean
	 */
	private boolean setUpgradeExpandedStates(TreeItem<DOMTreeUpgradeNode> treeItem) {
		boolean expandParent = false;
		
		for (TreeItem<DOMTreeUpgradeNode> childItem : treeItem.getChildren()) {
			boolean childResult = setUpgradeExpandedStates( childItem );
			expandParent |= childResult;
		}
		treeItem.setExpanded( expandParent ); // Expand this nodes if any children requested it
		return expandParent || !ExampleMatchType.isMatch( treeItem.getValue().getMatchType() );
	}
	
	/**
	 * Traverses the given tree and sets the expanded states such that the parents
	 * of any unreferenced items are expanded.
	 * 
	 * @param treeItem  the tree item to configure
	 * @return boolean
	 */
	private boolean setOriginalExpandedStates(TreeItem<DOMTreeOriginalNode> treeItem) {
		boolean expandParent = false;
		
		for (TreeItem<DOMTreeOriginalNode> childItem : treeItem.getChildren()) {
			boolean childResult = setOriginalExpandedStates( childItem );
			expandParent |= childResult;
		}
		treeItem.setExpanded( expandParent ); // Expand this nodes if any children requested it
		return expandParent || (treeItem.getValue().getReferenceStatus() == ReferenceStatus.NOT_REFERENCED);
	}
	
	/**
	 * Forces an update of the visual representation of the given tree item and all of its
	 * children.
	 * 
	 * @param treeItem  the tree item to be refreshed
	 */
	private void refreshBranch(TreeItem<?> treeItem) {
		if (treeItem.isLeaf()) {
		    Event.fireEvent( treeItem,
		    		new TreeModificationEvent<>( TreeItem.valueChangedEvent(), treeItem ) );
		}
		for (TreeItem<?> child : treeItem.getChildren()) {
			refreshBranch( child );
		}
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String, org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater( () -> {
			statusBarLabel.setText( message );
			
			libraryText.disableProperty().set( disableControls );
			libraryButton.disableProperty().set( disableControls );
			exampleText.disableProperty().set( disableControls );
			exampleButton.disableProperty().set( disableControls );
			rootElementPrefixText.disableProperty().set( disableControls );
			rootElementNSText.disableProperty().set( disableControls );
			entityChoice.disableProperty().set( disableControls );
			strategyButton.disableProperty().set( disableControls );
			resetButton.disableProperty().set( disableControls );
			saveButton.disableProperty().set( disableControls );
			upgradedTreeView.disableProperty().set( disableControls );
			originalTreeView.disableProperty().set( disableControls );
			bindingStyleChoice.disableProperty().set( disableControls );
			repeatCountSpinner.disableProperty().set( disableControls );
			facetSelectionTableView.disableProperty().set( disableControls );
			previewPane.disableProperty().set( disableControls );
		});
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller.
	 *
	 * @param primaryStage  the primary stage for this controller
	 */
	@Override
	protected void initialize(Stage primaryStage) {
		super.initialize( primaryStage );
		
		// Since the preview pane is a custom component, we have to configure it manually
		previewPane = new CodeArea();
		previewPane.setEditable( false );
		previewScrollPane = new VirtualizedScrollPane<>( previewPane );
		Node pane = new StackPane( previewScrollPane );
		previewTab.getChildren().add( pane );
		AnchorPane.setTopAnchor(pane, 0.0D);
		AnchorPane.setBottomAnchor(pane, 0.0D);
		AnchorPane.setLeftAnchor(pane, 0.0D);
		AnchorPane.setRightAnchor(pane, 0.0D);
		
        configureListeners();
		
		ObservableList<String> stylesheets = getPrimaryStage().getScene().getStylesheets();
		final EventHandler<WindowEvent> existingOnClose = getPrimaryStage().getOnCloseRequest();
		
		stylesheets.add( ExampleUpgradeController.class.getResource( "/styles/xml-highlighting.css" ).toExternalForm() );
		stylesheets.add( ExampleUpgradeController.class.getResource( "/styles/json-highlighting.css" ).toExternalForm() );
		stylesheets.add( ExampleUpgradeController.class.getResource( "/styles/tree-styles.css" ).toExternalForm() );
		
		getPrimaryStage().setOnCloseRequest( event -> {
            if ((upgradeDocument != null) && upgradeDocumentDirty) {
                Alert confirmDialog = new Alert( AlertType.CONFIRMATION );
                
                confirmDialog.setTitle( "Unsaved Changes" );
                confirmDialog.setHeaderText( null );
                confirmDialog.setContentText(
                        "Your upgraded EXAMPLE document has unsaved changes.  "
                        + "Click 'Ok' to save now or 'Cancel' to exit without saving.");
                confirmDialog.showAndWait();
                
                if (confirmDialog.getResult() == ButtonType.OK) {
                    saveExampleOutput( null );
                }
            }
            if (existingOnClose != null) {
                existingOnClose.handle( event );
            }
		});
		updateControlStates();
	}

    /**
     * Configure listeners to capture interactions with the visual controls.
     */
    private void configureListeners() {
        List<String> bindingStyles = CompilerExtensionRegistry.getAvailableExtensionIds();
        String defaultStyle = CompilerExtensionRegistry.getActiveExtension();
        UserSettings settings = UserSettings.load();
        
        this.userSettings = settings;
		bindingStyleChoice.setItems( FXCollections.observableArrayList( bindingStyles ) );
		bindingStyleChoice.setValue( defaultStyle );
		bindingStyleChoice.valueProperty().addListener(
				(observable, oldValue, newValue) -> handleBindingStyleChange() );
		
		repeatCountSpinner.setValueFactory(
				new IntegerSpinnerValueFactory( 1, 3, settings.getRepeatCount(), 1 ) );
		
		otmObjectColumn.setCellValueFactory( cellData ->
		        new ReadOnlyObjectWrapper<String>(
					HelperUtils.getDisplayName( cellData.getValue().getEntityType(), true ) ) );
		otmObjectColumn.prefWidthProperty().bind( facetSelectionTableView.widthProperty().divide(2) );
		
		facetSelectionColumn.setCellValueFactory( cellData -> {
			String cellValue = cellData.getValue().getSelectedFacetName();
			return (cellValue == null) ? null : new ReadOnlyObjectWrapper<String>( cellValue );
		});
		facetSelectionColumn.setCellFactory( cellData -> new FacetSelectCell() );
		facetSelectionColumn.prefWidthProperty().bind( facetSelectionTableView.widthProperty().divide(2) );
		
		entityChoice.valueProperty().addListener( ( observable, oldValue, newValue ) -> {
			if (oldValue != newValue) {
				populateExampleContent( true );
			}
		});
		
		originalTreeView.setCellFactory( tv -> new OriginalTreeViewCellFactory() );
		
		upgradedTreeView.setCellFactory( tv -> new UpgradedTreeViewCellFactory() );
		
		upgradedTreeView.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> handleUpgradedItemSelectionChange( newValue ) );
    }

	/**
	 * Returns the set of options that should be used when generating unmatched sections
	 * of the upgraded EXAMPLE tree.
	 * 
	 * @return ExampleGeneratorOptions
	 */
	private ExampleGeneratorOptions getExampleOptions() {
		ExampleGeneratorOptions options = new ExampleGeneratorOptions();
		
		options.setMaxRepeat( repeatCountSpinner.getValue() );
		facetSelections.configureExampleOptions( options );
		return options;
	}
	
	/**
	 * Table cell that allows the user to select a facet from the available list
	 * for substitutable OTM entities.
	 */
    @SuppressWarnings("squid:MaximumInheritanceDepth") // Unavoidable since the base class is from core JavaFXx
	private class FacetSelectCell extends ChoiceBoxTableCell<EntityFacetSelection,String> {
		
		/**
		 * Default constructor.
		 */
		public FacetSelectCell() {
			setOnMouseEntered( me -> {
                boolean isEditable = FacetSelectCell.this.isEditable();
                FacetSelectCell.this.getScene().setCursor( isEditable ? Cursor.HAND : Cursor.DEFAULT );
			} );
			setOnMouseExited( me -> FacetSelectCell.this.getScene().setCursor( Cursor.DEFAULT ) );
		}

		/**
		 * @see javafx.scene.control.cell.ChoiceBoxTableCell#startEdit()
		 */
		@Override
		public void startEdit() {
			EntityFacetSelection facetSelection = (EntityFacetSelection) getTableRow().getItem();
			List<String> facetNames = (facetSelection == null) ? FXCollections.emptyObservableList()
					: FXCollections.observableArrayList( facetSelection.getFacetNames() );
			
			if (facetNames.size() > 1) {
				getItems().setAll( facetNames );
			} else {
				getItems().clear();
			}
			super.startEdit();
		}

		/**
		 * @see javafx.scene.control.TreeTableCell#commitEdit(java.lang.Object)
		 */
		@Override
		public void commitEdit(String value) {
			EntityFacetSelection facetSelection = (EntityFacetSelection) getTableRow().getItem();
			
			facetSelection.setSelectedFacet( value );
			super.commitEdit( value );
		}
		
		/**
		 * @see javafx.scene.control.cell.ChoiceBoxTreeTableCell#updateItem(java.lang.Object, boolean)
		 */
		@Override
		public void updateItem(String value, boolean empty) {
			List<String> itemList = null;
			boolean editable = false;
			
			if (!empty) {
				EntityFacetSelection facetSelection = (EntityFacetSelection) getTableRow().getItem();
				itemList = (facetSelection == null) ? null : facetSelection.getFacetNames(); 
				
				editable = (itemList != null) && (itemList.size() > 1);
			}
			setEditable( editable );
			setStyle( editable ? "-fx-font-weight:bold;" : null );
			super.updateItem( value, empty );
		}
		
	}
	
    /**
     * Cell factory for the original tree view component.
     */
    @SuppressWarnings("squid:MaximumInheritanceDepth") // Unavoidable since the base class is from core JavaFXx
    private class OriginalTreeViewCellFactory extends StyledTreeCell<DOMTreeOriginalNode> {
        
        /**
         * Default constructor.
         */
        public OriginalTreeViewCellFactory() {
            setOnDragDetected( mouseEvent -> {
                TreeItem<DOMTreeOriginalNode> item = getTreeItem();
                
                if ((item != null) && (item.getValue() != null)) {
                    Dragboard dragboard = startDragAndDrop( TransferMode.COPY );
                    Map<DataFormat,Object> dbContent = new HashMap<>();
                    
                    dragItem = item;
                    dragId = UUID.randomUUID().toString();
                    dbContent.put( DRAG_FORMAT, dragId );
                    dragboard.setContent( dbContent );
                }
            });
        }
        
        /**
         * @see org.opentravel.exampleupgrade.StyledTreeCell#getConditionalStyleClasses()
         */
        @Override
        protected List<String> getConditionalStyleClasses() {
            return ReferenceStatus.getAllStyleClasses();
        }
        
        /**
         * @see org.opentravel.exampleupgrade.StyledTreeCell#getConditionalStyleClass()
         */
        @Override
        protected String getConditionalStyleClass() {
            TreeItem<DOMTreeOriginalNode> item = getTreeItem();
            return (item == null) ? null : item.getValue().getReferenceStatus().getStyleClass();
        }
    }
    
    /**
     * Cell factory for the original tree view component.
     */
    @SuppressWarnings("squid:MaximumInheritanceDepth") // Unavoidable since the base class is from core JavaFXx
    private class UpgradedTreeViewCellFactory extends StyledTreeCell<DOMTreeUpgradeNode> {
        
        /**
         * Default constructor.
         */
        public UpgradedTreeViewCellFactory() {
            setOnDragOver( dragEvent -> {
                DOMTreeUpgradeNode value = getItem();
                
                if ((value != null) && (!ExampleMatchType.isMatch( value.getMatchType() )
                        || (value.getMatchType() == ExampleMatchType.MANUAL))) {
                    dragEvent.acceptTransferModes( TransferMode.COPY );
                    setEffect( new Lighting() );
                }
            });
            setOnDragExited( dragEvent -> Platform.runLater( () -> setEffect( null ) ) );
            setOnDragDropped( this::handleDragDropEvent );
            setOnMouseReleased( event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    TreeItem<DOMTreeUpgradeNode> selectedItem = getTreeItem();
                    
                    if (selectedItem != null) {
                        displayContextMenu( selectedItem, event.getScreenX(), event.getScreenY() );
                    }
                }
            });
            setOnMousePressed( event -> {
                if (upgradeContextMenu != null) {
                    upgradeContextMenu.hide();
                    upgradeContextMenu = null;
                }
            });
        }

        /**
         * Called when a drag/drop event occurs within this cell.
         * 
         * @param dragEvent  the drag event that was dropped on this cell
         */
        private void handleDragDropEvent(DragEvent dragEvent) {
            Dragboard dragboard = dragEvent.getDragboard();
            TreeItem<DOMTreeOriginalNode> origDragItem = ExampleUpgradeController.this.dragItem;
            TreeItem<DOMTreeUpgradeNode> dropItem = getTreeItem();
            
            if ((dropItem != null) && (dropItem.getValue() != null)
                    && dragboard.hasContent( DRAG_FORMAT )) {
                Object dropId = dragboard.getContent( DRAG_FORMAT );
                
                if (dropId.equals( dragId )) {
                    ExampleUpgradeController.this.dragItem = null;
                    handleDragDropEvent( origDragItem, dropItem );
                    dragEvent.setDropCompleted( true );
                }
            }
        }
        
        /**
         * @see org.opentravel.exampleupgrade.StyledTreeCell#getConditionalStyleClasses()
         */
        @Override
        protected List<String> getConditionalStyleClasses() {
            return ExampleMatchType.getAllStyleClasses();
        }
        
        /**
         * @see org.opentravel.exampleupgrade.StyledTreeCell#getConditionalStyleClass()
         */
        @Override
        protected String getConditionalStyleClass() {
            TreeItem<DOMTreeUpgradeNode> item = getTreeItem();
            return (item == null) ? null : item.getValue().getMatchType().getStyleClass();
        }
        
        /**
         * Displays the context menu, allowing the user to select different options
         * depending upon the state of the selected item.
         * 
         * @param selectedItem  the tree item for which to display the context menu
         * @param screenX  the screen X coordinate where the context menu should be displayed
         * @param screenY  the screen Y coordinate where the context menu should be displayed
         */
        private void displayContextMenu(TreeItem<DOMTreeUpgradeNode> selectedItem,
                double screenX, double screenY) {
            DOMTreeUpgradeNode selectedNode = selectedItem.getValue();
            List<MenuItem> menuItems = new ArrayList<>();
            MenuItem menuItem;
            
            switch (selectedNode.getMatchType()) {
                case MISSING:
                    menuItem = new MenuItem( "Auto-Generate Content" );
                    menuItem.setOnAction( e -> handleAutoGenerateContent( selectedItem ) );
                    menuItems.add( menuItem );
                    break;
                case NONE:
                    menuItem = new MenuItem( "Re-Generate Content" );
                    menuItem.setOnAction( e -> handleAutoGenerateContent( selectedItem ) );
                    menuItems.add( menuItem );
                    menuItem = new MenuItem( "Clear Content" );
                    menuItem.setOnAction( e -> handleClearContent( selectedItem ) );
                    menuItems.add( menuItem );
                    break;
                case MANUAL:
                    menuItem = new MenuItem( "Clear Content" );
                    menuItem.setOnAction( e -> handleClearContent( selectedItem ) );
                    menuItems.add( menuItem );
                    break;
                default:
                    break;
            }
            
            if (!menuItems.isEmpty()) {
                upgradeContextMenu = new ContextMenu();
                
                upgradeContextMenu.getItems().addAll( menuItems );
                upgradeContextMenu.show( upgradedTreeView, screenX, screenY );
                upgradeContextMenu.setOnAction( e -> upgradeContextMenu = null );
            }
        }
        
        /**
         * Handles a drag-n-drop event in which a node from the original DOM tree is dropped
         * onto the upgrade tree.
         * 
         * @param originalItem  the tree item from the original DOM document
         * @param upgradeItem  the tree item from the upgrade DOM document
         */
        private void handleDragDropEvent(TreeItem<DOMTreeOriginalNode> originalItem,
                TreeItem<DOMTreeUpgradeNode> upgradeItem) {
            try {
                TreeItem<DOMTreeUpgradeNode> newUpgradeItem =
                        new UpgradeTreeBuilder( upgradeDocument, getExampleOptions() )
                                .replaceUpgradeDOMBranch( upgradeItem, originalItem.getValue().getDomNode() );
                
                if (newUpgradeItem.getParent() == null) {
                    upgradedTreeView.setRoot( newUpgradeItem );
                }
                updatePreviewPane( false );
                refreshBranch( originalItem );
                upgradeDocumentDirty = true;
                
            } catch (Exception e) {
                Alert errorDialog = new Alert( AlertType.ERROR );
                
                log.error( "Error handling drag/drop event.", e );
                errorDialog.setTitle( "Error" );
                errorDialog.setHeaderText( null );
                errorDialog.setContentText( HelperUtils.getErrorMessage( e ) );
                errorDialog.showAndWait();
            }
        }
        
        /**
         * Called when the user has elected to auto-generate content for a missing
         * node in the upgrade tree.
         * 
         * @param upgradeItem  the upgrade tree item from which to start auto-generating content
         */
        private void handleAutoGenerateContent(TreeItem<DOMTreeUpgradeNode> upgradeItem) {
            try {
                TreeItem<DOMTreeUpgradeNode> newUpgradeItem =
                        new UpgradeTreeBuilder( upgradeDocument, getExampleOptions() )
                                .replaceUpgradeDOMBranch( upgradeItem, null );
                
                if (newUpgradeItem.getParent() == null) {
                    upgradedTreeView.setRoot( newUpgradeItem );
                }
                updatePreviewPane( false );
                upgradeDocumentDirty = true;
                
            } catch (Exception e) {
                Alert errorDialog = new Alert( AlertType.ERROR );
                
                log.error( "Error generating example content.", e );
                errorDialog.setTitle( "Error" );
                errorDialog.setHeaderText( null );
                errorDialog.setContentText( HelperUtils.getErrorMessage( e ) );
                errorDialog.showAndWait();
            }
        }
        
        /**
         * Called when the user has elected to clear the content of a node in
         * the upgrade tree.
         * 
         * @param upgradeItem  the upgrade tree item from which to start auto-generating content
         */
        private void handleClearContent(TreeItem<DOMTreeUpgradeNode> upgradeItem) {
            TreeItem<DOMTreeUpgradeNode> newUpgradeItem =
                    new UpgradeTreeBuilder( upgradeDocument, getExampleOptions() )
                            .clearUpgradeDOMBranch( upgradeItem );
            
            if (newUpgradeItem.getParent() == null) {
                upgradedTreeView.setRoot( newUpgradeItem );
            }
            updatePreviewPane( false );
            upgradeDocumentDirty = true;
        }
        
    }
}
