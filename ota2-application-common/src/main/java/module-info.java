module org.opentravel.apps.common {

    requires transitive java.desktop;
    requires transitive org.opentravel.schemacompiler;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires spring.jcl;
    requires org.apache.logging.log4j;
    requires org.fxmisc.richtext;

    exports org.opentravel.application.common;
    exports org.opentravel.application.common.events;

}
