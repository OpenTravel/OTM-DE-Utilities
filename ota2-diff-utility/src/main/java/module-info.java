module org.opentravel.apps.diffutil {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.opentravel.schemacompiler;
    requires org.apache.logging.log4j;

    exports org.opentravel.diffutil;

    opens org.opentravel.diffutil;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.diffutil.DiffUtilityApplicationProvider;

}
