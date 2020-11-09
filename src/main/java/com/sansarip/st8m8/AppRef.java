package com.sansarip.st8m8;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * Stores the St8M8 tool window factory
 * and allows tool window instances to be referenced from Actions
 */
public class AppRef extends SimpleToolWindowPanel {
    public App app;
    public AppRef(App app, String appId) {
        super(true, false);
        this.app = app;
        Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", false);
        app.apps.get(appId).toolWindow.getContentManager().addContent(content);
    }
}
