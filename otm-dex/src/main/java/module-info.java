module org.opentravel.apps.otmdex {

    requires java.desktop;
    requires org.opentravel.apps.common;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.opentravel.schemacompiler;
    requires spring.jcl;
    requires org.apache.logging.log4j;
    requires org.controlsfx.controls;

    opens org.opentravel.common;
    opens org.opentravel.common.cellfactories;
    opens org.opentravel.dex.action.manager;
    opens org.opentravel.dex.actions.constraints;
    opens org.opentravel.dex.actions.resource;
    opens org.opentravel.dex.actions.string;
    opens org.opentravel.dex.controllers;
    opens org.opentravel.dex.controllers.graphics;
    opens org.opentravel.dex.controllers.graphics.sprites;
    opens org.opentravel.dex.controllers.graphics.sprites.connections;
    opens org.opentravel.dex.controllers.graphics.sprites.rectangles;
    opens org.opentravel.dex.controllers.library;
    opens org.opentravel.dex.controllers.library.usage;
    opens org.opentravel.dex.controllers.member;
    opens org.opentravel.dex.controllers.member.filters;
    opens org.opentravel.dex.controllers.member.properties;
    opens org.opentravel.dex.controllers.member.usage;
    opens org.opentravel.dex.controllers.popup;
    opens org.opentravel.dex.controllers.repository;
    opens org.opentravel.dex.controllers.resources;
    opens org.opentravel.dex.controllers.search;
    opens org.opentravel.dex.events;
    opens org.opentravel.dex.tasks;
    opens org.opentravel.dex.tasks.model;
    opens org.opentravel.dex.tasks.repository;
    opens org.opentravel.model;
    opens org.opentravel.model.otmContainers;
    opens org.opentravel.model.otmFacets;
    opens org.opentravel.model.otmLibraryMembers;
    opens org.opentravel.model.otmProperties;
    opens org.opentravel.model.resource;
    opens org.opentravel.objecteditor;
    opens org.opentravel.repositoryviewer;
    opens icons;
    opens Dialogs;
    opens GraphicViews;
    opens LibraryWhereUsedViews;
    opens MemberViews;
    opens RepositoryViews;
    opens ResourceViews;
    opens SearchViews;
    opens WhereUsedViews;

    provides org.opentravel.application.common.OTA2ApplicationProvider
        with org.opentravel.objecteditor.ObjectEditorApplicationProvider,
        org.opentravel.repositoryviewer.RepositoryViewerApplicationProvider;

}
