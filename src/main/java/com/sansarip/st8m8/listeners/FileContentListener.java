package com.sansarip.st8m8.listeners;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.sansarip.st8m8.Utilities;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class FileContentListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (VFileEvent event : events) {
            for (Project project : projects) {
                VirtualFile file = event.getFile();
                if (file != null) {
                    Boolean projectIsFocused = ProjectFileIndex.getInstance(project).isInContent(file);
                    if (projectIsFocused) {
                        try {
                            if (Utilities.loadableFile(project, file.getPath(), true)) {
                                Utilities.loadClojureFile(project);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

}
