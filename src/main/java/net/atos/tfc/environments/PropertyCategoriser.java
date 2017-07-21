package net.atos.tfc.environments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PropertyCategoriser {

    private static Logger LOG = LoggerFactory.getLogger(PropertyCategoriser.class);

    private Map<String, Map<String, ApplicationEnvironment>> environments;
    private NewConfig newConfig;

    public PropertyCategoriser(Map<String, Map<String, ApplicationEnvironment>> environments, NewConfig newConfig) {
        this.environments = environments;
        this.newConfig = newConfig;
    }

    public PropertyType categoriseProperty(String propertyName, String value) {

        if (propertyName.equals("TFC.thaler.password")) {
            System.out.println(propertyName);
        }

        PropertyType propertyType = null;

        Set<String> environmentsUsingProperty = new TreeSet<>();
        Set<String> applicationsUsingProperty = new TreeSet<>();
        Set<String> values = new TreeSet<>();
        Map<String, Set<String>> valueApplicationMap = new TreeMap<>();
        Map<String, Set<String>> valueEnvironmentMap = new TreeMap<>();

        Map<String, Map<String, Set<String>>> environmentValueApplicationMap = new TreeMap<>();
        Map<String, Map<String, Set<String>>> applicationsValueEnvironmentMap = new TreeMap<>();
        Map<String, Map<String, Set<String>>> applicationEnvironmentValueMap = new TreeMap<>();

        for (String environmentName: environments.keySet()) {
            Map<String, ApplicationEnvironment> applicationMap = environments.get(environmentName);

            for (String applicationName : applicationMap.keySet()) {
                ApplicationEnvironment appEnv = applicationMap.get(applicationName);

                Properties props = appEnv.getProperties();

                if (props.containsKey(propertyName)) {
                    String currentValue = props.getProperty(propertyName);

                    environmentsUsingProperty.add(environmentName);
                    applicationsUsingProperty.add(applicationName);

                    valueApplicationMap.putIfAbsent(currentValue, new HashSet<>());
                    valueApplicationMap.get(currentValue).add(applicationName);

                    valueEnvironmentMap.putIfAbsent(currentValue, new HashSet<>());
                    valueEnvironmentMap.get(currentValue).add(environmentName);

                    environmentValueApplicationMap.putIfAbsent(environmentName, new TreeMap<>());
                    environmentValueApplicationMap.get(environmentName).putIfAbsent(currentValue, new HashSet<>());
                    environmentValueApplicationMap.get(environmentName).get(currentValue).add(applicationName);

                    applicationsValueEnvironmentMap.putIfAbsent(applicationName, new TreeMap<>());
                    applicationsValueEnvironmentMap.get(applicationName).putIfAbsent(currentValue, new HashSet<>());
                    applicationsValueEnvironmentMap.get(applicationName).get(currentValue).add(environmentName);

                    applicationEnvironmentValueMap.putIfAbsent(applicationName, new TreeMap<>());
                    applicationEnvironmentValueMap.get(applicationName).putIfAbsent(environmentName, new HashSet<>());
                    applicationEnvironmentValueMap.get(applicationName).get(environmentName).add(currentValue);

                    values.add(currentValue);
                }
            }
        }

        if (values.size() == 1) {
            propertyType = processSingleValue(propertyName, value, environmentsUsingProperty, applicationsUsingProperty);
        } else if (applicationsUsingProperty.size() == 1 ) {
            propertyType = processValueUsedByASingleApplication((String) applicationsUsingProperty.toArray()[0], propertyName, valueEnvironmentMap);
        } else {
            LOG.warn("{}", applicationsUsingProperty);
            propertyType = processValueUsedByMultipleApplications(propertyName, valueEnvironmentMap, valueApplicationMap, environmentValueApplicationMap, applicationsValueEnvironmentMap, applicationEnvironmentValueMap);
        }

        if (propertyType == null) {
            LOG.warn("Not sure - {}", propertyName);
            LOG.warn("Environments: {}", environmentsUsingProperty);
            LOG.warn("Environments: {}", valueEnvironmentMap);
            LOG.warn("Applications: {}", applicationsUsingProperty);
            LOG.warn("Applications: {}", valueApplicationMap);
            LOG.warn("Values:       {}", values);
        }

        return propertyType;
    }

    private PropertyType processSingleValue(String propertyName, String value, Set<String> environmentsUsingProperty, Set<String> applicationsUsingProperty) {
        PropertyType propertyType = null;

        if (applicationsUsingProperty.size() > 1 && environmentsUsingProperty.size() > 1) {
            newConfig.addGlobalProperty(propertyName, value);
            propertyType = PropertyType.GLOBAL;
        } else if (applicationsUsingProperty.size() == 1 && environmentsUsingProperty.size() > 1) {
            newConfig.addApplicationProperty((String) applicationsUsingProperty.toArray()[0], propertyName, value);
            propertyType = PropertyType.APPLICATION;
        } else if (applicationsUsingProperty.size() > 1 && environmentsUsingProperty.size() == 1) {
            newConfig.addEnvironmentProperty((String) environmentsUsingProperty.toArray()[0], propertyName, value);
            propertyType = PropertyType.ENVIRONMENT;
        } else if (applicationsUsingProperty.size() == 1 && environmentsUsingProperty.size() == 1) {
            newConfig.addApplicationEnvironmentProperty((String) environmentsUsingProperty.toArray()[0], (String) applicationsUsingProperty.toArray()[0], propertyName, value);
            propertyType = PropertyType.APPLICATION_ENVIRONMENT;
        }

        return propertyType;
    }

    private PropertyType processValueUsedByASingleApplication(String applicationName, String propertyName, Map<String, Set<String>> mapOfValuesToEnvironments) {
        Map<String, Set<String>> sortedMapOfValuesToEnvironments = sortByValueCount(mapOfValuesToEnvironments);

        PropertyType propertyType = null;
        boolean applicationPropertySet = false;

        for (String currentValue: sortedMapOfValuesToEnvironments.keySet()) {
            if (!applicationPropertySet && usedInMoreThanOne(sortedMapOfValuesToEnvironments, currentValue)) {
                newConfig.addApplicationProperty(applicationName, propertyName, currentValue);
                propertyType = PropertyType.APPLICATION;
                applicationPropertySet = true;
            } else {
                for (String environment: mapOfValuesToEnvironments.get(currentValue)) {
                    newConfig.addEnvironmentProperty(environment, propertyName, currentValue);
                    propertyType = PropertyType.ENVIRONMENT;
                }
            }
        }
        return propertyType;
    }

    private boolean usedInMoreThanOne(Map<String, Set<String>> mapOfValues, String currentValue) {
        return mapOfValues.get(currentValue).size() > 1;
    }

    private boolean usedInOnlyOne(Map<String, Set<String>> mapOfValues, String currentValue) {
        return mapOfValues.get(currentValue).size() == 1;
    }

    private PropertyType processValueUsedByMultipleApplications(
            String propertyName, Map<String,
            Set<String>> valueEnvironmentMap, Map<String,
            Set<String>> valueApplicationMap, Map<String,
            Map<String, Set<String>>> environmentValueApplicationMap,
            Map<String, Map<String, Set<String>>> applicationValueEnvironmentMap,
            Map<String, Map<String, Set<String>>> applicationEnvironmentValueMap) {

        Map<String, Set<String>> sortedValues;
        if (usedInAllEnvironments(valueEnvironmentMap)) {
            sortedValues = sortByValueCount(valueApplicationMap);
        } else {
            sortedValues = sortByValueCount(valueEnvironmentMap);
        }

        PropertyType propertyType = null;
        boolean globalPropertySet = false;
        Set<String> applicationPropertySet = new HashSet<>();
        Set<String> environmentPropertySet = new HashSet<>();


        for (String currentValue: sortedValues.keySet()) {

            if (!globalPropertySet && usedInMoreThanOne(sortedValues, currentValue)) {
                newConfig.addGlobalProperty(propertyName, currentValue);
                propertyType = PropertyType.GLOBAL;
                globalPropertySet = true;

            // This particular value only used by a single application and it has not been added to the application properties yet
            } else if (valueApplicationMap.get(currentValue).size() == 1 && !applicationPropertySet.contains(valueApplicationMap.get(currentValue).toArray()[0])) {
                String applicationName = (String) valueApplicationMap.get(currentValue).toArray()[0];
                newConfig.addApplicationProperty(applicationName, propertyName, currentValue);
                propertyType = PropertyType.APPLICATION;
                applicationPropertySet.add(applicationName);

            // This particular value only used by a single environment and it has not been added to the application properties yet
//            } else if (valueEnvironmentMap.get(currentValue).size() == 1 && !environmentPropertySet.contains(valueEnvironmentMap.get(currentValue).toArray()[0])) {
//                String environmentName = (String) valueApplicationMap.get(currentValue).toArray()[0];
//                newConfig.addEnvironmentProperty(environmentName, propertyName, currentValue);
//                propertyType = PropertyType.ENVIRONMENT;
//                environmentPropertySet.add(environmentName);

            } else {
                for (String applicationName: valueApplicationMap.get(currentValue)) {
                    if (!applicationPropertySet.contains(applicationName)) {
                        newConfig.addApplicationProperty(applicationName, propertyName, currentValue);
                        propertyType = PropertyType.APPLICATION;
                        applicationPropertySet.add(applicationName);
                    }
                }

//                for (String environmentName: sortedValues.get(currentValue)) {
//
//                    // This particular value only used by multiple applications and it has not been added to the environment properties yet
//                    if (valueApplicationMap.get(currentValue).size() > 1 && !environmentPropertySet.contains(environmentName)) {
//                        newConfig.addEnvironmentProperty(environmentName, propertyName, currentValue);
//                        propertyType = PropertyType.ENVIRONMENT;
//                        environmentPropertySet.add(environmentName);
//                    } else {
//                        for (String applicationName: valueApplicationMap.get(currentValue)) {
//                            if (!applicationPropertySet.contains(applicationName)) {
//                                newConfig.addApplicationEnvironmentProperty(environmentName, applicationName, propertyName, currentValue);
//                                propertyType = PropertyType.APPLICATION_ENVIRONMENT;
//                            }
//                        }
//                    }
//                }
            }
        }

        // Now need to go and check no values are overwriting each other and if so add a lower level override
        Object globalValue = newConfig.getGlobalConfig().getProperties().get(propertyName);

        for (String applicationName: applicationEnvironmentValueMap.keySet()) {
            Map<String, Set<String>> mapOfEnvironmentsToValue = applicationEnvironmentValueMap.get(applicationName);

            Object applicationValue = newConfig.getApplicationConfig().getOrDefault(applicationName, new ApplicationEnvironment()).getProperties().getOrDefault(propertyName, globalValue);

            for (String environmentName: mapOfEnvironmentsToValue.keySet()) {
                Set<String> values = mapOfEnvironmentsToValue.get(environmentName);

                Object environmentValue = newConfig.getEnvironmentConfig().getOrDefault(environmentName, new ApplicationEnvironment()).getProperties().getOrDefault(propertyName, applicationValue);

                if (values.size() > 1) {
                    throw new RuntimeException("Application/Environment has more than one value: " + values);
                }

                String targetValue = (String) values.toArray()[0];

                if (!targetValue.equals(environmentValue)) {
                    newConfig.addApplicationEnvironmentProperty(environmentName, applicationName, propertyName, targetValue);
                    propertyType = PropertyType.APPLICATION_ENVIRONMENT;
                }
            }
        }

        return propertyType;
    }

    private boolean usedInAllEnvironments(Map<String, Set<String>> mapOfValuesToEnvironments) {
        boolean usedInAllEnvironments = true;

        for (Set<String> environments : mapOfValuesToEnvironments.values()) {
            if (environments.size() != 12) {
                usedInAllEnvironments = false;
            }
        }
        return usedInAllEnvironments;
    }

    public static <K, V extends Set<String>> Map<K, V> sortByValueCount(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue().size() - o1.getValue().size());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
