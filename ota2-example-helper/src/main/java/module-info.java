module org.opentravel.apps.examplehelper {

    requires transitive org.opentravel.apps.common;
    requires transitive org.opentravel.schemacompiler;
    requires org.apache.logging.log4j;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires reactfx;

    opens org.opentravel.examplehelper;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.examplehelper.ExampleHelperApplicationProvider;

}
