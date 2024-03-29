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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.graphics.sprites.DexSprite;
import org.opentravel.dex.controllers.graphics.sprites.MemberSprite;
import org.opentravel.dex.controllers.graphics.sprites.SettingsManager;
import org.opentravel.dex.controllers.graphics.sprites.SpriteManager;
import org.opentravel.dex.controllers.graphics.sprites.rectangles.ColumnRectangle;
import org.opentravel.dex.events.DexEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObjects;
import org.opentravel.objecteditor.UserSettings;

import java.util.ArrayList;
import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Manage the Graphics display
 * 
 * @author dmh
 */
public class GraphicsCanvasController extends DexIncludedControllerBase<OtmObject> {
    private static Logger log = LogManager.getLogger( GraphicsCanvasController.class );

    public static final Font DEFAULT_FONT = new Font( "Monospaced", 15 );
    public static final Font DEFAULT_FONT_ITALIC = Font.font( "Monospaced", FontWeight.NORMAL, FontPosture.ITALIC, 15 );

    private static final EventType[] subscribedEvents = {DexModelChangeEvent.MODEL_CHANGED,
        DexMemberSelectionEvent.MEMBER_SELECTED, DexMemberSelectionEvent.DOUBLE_CLICK_MEMBER_SELECTED};
    private static final EventType[] publishedEvents = {DexMemberSelectionEvent.MEMBER_SELECTED};

    @FXML
    private AnchorPane graphicsPane;
    @FXML
    private VBox graphicsVBox;

    private DexMainController parentController = null;
    private SpriteManager spriteManager;
    private SettingsManager settingsManager;

    private Pane spritePane;
    private ScrollPane scrollPane = null;

    private Canvas backgroundCanvas;
    private GraphicsContext backgroundGC;

    private Canvas doodleCanvas = null;
    private GraphicsContext dgc = null;

    private boolean ignoreEvents = false;

    private UserSettings globalSettings;
    private double globalSize = 5;
    private boolean isLocked = false;
    private boolean tracking = true;
    private boolean showDomains = true;

    public GraphicsCanvasController() {
        super( subscribedEvents, publishedEvents );
    }

    @Override
    public void checkNodes() {
        if (graphicsPane == null)
            throw new IllegalStateException( "Null pane in graphics controller." );
        if (graphicsVBox == null)
            throw new IllegalStateException( "Null graphics vBox in graphics controller." );
    }

    @Override
    public void clear() {
        spriteManager.clear();
        backgroundGC.clearRect( 0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight() );
    }

    @Override
    public void configure(DexMainController parent, int viewGroupId) {
        super.configure( parent, viewGroupId );
        eventPublisherNode = graphicsPane;
        parentController = parent;

        // Global User Settings
        globalSettings = parent.getUserSettings();
        if (globalSettings != null) {
            tracking = globalSettings.getGraphicsTracking();
            globalSize = globalSettings.getGraphicsSize();
            showDomains = globalSettings.getGraphicsDomains();
        }

        createToolBar( graphicsVBox ); // to first to have at top

        spritePane = createSpritePane( graphicsVBox );

        // Create a background and bind the dimensions when the user resizes the window.
        backgroundCanvas = new Canvas();
        backgroundCanvas.widthProperty().bind( spritePane.widthProperty() );
        backgroundCanvas.heightProperty().bind( spritePane.heightProperty() );
        spritePane.getChildren().add( backgroundCanvas );
        backgroundGC = backgroundCanvas.getGraphicsContext2D();

        settingsManager = new SettingsManager( spritePane, this, backgroundGC );

        backgroundGC.setFont( settingsManager.getDefaultFont() );
        backgroundGC.setFill( settingsManager.getDefaultFill() );
        backgroundGC.setStroke( settingsManager.getDefaultStroke() );
        backgroundGC.setLineWidth( 1 );

        spriteManager = new SpriteManager( this, settingsManager );

        // log.debug( "Configured graphics canvas." );
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
        spritePane = new Pane();
        scrollPane = new ScrollPane( spritePane );
        parent.getChildren().add( scrollPane );
        // Configure sizes
        VBox.setVgrow( scrollPane, Priority.ALWAYS );
        scrollPane.setMaxWidth( Double.MAX_VALUE );
        return spritePane;
    }

