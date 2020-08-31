package com.sansarip.st8m8.listeners;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.sansarip.st8m8.Utilities;

public class FileWatcher implements ProjectComponent {
    private final Project project;

    public FileWatcher(Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
        //Utilities.watchAndLoad(project);
        // TODO: insert component initialization logic here
    }

}
