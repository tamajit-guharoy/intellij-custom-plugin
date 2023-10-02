package com.tamajit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.*;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.psi.search.GlobalSearchScope;

public class AddMethodAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];

        // Get the JavaPsiFacade instance for the project
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);

// Search for a class by its fully qualified name
        String fullyQualifiedName = "com.tamajit.Main2";
        PsiClass psiClass = psiFacade.findClass(fullyQualifiedName, GlobalSearchScope.allScope(project));
// Check if the class is found
        AddMethod(project, psiClass);
    }

    public static void AddMethod(Project project, PsiClass psiClass) {
        if (psiClass != null) {
            // Class found, create the new method within a write action
            WriteCommandAction.runWriteCommandAction(project, () -> {
                // Get the PsiElementFactory within the write action
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

                // Create the method text
                String methodText = "public void myMethod() {\n" +
                        "    // Method body\n" +
                        "}";

                // Create the PsiMethod object from the method text
                PsiMethod newMethod = factory.createMethodFromText(methodText, psiClass);

                // Add the new method to the class
                psiClass.add(newMethod);
            });
        }
    }
}