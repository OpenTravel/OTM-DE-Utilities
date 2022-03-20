module org.opentravel.apps.messagevalidator {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.opentravel.schemacompiler;
    requires org.apache.logging.log4j;
    requires java.xml.bind;
    requires jackson.coreutils;
    requires json.schema.core;
    requires json.schema.validator;
    requires btf;

    opens org.opentravel.messagevalidate;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.messagevalidate.OTMMessageValidatorApplicationProvider;

}
