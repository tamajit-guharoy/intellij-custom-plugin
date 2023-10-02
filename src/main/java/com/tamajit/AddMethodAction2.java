package com.tamajit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import static com.tamajit.AddMethodAction.AddMethod;


public class AddMethodAction2 extends AnAction {

    @Override
    public void actionPerformed(com.intellij.openapi.actionSystem.AnActionEvent event) {
        // Get the current project
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(event.getProject());

        // Get the selected class
        PsiClass psiClass = getPsiClassFromContext(event);

        AddMethod(event.getProject(),psiClass);
    }

    // Helper method to get the selected class from the context
    private PsiClass getPsiClassFromContext(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        int offset = event.getData(LangDataKeys.CARET).getOffset();
        if (psiFile instanceof PsiJavaFile) {
            PsiElement element = psiFile.findElementAt(offset);
            return PsiTreeUtil.getParentOfType(element, PsiClass.class);
        }
        return null;
    }
}
