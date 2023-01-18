# Blues

This repository contains source code for Blues, an unsupervised information-Retrieval-based fault localization technique that uses bug reports to rank suspicious program statements.

If you use Blues, please include the following citation:

Manish Motwani and Yuriy Brun, **Better Automatic Program Repair by Using Bug Reports and Tests Together**, in Proceedings of the 45th International Conference on Software Engineering (ICSE), 2023

## How to Install?

We evaluate Blues using 815 defects in the Defects4J~v2.0 benchmark. 
The dependencies listed below consider this usecase of executing Blues. 

1. Clone this repository.
2. Install [Java-1.8](https://www.oracle.com/java/technologies/downloads/#java8) and set `JAVA_HOME` to the path of Java-8 installation.
3. Install [Defects4J~V2.0](https://github.com/rjust/defects4j/releases/tag/v2.0.0) inside the cloned repository.
4. Intall [Eclipse Neon~v4.6.3](https://archive.eclipse.org/eclipse/downloads/drops4/R-4.6.3-201703010400/)

## How to run?

There are two ways to run Blues: using the executable jar file on command line or importing the project in Eclipse and executing in IDE. For both methods following steps are required. 

Replace `<path-to-blues>` with the absolute path to the current directory in the following files:
  - `blues.settings` 
  - `indri-5.3/site-search/crawl-index`
  - `indri-5.3/site-search/build.param`
  - `indri-5.3/config.status`

To execute Blues on Defects4J defects: 

1. To localize a single defect, run command: `java -jar blues.jar <project_bugid>` (e.g., run `java -jar blues.jar Chart_1` to localize Chart 1 defect ).
2. To localize all 815 defects, run command: `java -jar blues.jar all`. 

We also provide a more general version of Blues that can be used to localize statements in any arbitraty codebase and bug report (hosted on GitHub, Apache Issues, or SourceForge). 

For example, to localize the bug described in https://github.com/jfree/jfreechart/issues/98, which is **not** part of Defects4J, you can use Blues in the following way.

1. Clone the project associated with the bug report (e.g., clone https://github.com/jfree/jfreechart).
2. Identify the absolute path to the directory that contains the source code of the cloned project (e.g. <path-to-jfreechart>/src/main/java).
3. Execute Blues using the command `java -jar blues.jar <project_bugid>  <path-to-source-directory> <URL of bug report>` (e.g., `java -jar blues.jar Chart_98  /home/manish/BluesReleased/jfreechart/src/main/java  https://github.com/jfree/jfreechart/issues/98`)

The localized list of suspicious statements and their scores will get stored in the six sub-directories under `blues_configuration_results` directory. 
Five corresponding to `m` = {1, 25, 50, 100, All} and `ScoreFn=high` and one corresponding to `m=all`, `ScoreFn=wted`

4. Finally, to combine the six Blues configuration results into Blues ensemble, execute the following command. 
 `python combineBluesUsingMaxScoreConsensus.py <path-to-file-listing-defects> <path-to-blues_configuration_results> <path-to-blues_results>`
 
 The final list of the localized statements will be stored in `<path-to-blues_results>` specified in the above command. 

 ## How to extend or experiment using different configuration parameters?

- Import the project in Eclipse and follow the steps 1 described above. 
- Use the main function defined in [`Blues.java`](https://github.com/LASER-UMASS/Blues/blob/main/source/mmotwani/java/main/Blues.java) to launch Blues 
by providing the command line arguments described in 2 or 3 above. 
- To experiment with different weights and scoring functions update the parameters in the file [`ConfigurationParameters.java`](https://github.com/LASER-UMASS/Blues/blob/main/source/mmotwani/java/configuration/ConfigurationParameters.java). 
