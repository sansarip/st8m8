package com.sansarip.st8m8;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class DB extends SimpleToolWindowPanel {
    public App app;
    public DB(App app) {
        super(true, false);
        this.app = app;
        final Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", false);
        app.toolWindow.getContentManager().addContent(content);
    }
}
