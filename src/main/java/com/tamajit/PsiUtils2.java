package com.tamajit;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

public class PsiUtils2 {
    public static PsiDirectory findOrCreatePackage(Project project, String packageName) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            PsiDirectory sourceRoot = getModuleSourceRoot(module);
            if (sourceRoot != null) {
                PsiDirectory packageDir = findOrCreatePackageInSourceRoot(sourceRoot, packageName);
                if (packageDir != null) {
                    return packageDir;
                }
            }
        }
        return null;
    }

    private static PsiDirectory getModuleSourceRoot(Module module) {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots();
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        for (VirtualFile sourceRoot : sourceRoots) {
            PsiDirectory psiSourceRoot = psiManager.findDirectory(sourceRoot);
            if (psiSourceRoot != null && psiSourceRoot.getVirtualFile().getPath().contains("main")) {
                return psiSourceRoot;
            }
        }
        return null;
    }

    private static PsiDirectory findOrCreatePackageInSourceRoot(PsiDirectory sourceRoot, String packageName) {
        String[] packageNames = packageName.split("\\.");
        PsiDirectory currentDir = sourceRoot;
        for (String name : packageNames) {
            PsiDirectory subDir = currentDir.findSubdirectory(name);
            if (subDir == null) {
                subDir = currentDir.createSubdirectory(name);
            }
            currentDir = subDir;
        }
        return currentDir;
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
