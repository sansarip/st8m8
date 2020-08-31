package com.sansarip.st8m8.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.sansarip.st8m8.Utilities;
import org.jetbrains.annotations.NotNull;

public class ReadAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Utilities.loadClojureFile(project);
    }
}
