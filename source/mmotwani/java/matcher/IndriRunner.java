/*
# MIT License
#
# Copyright (c) 2022 LASER-UMASS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
# ==============================================================================
*/

package matcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import parser.ParseSourceCode;

import configuration.ConfigurationParameters;

public class IndriRunner
{
  
  protected static Logger logger = Logger.getLogger(IndriRunner.class);

  
  public IndriRunner() {}
    
  public static void createQuery(String xmlQueriesPath, String processedQueriesPath, String defect) {
	  
	String xmlpath = xmlQueriesPath + "/" + defect + ".xml";
	String processedpath = processedQueriesPath + "/" + defect;
	  
    if ((xmlpath == null) || (processedpath == null)) {
    	 logger.info("You have to provide the bug repo (xml file) location, and specify the location where the query will be stored.");
    	 logger.info("-bugRepoLocation\n-queryFilePath\n");
      return;
    }
    try {
    	 logger.info("Query creation is in progress...This may take a few minutes.");
    	 QueryExtractor.extractSumDesField(xmlpath, processedpath);
    } catch (IOException e) {
    	 logger.info("Please check your bug repo or query file path...");
    }
  }
  
  public static void createDocs(String sourcefolder, String doc_dir, String project, String defectid) throws Exception { 
	 try {
			ParseSourceCode.extractEclipseFacts(sourcefolder, doc_dir, project, defectid);
		} catch (IOException e) {
			logger.error("Error in parsing source file:" + e.getMessage());
		} catch (CoreException e) {
			logger.error(e.getMessage());
		}
  }
  
  public static void indexDocs(String docsLocation, String indexLocation) {
    String indriPath = ConfigurationParameters.IndriPath; //null;

    if (indexLocation == null) { 
    	 logger.info("please provide the document collection location, and specify the location where the index will be stored.");
    	 logger.info("-docLocation\n-indexLocation\n");
      return;
    }
    
    File indexDir = new File(indexLocation);
   
    if (!indexDir.exists()) {
      boolean success = indexDir.mkdir();
      if (!success) {
    	  logger.info("Could not create the Index directory.");
    	  logger.info("Stopping Execution....");
        return;
      }
    }
    
    logger.info(indriPath);
    if (!indriPath.endsWith(File.separator)) {
      indriPath = indriPath + File.separator;
    }
    
    String command = indriPath + "buildindex/IndriBuildIndex -corpus.path=" + docsLocation + " -corpus.class=trectext -index=" + indexLocation + " -memory=2000M -stemmer.name=Krovetz "
    		+ ConfigurationParameters.stopwordsFile + " " + ConfigurationParameters.fieldsFile;
    logger.info("index cmd: " + command);
    executeIndexCommand(command);
    logger.info("Index created successfully.");
  }
  
  public static void retrieveDocs(String queryFilePath, String resultDirPath, String indexLocation, String defect, Integer topk, Double k1, Double b)
  {
    String indriPath = ConfigurationParameters.IndriPath;
    
    File resultDir = new File(resultDirPath);
    logger.info(resultDirPath);
    if (!resultDir.exists()) {
        boolean success = resultDir.mkdir();
        if (!success) {
        	 logger.info("Could not create the Result directory.");
        	 logger.info("Stopping Execution....");
          return;
        }
      }
    
    if ((indexLocation == null) || (queryFilePath == null)) {
      logger.info("You have to provide both the index location and result path.");
      logger.info("-queryFilePath\n-indexLocation\n-resultPath\n-topN [Optional]");return;
    }
    

    if (!indriPath.endsWith(File.separator)) {
      indriPath = indriPath + File.separator;
    }
    
    String command = indriPath + "runquery/IndriRunQuery " + queryFilePath + "  -count=" + topk 
    		+ " -index=" + indexLocation + "  -trecFormat=true -rule=method:tfidf,k1:"+ k1 + ",b:" + b;
    String resultPath = resultDirPath + "/" + defect;
    logger.info(command);
    executeRetrievalCommand(command, resultPath);
  }
  

  private static void executeRetrievalCommand(String command, String resultPath)
  {
	  logger.info("Retrieval in progress. This may take few minutes to even hours depending on the number of queries and size of document collection.");
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(resultPath));
    } catch (IOException e1) {
      logger.error("Problems with result file path");
      return;
    }
    
    try
    {
      Process p = Runtime.getRuntime().exec(command);
      
      BufferedReader reader = 
        new BufferedReader(new InputStreamReader(p.getInputStream()));
      
      String line = "";
      while ((line = reader.readLine()) != null) {
        try {
          bw.write(line);
          bw.newLine();
        } catch (IOException e) {
          bw.close();
          logger.info("Problems in writing result");
          return;
        }
      }
      p.waitFor();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    try
    {
      bw.close();
    } catch (IOException e) {
    	 logger.info("Problems in closing results file after writing.");
      return;
    }
    logger.info("Results are stored successfully in " + resultPath);
  }
  


  private static String executeIndexCommand(String command)
  {
    StringBuffer output = new StringBuffer();
    
    try
    {
      Process p = Runtime.getRuntime().exec(command);
      
      BufferedReader reader = 
        new BufferedReader(new InputStreamReader(p.getInputStream()));
      
      String line = "";
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
      p.waitFor();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return output.toString();
  }
  
  public static ArrayList<String> getSuspiciousFilesForDefect(String defect, int topN) throws IOException{
	  ArrayList<String> filelist = new ArrayList<String>();
	  String resultfilepath = ConfigurationParameters.sourceResultPath + "/top" + topN + "/" + defect + "/" + defect;
	  File file = new File(resultfilepath);
	  BufferedReader br = new BufferedReader(new FileReader(file)); 
  	  String st; 
  	  while ((st = br.readLine()) != null){ 
  	    filelist.add(st.split(" ")[2].trim());
  	  }
  	  br.close();
	  return filelist;
  }
  
}