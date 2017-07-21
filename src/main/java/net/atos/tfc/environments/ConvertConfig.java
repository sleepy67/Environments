package net.atos.tfc.environments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class ConvertConfig {

    private static Logger LOG = LoggerFactory.getLogger(ConvertConfig.class);
    private static Set<String> categorisedProperties = new TreeSet<>();
    private static Set<String> categorisedFiles = new TreeSet<>();
    private static Map<String, Map<String, ApplicationEnvironment>> environments;
    private static Set<String> applications;
    private static NewConfig newConfig = new NewConfig();
    private static int propertyInstanceCount = 0;

    public static final void main(String[] args) {

        ConfigDirectoryWalker directoryWalker = new ConfigDirectoryWalker();
        directoryWalker.walk(new File("."));

        environments = findAllExistingConfiguredEnvironments(directoryWalker);
        applications = findAllExistingConfiguredApplications(directoryWalker);

        for (String environmentName: environments.keySet()) {
            Map<String, ApplicationEnvironment> applicationMap = environments.get(environmentName);

            processApplicationWithinEnvironment(applicationMap);
        }

        ConfigFileGenerator configFileGenerator = new ConfigFileGenerator();
        configFileGenerator.generateFiles(newConfig);

        System.out.println("Unique properties found   : " + categorisedProperties.size());
        System.out.println("Properties instances found: " + propertyInstanceCount);
        System.out.println("New property instances    : " + calculateNewStats());
    }

    private static int calculateNewStats() {
        int newPropertyInstanceCount = 0;

        for (Object propertyName: newConfig.getGlobalConfig().getProperties().keySet()) {
            newPropertyInstanceCount++;
        }

        for (String applicationName: newConfig.getApplicationConfig().keySet()) {
            for (Object propertyName: newConfig.getApplicationConfig().get(applicationName).getProperties().keySet()) {
                newPropertyInstanceCount++;
            }
        }

        for (String environmentName: newConfig.getEnvironmentConfig().keySet()) {
            for (Object propertyName: newConfig.getEnvironmentConfig().get(environmentName).getProperties().keySet()) {
                newPropertyInstanceCount++;
            }
        }

        for (String applicationEnvironmentName: newConfig.getApplicationEnvironmentConfig().keySet()) {
            for (Object propertyName: newConfig.getApplicationEnvironmentConfig().get(applicationEnvironmentName).getProperties().keySet()) {
                newPropertyInstanceCount++;
            }
        }

        return newPropertyInstanceCount;
    }

    private static Set<String> findAllExistingConfiguredApplications(ConfigDirectoryWalker directoryWalker) {
        return directoryWalker.getApplications();
    }

    private static Map<String, Map<String, ApplicationEnvironment>> findAllExistingConfiguredEnvironments(ConfigDirectoryWalker directoryWalker) {
        return directoryWalker.getEnvironments();
    }

    private static void processApplicationWithinEnvironment(Map<String, ApplicationEnvironment> applicationMap) {
        for (String applicationName : applicationMap.keySet()) {
            ApplicationEnvironment appEnv = applicationMap.get(applicationName);

            Properties props = appEnv.getProperties();

            props.forEach((k, v) -> {
                propertyInstanceCount++;

                if (!categorisedProperties.contains(k)) {
                    PropertyCategoriser propertyCategoriser = new PropertyCategoriser(environments, newConfig);
                    propertyCategoriser.categoriseProperty((String) k, (String) v);
                    categorisedProperties.add((String) k);
                }
            });

            appEnv.getGeneratedFiles().forEach((filename) -> {
                if (!categorisedFiles.contains(filename)) {
                    FileCategoriser fileCategoriser = new FileCategoriser(environments, applications, newConfig);
                    fileCategoriser.categoriseFile(filename, appEnv.getOutputDirectory());
                    categorisedFiles.add(filename);
                }
            });
        }
    }
}
