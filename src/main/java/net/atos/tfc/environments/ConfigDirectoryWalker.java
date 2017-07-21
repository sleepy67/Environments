package net.atos.tfc.environments;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.removeEnd;

public class ConfigDirectoryWalker extends DirectoryWalker {

    private static Logger LOG = LoggerFactory.getLogger(ConfigDirectoryWalker.class);

    private File baseDirectory;
    private Map<String, Map<String, ApplicationEnvironment>> environments = new TreeMap<>();
    private Set<String> applications = new TreeSet<>();


    public ConfigDirectoryWalker() {
    }

    public Map<String, Map<String, ApplicationEnvironment>> getEnvironments() {
        return environments;
    }

    public Set<String> getApplications() {
        return applications;
    }

    public List walk(File startDirectory) {
        List results = new ArrayList();
        try {
            walk(startDirectory, results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println();
        System.out.println(String.format("Environments: %s", environments.keySet()));
        System.out.println(String.format("Applications: %s", applications));
        return results;
    }

    @Override
    protected boolean handleDirectory(File directory, int depth, Collection results) {
        if (isIgnoreableDirectory(depth, directory)) {
            return false;
        }

        if (depth == 1) {
            environments.put(directory.getName(), new TreeMap<>());
        } else if (depth == 2) {
            applications.add(cleanApplicationName(directory.getName()));
        }

        System.out.println(String.format("%s Directory: %s", depth, directory));
        return true;
    }

    private boolean isIgnoreableDirectory(int depth, File directory) {
        return (directory.getName().equals("target")
                        || directory.getName().equals(".idea")
                        || directory.getName().equals("src")
                        || directory.getName().equals("templates")
                        || directory.getName().equals("tfc")
                        || directory.getName().equals(".git"))
                || depth == 3
                        && (directory.getName().equals(directory.getParentFile().getParentFile().getName())
                        || directory.getName().equals("config"));
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        if (depth == 4 && file.getName().equals("filter.properties")) {
            loadEnvironmentApplicationProperties(file);
        } else if (depth == 4 && file.getName().equals("config.xml")) {
            loadEnvironmentApplicationFilesGenerated(file);
        }
        results.add(file);
    }

    private void loadEnvironmentApplicationProperties(File file) {
        LOG.info("Loading properties from file {}", file);

        String environment = file.getParentFile().getParentFile().getParentFile().getName();
        String application = cleanApplicationName(file.getParentFile().getParentFile().getName());

        Map<String, ApplicationEnvironment> environmentMap = environments.get(environment);

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            Properties properties = new Properties();
            properties.load(fis);

            environmentMap.putIfAbsent(application, new ApplicationEnvironment());
            environmentMap.get(application).setProperties(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(fis);
        }
    }

    private void loadEnvironmentApplicationFilesGenerated(File file) {
        LOG.info("Loading files generated from file {}", file);

        String environment = file.getParentFile().getParentFile().getParentFile().getName();
        String application = cleanApplicationName(file.getParentFile().getParentFile().getName());

        Map<String, ApplicationEnvironment> environmentMap = environments.get(environment);

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            Set<String> generatedFiles = extractGeneratedFiles(file);
            String outputDirectory = extractOutputDirectory(file);

            environmentMap.putIfAbsent(application, new ApplicationEnvironment());
            ApplicationEnvironment appEnv = environmentMap.get(application);
            appEnv.setGeneratedFiles(generatedFiles);
            appEnv.setOutputDirectory(outputDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(fis);
        }
    }

    private Set<String> extractGeneratedFiles(File file) {

        Set<String> filenames = new TreeSet<>();

        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(file);

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("//include/text()");

            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;

            for (int i = 0; i < nodes.getLength(); i++) {
                filenames.add(nodes.item(i).getTextContent());
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
        return filenames;
    }

    private String extractOutputDirectory(File file) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(file);

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("//outputDirectory/text()");

            Object result = expr.evaluate(doc, XPathConstants.NODE);
            if (result != null) {
                return ((Node) result).getTextContent();
            } else {
                return "";
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String cleanApplicationName(String name) {
        return removeEnd(name, "-config");
    }
}
