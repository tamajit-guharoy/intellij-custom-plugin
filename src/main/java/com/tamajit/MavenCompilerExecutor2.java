package com.tamajit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;

import java.util.Arrays;

public class MavenCompilerExecutor2 extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        executeMavenCompilerPlugin(project, project.getBasePath() + "/" + "moduleName2");
    }

    public static void executeMavenCompilerPlugin(Project project, String modulePath) {
       /* ApplicationManager.getApplication().runWriteAction(() -> {
            MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
            mavenProjectsManager.waitForResolvingCompletion();*/

        Task.Backgroundable task = new Task.Backgroundable(project, "Maven Compilation") {
            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.setText("Compiling Maven project...");


                MavenRunnerParameters parameters = new MavenRunnerParameters();
                parameters.setWorkingDirPath(modulePath);
                parameters.setGoals(Arrays.asList("compile"));

                MavenRunnerSettings settings = new MavenRunnerSettings();

                MavenRunner.getInstance(project).run(parameters, settings, () -> {
                    // Add your completion logic here

                    // Add your success and error handling logic here
                });

            }
        };

        ProgressManager.getInstance().run(task);
    }

    }


