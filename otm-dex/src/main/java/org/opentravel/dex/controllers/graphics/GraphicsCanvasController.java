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
import org.controlsfx.control.ToggleSwitch;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
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
    private ToolBar graphicsToolbar;
    @FXML
    private AnchorPane graphicsPane;
    // @FXML
    // private ScrollPane scrollPane;
    @FXML
    private VBox graphicsVBox;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider fontSlider;


    // Pane graphicsRoot;
    DexMainController parentController = null;

    // private static final Font DEFAULT_FONT = new Font( "Arial", 14 );
    public static final Font DEFAULT_FONT = new Font( "Monospaced", 15 );
    private static final Paint DEFAULT_STROKE = Color.BLACK;
    static final Paint DEFAULT_FILL = Color.gray( 0.8 );
    boolean isLocked;

    // private Color backgroundStrokeColor;

    private SpriteManager spriteManager;
    private ScrollPane scrollPane = null;
    private Canvas backgroundCanvas;
    private GraphicsContext backgroundGC;
    private Canvas doodleCanvas = null;

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
        if (!ignoreEvents && !isLocked) {
            log.debug( "Graphics canvas controller posting object: " + o );
            if (o instanceof OtmLibraryMember) {
                DexSprite<?> s = spriteManager.add( ((OtmLibraryMember) o) );
                if (s != null) {
                    // Scroll to the new sprite's location
                    double dx = s.getBoundaries().getX() / backgroundCanvas.getWidth();
                    double dy = s.getBoundaries().getMaxY() / backgroundCanvas.getHeight();
                    scrollPane.setVvalue( dy );
                    scrollPane.setHvalue( dx );
                }
            }
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

        createToolBar( graphicsVBox );

        Pane spritePane = createSpritePane( graphicsVBox );
        backgroundCanvas = createCanvas( spritePane );
        backgroundGC = backgroundCanvas.getGraphicsContext2D();
        // doodleCanvas = createCanvas( spritePane );

        backgroundGC.setFont( DEFAULT_FONT );
        backgroundGC.setFill( DEFAULT_FILL );
        backgroundGC.setStroke( DEFAULT_STROKE );
        backgroundGC.setLineWidth( 1 );

        spriteManager = new SpriteManager( spritePane, this, backgroundGC );

        log.debug( "Configured graphics canvas." );
    }

    /**
     * Create the background canvas and GC.
     * 
     * @param spritePane to be parent of canvas
     */
    private Canvas createCanvas(Pane spritePane) {
        Canvas canvas = new Canvas();
        // bind the dimensions when the user resizes the window.
        canvas.widthProperty().bind( spritePane.widthProperty() );
        canvas.heightProperty().bind( spritePane.heightProperty() );
        spritePane.getChildren().add( canvas );
        return canvas;
    }

    private ToolBar createToolBar(VBox parent) {
        Button clearB = new Button( "Clear" );
        clearB.setOnAction( this::doClear );

        Button refreshB = new Button( "Refresh" );
        refreshB.setOnAction( this::doRefresh );

        Separator cSep = new Separator( Orientation.VERTICAL );
        ToggleSwitch collapseS = new ToggleSwitch( "Collapse" );
        collapseS.selectedProperty().addListener( (v, o, n) -> doCollapse( n ) );

        Separator lockSep = new Separator( Orientation.VERTICAL );
        ToggleSwitch lockS = new ToggleSwitch( "Lock" );
        ImageView lockI = ImageManager.get( Icons.LOCK );
        lockS.selectedProperty().addListener( (v, o, n) -> doLock( n, lockI ) );

        Separator fontSep = new Separator( Orientation.VERTICAL );
        Label fontL = new Label( "Font" );
        Slider fontS = new Slider( 8, 24, 14 ); // Min, max, current
        fontS.valueProperty().addListener( (v, o, n) -> doFont( n ) );

        // Separator dSep = new Separator( Orientation.VERTICAL );
        // ToggleSwitch doodleS = new ToggleSwitch( "Draw" );
        // doodleS.selectedProperty().addListener( (v, o, n) -> doDoodle( n ) );

        ColorPicker colorP = new ColorPicker();
        colorP.setOnAction( this::doColor );

        ToolBar tb =
            new ToolBar( clearB, refreshB, cSep, collapseS, lockSep, lockS, lockI, fontSep, fontL, fontS, colorP );
        parent.getChildren().add( tb );
        tb.setStyle( "-fx-background-color: #7cafc2" );
        return tb;
    }

    /**
     * Create sprite pane controlled by scroll pane.
     * <p>
     * fxml only has VBox with toolbar
     * 
     * @param parent
     * @return
     */
    private Pane createSpritePane(VBox parent) {
        // Create panes and add to parent
        Pane spritePane = new Pane();
        scrollPane = new ScrollPane( spritePane );
        parent.getChildren().add( scrollPane );
        // Configure sizes
        VBox.setVgrow( scrollPane, Priority.ALWAYS );
        scrollPane.setMaxWidth( Double.MAX_VALUE );
        return spritePane;

        // Other stuff that could be done
        // // drawingPane.setPrefSize( 800, 800 );
        // scrollPane.setHbarPolicy( ScrollBarPolicy.AS_NEEDED );
        // scrollPane.setVbarPolicy( ScrollBarPolicy.AS_NEEDED );
        // // drawingPane.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );
        // scrollPane.setPrefSize( vPane.getWidth(), vPane.getHeight() );
        // scrollPane.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );
        // scrollPane.setViewportBounds( value ); // ???
        // scrollPane.setPrefViewportWidth(1000);
        // scrollPane.setPrefViewportHeight(1000);
        // scrollPane.setFitToWidth( true ); // Sizes contents to the scroll bar area
        // scrollPane.setFitToHeight( true );
        // scrollPane.setHmax( 3 ); // Sizes the slider control
        // scrollPane.setHvalue( 2 ); // Moves to the right
        // scrollPane.setStyle( "-fx-focus-color: transparent;" );
        // VBox.setVgrow( scrollPane, Priority.ALWAYS );
        // scrollPane.setMaxWidth( Double.MAX_VALUE );
        // scrollPane.maxWidthProperty().bind( vPane.widthProperty() );
        // scrollPane.maxHeightProperty().bind( vPane.heightProperty() );
        // scrollPane.setPrefSize( vPane.getPrefWidth(), vPane.getPrefHeight() );
        // spritePane.setPrefSize( vPane.getPrefWidth(), vPane.getPrefHeight() );
        //
        // Fill the AnchorPane with the ScrollPane and set the Anchors to 0.0
        // AnchorPane.setTopAnchor( scrollPane, 0.0 );
        // AnchorPane.setBottomAnchor( scrollPane, 0.0 );
        // AnchorPane.setLeftAnchor( scrollPane, 0.0 );
        // AnchorPane.setRightAnchor( scrollPane, 0.0 );
        // scrollPane.setHbarPolicy( ScrollBarPolicy.ALWAYS );
        // scrollPane.setVbarPolicy( ScrollBarPolicy.ALWAYS );
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void doLock(boolean lock, ImageView icon) {
        // log.debug( "do lock boolean." );
        isLocked = lock;
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

    public void doColor(ActionEvent e) {
        if (e.getTarget() instanceof ColorPicker && spriteManager != null) {
            spriteManager.update( ((ColorPicker) e.getTarget()).getValue() );
        }
    }

    public void doCollapse(boolean collapse) {
        // log.debug( "TODO - colapse all." );
        spriteManager.setCollapsed( collapse );
    }


    // @FXML
    public void doClear(ActionEvent e) {
        clear();
    }
    //
    // @Deprecated
    // @FXML
    // public void doLock(ActionEvent e) {
    // log.debug( "do lock - DEPRECATED." );
    // Demos.postAnimation( backgroundGC );
    // }
    //
    // @FXML
    // public void doRefresh() {
    // log.debug( "do refresh." );
    // spriteManager.update( Color.gray( 0.9 ) );
    // }

    public void doRefresh(ActionEvent e) {
        log.debug( "do refresh." );
        spriteManager.update( Color.gray( 0.9 ) );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (!isLocked) {
            // log.debug( event.getEventType() + " event received. " );
            if (event instanceof DexMemberSelectionEvent)
                post( ((DexMemberSelectionEvent) event).getMember() );
            else
                refresh();
        }
    }

    // FUTURE - Mouse pressed is not dispatched to this handler
    // private void doDoodle(boolean run) {
    // GraphicsContext dgc = doodleCanvas.getGraphicsContext2D();
    // if (run) {
    // log.debug( "Starting doodle." );
    // dgc.setStroke( Color.GREENYELLOW );
    // dgc.setStroke( Color.ORANGERED );
    // dgc.setLineWidth( 4 );
    // dgc.strokeOval( 100, 100, 60, 60 );
    // dgc.fillOval( 100, 100, 40, 40 );
    //
    // doodleCanvas.setOnMouseClicked( e -> {
    // log.debug( " doodle click" );
    // } );
    // doodleCanvas.setOnMousePressed( e -> {
    // log.debug( " doodle point" );
    // dgc.beginPath();
    // dgc.lineTo( e.getX(), e.getY() );
    // // graphicsContext.moveTo(event.getX(), event.getY());
    // dgc.stroke();
    // } );
    // } else {
    // dgc.setStroke( DEFAULT_STROKE );
    // dgc.setLineWidth( 1 );
    // }
    // }
}
