# Table of Content 

- [Jasmine](#jasmine)
  * [Doop_Jasmine](#doop_jasmine)
    + [1. Set up Doop_Jasmine as docker container](#1-set-up-doop_jasmine-as-docker-container)
    + [2. Reproduce Paper Section 5.2 Results on Doop](#2-reproduce-paper-section-52-results-on-doop)
      - [2.1 JackEE, Default and Jasmine use context-insensitive analysis (corresponding to ID-1-EntryPoint, ID-4-SpringAOP, ID-7-Reachable  Methods, ID-8-Application Edges)](#21-jackee-default-and-jasmine-use-context-insensitive-analysis-corresponding-to-id-1-entrypoint-id-4-springaop-id-7-reachable--methods-id-8-application-edges)
      - [2.2 JackEE, Default and Jasmine use context-insensitive analysis and taint analysis (corresponding to ID-2-DI(Singleton), ID-3-DI(Prototype), ID-5-InfoLeak)](#22-jackee-default-and-jasmine-use-context-insensitive-analysis-and-taint-analysis-corresponding-to-id-2-disingleton-id-3-diprototype-id-5-infoleak)
    + [3. Reproduce Paper Section 5.3 Results on JackEE](#3-reproduce-paper-section-53-results-on-jackee)
      - [3.1 Run Jasmine and JackEE](#31-run-jasmine-and-jackee)
      - [3.2 Run data processing scripts](#32-run-data-processing-scripts)
  * [Soot_Jasmine and PTA enhancement](#soot_jasmine-and-pta-enhancement)
    + [1. Setup Soot_Jasmine:](#1-setup-soot_jasmine)
      - [1.1 Environment requirements](#11-environment-requirements)
      - [1.2 CHA, SPARK and Jasmine（corresponding to ID-1-EntryPoint, ID-4-SpringAOP, ID-7-Reachable  Methods, ID-8-Application Edges）](#12-cha-spark-and-jasminecorresponding-to-id-1-entrypoint-id-4-springaop-id-7-reachable--methods-id-8-application-edges)
      - [1.3 SPARK, Jasmine context-sensitive（corresponding to ID-2-DI(Singleton), ID-3-DI(Prototype)）](#13-spark-jasmine-context-sensitivecorresponding-to-id-2-disingleton-id-3-diprototype)
  * [FlowDroid_Jasmine](#flowdroid_jasmine)

---

# Jasmine

This file contains instructions on how to build and run the tool and how to reproduce experiments in the Jasmine paper.

---

## Doop_Jasmine

### 1. Set up Doop_Jasmine as docker container

Doop_Jasmine is setup as a docker container. To setup: 

* Execute the Dockerfile in the [Jasmine repository](https://github.com/SpringJasmine/Jasmine) to generate a docker container, ```/localdata``` is the folder path of your machine. Please change it according to the actual situation.

```sh
$ git clone git@github.com:SpringJasmine/Jasmine.git # clone this repo
$ cd Jasmine
$ docker build -t jasmine:v1 .
$ docker run -it -h Jasmine.gt --privileged=True -e "container=docker" -v /localdata:/data --net=host --name="Jasmine" jasmine:v1 bash
# /localdata is your local folder path
```

* Now you have a built Doop_Jasmine setup in the container. In the container, you can further run bash commands mentioned in the following sections to reproduce experiments of the paper. 

### 2. Reproduce Paper Section 5.2 Results on Doop

This table mainly shows the improvement of Jasmine's detection ability of JackEE, including TABLE II and Fig.4.

#### 2.1 JackEE, Default and Jasmine use context-insensitive analysis (corresponding to ID-1-EntryPoint, ID-4-SpringAOP, ID-7-Reachable  Methods, ID-8-Application Edges)

* In the docker container, run the following command.

```sh
# JackEE
$ bash /root/JackEEforSpringDemo.bash

# Defalut
$ bash /root/DefaultforSpringDemo.bash

# Jasmine
$ bash /root/JasmineforSpringDemo.bash
```

#### 2.2 JackEE, Default and Jasmine use context-insensitive analysis and taint analysis (corresponding to ID-2-DI(Singleton), ID-3-DI(Prototype), ID-5-InfoLeak)

* In the docker container, run the following command.

```sh
# JackEE
$ bash /root/JackEEforInfoSpringDemo.bash

# Defalut
$ bash /root/DefaultforInfoSpringDemo.bash

# Jasmine
$ bash /root/JasmineforInfoSpringDemo.bash
```

### 3. Reproduce Paper Section 5.3 Results on JackEE

This tab shows the improvement of Jasmine's detection ability of JackEE, including TABLE III, TABLE IV, Fig.5, and Fig.6. Due to differences in the performance of the relevant machines, there may be some minor differences in the correlation results.

#### 3.1 Run Jasmine and JackEE

The result of **TABLE III, Fig.5 and Fig.6** can be counted by executing the following commands, in the docker container.

```sh
# in docker container

$ cd /root/newdoop
# Jas_JackEE
$ bash /root/TurnOnJasmine.bash
$ python3 /root/newdoop/runjasmine.py
# JackEE
$ bash /root/TurnOffJasmine.bash
$ python3 /root/newdoop/runjackee.py
```

#### 3.2 Run data processing scripts

Dockerfile generates ```config.properties``` in ```/root```. Please modify items according to the output items under``` /data/doop/out/```, in the docker container.


```properties
# config.properties
process_project_name = mall-admin
Jasmine_CallGraph_Path = /data/doop/out/jasminemall-admin/database/CallGraphEdge.csv
JackEE_CallGraph_Path = /data/doop/out/jackeemall-admin/database/CallGraphEdge.csv
Jasmine_ReachableMethod_Path = /data/doop/out/jasminemall-admin/database/Stats_Simple_Application_ReachableMethod.csv
JackEE_ReachableMethod_Path = /data/doop/out/jackeemall-admin/database/Stats_Simple_Application_ReachableMethod.csv
resultPath = /root/output/
```
![image-20220830160118097](https://github.com/SpringJasmine/IMAGE/blob/main/image-20220830160118097.png)

The results of **TABLE III and TABLE IV** can be counted by executing the following commands in the docker container:

```sh
# in docker container 
$ java -jar /root/DataCollect-1.0-SNAPSHOT.jar -c /root/config.properties
```
![image-20220830155609790](https://github.com/SpringJasmine/IMAGE/blob/main/image-20220830155609790.png)



---
 
## Soot_Jasmine and PTA enhancement

### 1. Setup Soot_Jasmine: 

_The docker container used in the previous section is for Doop_Jasmine only; for experiments on Soot, you don't need to setup that container._

#### 1.1 Environment requirements

* Java version is Java 8
* Maven version is 3.6.1
* Clone the following two projects:
 ```bash
 git clone https://github.com/SpringJasmine/Jasmine

 ```
* Open [the cloned project](https://github.com/SpringJasmine/Jasmine) in IntelliJ (or other IDE)


#### 1.2 CHA, SPARK and Jasmine（corresponding to ID-1-EntryPoint, ID-4-SpringAOP, ID-7-Reachable  Methods, ID-8-Application Edges）

* Modify `src/main/resources/config.properties` to specify `bean_xml_paths` to the absolute path of the xml file specifying beans 

```properties
# Modify bean_xml_paths to the absolute path where the corresponding file

# For example, to use Jasmine to analyze the application `demo` in `/User/Jasmine/demo`
# `demo` has its xml file specifying beans in `demo/target-demo-0.0.1/BOOT-INF/classes/bean.xml`
bean_xml_paths = /User/Jasmine/demo/target-demo-0.0.1/BOOT-INF/classes/bean.xml
```

<img src="https://github.com/SpringJasmine/IMAGE/blob/main/image-20220830192659193.png" alt="image-20220830190525954" style="zoom:33%;" />
<img src="https://github.com/SpringJasmine/IMAGE/blob/main/image-20220830185918067.png" alt="image-20220830190525954" style="zoom:33%;" />

* Modify ```src/main/java/ParserSpringMain.java```

```java
//When the following three lines are not commented, jasmine is turned on
29: CreateEdge createEdge = new CreateEdge();
30: String path = "config.properties"; // Modify field to the path of config.properties in resources
31: createEdge.initCallGraph(path);
...    
74: // turn on SPARK and Jasmine
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
87: Options.v().set_verbose(true);
88:
89: // turn on CHA
    // Options.v().setPhaseOption("cg.cha", "on");
    // Options.v().setPhaseOption("cg.cha", "enabled:true");
    // Options.v().setPhaseOption("cg.cha", "verbose:true");
    // Options.v().setPhaseOption("cg.cha", "apponly:true");
94: // Options.v().set_verbose(true);
```

<img src="https://github.com/SpringJasmine/IMAGE/blob/main/image-20220830193130.png" alt="image-20220830190756735" style="zoom:33%;" />

* Execute the main method in ```src/main/java/ParserSpringMain```

<img src="https://github.com/SpringJasmine/IMAGE/blob/main/image-20220830190756735.png" alt="image-20220830190756735" style="zoom:33%;" />

#### 1.3 SPARK, Jasmine context-sensitive（corresponding to ID-2-DI(Singleton), ID-3-DI(Prototype)）

* Download [Turner](http://www.cse.unsw.edu.au/~corg/turner/), a static acceleration tool for pointer analysis tool that represents a new sweet spot between precision and efficiency of Pointer Analysis.
```sh
# in the base directory where you clone other repositories
$ wget http://www.cse.unsw.edu.au/~corg/turner/Turner-src-v1.0.tar.xz
$ tar -xf Turner-src-v1.0.tar.xz
```

* Create a `libs` folder under the project directory and add [**`PointerAnalysis-1.0-SNAPSHOT.jar`**](https://github.com/SpringJasmine/FlowDroid_Jasmine/tree/main/dataleak/dist) to `libs`: 
```shell
cd Turner
cp ../FlowDroid_Jasmine/dataleak/dist/PointerAnalysis-1.0-SNAPSHOT.jar libs/.
```

* Add the **PointerAnalysis-1.0-SNAPSHOT.jar** as a project dependency for Turner
```
cd Turner/pta
vim build.gradle
```
and add the following line in `pta/build.gradle`:
```diff
     ...
     compile files("${rootDir}/libs/util-1.0-SNAPSHOT.jar")
+    compile files("${rootDir}/libs/PointerAnalysis-1.0-SNAPSHOT.jar")
```

* Add Jasmine in ```Turner/pta/src/driverMain.java```:

```diff
+ import analysis.CreateEdge;
...
24: public static PTA run(String[] args) {
25:     PTA pta;
26:     new PTAOption().parseCommandLine(args);
27:     setupSoot();
+        // Modify field to the path of config.properties
+        CreateEdge createEdge = new CreateEdge();
+        createEdge.initCallGraph("config.properties");
```

* Build the modified Turner tool
```shell
$ cd Turner
$ ./run.sh
# the executable file `turner-artifact-1.0-SNAPSHOT.jar` will be put into `Turner/artifact/pta/`directory.
```
* Execute the main method in `driver.Main`; or, run the PTA on a benchmark using the scripts comes with the Turner repository: 
```shell
# using the script in artifact/run.py
$ python3 artifact/run.py antlr # antlr is an example benchmark option specified in artifact/util/benchmark.py file
```


--- 

## FlowDroid_Jasmine 

Setup ad Run FlowDroid_Jasmine, please refer to instructions in https://github.com/dorawyy/FlowDroid_Jasmine. 