    private ToolBar createToolBar(VBox parent) {
        Button clearB = new Button( "Clear" );
        clearB.setOnAction( e -> clear() );

        Button refreshB = new Button( "Refresh", ImageManager.get( Icons.REFRESH ) );
        refreshB.setOnAction( this::doRefresh );

        Button clipboardB = new Button( "Copy", ImageManager.get( Icons.CLIPBOARD ) );
        clipboardB.setOnAction( e -> snapshot() );

        Separator tSep = new Separator( Orientation.VERTICAL );
        ToggleSwitch trackS = new ToggleSwitch();
        trackS.selectedProperty().addListener( (v, o, n) -> doTrack( n ) );
        trackS.setSelected( tracking );
        trackS.setTooltip( new Tooltip( "Track object selected in members panel." ) );
        Node trackBox = decorateControl( trackS, "Track", null );

        Separator lockSep = new Separator( Orientation.VERTICAL );
        ToggleSwitch lockS = new ToggleSwitch();
        lockS.selectedProperty().addListener( (v, o, n) -> doLock( n ) );
        lockS.setSelected( isLocked );
        lockS.setTooltip( new Tooltip( "Lock display." ) );
        Node lockVBox = decorateControl( lockS, " Lock ", ImageManager.get( Icons.LOCK ) );

        Separator domainSep = new Separator( Orientation.VERTICAL );
        ToggleSwitch domainS = new ToggleSwitch();
        domainS.selectedProperty().addListener( (v, o, n) -> doDomains( n ) );
        domainS.setSelected( showDomains );
        domainS.setTooltip( new Tooltip( "Show domain relationships. (Experimental)" ) );
        Node domainBox = decorateControl( domainS, "Domains ", ImageManager.get( Icons.DOMAIN ) );

        Separator fontSep = new Separator( Orientation.VERTICAL );
        Slider fontS = new Slider( 1, 10, globalSize ); // Min, max, current
        fontS.setShowTickMarks( true );
        fontS.valueProperty().addListener( (v, o, n) -> doSize( n ) );
        Node sizeBox = decorateControl( fontS, " Size ", null );

        Separator dSep = new Separator( Orientation.VERTICAL );
        ToggleSwitch doodleS = new ToggleSwitch( "Draw" );
        doodleS.selectedProperty().addListener( (v, o, n) -> doDoodle( n ) );

        ColorPicker colorP = new ColorPicker();
        colorP.setOnAction( this::doColor );

        ToolBar tb = new ToolBar( clearB, refreshB, clipboardB, tSep, trackBox, lockSep, lockVBox, domainSep, domainBox,
            fontSep, sizeBox, new Separator( Orientation.VERTICAL ), colorP, dSep, doodleS );
        // ToolBar tb = new ToolBar( clearB, refreshB, clipboardB, tSep, trackS, lockSep, lockS, lockI, domainSep,
        // domainS,
        // domainI, fontSep, fontL, fontS, colorP, dSep, doodleS );

        parent.getChildren().add( tb );
        tb.setStyle( "-fx-background-color: #7cafc2" );
        return tb;
    }

    /**
     * Create vbox with hbox to contain the switch/slider and its lable and icon.
     * 
     * @return
     */
    private Node decorateControl(Node tSwitch, String label, ImageView icon) {
        double width = 50;
        VBox sBox = new VBox();
        sBox.setAlignment( Pos.CENTER_RIGHT );
        sBox.setMinWidth( width );

        HBox swBox = new HBox();
        swBox.setAlignment( Pos.CENTER );
        swBox.getChildren().add( tSwitch );
        if (tSwitch instanceof ToggleSwitch)
            swBox.getChildren().add( new Label( "     " ) );
        sBox.getChildren().add( swBox );

        HBox sLabel = new HBox();
        sLabel.setAlignment( Pos.CENTER );
        sLabel.setMinWidth( width );
        sLabel.getChildren().add( new Label( label ) );
        if (icon != null)
            sLabel.getChildren().add( icon );
        sBox.getChildren().add( sLabel );

        return sBox;
    }

