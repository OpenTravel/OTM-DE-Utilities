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

package org.opentravel.dex.controllers.graphics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.graphics.sprites.Demos;
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * Manage the search results display
 * 
 * @author dmh
 */
public class GraphicsCanvasController extends DexIncludedControllerBase<OtmObject> {
    private static Log log = LogFactory.getLog( GraphicsCanvasController.class );

    @FXML
    private Pane spriteArea;
    // @FXML
    // private AnchorPane spritePane;

    @FXML
    private AnchorPane graphicsPane;
    // @FXML
    // private ScrollPane scrollPane;
    @FXML
    private VBox graphicsVBox;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider slider;


    OtmBusinessObject bo;
    Pane graphicsRoot;
    // List<DexSprite> activeSprites = new ArrayList<>();
    DexMainController parentController = null;

    // private GraphicsContext gc;
    private Canvas backgroundCanvas;
    private GraphicsContext backgroundGC;
    // private static final Font DEFAULT_FONT = new Font( "Arial", 14 );
    public static final Font DEFAULT_FONT = new Font( "Monospaced", 15 );
    private static final Paint DEFAULT_STROKE = Color.BLACK;
    static final Paint DEFAULT_FILL = Color.gray( 0.8 );

    // private Color backgroundStrokeColor;

    private SpriteManager spriteManager;

    private boolean ignoreEvents = false;

