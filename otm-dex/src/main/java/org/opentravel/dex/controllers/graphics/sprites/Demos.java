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

package org.opentravel.dex.controllers.graphics.sprites;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

/**
 * Graphics Display Object (Sprite) for containing OTM library members.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class Demos {
    private static Logger log = LogManager.getLogger( Demos.class );

    private Demos() {
        // NO-OP static methods

        // gc = canvas.getGraphicsContext2D();
        // gc.setFill( Color.gray( 0.6 ) );
        // gc.fillRect( 0, 0, canvas.getWidth(), canvas.getHeight() );

        // double scenex = canvas.getScene().getX();
        // double sceney = canvas.getScene().getY();
        // double layX = canvas.getLayoutX();
        // double transX = canvas.getTranslateX();

        // bind the dimensions when the user resizes the window.
        // canvas.widthProperty().bind(primaryStage.widthProperty());
        // canvas.heightProperty().bind(primaryStage.heightProperty());

        // Set the Style-properties of the Pane
        // root.setStyle("-fx-padding: 10;" +
        // "-fx-border-style: solid inside;" +
        // "-fx-border-width: 2;" +
        // "-fx-border-insets: 5;" +
        // "-fx-border-radius: 5;" +
        // "-fx-border-color: blue;");

        // gc.setStroke( Color.GREEN );
        // gc.setLineWidth( 10 )
    }

    public static void postDemo(GraphicsContext gc) {
        gc.setFill( Color.GREEN );
        gc.setStroke( Color.BLUE );
        gc.setLineWidth( 5 );
        // Draw a line
        gc.strokeLine( 40, 10, 10, 40 );
        // Draw two circles
        gc.fillOval( 10, 60, 30, 30 );
        gc.strokeOval( 60, 60, 30, 30 );
        // Draw two rectangles with horizontal space
        gc.fillRoundRect( 140, 60, 30, 30, 10, 10 );
        gc.strokeRoundRect( 190, 60, 30, 30, 10, 10 );

        gc.fillArc( 10, 110, 30, 30, 45, 240, ArcType.OPEN );
        gc.fillArc( 60, 110, 30, 30, 45, 240, ArcType.CHORD );
        gc.fillArc( 110, 110, 30, 30, 45, 240, ArcType.ROUND );

        gc.strokeArc( 10, 160, 30, 30, 45, 240, ArcType.OPEN );
        gc.strokeArc( 60, 160, 30, 30, 45, 240, ArcType.CHORD );
        gc.strokeArc( 110, 160, 30, 30, 45, 240, ArcType.ROUND );

        gc.fillPolygon( new double[] {10, 40, 10, 40}, new double[] {210, 210, 240, 240}, 4 );
        gc.strokePolygon( new double[] {60, 90, 60, 90}, new double[] {210, 210, 240, 240}, 4 );
        gc.strokePolyline( new double[] {110, 140, 110, 140}, new double[] {210, 210, 240, 240}, 4 );

        gc.strokeText( "Stoked text", 10, 270 );
        gc.fillText( "Text test", 90, 270 );
        gc.setFont( new Font( "Arial", 24 ) );

        gc.setStroke( Color.BROWN );
        gc.setLineWidth( 10 );
        gc.beginPath();
        gc.lineTo( 40, 40 );
        gc.stroke();

        gc.setStroke( Color.CRIMSON );
        gc.strokeLine( 20, 300, 120, 300 );

        Line line1 = new Line();
        line1.setStartX( 20 );
        line1.setStartY( 280 );
        line1.setEndX( 120 );
        line1.setEndY( 280 );

    }

    public static void postAnimation(GraphicsContext gc) {
        AnimationTimer timer = new AnimationTimer() {
            double x = 0;
            double y = 0;

            @Override
            public void handle(long now) {
                postSmileyFace( gc, x, y );
                x += 10;
                y += 10;
                log.debug( "Posted sprite" );

                if (x > 300)
                    stop();
            }
        };
        timer.start();
    }

    public static void postSmileyFace(GraphicsContext gc, double x, double y) {
        if (gc != null) {
            Paint saveFill = gc.getFill();
            Paint saveStroke = gc.getStroke();
            double saveLW = gc.getLineWidth();

            gc.setFill( Color.DARKSLATEBLUE );
            gc.setStroke( Color.LIGHTSTEELBLUE );
            gc.setLineWidth( 5 );

            gc.strokeOval( x + 100, y + 50, 200, 200 );
            gc.fillOval( x + 155, y + 100, 10, 20 );
            gc.fillOval( x + 230, y + 100, 10, 20 );
            gc.strokeArc( x + 150, y + 160, 100, 50, 180, 180, ArcType.OPEN );

            gc.setFill( saveFill );
            gc.setStroke( saveStroke );
            gc.setLineWidth( saveLW );
        }
    }

    public static void drawLines(GraphicsContext gc, double x, double y) {
        // gc.beginPath();
        gc.moveTo( x, y );

        gc.strokeOval( x - 5, y - 5, 10, 10 );
        gc.lineTo( x + 50, y + 50 );
        // gc.quadraticCurveTo( x + 40, 40, 80, 80 );
        gc.strokeOval( x + 45, y + 45, 10, 10 );

        gc.quadraticCurveTo( x + 60, y + 90, x + 80, y + 80 );
        gc.strokeOval( x + 80, y + 80, 10, 10 );
        //
        gc.bezierCurveTo( x + 100, y + 120, x + 120, y + 140, x + 140, y + 140 );
        gc.strokeOval( x + 140, y + 140, 10, 10 );
        //
        // // gc.arc(
        // gc.arcTo( x + 150, y + 150, x + 180, y + 150, 50 );
        // gc.strokeOval( x + 180, y + 180, 10, 10 );

        // appendSVGPath(),
        gc.stroke();
        // gc.closePath();
        // rect()
    }

}
