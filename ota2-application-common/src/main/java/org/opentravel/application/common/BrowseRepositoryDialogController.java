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

package org.opentravel.application.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the dialog in which the user can select an OTM library from
 * a remote repository.
 */
public class BrowseRepositoryDialogController {
	
	private static final String FXML_FILE = "/browse-repository.fxml";
	
	private Stage dialogStage;
	private boolean okSelected = false;
	private RepositoryItemType itemTypeFilter;
	private RepositoryItem selectedRepositoryItem;
	
	@FXML private TreeView<RepositoryTreeNode> repositoryTreeView;
	@FXML private Button okButton;
	
	/**
	 * Initializes the dialog stage and controller used to select an OTM library
	 * or release from a remote repository.
	 * 
	 * @param title  the title of the dialog box
	 * @param itemTypeFilter  the type filter to apply for repository items
	 * @param stage  the stage that will own the new dialog
	 * @return BrowseRepositoryDialogController
	 */
	public static BrowseRepositoryDialogController createDialog(
			String title, RepositoryItemType itemTypeFilter, Stage stage) {
		BrowseRepositoryDialogController controller = null;
		try {
			FXMLLoader loader = new FXMLLoader( BrowseRepositoryDialogController.class.getResource( FXML_FILE ) );
			Parent page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( title );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.initOwner( stage );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.setDialogStage( dialogStage );
			controller.setItemTypeFilter( itemTypeFilter );
			controller.initializeTreeView();
			
		} catch (IOException e) {
			e.printStackTrace( System.out );
		}
		return controller;
	}
	
	/**
	 * Called when the user clicks the Ok button to confirm their library
	 * selection.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOk(ActionEvent event) {
		okSelected = true;
		dialogStage.close();
	}
	
	/**
	 * Called when the user clicks the Ok button to cancel their library
	 * selection.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectCancel(ActionEvent event) {
		dialogStage.close();
	}
	
	/**
	 * Called when a tree item is expanded to determine if lazy initialization is
	 * still required.
	 * 
	 * @param treeItem  the tree item that was expanded
	 */
	private void handleTreeItemExpand(TreeItem<RepositoryTreeNode> treeItem) {
		if (treeItem.getChildren().size() == 1) {
			TreeItem<RepositoryTreeNode> childItem = treeItem.getChildren().get( 0 );
			RepositoryTreeNode treeNode = childItem.getValue();
			
			if (treeNode.tempNode) {
				try {
					List<RepositoryItem> repoItemList = treeNode.repository.listItems(
							treeNode.baseNS, TLLibraryStatus.DRAFT, false, itemTypeFilter );
					
					// Remove the temporary item and replace it with OTM library items
					treeItem.getChildren().clear();
					
					for (RepositoryItem repoItem : repoItemList) {
						Image icon = RepositoryItemType.RELEASE.isItemType( repoItem.getFilename() )
										? Images.releaseIcon : Images.libraryIcon;
						TreeItem<RepositoryTreeNode> libraryItem = new TreeItem<>(new RepositoryTreeNode(
								repoItem.getFilename() + " (" + repoItem.getVersion() + ")", repoItem ),
								new ImageView( icon ) );
						
						treeItem.getChildren().add( libraryItem );
					}
					
				} catch (RepositoryException e) {
					e.printStackTrace( System.out );
				}
				
			}
		}
	}
	
	/**
	 * Called when a tree item is selected to determine whether a library item
	 * has been selected by the user.
	 * 
	 * @param treeItem  the tree item that was selected
	 */
	private void handleTreeItemSelection(TreeItem<RepositoryTreeNode> treeItem) {
		selectedRepositoryItem = treeItem.getValue().item;
		okButton.setDisable( selectedRepositoryItem == null );
	}
	
