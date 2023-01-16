# Blues

This repository contains source code for Blues, an unsupervised information-Retrieval-based fault localization technique that uses bug reports to rank suspicious program statements.

If you use Blues, please include the following citation:

Manish Motwani and Yuriy Brun, Better Automatic Program Repair by Using Bug Reports and Tests Together, in Proceedings of the 45th International Conference on Software Engineering (ICSE), 2023

## How to Install?

We evaluate Blues using 815 defects in the Defects4J~v2.0 benchmark. 
The dependencies listed below consider this usecase of executing Blues. 

1. Clone this repository.
2. Install Java-8 and set `JAVA_HOME` to the path of Java-8 installation.
3. Intall Defects4J~V2.0 inside the cloned repository.
4. Intall Eclipse Neon~v4.6.3

## How to run?

There are two ways to run Blues: using the executable jar file on command line or importing the project in Eclipse and executing in IDE. For both methods following steps are required. 

1. Replace `<path-to-blues>` with the absolute path to the current directory in the following files:
  - `blues.settings` 
  - `indri-5.3/site-search/crawl-index`
  - `indri-5.3/site-search/build.param`
  - `indri-5.3/config.status`
2. To localize a single defect, run cmd: `java -jar blues.jar Chart_1` (e.g., to localize Chart 1 defect)
3. To localize all 815 defects, run cmd: `java -jar blues.jar all`. 

The localized list of suspicious statements and their scored will get stored in the six sub-folders under `blues_configuration_results` directory. 
Five corresponding to `m` = {1, 25, 50, 100, All} and `ScoreFn=high` and one corresponding to `m=all`, `ScoreFn=wted`

4. Combine the six Blues configuration results into Blues ensemble by executing the following command. 
 `python combineBluesUsingMaxScoreConsensus.py 815defects.txt <path-to-blues_configuration_results> <path-to-blues_results>`
 
 The final list of the localized statements will be stored in `<path-to-blues_results>` specified in the above command. 
 
 ## How to extend or experiment using different configuration parameters?

- Import the project in Eclipse and follow the steps 1 described above. 
- Use the main function defined in [`Blues.java`](https://github.com/LASER-UMASS/Blues/blob/main/src/main/java/main/Blues.java) to launch Blues 
by providing the command line arguments described in 2 or 3 above. 
- To experiment with different weights and scoring functions update the parameters in the file [`ConfigurationParameters.java`](https://github.com/LASER-UMASS/Blues/blob/main/src/main/java/configuration/ConfigurationParameters.java). 
