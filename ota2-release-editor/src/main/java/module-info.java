module org.opentravel.apps.releaseeditor {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.opentravel.schemacompiler;
    requires org.slf4j;
    requires jfxtras.controls;

    opens org.opentravel.release;
    opens org.opentravel.release.navigate;
    opens org.opentravel.release.navigate.impl;
    opens org.opentravel.release.undo;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.release.OTMReleaseApplicationProvider;

}