    public void doClear() {
        clear();
    }

    public void doCollapse(boolean collapse) {
        spriteManager.setCollapsed( collapse );
    }

    public void doColor(ActionEvent e) {
        if (e.getTarget() instanceof ColorPicker && spriteManager != null) {
            spriteManager.update( ((ColorPicker) e.getTarget()).getValue() );
        }
    }

    /**
     * Let user annotate (doodle) on the drawing surface
     * 
     * @param run
     */
    private void doDoodle(boolean run) {
        if (run) {
            // log.debug( "Starting doodle." );
            if (doodleCanvas == null) {
                doodleCanvas =
                    new Canvas( graphicsVBox.getWidth(), spritePane.getHeight() > 0 ? spritePane.getHeight() : 1000 );
                spritePane.getChildren().add( doodleCanvas );
            }
            dgc = doodleCanvas.getGraphicsContext2D();
            // if (colorPicker != null && colorPicker.getValue() != null)
            // dgc.setStroke( colorPicker.getValue() );
            // else
            dgc.setStroke( Color.DARKRED );
            dgc.setLineWidth( 4 );

            // TODO - Canvas may be too small to capture all mouse clicks
            doodleCanvas.setOnMouseDragged( e -> {
                // log.debug( " doodle drag" );
                dgc.lineTo( e.getX(), e.getY() );
                dgc.stroke();
                e.consume();
            } );
            doodleCanvas.setOnMousePressed( e -> {
                // log.debug( " doodle point" );
                dgc.beginPath();
                dgc.lineTo( e.getX(), e.getY() );
                dgc.stroke();
                e.consume();
            } );
        } else {
            if (dgc != null) {
                dgc.setFill( backgroundGC.getFill() );
                dgc.setStroke( backgroundGC.getStroke() );
                dgc.fillRect( 0, 0, doodleCanvas.getWidth(), doodleCanvas.getHeight() );
                dgc.setLineWidth( 1 );
                spritePane.getChildren().remove( doodleCanvas );
                doodleCanvas = null;
            }
        }
    }

    public void doSize(Number v) {
        // log.debug( "Change font size to: " + v.intValue() );
        spriteManager.update( v.intValue() );

        if (globalSettings != null) {
            globalSettings.setGraphicsSize( v.intValue() );
            globalSettings.save();
            // log.debug( "Size set to: " + tracking );
        }
    }

    public void doLock(boolean lock) {
        isLocked = lock;
    }

    public void doDomains(boolean show) {
        showDomains = show;
        if (globalSettings != null) {
            globalSettings.setGraphicsDomains( showDomains );
            globalSettings.save();
        }
    }

    public void doRefresh(ActionEvent e) {
        // log.debug( "do refresh." );
        spriteManager.update( Color.gray( 0.9 ) );
    }

