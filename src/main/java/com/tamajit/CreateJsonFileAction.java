package com.tamajit;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;

public class CreateJsonFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();

        if (project != null) {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiDirectory baseDir = psiManager.findDirectory(project.getBaseDir());

            // Perform the file creation within a write command action
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                String jsonContent = "{ \"message\": \"Hello, World!\" }";
                try {
                    PsiFile newJsonFile = psiFileFactory.createFileFromText("new_file.json", JsonFileType.INSTANCE, jsonContent);

                    // Add the newly created JSON file to the base directory
                    if (baseDir != null) {
                        baseDir.add(newJsonFile);
                    }
                } catch (IncorrectOperationException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
