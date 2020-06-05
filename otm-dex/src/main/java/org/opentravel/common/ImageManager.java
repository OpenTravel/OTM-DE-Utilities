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

package org.opentravel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.model.OtmObject;

import java.util.EnumMap;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Manage access to icons and images.
 * 
 * @author dmh
 *
 */
public class ImageManager {
    private static Log log = LogFactory.getLog( ImageManager.class );

    public enum Icons {
        APPLICATION("/icons/alt_window_16.gif"),
        ALIAS("/icons/alias.gif"),
        BUSINESS("/icons/BusinessObject.png"),
        ENUMERATION_OPEN("/icons/EnumerationOpen.gif"),
        ENUMERATION_CLOSED("/icons/EnumerationClosed.gif"),
        ENUMERATION_VALUE("/icons/EnumerationValue.jpg"),
        ELEMENT("/icons/Element.gif"),
        ELEMENTREF("/icons/ElementRef.gif"),
        IDATTR("/icons/Attribute.jpg"),
        IDREFATTR("/icons/Id_attr_ref.gif"),
        IDREFELE("/icons/Id_ele_ref.gif"),
        Error("/icons/error.gif"),
        LIBRARY("/icons/library.png"),
        CORE("/icons/CoreObject.gif"),
        CHOICE("/icons/Choice.gif"),
        FACET("/icons/Facet.gif"),
        FACET_CONTEXTUAL("/icons/Facet-contextual.gif"),
        FACET_CONTRIBUTED("/icons/Facet-contributed.gif"),
        ATTRIBUTE("/icons/Attribute.jpg"),
        INDICATOR("/icons/Indicator.gif"),
        INDICATORELEMENT("/icons/IndicatorElement.gif"),
        NAMESPACEFACET("/icons/namespace.gif"),
        OPERATION("/icons/Operation.jpg"),
        RESOURCE("/icons/ResourceObject.gif"),
        RESOURCE_ACTION("/icons/ResourceAction.gif"),
        RESOURCE_PARAMETER("/icons/ResourceParameterGroup.gif"),
        RESOURCE_PARAMETERGROUP("/icons/ResourceParameter.gif"),
        RESOURCE_PARENTREF("/icons/ResourceParentRef.png"),
        RESOURCE_REQUEST("/icons/ResourceRequest.gif"),
        RESOURCE_RESPONSE("/icons/ResourceResponse.gif"),
        RUN("/icons/run.gif"),
        SERVICE("/icons/Service.gif"),
        SIMPLE("/icons/SimpleObject.gif"),
        V_OK("/icons/checkmark.png"),
        V_ERROR("/icons/error_st_obj.gif"),
        V_WARN("/icons/warning_st_obj.gif"),
        VWA("/icons/VWA.gif"),
        XSD_SIMPLE("/icons/XSDSimpleType.gif"),
        LOCK("/icons/lock.png"),
        UNLOCK("icons/unlock.png");
        private String label; // User displayed value

        Icons(String label) {
            this.label = label;
        }
    }

    boolean initalized = false;
    private static Map<Icons,Image> iconMap = new EnumMap<>( Icons.class );

    /**
     * Use primary stage icons. Will throw npe if not initialized.
     */
    // public ImageManager() {
    // // Only used as to run get(OtmObject)
    // }

    public ImageManager(Stage primaryStage) {
        if (!initalized) {
            // Load the application icon in the foreground, the rest in the background
            if (primaryStage != null)
                primaryStage.getIcons().add( new Image( Icons.APPLICATION.label ) );

            // All icons must be loaded into the stage and retained for reuse
            Image image;
            for (Icons icon : Icons.values()) {
                try {
                    if (icon == Icons.APPLICATION)
                        continue;
                    // image = new Image( icon.label );
                    // Control height, width, ratio, smooth resize, in background
                    image = new Image( icon.label, 16, 16, true, true, true );
                    if (primaryStage != null)
                        primaryStage.getIcons().add( image );
                    iconMap.put( icon, image );
                } catch (Exception e) {
                    log.error( "Could not create image: " + e.getLocalizedMessage() );
                }
            }
        }
        initalized = true;
    }

    /**
     * Preferred method for getting an image view to represent an OTM object.
     * 
     * @param otm OtmObject to select which type of icon
     * @return new imageView containing the image associated with the icon or null if no icon image is found
     */
    public static ImageView get(OtmObject otm) {
        return otm != null && getImage( otm.getIconType() ) != null ? new ImageView( getImage( otm.getIconType() ) )
            : null;
    }

    /**
     * Get an image view for a non-OTM object.
     * 
     * @see #get(OtmObject)
     * 
     * @param icon is one of the icon types listed in the enumeration
     * @return a JavaFX node for the icon
     */
    public static Image getImage(Icons icon) {
        return icon != null ? iconMap.get( icon ) : null;
        // return icon != null ? new Image(getClass().getResourceAsStream(icon.label)) : null;
    }

    /**
     * Get an image view for a non-OTM object.
     * 
     * @see #get(OtmObject)
     * 
     * @param icon is one of the icon types listed in the enumeration
     * @return a JavaFX node for the icon
     */
    public static ImageView get(Icons icon) {
        return new ImageView( getImage( icon ) );
    }

    // // TEST - make a map of icon type and Image - use that map in getView()
    // // TODO - make this a child of modelManager and choose ONE api method
    // @Deprecated
    // public Image get_OLD(Icons icon) {
    // return icon != null ? iconMap.get( icon ) : null;
    // // return icon != null ? new Image(getClass().getResourceAsStream(icon.label)) : null;
    // }
    //
    // /**
    // * Preferred method for getting an image view to represent an OTM object.
    // *
    // * @param element OtmObject to select which type of icon
    // * @return new imageView containing the image associated with the icon or null if no icon image is found
    // */
    // @Deprecated
    // public ImageView get_OLD(OtmObject element) {
    // return get_OLD( element.getIconType() ) != null ? new ImageView( get_OLD( element.getIconType() ) ) : null;
    // }


    // /**
    // * Get an image view for a non-OTM object.
    // *
    // * @see #get_OLD(OtmObject)
    // *
    // * @param icon is one of the icon types listed in the enumeration
    // * @return a JavaFX node for the icon
    // */
    // @Deprecated
    // public ImageView getViewX(Icons icon) {
    // // Image i = get(icon);
    // // ImageView iv = new ImageView(i);
    // return new ImageView( get_OLD( icon ) );
    // }

    // /**
    // * @param Image
    // * from OtmModelElement.getIcon()
    // *
    // * @return a javafx node for the icon
    // */
    // @Deprecated
    // public ImageView getView(Image icon) {
    // return new ImageView(icon);
    // }

    // /**
    // * get an image view to represent an OTM object.
    // *
    // * @param OtmModelElement
    // *
    // * @return a JavaFX node for the icon
    // */
    // @Deprecated
    // public ImageView getView(OtmObject element) {
    // return new ImageView( element.getIcon() );
    // }

    // Image imageOk = new Image(getClass().getResourceAsStream("/icons/BusinessObject.png"));
    // Image error = new Image(getClass().getResourceAsStream("/icons/error.gif"));

}
