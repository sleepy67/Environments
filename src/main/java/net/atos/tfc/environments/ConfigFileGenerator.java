package net.atos.tfc.environments;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.String.format;

public class ConfigFileGenerator {

    private static final File BASE_DIR = new File("target/potential-config");
    private static final File TEMPLATE_DIR = new File("templates/external-config");

    private static Logger LOG = LoggerFactory.getLogger(ConfigFileGenerator.class);

    private NewConfig newConfig;

    public ConfigFileGenerator() {
        FileUtils.deleteQuietly(BASE_DIR);
    }

    public void generateFiles(NewConfig newConfig) {
        this.newConfig = newConfig;
        generatePropertyFiles();
        generateTemplateFiles();
    }


    public void generatePropertyFiles() {
        generateConfigFile("global", "global.properties", newConfig.getGlobalConfig());

        newConfig.getApplicationConfig().forEach((applicationName, applicationEnvironment) -> {
            generateConfigFile(format("applications/%s", applicationName), "application.properties", applicationEnvironment);
        });

        newConfig.getEnvironmentConfig().forEach((environmentName, applicationEnvironment) -> {
            generateConfigFile(format("environments/%s", environmentName), "environment.properties", applicationEnvironment);
        });

        newConfig.getApplicationEnvironmentConfig().forEach((name, applicationEnvironment) -> {
            String[] names = StringUtils.split(name, "~");
            String environment = names[0];
            String application = names[1];
            generateConfigFile(format("environments/%s/applications/%s", environment, application), "application.properties", applicationEnvironment);
        });
    }

    private static void generateConfigFile(String dirName, String filename, ApplicationEnvironment applicationEnvironment) {
        FileOutputStream fos = null;
        try {
            File dir = new File(BASE_DIR, dirName);
            FileUtils.forceMkdir(dir);

            File file = new File(dir, filename);
            fos = new FileOutputStream(file);

            Set<String> sortedPropertyNames = new TreeSet(applicationEnvironment.getProperties().stringPropertyNames());
            String previousNamePart = "";

            for (String propertyName : sortedPropertyNames) {
                String[] propertyNameParts = StringUtils.split(propertyName, ".");

                if (previousNamePart != "" && !previousNamePart.equals(propertyNameParts[1])) {
                    FileUtils.writeStringToFile(file, "\n", Charset.defaultCharset(), true);
                }
                previousNamePart = propertyNameParts[1];

                String line = format("%s=%s\n", propertyName, applicationEnvironment.getProperties().getProperty(propertyName));
                FileUtils.writeStringToFile(file, line, Charset.defaultCharset(), true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    private void generateTemplateFiles() {
        copyTemplateFiles("global", newConfig.getGlobalConfig());

        newConfig.getApplicationConfig().forEach((applicationName, applicationEnvironment) -> {
            copyTemplateFiles(format("applications/%s", applicationName), applicationEnvironment);
        });
    }

    private void copyTemplateFiles(String dirName, ApplicationEnvironment applicationEnvironment) {

        applicationEnvironment.getGeneratedFiles().forEach((filename) -> {
            String actualFilename = filename;
            File srcFile = new File(TEMPLATE_DIR, actualFilename);

            if (!srcFile.exists()) {
                actualFilename = filename + ".tmpl";
                srcFile = new File(TEMPLATE_DIR, actualFilename);
            }

            String subDir = applicationEnvironment.getOutputDirectory() != null ? "/" + applicationEnvironment.getOutputDirectory() : "";
            File destFile = new File(new File(BASE_DIR, dirName + "/templates" + subDir), actualFilename);

            try {
                if (srcFile.exists()) {
                    FileUtils.copyFile(srcFile, destFile);
                } else {
                    LOG.error("Template does not exist for copying: {}", actualFilename);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