    public void doTrack(boolean track) {
        this.tracking = track;
        if (globalSettings != null) {
            globalSettings.setGraphicsTracking( tracking );
            globalSettings.save();
            // log.debug( "Tracking set to: " + tracking );
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( "Event received: " + event.getEventType() );
        if (!isLocked) {
            if (!tracking && event.getEventType() == DexMemberSelectionEvent.DOUBLE_CLICK_MEMBER_SELECTED)
                post( ((DexMemberSelectionEvent) event).getMember() );
            else if (tracking && event instanceof DexMemberSelectionEvent)
                post( ((DexMemberSelectionEvent) event).getMember() );
            else if (event instanceof DexModelChangeEvent)
                clear();
            else
                refresh();

            // FIXME - handle object change
        }
    }

    @Override
    @FXML
    public void initialize() {
        // log.debug( "Graphics canvas controller initialized." );
    }

    public boolean isLocked() {
        return isLocked;
    }

    private DexSprite postBase(MemberSprite<?> memberSprite) {
        MemberSprite<?> baseSprite = null;
        if (memberSprite instanceof MemberSprite) {
            OtmLibraryMember member = memberSprite.getMember();
            if (member.getBaseType() instanceof OtmLibraryMember && !(member instanceof OtmContextualFacet)) {
                ColumnRectangle column = memberSprite.getColumn().getPrev();
                boolean collapsed = true;
                baseSprite = spriteManager.add( (OtmLibraryMember) member.getBaseType(), column, collapsed );
                memberSprite.connect().setCollapsed( true );
            }
        }
        return baseSprite;
    }

    // Add member and users and providers
    private void postProviders(MemberSprite<?> memberSprite) {
        if (memberSprite != null) {
            OtmLibraryMember member = memberSprite.getMember();
            Collection<OtmTypeUser> users = new ArrayList<>( member.getDescendantsTypeUsers() );
            for (OtmTypeUser user : users) {
                if (user.getAssignedType() != null && !(user.getAssignedType() instanceof OtmSimpleObjects))
                    memberSprite.addConnection( user );
            }
        }
    }

    private void postUsers(MemberSprite<?> memberSprite) {
        if (memberSprite != null) {
            OtmLibraryMember member = memberSprite.getMember();
            ColumnRectangle column = memberSprite.getColumn().getPrev();
            boolean collapsed = true;

            for (OtmLibraryMember user : member.getWhereUsed())
                if (user != member)
                    spriteManager.add( user, column, collapsed );
        }
    }

    @Override
    public void post(OtmObject o) {
        // log.debug( "Graphics canvas controller posting object: " + o );
        if (o instanceof OtmLibraryMember) {
            OtmLibraryMember member = (OtmLibraryMember) o;
            ColumnRectangle memberColumn = spriteManager.getColumn( 2 );
            ColumnRectangle domainColumn = spriteManager.getColumn( 1 );

            if (tracking) {
                spriteManager.clear();
                if (showDomains)
                    spriteManager.add( member.getLibrary(), domainColumn );
                // TODO - add domain sprites to other posters
            }
            MemberSprite<?> memberS = spriteManager.add( member, memberColumn, false );

            if (memberS != null)
                if (tracking) {
                    // Post the related objects
                    postBase( memberS );
                    postProviders( memberS );
                    postUsers( memberS );
                    // Scroll to origin (0,0)
                    scrollPane.setVvalue( 0 );
                    scrollPane.setHvalue( 0 );
                } else {
                    // Scroll to the new sprite's location
                    double dx = memberS.getBoundaries().getX() / backgroundCanvas.getWidth();
                    double dy = memberS.getBoundaries().getMaxY() / backgroundCanvas.getHeight();
                    scrollPane.setVvalue( scrollPane.getVmin() );
                    scrollPane.setHvalue( dx );
                }
        }
        spriteManager.refresh();
    }

    @Override
    public void publishEvent(DexEvent event) {
        ignoreEvents = true;
        fireEvent( event );
        ignoreEvents = false;
    }

    private void snapshot() {
        WritableImage snapshot = spritePane.snapshot( new SnapshotParameters(), null );
        // saveImage( snapshot );
        Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putImage( snapshot );
        clipboard.setContent( content );
    }

    // This works - needs file picker
    // File file = new File( "C:/Users/dmh/Desktop/Sample Images/test.jpg" );
    // // File file = new File( "C:/Users/dmh/Desktop/test.jpg" );
    // BufferedImage bufferedImage = new BufferedImage( 550, 400, BufferedImage.TYPE_INT_ARGB );
    //
    // private void saveImage(WritableImage snapshot) {
    // BufferedImage image;
    // image = javafx.embed.swing.SwingFXUtils.fromFXImage( snapshot, bufferedImage );
    // try {
    // Graphics2D gd = (Graphics2D) image.getGraphics();
    // gd.translate( scrollPane.getWidth(), scrollPane.getHeight() );
    // ImageIO.write( image, "png", file );
    // } catch (IOException ex) {
    // log.debug( "Error saving image. " + ex );
    // }
    // }

}
