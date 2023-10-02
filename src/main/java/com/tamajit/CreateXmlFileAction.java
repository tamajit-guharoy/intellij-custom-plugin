package com.tamajit;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;

public class CreateXmlFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();

        if (project != null) {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiDirectory baseDir = psiManager.findDirectory(project.getBaseDir());

            // Perform the file creation within a write command action
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                PsiFile newXmlFile = psiFileFactory.createFileFromText("new_file.xml", XmlFileType.INSTANCE, "");
                if (newXmlFile instanceof XmlFile) {
                    XmlFile xmlFile = (XmlFile) newXmlFile;
                    XmlDocument document = xmlFile.getDocument();
                    if (document != null) {
                        XmlTag rootTag = document.getRootTag();
                        if (rootTag != null) {
                            rootTag.delete();
                        }

                        // Create a new root tag
                        XmlElementFactory elementFactory = XmlElementFactory.getInstance(project);
                        rootTag = elementFactory.createTagFromText("<root></root>");

                        // Set the new root tag as the root element of the document
                        XmlTag oldRootTag = document.getRootTag();
                        if (oldRootTag != null) {
                            oldRootTag.replace(rootTag);
                        } else {
                            document.add(rootTag);
                        }

                        // Customize the XML structure as needed
                        XmlTag childTag = elementFactory.createTagFromText("<child>Child Content</child>");
                        rootTag.addSubTag(childTag, false);
                    }

                    // Add the newly created XML file to the base directory
                    if (baseDir != null) {
                        baseDir.add(newXmlFile);
                    }
                }
            });
        }
    }
}
