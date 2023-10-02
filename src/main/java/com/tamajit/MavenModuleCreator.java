package com.tamajit;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactoryImpl;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MavenModuleCreator extends com.intellij.openapi.actionSystem.AnAction {

    public static void createModule(Project project, String moduleName) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            Module module = ModuleManager.getInstance(project).getModules()[0] ;

            // Create submodule in base directory
            createSubmodule(project, moduleName);
        });
    }

    private static VirtualFile createModuleContentRoot(String moduleDirectory, String moduleName) {
        File moduleContentRoot = new File(moduleDirectory + "/" + moduleName);
        if (!moduleContentRoot.exists()) {
            moduleContentRoot.mkdirs();
        }
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(moduleContentRoot);
    }

    private static void generatePomXml(Project project , VirtualFile moduleContentRoot, String moduleName) {
        PsiDirectory psiDirectory = PsiDirectoryFactoryImpl.getInstance(project).createDirectory(moduleContentRoot);
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);

        // Create pom.xml file
        String pomXmlContent = generatePomXmlContent(moduleName);
        PsiFile pomXmlFile = psiFileFactory.createFileFromText("pom.xml", pomXmlContent);

        PsiDirectory main = psiDirectory.createSubdirectory("main");
        PsiDirectory javaSrc = main.createSubdirectory("java");

        //mark javaSrc as source directory
        VirtualFile directoryVirtualFile = javaSrc.getVirtualFile();
        Module module = ModuleUtilCore.findModuleForFile(directoryVirtualFile, project);
        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        ContentEntry contentEntry = model.getContentEntries()[0]; // Assuming there is only one content entry
        contentEntry.addSourceFolder(directoryVirtualFile, false);
        model.commit();

        main.createSubdirectory("resources");
        PsiDirectory test = psiDirectory.createSubdirectory("test");
        test.createSubdirectory("java");
        test.createSubdirectory("resources");
        // Add pom.xml file to module content root
        psiDirectory.add(pomXmlFile);
    }

    private static String generatePomXmlContent(String moduleName) {
        String groupId = "com.example";
        String artifactId = moduleName;
        String version = "1.0-SNAPSHOT";

        StringBuilder pomXmlContent = new StringBuilder();
        pomXmlContent.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        pomXmlContent.append("         xmlns:xsi=\"http://www.w3.org/XMLSchema-instance\"\n");
        pomXmlContent.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n");
        pomXmlContent.append("                             http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        pomXmlContent.append("  <modelVersion>4.0.0</modelVersion>\n");
        pomXmlContent.append("  <groupId>").append(groupId).append("</groupId>\n");
        pomXmlContent.append("  <artifactId>").append(artifactId).append("</artifactId>\n");
        pomXmlContent.append("  <version>").append(version).append("</version>\n");
        pomXmlContent.append("</project>\n");

        return pomXmlContent.toString();
    }

    private static void createSubmodule(Project project, String subModuleName) {

        String submodulePath =  project.getBasePath() + "/" + subModuleName;

        //create module
        String moduleFilePath = submodulePath+ ".iml";
        String moduleFilePathCanonical = PathUtil.getCanonicalPath(moduleFilePath);
        ModuleManager.getInstance(project).newModule(moduleFilePathCanonical, subModuleName);

        File submoduleDirectory = new File(submodulePath);
        if (!submoduleDirectory.exists()) {
            submoduleDirectory.mkdirs();

            // Generate pom.xml inside submodule
            generatePomXml(project, LocalFileSystem.getInstance().refreshAndFindFileByIoFile(submoduleDirectory), subModuleName);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        createModule(e.getProject(),"moduleName2");
    }
}
