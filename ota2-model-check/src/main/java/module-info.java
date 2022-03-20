module org.opentravel.apps.modelcheck {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.opentravel.schemacompiler;
    requires org.slf4j;

    opens org.opentravel.modelcheck;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.modelcheck.ModelCheckApplicationProvider;

}
