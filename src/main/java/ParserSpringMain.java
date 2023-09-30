import analysis.CreateEdge;
import analysis.ForwardTransformer;
import soot.*;
import soot.options.Options;
import utils.GenJimpleUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Test class for processing Spring framework
 */
public class ParserSpringMain {
    // Obtain the path of the project and dependent packages to be tested
    // public static String appDirectory = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "target-demo-0.0.1" ;
    public static String jasmineConfig = System.getProperty("user.dir") + File.separator + "config" + File.separator + "jasmine.properties";
    public static String beanConfig = System.getProperty("user.dir") + File.separator + "config" + File.separator + "bean.properties";
    public static String sourceDir = ""; 
    public static String dependencyDir = ""; 

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // read jasmine config and bean config
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(jasmineConfig));
            String appDir = properties.getProperty("benchmark"); // should be unzipped jar directory
            sourceDir = appDir + File.separator + "BOOT-INF" + File.separator + "classes";
            dependencyDir = appDir + File.separator + "BOOT-INF" + File.separator + "lib";
            System.out.println("sourceDir is: " + sourceDir);
            System.out.println("dependencyDir is: " + dependencyDir);
            initializeSoot(sourceDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // processing Spring framework
        CreateEdge createEdge = new CreateEdge();
        createEdge.initCallGraph(beanConfig);
        // print jimple
        Iterator<SootClass> iterator = Scene.v().getApplicationClasses().snapshotIterator();
        while (iterator.hasNext()) {
            SootClass applicationClass = iterator.next();
            for (SootMethod method : applicationClass.getMethods()) {
                if (!(method.isAbstract() || method.isNative())) {
                    if (!method.hasActiveBody()) {
                        method.retrieveActiveBody();
                    }
                    /*
                    Body body = method.getActiveBody();
                    try {
                        body = Shimple.v().newBody(body);
                        method.setActiveBody(body);
                    } catch (Exception e) {
                        System.err.println(method);
                        System.err.println(body);
                        System.err.println(e);
                    }
                    */
                }

            }
            System.out.println("GenJimpleUtil.write(" + applicationClass + ")");
            GenJimpleUtil.write(applicationClass);
        }
        System.out.println("Writing call graph...");
        Pack pack = PackManager.v().getPack("cg");
        pack.apply();

        pack = PackManager.v().getPack("wjtp");
        pack.add(new Transform("wjtp.ForwardTrans", new ForwardTransformer(sourceDir)));
        pack.apply();

        long endTime = System.currentTimeMillis();
        System.out.println("run time: " + (endTime - startTime) / (1000.0) + "s");
        PackManager.v().writeOutput(); // write output jimple files
    }


    public static void initializeSoot(String sourceDirectory) {
        G.reset();
        List<String> dir = new ArrayList<>();
        dir.add(sourceDirectory);

        System.out.println(dir);
        Options.v().set_process_dir(dir);
        // turn on SPARK and Jasmine
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "verbose:true");
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "propagator:worklist");
        Options.v().setPhaseOption("cg.spark", "simple-edges-bidirectional:false");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
        // Options.v().setPhaseOption("cg.spark", "pre-jimplify:true");
        Options.v().setPhaseOption("cg.spark", "double-set-old:hybrid");
        Options.v().setPhaseOption("cg.spark", "double-set-new:hybrid");
        Options.v().setPhaseOption("cg.spark", "set-impl:double");
        Options.v().setPhaseOption("cg.spark", "apponly:true");
        Options.v().setPhaseOption("cg.spark", "simple-edges-bidirectional:false");
        Options.v().set_verbose(true);

        // turn on CHA
        // Options.v().setPhaseOption("cg.cha", "on");
        // Options.v().setPhaseOption("cg.cha", "enabled:true");
        // Options.v().setPhaseOption("cg.cha", "verbose:true");
        // Options.v().setPhaseOption("cg.cha", "apponly:true");
        // Options.v().set_verbose(true);

        // Soot configuration
        Options.v().set_whole_program(true);
        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_exclude(excludedPackages());
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_soot_classpath(getSootClassPath());
        Options.v().set_verbose(true);
        Options.v().set_output_format(Options.output_format_class);
        Options.v().set_output_dir(sourceDir);
        Scene.v().loadNecessaryClasses();
        PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");
    }

    private static String getSootClassPath() {
        String userdir = System.getProperty("user.dir");
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.equals(""))
            throw new RuntimeException("Could not get property java.home!");

        String sootCp = javaHome + File.separator + "lib" + File.separator + "rt.jar";
        sootCp += File.pathSeparator + javaHome + File.separator + "lib" + File.separator + "jce.jar";

        File file = new File(dependencyDir);
        File[] fs = file.listFiles();
        if(fs != null){
            for (File f : Objects.requireNonNull(fs)) {
                if (!f.isDirectory())
                    sootCp += File.pathSeparator + dependencyDir + File.separator + f.getName();
            }
        }
        return sootCp;
    }

    private static List<String> excludedPackages() {
        List<String> excludedPackages = new ArrayList<>();
        excludedPackages.add("javax.swing.*");
        excludedPackages.add("java.awt.*");
        excludedPackages.add("sun.awt.*");
        excludedPackages.add("com.sun.java.swing.*");
        excludedPackages.add("reactor.*");
        excludedPackages.add("net.sf.cglib.*");
        excludedPackages.add("org.springframework.*");
        excludedPackages.add("org.apache.poi.*");
        excludedPackages.add("com.mysql.*");
        excludedPackages.add("org.ehcache.impl.internal.*");
        excludedPackages.add("ch.qos.*");
        excludedPackages.add("org.apache.*");
        excludedPackages.add("org.eclipse.*");
        excludedPackages.add("java.util.*");
        return excludedPackages;
    }
}
