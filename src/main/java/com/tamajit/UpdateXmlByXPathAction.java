package com.tamajit;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class UpdateXmlByXPathAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        // Get the base directory of the project
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) {
            return;
        }

        // Create the file object for the XML file
        String xmlFileName = "new_file.xml";
        VirtualFile xmlFile = baseDir.findFileByRelativePath(xmlFileName);
        if (xmlFile == null) {
            return;
        }

        // Update the XML file by XPath
        updateXmlByXPath(xmlFile);
    }

    private void updateXmlByXPath(VirtualFile xmlFile) {
        WriteCommandAction.runWriteCommandAction(null, () -> {
            String xmlContent = null;
            try {
                xmlContent = readXmlFile(xmlFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String xpathExpression = "//hello";

            try {
                // Parse the XML content
                SAXBuilder builder = new SAXBuilder();
                Document document = builder.build(new StringReader(xmlContent));

                // Evaluate the XPath expression
                XPath xpath = XPath.newInstance(xpathExpression);
                List<Element> elements = xpath.selectNodes(document);
                if (!elements.isEmpty()) {
                    Element helloElement = elements.get(0);
                    helloElement.setText("Updated value");
                }

                // Update the XML file content
                writeXmlFile(document, xmlFile.getPath());
            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String readXmlFile(VirtualFile xmlFile) throws IOException {
        byte[] contentBytes = xmlFile.contentsToByteArray();
        return new String(contentBytes);
    }

    private void writeXmlFile(Document document, String filePath) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlContent = xmlOutputter.outputString(document);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(xmlContent);
        }
    }
}

