package com.tamajit;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;

public class CreateXmlFileAction2 extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();

        if (project != null) {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiDirectory baseDir = psiManager.findDirectory(project.getBaseDir());

            // Perform the file creation within a write command action
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                String xmlContent = "<hello>world</hello>";
                PsiFile newXmlFile = psiFileFactory.createFileFromText("new_file.xml", XmlFileType.INSTANCE, xmlContent);

                // Add the newly created XML file to the base directory
                if (baseDir != null) {
                    baseDir.add(newXmlFile);
                }
            });
        }
    }
}
