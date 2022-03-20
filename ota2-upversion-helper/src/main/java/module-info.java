module org.opentravel.apps.upversionhelper {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.opentravel.schemacompiler;
    requires java.xml.bind;

    opens org.opentravel.upversion;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.upversion.UpversionHelperApplicationProvider;

}
