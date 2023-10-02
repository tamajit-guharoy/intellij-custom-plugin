package com.tamajit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class PsiUtils {
    public static PsiDirectory findOrCreatePackage(Project project, String packageName) {
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile baseDir = project.getBaseDir();
        PsiDirectory basePsiDir = psiManager.findDirectory(baseDir);
        if (basePsiDir == null) {
            return null;
        }

        String[] packageNames = packageName.split("\\.");
        PsiDirectory currentDir = basePsiDir;
        for (String name : packageNames) {
            PsiDirectory subDir = currentDir.findSubdirectory(name);
            if (subDir == null) {
                subDir = currentDir.createSubdirectory(name);
            }
            currentDir = subDir;
        }
        return currentDir;
    }


    private static PsiDirectory createSubdirectory(PsiDirectory parentDirectory, String name) {
        PsiDirectory subdirectory = parentDirectory.findSubdirectory(name);
        if (subdirectory == null) {
            subdirectory = parentDirectory.createSubdirectory(name);
        }
        return subdirectory;
    }

    public static PsiFile createClassInPackage(Project project, PsiDirectory packageDir, String classDefinition) {
        String className = getClassName(classDefinition);
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        PsiFile classFile = fileFactory.createFileFromText(className + ".java", classDefinition);
        return (PsiFile) packageDir.add(classFile);
    }

    public static String getClassName(String classDefinition) {
        String className = classDefinition.trim().split("public class")[1];
        return className.trim().split("\s")[0];
    }

    public static void reformatFile(PsiFile file) {
        CodeStyleManager.getInstance(file.getProject()).reformat(file);
    }
}
