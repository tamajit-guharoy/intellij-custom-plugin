package com.tamajit;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class CreateClassAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        // Get the selected text containing the class definition
        String classDefinition = event.getData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        if (classDefinition == null) {
            return;
        }

        // Create the class in the com package
        PsiDirectory comPackage = PsiUtils2.findOrCreatePackage(project, "com");
        if (comPackage == null) {
            return;
        }

        PsiFile classFile = PsiUtils2.createClassInPackage(project, comPackage, classDefinition);
        if (classFile != null) {
            PsiUtils2.reformatFile(classFile);
        }
    }
}
