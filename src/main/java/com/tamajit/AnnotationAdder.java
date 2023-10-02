package com.tamajit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tamajit.AddMethodAction.AddMethod;

public class AnnotationAdder extends AnAction {

    @Override
    public void actionPerformed(com.intellij.openapi.actionSystem.AnActionEvent event) {
        // Get the current project
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(event.getProject());

        // Get the selected class
        PsiClass psiClass = getPsiClassFromContext(event);

        addAnnotationToInstanceVariable(event.getProject(), psiClass, "myfield", "@Pattern(regexp = \"YN\", flags = {\n" +
                "            Pattern.Flag.CASE_INSENSITIVE }, message = \"Invalid Proficiency 5, Enter text not matches with the standards\")\n");

        addImportStatement(event.getProject(), psiClass, "javax.validation.constraints.Pattern");

        List<PsiMethod> myMethod = getMethodsWithAnnotation(psiClass, "myMethod", "javax.validation.constraints.Pattern");
        if (myMethod != null && myMethod.size() > 0) {
            addAnnotationToParameter(event.getProject(), myMethod.get(0),"param1","@NotNull");
            addImportStatement(event.getProject(), psiClass, "javax.validation.constraints.NotNull");
        }
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


    public static void addAnnotationToInstanceVariable(Project project, PsiClass psiClass, String fieldName, String annotationText) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiField field = psiClass.findFieldByName(fieldName, false);
            if (field != null) {
                PsiModifierList modifierList = field.getModifierList();
                if (modifierList != null) {
                    PsiAnnotation annotation = modifierList.findAnnotation(annotationText);
                    if (annotation == null) {
                        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
                        annotation = elementFactory.createAnnotationFromText(annotationText, psiClass);
                        modifierList.addBefore(annotation, modifierList.getFirstChild());
                    }
                }
            }
        });
    }

    public static void addImportStatement(Project project, PsiClass psiClass, String importStatement) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiClass.getContainingFile();
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            PsiClass importedClass = JavaPsiFacade.getInstance(project).findClass(importStatement, psiJavaFile.getResolveScope());

            if (importedClass != null && !isImportStatementPresent(psiJavaFile, importedClass)) {
                PsiImportStatement importStatementToAdd = elementFactory.createImportStatement(importedClass);
                psiJavaFile.getImportList().add(importStatementToAdd);
            }
        });
    }

    private static boolean isImportStatementPresent(PsiJavaFile psiJavaFile, PsiClass importedClass) {
        String importClassName = importedClass.getQualifiedName();
        if (importClassName != null) {
            for (PsiImportStatement importStatement : psiJavaFile.getImportList().getImportStatements()) {
                String existingImport = importStatement.getQualifiedName();
                if (importClassName.equals(existingImport)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void addAnnotationToParameter(Project project, PsiMethod method, String parameterName, String annotationText) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiParameterList parameterList = method.getParameterList();
            for (PsiParameter parameter : parameterList.getParameters()) {
                if (parameter.getName().equals(parameterName)) {
                    PsiModifierList modifierList = parameter.getModifierList();
                    if (modifierList != null) {
                        PsiAnnotation annotation = JavaPsiFacade.getElementFactory(project).createAnnotationFromText(annotationText, parameter);
                        modifierList.addAfter(annotation, null);
                    }
                    break;  // Stop iterating once the parameter is found
                }
            }
        });
    }

    public static PsiMethod[] getMethodsByName(PsiClass psiClass, String methodName) {
        return psiClass.findMethodsByName(methodName, false);
    }

    public static List<PsiMethod> getPublicMethodsByName(PsiClass psiClass, String methodName) {
        return Arrays.stream(psiClass.findMethodsByName(methodName, false))
                .filter(m -> m.hasModifierProperty(PsiModifier.PUBLIC))
                .collect(Collectors.toList());
    }

    public static List<PsiMethod> getMethodsWithAnnotation(PsiClass psiClass, String methodName, String annotationQualifiedName) {
        return Arrays.stream(psiClass.findMethodsByName(methodName, false))
                .filter(m -> m.getModifierList().hasAnnotation(annotationQualifiedName))
                .collect(Collectors.toList());
    }

}
