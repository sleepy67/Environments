package net.atos.tfc.environments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.format;

public class NewConfig {

    private static Logger LOG = LoggerFactory.getLogger(NewConfig.class);

    private ApplicationEnvironment globalConfig = new ApplicationEnvironment();
    private Map<String, ApplicationEnvironment> environmentConfig = new TreeMap<>();
    private Map<String, ApplicationEnvironment> applicationConfig = new TreeMap<>();
    private Map<String, ApplicationEnvironment> applicationEnvironmentConfig = new TreeMap<>();

    public void addGlobalProperty(String name, String value) {
        globalConfig.getProperties().setProperty(name, value);

        logProperty("GLOBAL", "", "", name, value);
    }

    public void addApplicationProperty(String applicationName, String name, String value) {
        applicationConfig.putIfAbsent(applicationName, new ApplicationEnvironment());
        ApplicationEnvironment applicationEnvironment = applicationConfig.get(applicationName);
        applicationEnvironment.getProperties().setProperty(name, value);

        logProperty("APPLICATION", "", applicationName, name, value);
    }

    public void addEnvironmentProperty(String environmentName, String name, String value) {
        environmentConfig.putIfAbsent(environmentName, new ApplicationEnvironment());
        ApplicationEnvironment applicationEnvironment = environmentConfig.get(environmentName);
        applicationEnvironment.getProperties().setProperty(name, value);

        logProperty("ENVIRONMENT", environmentName, "", name, value);
    }

    public void addApplicationEnvironmentProperty(String environmentName, String applicationName, String name, String value) {
        String key = format("%s~%s", environmentName, applicationName);
        applicationEnvironmentConfig.putIfAbsent(key, new ApplicationEnvironment());
        ApplicationEnvironment applicationEnvironment = applicationEnvironmentConfig.get(key);
        applicationEnvironment.getProperties().setProperty(name, value);

        logProperty("APPL_ENV", environmentName, applicationName, name, value);
    }

    private void logProperty(String logType, String environmentName, String applicationName, String propertyName, String value) {
        LOG.debug("{} {} {} {}={}", format("%-15s", logType), format("%-10s", environmentName), format("%-25s", applicationName), propertyName, value);
    }

    public void addGlobalFile(String filename, String outputDirectory) {
        globalConfig.getGeneratedFiles().add(filename);
        globalConfig.setOutputDirectory(outputDirectory);

        logFile("GLOBAL", "", "", filename);
    }

    public void addApplicationFile(String applicationName, String filename, String outputDirectory) {
        applicationConfig.putIfAbsent(applicationName, new ApplicationEnvironment());
        applicationConfig.get(applicationName).getGeneratedFiles().add(filename);
        applicationConfig.get(applicationName).setOutputDirectory(outputDirectory);

        logFile("APPLICATION", "", applicationName, filename);
    }

    private void logFile(String logType, String environmentName, String applicationName, String filename) {
        LOG.debug("FILE - {} {} {} {}", format("%-15s", logType), format("%-10s", environmentName), format("%-25s", applicationName), filename);
    }

    public ApplicationEnvironment getGlobalConfig() {
        return globalConfig;
    }

    public Map<String, ApplicationEnvironment> getEnvironmentConfig() {
        return environmentConfig;
    }

    public Map<String, ApplicationEnvironment> getApplicationConfig() {
        return applicationConfig;
    }

    public Map<String, ApplicationEnvironment> getApplicationEnvironmentConfig() {
        return applicationEnvironmentConfig;
    }
}
