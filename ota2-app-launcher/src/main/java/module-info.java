module org.opentravel.apps.launcher {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires org.opentravel.apps.examplehelper;
    requires org.opentravel.apps.diffutil;
    requires org.opentravel.apps.releaseeditor;
    requires org.opentravel.apps.upversionhelper;
    requires org.opentravel.apps.exampleupgrade;
    requires org.opentravel.apps.messagevalidator;
    requires org.opentravel.apps.modelcheck;
    requires org.opentravel.apps.otmdex;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires commons.lang3;
    requires org.apache.logging.log4j;

    opens org.opentravel.launcher;

    uses org.opentravel.application.common.OTA2ApplicationProvider;

}
