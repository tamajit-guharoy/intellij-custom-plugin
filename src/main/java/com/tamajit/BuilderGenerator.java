package com.tamajit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.io.IOException;
import java.util.Properties;

public class BuilderGenerator extends AnAction {
    static Properties defaultValues = new Properties();
    @Override
    public void actionPerformed(AnActionEvent event) {
        loadDefaultValues();
        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            PsiClass currentClass = getPsiClassFromContext(event);
            PsiField[] allFields = currentClass.getAllFields();
            String className = currentClass.getName();
            String builderClassName = className + "Builder";

            String psiClassPackageName = getPsiClassPackageName(event.getProject(), currentClass);
            // Create the class in the com package
            PsiDirectory comPackage = PsiUtils2.findOrCreatePackage(event.getProject(), psiClassPackageName);
            if (comPackage == null) {
                return;
            }

            PsiFile classFile = createClassInPackage(event.getProject(), comPackage, "public class " + builderClassName + "{\n}", psiClassPackageName, builderClassName);
            if (classFile == null) {
                return;
            }
            PsiClass builderPsiClass = getPsiClassFromPsiFile(classFile);
            //   addPackageToPsiClass(event.getProject(), builderPsiClass, psiClassPackageName);
            addFieldToPsiClass(event.getProject(), builderPsiClass, className.toLowerCase(), className);
            createDefaultConstructor(event.getProject(), builderPsiClass, className.toLowerCase() + "=" + "new " + className + "();");

            StringBuffer newInstanceMethodContent=new StringBuffer("public static "+className+" getInstance(){\n" +
                    "return new "+builderClassName+"()");
            for (PsiField field : allFields) {

                String methodContent = "public " + builderClassName + " " + field.getName() + "(" + field.getType().getPresentableText() + " " + field.getName() + "){" +
                        "this." + className.toLowerCase() + ".set" + capitalize(field.getName() )+"("+field.getName()+");\n"
                        +"return this;\n"
                        +"}";
                addMethod(event.getProject(), builderPsiClass, methodContent);
                addImportToPsiClass(builderPsiClass,field.getType().getCanonicalText());
                newInstanceMethodContent.append("\n."+field.getName()+"("+getDefaultValue(field)+")" );
            }
            newInstanceMethodContent.append("\n.build();\n}\n");
            addMethod(event.getProject(),builderPsiClass,"public "+className+" build(){\n" +
                    "        return this."+className.toLowerCase()+";" +
                    "   \n }");
            addMethod(event.getProject(), builderPsiClass, newInstanceMethodContent.toString());
            if (classFile != null) {
                PsiUtils2.reformatFile(classFile);
            }
        });
    }

    private static  Object getDefaultValue(PsiField field) {


        String presentableText = field.getType().getCanonicalText();
        if(presentableText.equals("java.lang.String")){
            return "\""+field.getName()+"\"";
        }
        Object o = defaultValues.getOrDefault(presentableText, capitalize(field.getName())+"Builder.getInstance()");
        return o;
    }

    private PsiClass getPsiClassFromContext(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        int offset = event.getData(LangDataKeys.CARET).getOffset();
        if (psiFile instanceof PsiJavaFile) {
            PsiElement element = psiFile.findElementAt(offset);
            return PsiTreeUtil.getParentOfType(element, PsiClass.class);
        }
        return null;
    }

    private static String getPsiClassPackageName(Project project, PsiClass psiClass) {
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(javaFile.getPackageName());
            if (psiPackage != null) {
                return psiPackage.getQualifiedName();
            }
        }
        return null;
    }

    public static void addMethod(Project project, PsiClass psiClass, String methodText) {
        if (psiClass != null) {
            // Class found, create the new method within a write action
            WriteCommandAction.runWriteCommandAction(project, () -> {
                // Get the PsiElementFactory within the write action
                PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

                // Create the method text
 /*               String methodText = "public void myMethod() {\n" +
                        "    // Method body\n" +
                        "}";*/

                // Create the PsiMethod object from the method text
                System.out.println(methodText);
                PsiMethod newMethod = factory.createMethodFromText(methodText, psiClass);

                // Add the new method to the class
                psiClass.add(newMethod);
            });
        }
    }

    private static void createDefaultConstructor(Project project, PsiClass psiClass, String content) {
        if (psiClass != null) {
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            PsiMethod constructor = elementFactory.createConstructor();

            // Add constructor modifiers (if needed)
            constructor.getModifierList().setModifierProperty(PsiModifier.PUBLIC, true);

            // Add constructor body (if needed)
            constructor.getBody().add(elementFactory.createStatementFromText(content, psiClass));

            // Add constructor to the class
            psiClass.add(constructor);
        }
    }

    private static PsiField addFieldToPsiClass(Project project, PsiClass psiClass, String fieldName, String fieldType) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);

        // Create the field declaration
        PsiField field = elementFactory.createField(fieldName, elementFactory.createTypeFromText(fieldType, psiClass));

        // Set field modifiers (if needed)
        field.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);

        // Add the field to the class
        psiClass.add(field);

        return field;
    }

    public static PsiFile createClassInPackage(Project project, PsiDirectory packageDir, String classDefinition, String packageName, String className) {
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);

        PsiFile classFile = fileFactory.createFileFromText(className + ".java", classDefinition);
        PsiFile file = (PsiFile) packageDir.add(classFile);

        return file;
    }

    private static void addPackageToPsiClass(Project project, PsiClass psiClass, String packageName) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);

        // Create the package statement
        PsiPackageStatement packageStatement = elementFactory.createPackageStatement(packageName);

        // Get the class file and add the package statement as the first child
        PsiJavaFile psiFile = (PsiJavaFile) psiClass.getContainingFile();
        psiFile.addBefore(packageStatement, psiFile.getFirstChild());
    }

    private static PsiClass getPsiClassFromPsiFile(PsiFile psiFile) {
        PsiClass psiClass = null;

        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            PsiClass[] classes = psiJavaFile.getClasses();
            if (classes.length > 0) {
                psiClass = classes[0];
            }
        }

        return psiClass;
    }
    public static final String capitalize(String str)
    {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void  loadDefaultValues(){
        try {
            defaultValues.load(BuilderGenerator.class.getClassLoader().getResourceAsStream("defaultValues.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addImportToPsiClass(PsiClass psiClass, String newImportStatement) {
        if(!newImportStatement.contains(".")) return;
        PsiJavaFile psiFile = (PsiJavaFile) psiClass.getContainingFile();
        PsiImportList importList = psiFile.getImportList();

        if (importList != null) {
            // Check if the import statement already exists
            boolean importExists = false;
            for (PsiImportStatement importStatement : importList.getImportStatements()) {
                if (importStatement.getQualifiedName().equals(newImportStatement)) {
                    importExists = true;
                    break;
                }
            }

            if (!importExists) {
                // Add the import statement to the import list
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiClass.getContainingFile();
                PsiClass importedClass = JavaPsiFacade.getInstance(psiClass.getProject()).findClass(newImportStatement, psiJavaFile.getResolveScope());

                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
                PsiImportStatement newImport = elementFactory.createImportStatement(importedClass);
                importList.add(newImport);
            }
        }
    }
}
