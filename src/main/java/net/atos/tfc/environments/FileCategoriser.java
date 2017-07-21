package net.atos.tfc.environments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FileCategoriser {

    private static Logger LOG = LoggerFactory.getLogger(FileCategoriser.class);

    private Map<String, Map<String, ApplicationEnvironment>> environments;
    private Set<String> applications;
    private final NewConfig newConfig;

    public FileCategoriser(Map<String, Map<String, ApplicationEnvironment>> environments, Set<String> applications, NewConfig newConfig) {
        this.environments = environments;
        this.applications = applications;
        this.newConfig = newConfig;
    }

    public void categoriseFile(String filename, String outputDirectory) {
        PropertyType propertyType = null;

        Set<String> environmentsUsingFile = new TreeSet<>();
        Set<String> applicationsUsingFile = new TreeSet<>();
        Set<String> files = new TreeSet<>();
        Map<String, Set<String>> mapOfFileToApplications = new TreeMap<>();
        Map<String, Set<String>> mapOfFileToEnvironments = new TreeMap<>();

        for (String environmentName: environments.keySet()) {
            Map<String, ApplicationEnvironment> applicationMap = environments.get(environmentName);

            for (String applicationName : applicationMap.keySet()) {

                ApplicationEnvironment appEnv = applicationMap.get(applicationName);

                Set<String> filenames = appEnv.getGeneratedFiles();

                if (filenames.contains(filename)) {
                    environmentsUsingFile.add(environmentName);
                    applicationsUsingFile.add(applicationName);

                }
            }
        }

        // All applications use this file
        if (applicationsUsingFile.size() == applications.size()) {
            newConfig.addGlobalFile(filename, outputDirectory);
        // Used by a single application
        } else {
            for (String applicationName: applicationsUsingFile) {
                newConfig.addApplicationFile(applicationName, filename, outputDirectory);
            }
        }

    }
}