    private static final EventType[] subscribedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};



    @Override
    public void checkNodes() {
        if (graphicsPane == null)
            throw new IllegalStateException( "Null pane in graphics controller." );
        // if (spriteArea == null)
        // throw new IllegalStateException( "Null sprite pane in graphics controller." );
    }

    public GraphicsCanvasController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void clear() {
        clearCanvas();
        spriteManager.clear();
    }

    @Override
    @FXML
    public void initialize() {
        log.debug( "Graphics canvas controller initialized." );
    }

    @Override
    public void post(OtmObject o) {
        if (!ignoreEvents) {
            log.debug( "Graphics canvas controller posting object: " + o );
            if (o instanceof OtmLibraryMember)
                spriteManager.add( ((OtmLibraryMember) o) );
        }
    }

    @Override
    public void publishEvent(DexEvent event) {
        ignoreEvents = true;
        fireEvent( event );
        ignoreEvents = false;
    }

    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        eventPublisherNode = graphicsPane;
        parentController = parent;

        // Create a new AnchorPane
        Pane spritePane = new Pane();
        // Pane spritePane = spriteArea;
        ScrollPane scrollPane = new ScrollPane( spritePane );

        // fxml only has VBox with toolbar
        graphicsVBox.getChildren().add( scrollPane );
        VBox.setVgrow( scrollPane, Priority.ALWAYS );
        scrollPane.setMaxWidth( Double.MAX_VALUE );
        scrollPane.setContent( spritePane );

        // spriteArea.getChildren().add( scrollPane );

        // vPane.getChildren().add( scrollPane ); // Scroll has to be child for bars to appear

        // // Pane drawingPane = new Pane();
        // // drawingPane.setPrefSize( 800, 800 );
        // // drawingPane.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );
        // // ScrollPane scrollPane = new ScrollPane(drawingPane);
        // scrollPane.setPrefSize( vPane.getWidth(), vPane.getHeight() );
        // scrollPane.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );

        // scrollPane.setViewportBounds( value ); // ???
        // scrollPane.setFitToWidth( true ); // Sizes contents to the scroll bar area
        // scrollPane.setFitToHeight( true );

        // scrollPane.setHmax( 3 ); // Sizes the slider control
        // scrollPane.setHvalue( 2 ); // Moves to the right
        // scrollPane.setStyle( "-fx-focus-color: transparent;" );

        // scrollPane.maxWidthProperty().bind( vPane.widthProperty() );
        // scrollPane.maxHeightProperty().bind( vPane.heightProperty() );
        // scrollPane.setPrefSize( vPane.getPrefWidth(), vPane.getPrefHeight() );
        // spritePane.setPrefSize( vPane.getPrefWidth(), vPane.getPrefHeight() );

        //
        // anchorPane.setPrefSize(1920, 1080);
        // scrollPane.setPrefViewportWidth(1000);
        // scrollPane.setPrefViewportHeight(1000);

        // Fill the AnchorPane with the ScrollPane and set the Anchors to 0.0
        // That way the ScrollPane will take the full size of the Parent of
        // the AnchorPane
        // spritePane.getChildren().add( scrollPane );
        // spritePane.maxWidthProperty().bind( graphicsVBox.widthProperty() );
        // spritePane.maxHeightProperty().bind( graphicsVBox.heightProperty() );
        // AnchorPane.setTopAnchor( scrollPane, 0.0 );
        // AnchorPane.setBottomAnchor( scrollPane, 0.0 );
        // AnchorPane.setLeftAnchor( scrollPane, 0.0 );
        // AnchorPane.setRightAnchor( scrollPane, 0.0 );
        // scrollPane.setHbarPolicy( ScrollBarPolicy.ALWAYS );
        // scrollPane.setVbarPolicy( ScrollBarPolicy.ALWAYS );

        // //Add content ScrollPane
        // scrollPane.getChildren().add(vb2);
        // // Create and use the scroll pane
        // ScrollPane scrollPane = new ScrollPane();
        // scrollPane.setContent( spritePane );
        // scrollPane.setPrefSize( 300, 200 );
        // // scrollPane.setPrefSize( spritePane.getPrefWidth(), spritePane.getPrefHeight() );
        // scrollPane.setHbarPolicy( ScrollBarPolicy.AS_NEEDED );
        // scrollPane.setVbarPolicy( ScrollBarPolicy.AS_NEEDED );
        // graphicsVBox.getChildren().add( scrollPane );
        // VBox.setVgrow( scrollPane, Priority.ALWAYS );
        // scrollPane.setMaxWidth( Double.MAX_VALUE );

        // If i need to just use scroll bars not panes
        // GridPane scrollPane = new GridPane();
        // scrollPane.addColumn(0, canvas, hScroll);
        // scrollPane.add(vScroll, 1, 0);
        // ScrollBar vScroll = createScrollBar(Orientation.VERTICAL, 10000, 300);
        // ScrollBar hScroll = createScrollBar(Orientation.HORIZONTAL, 10000, 300);
        // public static ScrollBar createScrollBar(Orientation orientation, double fullSize, double canvasSize) {
        // ScrollBar sb = new ScrollBar();
        // sb.setOrientation(orientation);
        // sb.setMax(fullSize - canvasSize);
        // sb.setVisibleAmount(canvasSize);
        // return sb;
        // }


        backgroundCanvas = new Canvas();
        // bind the dimensions when the user resizes the window.
        backgroundCanvas.widthProperty().bind( spritePane.widthProperty() );
        backgroundCanvas.heightProperty().bind( spritePane.heightProperty() );
        backgroundGC = backgroundCanvas.getGraphicsContext2D();
        // spriteGrid.setRowIndex( backgroundCanvas, 1 );
        // GridPane.setColumnIndex( backgroundCanvas, 1 );
        // GridPane.setConstraints( backgroundCanvas, 1, 1 );
        spritePane.getChildren().add( backgroundCanvas );

        // // For Fun :)
        // backgroundGC.setFill( Color.GREEN );
        // backgroundGC.setStroke( Color.BLUE );
        // backgroundGC.setLineWidth( 5 );
        // Demos.postSmileyFace( backgroundGC, 20, 20 );
        //
        backgroundGC.setFont( DEFAULT_FONT );
        backgroundGC.setFill( DEFAULT_FILL );
        backgroundGC.setStroke( DEFAULT_STROKE );
        backgroundGC.setLineWidth( 1 );
        spriteManager = new SpriteManager( spritePane, this, backgroundGC );

        //

        // FIXME - Use this to give users a marker for presentations
        // TODO - put in a layer
        // Layers are just canvases added to the pane
        // canvas.setOnMousePressed( e -> {
        // // gc.beginPath();
        // // gc.lineTo( e.getX(), e.getY() );
        // boSprite.set( e.getX(), e.getY() );
        // Canvas spriteCanvas = boSprite.post( gc, this );
        // // spriteCanvas.setLayoutX( e.getX() );
        // // spriteCanvas.setLayoutY( e.getY() );
        // // gc.drawImage( spriteCanvas, e.getX(), e.getY() );
        // // graphicsVBox.getChildren().add( spriteCanvas );
        // graphicsRoot.getChildren().add( spriteCanvas );
        // log.debug( "Posted sprite." );
        // // gc.stroke();
        // // boSprite.set( e.getX(), e.getY() );
        // // ((MemberSprite) boSprite).drawLines( gc );
        // // timer.stop();
        // } );

        slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                doFont( new_val );
            }
        } );

        // fontSlider.valueProperty().addListener( v -> doFont( v ) );
        // TODO - turn this on with a button
        //
        // canvas.setOnMouseDragged( e -> {
        // log.debug( "Do mouse drag " + e.getX() + " " + e.getY() );
        // log.debug( "Do mouse drag scene " + e.getSceneX() + " " + e.getSceneY() );
        // // double offsetX = canvas.getParent().getLayoutX();
        // // log.debug( "offset = " + offsetX );
        // // log.debug( "Do mouse drag screen" + e.getScreenX() + " " + e.getScreenY() );
        // // gc.lineTo( e.getX(), e.getY() );
        // // gc.stroke();
        // } );

        log.debug( "Configured graphics canvas." );
    }

    public void doFont(Number v) {
        log.debug( "Change font size to: " + v.intValue() );
        spriteManager.update( v.intValue() );
    }

    public void clearCanvas() {
        spriteManager.clear();
        backgroundGC.clearRect( 0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight() );
    }

    @FXML
    public void doColor() {
        if (colorPicker != null && spriteManager != null) {
            spriteManager.update( colorPicker.getValue() );
        }
    }

    @FXML
    public void doClear() {
        clear();
    }

    @FXML
    public void doLock() {
        log.debug( "do lock." );
        Demos.postAnimation( backgroundGC );
    }

    @FXML
    public void doRefresh() {
        log.debug( "do refresh." );
        spriteManager.update( Color.gray( 0.9 ) );
        // spriteManager.refresh();

        // Demos.postDemo( backgroundGC );
        // Demos.drawLines( backgroundGC, 0, 0 );
        // Demos.postSmileyFace( backgroundGC, 0, 0 );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( event.getEventType() + " event received. " );
        if (event instanceof DexMemberSelectionEvent)
            post( ((DexMemberSelectionEvent) event).getMember() );
        else
            refresh();
    }


}
