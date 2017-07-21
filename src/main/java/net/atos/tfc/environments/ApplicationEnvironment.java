package net.atos.tfc.environments;

import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class ApplicationEnvironment {

    private Properties properties = new Properties();
    private Set<String> generatedFiles = new TreeSet<>();
    private String outputDirectory;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Set<String> getGeneratedFiles() {
        return generatedFiles;
    }

    public void setGeneratedFiles(Set<String> generatedFiles) {
        this.generatedFiles = generatedFiles;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }
}