	/**
	 * Initializes the contents of the repository tree view.
	 */
	private void initializeTreeView() {
		try {
			RepositoryManager manager = RepositoryManager.getDefault();
			List<RemoteRepository> remoteRepositories = manager.listRemoteRepositories();
			TreeItem<RepositoryTreeNode> rootItem = new TreeItem<>(
					new RepositoryTreeNode( "OTM Repositories", null ),
					new ImageView( Images.rootIcon ) );
			
			for (RemoteRepository repository : remoteRepositories) {
				boolean repoAvailable = true;
				List<String> baseNamespaces;
				
				try {
					baseNamespaces = repository.listBaseNamespaces();
					
				} catch (RepositoryException e) {
					baseNamespaces = new ArrayList<>();
					repoAvailable = false;
				}
				String repoDisplayName = repository.getDisplayName() + (repoAvailable ? "" : " (Unavailable)");
				TreeItem<RepositoryTreeNode> repoItem = new TreeItem<>(
						new RepositoryTreeNode( repoDisplayName, null ),
						new ImageView( repoAvailable ? Images.repositoryIcon : Images.errorIcon ) );
				
				if (repoAvailable) {
					for (String rootNS : repository.listRootNamespaces()) {
						TreeItem<RepositoryTreeNode> rootNSItem = new TreeItem<>(
								new RepositoryTreeNode( rootNS, null ),
								new ImageView( Images.rootNSIcon ) );
						
						for (String baseNS : baseNamespaces) {
							if (!baseNS.startsWith( rootNS )) continue;
							String baseNSLabel = baseNS.equals( rootNS ) ? "/" : baseNS.substring( rootNS.length() );
							TreeItem<RepositoryTreeNode> baseNSItem = new TreeItem<>(
									new RepositoryTreeNode( baseNSLabel, null ),
									new ImageView( Images.baseNSIcon ) );
							
							rootNSItem.getChildren().add( baseNSItem );
							baseNSItem.getChildren().add( new TreeItem<>( new RepositoryTreeNode( repository, baseNS ) ) );
							baseNSItem.expandedProperty().addListener( new ChangeListener<Boolean>() {
								@SuppressWarnings("unchecked")
								public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
									if ((newValue != null) && newValue) {
										BooleanProperty property = (BooleanProperty) observable;
										handleTreeItemExpand( (TreeItem<RepositoryTreeNode>) property.getBean() );
									}
								}
							});
						}
						repoItem.getChildren().add( rootNSItem );
					}
				}
				rootItem.getChildren().add( repoItem );
			}
			repositoryTreeView.setRoot( rootItem );
			repositoryTreeView.getSelectionModel().selectedItemProperty()
				.addListener( new ChangeListener<TreeItem<RepositoryTreeNode>>() {
					public void changed(ObservableValue<? extends TreeItem<RepositoryTreeNode>> observable,
							TreeItem<RepositoryTreeNode> oldValue, TreeItem<RepositoryTreeNode> newValue) {
						handleTreeItemSelection( newValue );
					}
				});
			repositoryTreeView.setOnMouseClicked( e -> {
				if (e.getClickCount() >  1) selectOk( null );
			});
			okButton.setDisable( true );
			
		} catch (RepositoryException e) {
			e.printStackTrace( System.out );
		}
	}

	/**
	 * Returns true if the user clicked the 'Ok' button to close
	 * the dialog.
	 *
	 * @return boolean
	 */
	public boolean isOkSelected() {
		return okSelected;
	}

	/**
	 * Returns the repository item (OTM library) that was selected by the user.  The
	 * return value of this method will only be non-null if the user selected an OTM
	 * library and clicked the 'Ok' button.
	 *
	 * @return RepositoryItem
	 */
	public RepositoryItem getSelectedRepositoryItem() {
		return selectedRepositoryItem;
	}
	
	/**
	 * Assigns the type of repository item that should be included in the
	 * filtered results.  If null, no filter will be applied.
	 * 
	 * @param itemTypeFilter  the filter to apply for repository item types
	 */
	public void setItemTypeFilter(RepositoryItemType itemTypeFilter) {
		this.itemTypeFilter = itemTypeFilter;
	}
	
	/**
	 * Assigns the stage for the dialog.
	 *
	 * @param dialogStage  the dialog stage to assign
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	/**
	 * @see javafx.stage.Stage#showAndWait()
	 */
	public void showAndWait() {
		dialogStage.showAndWait();
	}

	/**
	 * Encapsulates all information related to a single <code>TreeItem</code> in
	 * the repository tree.
	 *
	 * @author S. Livezey
	 */
	private static class RepositoryTreeNode {
		
		public String label;
		public RepositoryItem item;
		
		public boolean tempNode;
		public String baseNS;
		public RemoteRepository repository;
		
		/**
		 * Constructor that creates a temporary node with the given repository
		 * and base namespace to be used during lazy initialization.
		 * 
		 * @param repository  the remote repository to use for namespace lookups
		 * @param baseNS  the base namespace to use during lazy initialization
		 */
		public RepositoryTreeNode(RemoteRepository repository, String baseNS) {
			this.tempNode = true;
			this.repository = repository;
			this.baseNS = baseNS;
		}
		
		/**
		 * Full constructor.
		 * 
		 * @param label  the label for the tree item
		 * @param item  the repository item associated with this node (null for non-libraries)
		 */
		public RepositoryTreeNode(String label, RepositoryItem item) {
			this.tempNode = false;
			this.label = label;
			this.item = item;
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return tempNode ? "Retrieving Data..." : label;
		}
		
	}
	
}
