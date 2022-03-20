module org.opentravel.apps.exampleupgrade {

    requires org.opentravel.apps.common;
    requires org.opentravel.schemacompiler;
    requires org.apache.logging.log4j;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires reactfx;

    opens org.opentravel.exampleupgrade;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.exampleupgrade.ExampleUpgradeApplicationProvider;

}
