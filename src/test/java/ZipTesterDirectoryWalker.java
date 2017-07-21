import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import net.atos.tfc.environments.ApplicationEnvironment;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;

public class ZipTesterDirectoryWalker extends DirectoryWalker {

    private static Logger LOG = LoggerFactory.getLogger(ZipTesterDirectoryWalker.class);

    private File baseDirectory;
    private Map<String, Map<String, ApplicationEnvironment>> environments = new TreeMap<>();
    private Set<String> applications = new TreeSet<>();


    public ZipTesterDirectoryWalker() {
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

        System.out.println(results);

        return results;
    }

    @Override
    protected boolean handleDirectory(File directory, int depth, Collection results) {
        return !isIgnoreableDirectory(depth, directory);
    }

    private boolean isIgnoreableDirectory(int depth, File directory) {
        return (directory.getName().equals(".idea")
                        || directory.getName().equals("src")
                        || directory.getName().equals("templates")
                        || directory.getName().equals("tfc")
                        || directory.getName().equals(".git"))
                        || directory.getName().equals("config")
                        || (depth == 1 && directory.getName().equals("target"));
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        if (file.getName().endsWith(".zip")) {
            System.out.println();

            String environmentName = substringBetween(file.getPath(), "/", "/");
            String applicationName = removeEnd(substringBefore(file.getName(), "-2.5"), "-config");
            String version = removeEnd(substringAfter(file.getName(), "-2"), ".zip");

            File newZip = new File(format("target/generated-config/%s/%s-2%s.zip", environmentName, applicationName, version));

//            checkNewFileExists(newZip);

            try {
                if (newZip.exists()) {
                    compareZipFiles(file, newZip);
                } else {
                    System.out.println("ERROR: Missing Zip file: " + newZip);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            results.add(file);
        }
    }

    private void checkNewFileExists(File file) {
        if (file.exists()) {
            System.out.println(format("...... File exists %s", file));
        } else {
            System.out.println(format("...... ERROR: File does not exist %s", file));
        }
    }

    private void compareZipFiles(File zipFile1, File zipFile2) throws IOException {
        ZipFile file1 = new ZipFile(zipFile1);
        ZipFile file2 = new ZipFile(zipFile2);

        printStructure(file1, file2);

        Set set1 = new LinkedHashSet();
        for (Enumeration e = file1.entries(); e.hasMoreElements();)
            set1.add(((ZipEntry) e.nextElement()).getName());

        Set set2 = new LinkedHashSet();
        for (Enumeration e = file2.entries(); e.hasMoreElements();)
            set2.add(((ZipEntry) e.nextElement()).getName());

        int errcount = 0;
        int filecount = 0;

        for (Iterator i = set1.iterator(); i.hasNext();) {
            String name = (String) i.next();
            if (!set2.contains(name)) {
                System.out.println(name + " not found in " + zipFile1);
                errcount += 1;
                continue;
            }
            try {
                set2.remove(name);
                if (!streamsEqual(file1.getInputStream(file1.getEntry(name)), file2.getInputStream(file2.getEntry(name)))) {
                    System.out.println(name + " does not match");
                    errcount += 1;
                    continue;
                }
            } catch (Exception e) {
                System.out.println(name + ": IO Error " + e);
                e.printStackTrace();
                errcount += 1;
                continue;
            }
            filecount += 1;
        }
        for (Iterator i = set2.iterator(); i.hasNext();) {
            String name = (String) i.next();
            System.out.println(name + " not found in " + zipFile1);
            errcount += 1;
        }
        System.out.println(filecount + " entries matched");
        if (errcount > 0) {
            System.out.println(errcount + " entries did not match");
        }
    }

    private void printStructure(ZipFile file1, ZipFile file2) {
        System.out.println(format("%-100s %-100s", file1.getName(), file2.getName()));

        Set<String> set1 = new TreeSet<>();
        for (Enumeration e = file1.entries(); e.hasMoreElements();)
            set1.add(((ZipEntry) e.nextElement()).getName());

        Set<String> set2 = new TreeSet<>();
        for (Enumeration e = file2.entries(); e.hasMoreElements();)
            set2.add(((ZipEntry) e.nextElement()).getName());

        int maxSize = set1.size() > set2.size() ? set1.size() : set2.size();

        for (int i = 0; i < maxSize; i++) {
            String left = i < set1.size() ? (String) set1.toArray()[i] : "";
            String right = i < set2.size() ? (String) set2.toArray()[i] : "";

            System.out.println(format("|- %-97s |- %-97s", left, right));
        }
    }

    static boolean streamsEqual(InputStream stream1, InputStream stream2) throws IOException {

        try{
            boolean equal = true;

            List<String> content1 = IOUtils.readLines(stream1, StandardCharsets.UTF_8);
            List<String> content2 = IOUtils.readLines(stream2, StandardCharsets.UTF_8);

            Patch patch = DiffUtils.diff(content1, content2);

            for (Delta delta: patch.getDeltas()) {
                if (delta.getOriginal().getLines().size() > 0 && delta.getRevised().size() > 0) {
                    System.out.println("Original: " + delta.getOriginal().getLines());
                    System.out.println("Revised : " + delta.getRevised());
                    equal = false;
                }

            }

            return equal;
        } finally {
            stream1.close();
            stream2.close();
        }
    }
}
