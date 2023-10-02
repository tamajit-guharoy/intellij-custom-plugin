package com.tamajit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

public class PomXmlUpdater extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];

        addModuleEntryToPomXml(project,"moduleName2");
    }
    public static void addModuleEntryToPomXml(Project project, String moduleName) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiFile pomFile = psiManager.findFile(project.getBaseDir().findFileByRelativePath("pom.xml"));
            if (pomFile instanceof XmlFile) {
                XmlFile xmlFile = (XmlFile) pomFile;
                XmlDocument document = xmlFile.getDocument();
                if (document != null) {
                    XmlElementFactory factory = XmlElementFactory.getInstance(project);
                    XmlTag modulesTag = document.getRootTag().findFirstSubTag("modules");
                    if (modulesTag == null) {
                        modulesTag = document.getRootTag().addSubTag(factory.createTagFromText("<modules></modules>"), false);
                    }
                    XmlTag moduleTag = factory.createTagFromText("<module>" + moduleName + "</module>");
                    modulesTag.addSubTag(moduleTag, false);
                }
            }
        });
    }
}

