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

package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import configuration.ConfigurationParameters;
import parser.*;
import matcher.IndriRunner;
import utilities.*;
import postprocessor.*;

public class Blues {

	static HashSet<String> d4jprojects = new HashSet<String>();
	static ArrayList<String> d4jdefects = new ArrayList<String>();
	static HashMap<String, ArrayList<String>> d4jdefect_suspiciousfiles = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> d4jdefect_buggyfiles = new HashMap<String, ArrayList<String>>(); // developer modified files
	static String[] paths = null;
	protected static Logger logger = Logger.getLogger(Blues.class);

	public static void checkoutDefects4jDefect(String project, String defectid) throws Exception{

		File chkout_dir= new File(ConfigurationParameters.bugCheckoutDirectory);
		if (!chkout_dir.exists())
			chkout_dir.mkdir();
		
		String command1 = "rm -rf " + ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy";
		logger.info("running cmd: " + command1);
		ExecuteCommandLine.executeCommand(command1);

		String command2 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j checkout -p " + project + " -v " + defectid + "b -w "
				+ ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy";
		logger.info("running cmd: " + command2);
		ExecuteCommandLine.executeCommand(command2);

		String command3 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j compile "
				+ "-w " + ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy";
		logger.info("running cmd: " + command3);
		logger.info(ExecuteCommandLine.executeCommandAndGetOutput(command3));
	}


	public static void deleteDefects4jDefect(String project, String defectid) throws Exception{

		String command1 = "rm -rf " + ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy";
		logger.info("running cmd: " + command1);
		ExecuteCommandLine.executeCommand(command1);
	}

	public static void deleteIndriDocs(String defect) throws Exception{

		String command = "rm -rf " + ConfigurationParameters.statementDocumentsDirectory + "/" + defect;
		logger.info("running cmd: " + command);
		ExecuteCommandLine.executeCommand(command);
		command = "rm -rf " + ConfigurationParameters.sourceDocumentsDirectory + "/" + defect;
		logger.info("running cmd: " + command);
		ExecuteCommandLine.executeCommand(command);
	}

	
	/* crawl bug reports, extract relevant information, and store it in an XML format for each project
	 * 
	 */
	public static void fetchBugReportsAndCreateQuery(String defect) throws Exception {
		BugReportCrawler brc = new BugReportCrawler();
		if (defect.contentEquals("all")){
				for (String path : paths) {
					logger.info(path);
					String project = path.split("/")[path.split("/").length - 2].trim();
					if (!d4jprojects.contains(project)) {
						d4jprojects.add(project);
					}
					logger.info("fetching bug report link");
					d4jdefects.addAll(brc.getBugReportLinks(path, defect));
				}
				logger.info("extracting details from bug reports of all defects");
				brc.getBugReportDetails(d4jdefects);
				logger.info("creating XML query files for all defects");
				brc.writeToXML(ConfigurationParameters.XMLQueryPath, defect);
				logger.info("serializing list of defects processed in d4j-defects.txt");
				Serialize.serializeArrayList(d4jdefects, "d4j-defects.txt");
		} else{
				for (String path : paths) {
					String project = path.split("/")[path.split("/").length - 2].trim();
					if (!defect.contains(project)) continue;
					logger.info("fetching bug report link from " + path);
					ArrayList<String> d4jdefect = brc.getBugReportLinks(path, defect);
					if (!d4jdefects.contains(d4jdefect.get(0))) d4jdefects.add(d4jdefect.get(0));
				}
				ArrayList<String> defectarray = new ArrayList<String>();
				defectarray.add(defect);
				logger.info("extracting details from bug report of " + defect);
				brc.getBugReportDetails(defectarray);
				logger.info("creating XML query file for " + defect);
				brc.writeToXML(ConfigurationParameters.XMLQueryPath, defect);
				logger.info("serializing " + defect + " in d4j-defects.txt");
				Serialize.serializeArrayList(d4jdefects, "d4j-defects.txt");
			}		
	}

	/*  for each defect, get all the Java files from the source directory, preprocess code to extract tokens and write to docs
	 * 
	 */
	public static void extractJavaFilesAndCreateDocs(String defect) throws Exception {

		logger.info("processing defect: " + defect);
		File result_Dir = new File(ConfigurationParameters.sourceResultPath + "/top50/" + defect);
		if (result_Dir.exists()){
			logger.info("File results already exist. Skipping this defect.");
			return;
		}
		
		File src_doc_dir= new File(ConfigurationParameters.sourceDocumentsDirectory);
		if (!src_doc_dir.exists())
			src_doc_dir.mkdir();
		
		String doc_dir = ConfigurationParameters.sourceDocumentsDirectory + "/" + defect;
		File theDir = new File(doc_dir);
		logger.info("storing file docs at " + doc_dir);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.getName());
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				logger.error("cannot create directory at " + theDir.getName());
			}
			if (result) {
				logger.info("created directory");
			}
		} 

		String project = defect.split("_")[0];
		String defectid = defect.split("_")[1];
		logger.info("checkout buggy version of program");
		checkoutDefects4jDefect(project, defectid);

		String command5 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j export -p dir.src.classes "
				+ "-w " + ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy";
		logger.info("running cmd: " + command5);
		String[] output = ExecuteCommandLine.executeCommandAndGetOutput(command5).split("\n"); 
		String source_folder_name = output[output.length-1].trim();
		String source_folder_path = ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy/" + source_folder_name;
		logger.info("source folder name: " + source_folder_name);
		logger.info("source folder path: " + source_folder_path);


		File sourcepath = new File(source_folder_path);
		if (!sourcepath.exists()){
			logger.error("source path DNE!: " + sourcepath);
			return;
		}
		if (!sourcepath.isDirectory()){
			logger.error("source folder DNE!: " + source_folder_path);
			return;
		}
		logger.info("calling Indri to create docs");
		IndriRunner.createDocs(source_folder_path, doc_dir, project, defectid);
	}

	/* for each defect get top-50 Java files that are related to bug report and serialize the results
	 * 
	 */
	public static void getTopKRelevantJavaFiles(String defect, int topN) throws IOException {
		
		File result_Dir = new File(ConfigurationParameters.sourceResultPath + "/top" + topN + "/" + defect);
		if (result_Dir.exists()) {
			logger.info("File results already exist. Please delete existing directory to re-compute results.");
			return;
		}
		
		File result_fileDir = new File(ConfigurationParameters.sourceResultPath);
		if (!result_fileDir.exists()) {
			result_fileDir.mkdir();
		}
		result_fileDir = new File(ConfigurationParameters.sourceDocumentsIndex);
		if (!result_fileDir.exists()) {
			result_fileDir.mkdir();
		}
		result_fileDir = new File(ConfigurationParameters.ProcessedQueryPath);
		if (!result_fileDir.exists()) {
			result_fileDir.mkdir();
		}
		
		logger.info("############################### " + defect + " ##################################");
		String queryPath = ConfigurationParameters.ProcessedQueryPath + "/" + defect;
		String resultPath = ConfigurationParameters.sourceResultPath + "/top" + topN + "/" + defect;
		String indexPath = ConfigurationParameters.sourceDocumentsIndex + "/" + defect;
		logger.info("############################### query");
		// create query from xml file
		File queryfile = new File(queryPath);
		if (!queryfile.exists()) {
			IndriRunner.createQuery(ConfigurationParameters.XMLQueryPath,
					ConfigurationParameters.ProcessedQueryPath, defect);
		}
		logger.info("############################### index");
		// index document collection
		File indexfile = new File(indexPath);
		if (!indexfile.exists()) {
			IndriRunner.indexDocs(ConfigurationParameters.sourceDocumentsDirectory + "/" + defect,
					ConfigurationParameters.sourceDocumentsIndex + "/" + defect);
		}
		logger.info("############################### retrieve");
		// get top-N relevant Java files for each bug report (query)
		File resultfile = new File(resultPath);
		if (!resultfile.exists()) {
			File resultDir = new File(ConfigurationParameters.sourceResultPath + "/top" + topN);
			if (!resultDir.exists()) {
				boolean success = resultDir.mkdir();
				if (!success) {
					logger.info("Could not create the Result directory for defect");
					return;
				}
			}
			IndriRunner.retrieveDocs(queryPath, resultPath, indexPath, defect, topN, 1.0, 0.3);
		}
		logger.info("############################### DONE ############################");

	}

	public static void getBuggyFilesOfDefects() throws IOException{
		File defectfile = new File("d4j-defects.txt");
		boolean exists1 = defectfile.exists();
		if (exists1) {
			d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
		}
		for (String defect : d4jdefects) {
			String project = defect.split("_")[0];
			String defectid = defect.split("_")[1];
			logger.info("processing defect " + defect);
			if (!d4jdefect_buggyfiles.containsKey(defect))
				d4jdefect_buggyfiles.put(defect, BugReportCrawler.getFixedFilesForDefect(project, defectid));
		}
	}
	
	public static ArrayList<String> getSuspiciousFilesAndScoresOfDefect(String path) {
		ArrayList<String> file_score = new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {
				String filename = line.split(" ")[2].trim() + "::::" + line.split(" ")[4].trim();
				file_score.add(filename);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file_score;
	}
	
	public static void getSuspiciousFileOfDefects(){

		File defectfile = new File(ConfigurationParameters.file_level_results_path);
		boolean exists = defectfile.exists();
		if (exists) {
			d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
		}

		ArrayList<String> srcFilesPaths = new ArrayList<String>();
		FileProcessor.listFilesInPath(ConfigurationParameters.sourceResultPath + "/top50", srcFilesPaths);

		for (String filepath: srcFilesPaths){
			String defect = filepath.split("/")[filepath.split("/").length-1].trim();
			ArrayList<String> suspiciousDefectFiles = new ArrayList<String>();
			suspiciousDefectFiles = getSuspiciousFilesAndScoresOfDefect(filepath);
			if (!d4jdefect_suspiciousfiles.containsKey(defect))
				d4jdefect_suspiciousfiles.put(defect, suspiciousDefectFiles);
		}
	}

	/*
	 *  for each of the (at most 50) Java files, extract Java statements and create docs corresponding the Java statements
	 */
	public static void extractJavaStatementsAndCreateDocs(String defect, int topN) throws Exception{
		
		logger.info("Processing defect " + defect);
		String result_Path = ConfigurationParameters.statementResultPath + "/top" + topN + "/" + defect ;
		logger.info(result_Path);
		File result_file = new File(result_Path);
		if (result_file.exists()){
			logger.info("Statement results already exist. Please delete existing directory to re-compute results.");
			return;
		}
		
		File result_dir = new File(ConfigurationParameters.statementDocumentsDirectory);
		if (!result_dir.exists()) {
			result_dir.mkdir();
		}
		
		getBuggyFilesOfDefects();
		getSuspiciousFileOfDefects();

		logger.info("processing defect: " + defect);
		logger.info(d4jdefect_suspiciousfiles.keySet().toString());
		logger.info("total number of suspicious files considered: " + d4jdefect_suspiciousfiles.get(defect).size());
		String stmt_dir = ConfigurationParameters.statementDocumentsDirectory + "/" + defect;
		File theDir = new File(stmt_dir);
		logger.info("storing docs in " + stmt_dir);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.getName());
			try{
				theDir.mkdir();
			} 
			catch(SecurityException se){
				logger.error("cannot create directory: " + theDir.getName());
			}        
		}else{
			return;
		}
		String project = defect.split("_")[0];
		String defectid = defect.split("_")[1];
		checkoutDefects4jDefect(project, defectid);
		String command5 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j export -p dir.src.classes "
				+ "-w " + ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy";
		logger.info("running cmd: " + command5);
		String[] output = ExecuteCommandLine.executeCommandAndGetOutput(command5).split("\n"); 
		String source_folder_name = output[output.length-1].trim();
		String source_folder_path = ConfigurationParameters.bugCheckoutDirectory + "/" + project + defectid + "buggy/" + source_folder_name;
		logger.info("source folder name: " + source_folder_name);
		logger.info("source folder path: " + source_folder_path);

		ArrayList<JavaStatement> javastatements = null;
		for(String suspfile: d4jdefect_suspiciousfiles.get(defect)){
			suspfile = suspfile.split("::::")[0].replace(".", "/");
			suspfile = suspfile.substring(0, suspfile.lastIndexOf('/')) + "." + "java";
			logger.info(suspfile);
			String filename = source_folder_path + "/" + suspfile;
			logger.info("Processing susps file: " + filename);
			javastatements = StatementLevelFL.parseJavaFile(filename);
			logger.info("#statements extracted: " + javastatements.size());
			if (javastatements != null){
				StatementLevelFL.createDocsForStatements(defect, filename, javastatements, 
						ConfigurationParameters.statementDocumentsDirectory, project, defect);
			}
			javastatements.clear();
		}

		deleteDefects4jDefect(project, defectid);
	}

	/*
	 * compute statement level FL by getting top-N statements from top-50 files, write the results to a file
	 */
	public static void getTopKRelevantJavaStatements(String defect, int topN) throws Exception{
		
		logger.info("Processing defect " + defect);
		String result_Path = ConfigurationParameters.statementResultPath + "/top" + topN + "/" + defect ;
		File result_file = new File(result_Path);
		if (result_file.exists()){
			logger.info("Statement results already exist. Please delete existing directory to re-compute results.");
			return;
	
		}
		
		File result_dir = new File(ConfigurationParameters.statementResultPath);
		if (!result_dir.exists()) {
			result_dir.mkdir();
		}
		
		// create query from xml file
		logger.info("CREATE QUERY");
		String queryPath = ConfigurationParameters.ProcessedQueryPath + "/" + defect;
		File queryfile = new File(queryPath);
		if (!queryfile.exists()){
			IndriRunner.createQuery(ConfigurationParameters.XMLQueryPath, ConfigurationParameters.ProcessedQueryPath, defect);
		}
		logger.info("CREATE INDEX");
		// index document collection
		String index_root_dir = ConfigurationParameters.statementDocumentsIndex ;
		File indexRootDir = new File(index_root_dir);
		logger.info(index_root_dir);
		// if the directory does not exist, create it
		if (!indexRootDir.exists()) {
			logger.info("creating index root directory: " + indexRootDir.getName());
			try{
				indexRootDir.mkdir();
			} 
			catch(SecurityException se){
				logger.error("cannot create directory: " + indexRootDir.getName());
			}        
		}

		String index_dir = ConfigurationParameters.statementDocumentsIndex + "/" + defect;
		File indexDir = new File(index_dir);
		logger.info(index_dir);
		// if the directory does not exist, create it
		if (!indexDir.exists()) {
			logger.info("creating index directory: " + indexDir.getName());
			try{
				indexDir.mkdir();
				IndriRunner.indexDocs(ConfigurationParameters.statementDocumentsDirectory + "/" + defect, index_dir);
			} 
			catch(SecurityException se){
				logger.error("cannot create directory: " + indexRootDir.getName());
			}        
		}

		logger.info("RETRIEVE RESULTS");

		String result_root_dir = ConfigurationParameters.statementResultPath;
		File resultRootDir = new File(result_root_dir);
		logger.info(result_root_dir);
		// if the directory does not exist, create it
		if (!resultRootDir.exists()) {
			logger.info("creating result root directory: " + resultRootDir.getName());
			try{
				resultRootDir.mkdir();
			} 
			catch(SecurityException se){
				logger.error("Cannot create directory: " + resultRootDir.getName());
			}        
		}
		
		String stmt_result_dir = ConfigurationParameters.statementResultPath + "/top" + topN;
		File resultDir = new File(stmt_result_dir);
		logger.info(stmt_result_dir);
		// if the directory does not exist, create it
		if (!resultDir.exists()) {
			logger.info("creating result directory: " + resultRootDir.getName());
			try{
				resultDir.mkdir();
			} 
			catch(SecurityException se){
				logger.error("Cannot create directory: " + resultRootDir.getName());
			}        
		}
		
		// get top-N relevant Java statements for each bug report (query)
		String resultPath = stmt_result_dir + "/" + defect ;
		logger.info(resultPath);
		File resultfile = new File(resultPath);
		if (!resultfile.exists()){
			IndriRunner.retrieveDocs(queryPath, resultPath, index_dir, defect, topN, 1.0, 0.3);
		}
	}
	
	public static void setParametersFromSettingsFile() throws IOException{
		File settings_file = new File("blues.settings");
		BufferedReader br = new BufferedReader(new FileReader(settings_file));
		String line;
		while ((line = br.readLine()) != null){
		    if (line.contains("root"))
		    	ConfigurationParameters.rootDirectory = line.split("=")[1].trim();
		}
		br.close();
		logger.info("Setting ROOT DIR as: " + ConfigurationParameters.rootDirectory);
		ConfigurationParameters.setParameters(ConfigurationParameters.rootDirectory);
		paths = ConfigurationParameters.CommitDBPaths.split(",");
	}
	
	public static void runBluesStandAlone(String defect, String src_path, String bugreport_url) throws Exception{
		String project = defect.split("_")[0].trim();
		String defectid = defect.split("_")[1].trim();
		
		// process bug report and create query
		BugReportCrawler brc = new BugReportCrawler();
		logger.info("extracting details from bug report of a defect");
		brc.getBugReportDetails(project, defectid, bugreport_url);
		logger.info("creating XML query file for the defect");
		brc.writeToXML(ConfigurationParameters.XMLQueryPath, defect);
		
		// process source files and create docs
		String doc_dir = ConfigurationParameters.sourceDocumentsDirectory;
		File theDir = new File(doc_dir);
		logger.info("storing file docs at " + doc_dir);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.getName());
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				logger.error("cannot create directory at " + theDir.getName());
			}
			if (result) {
				logger.info("created directory");
			}
		} 
			
		doc_dir = ConfigurationParameters.sourceDocumentsDirectory + "/" + defect;
		File sourcepath = new File(src_path);
		if (!sourcepath.exists()){
			logger.error("source path DNE!: " + src_path);
			return;
		}
		if (!sourcepath.isDirectory()){
			logger.error("source folder DNE!: " + src_path);
			return;
		}
		logger.info("calling Indri to create docs");
		IndriRunner.createDocs(src_path, doc_dir, project, defectid);
		
		logger.info("Compute Top-50 suspicious Java source files based on bug report"); 
		Blues.getTopKRelevantJavaFiles(defect, ConfigurationParameters.k);
		
		// create docs using 57 tyoes of Java AST statements from top-50 ranked files
		
		String result_Path = ConfigurationParameters.statementResultPath + "/top10000/" + defect ;
		logger.info(result_Path);
		File result_file = new File(result_Path);
		if (result_file.exists()){
			logger.info("Statement results already exist. Please delete existing directory to re-compute results.");
		}
		
		File result_dir = new File(ConfigurationParameters.statementDocumentsDirectory);
		if (!result_dir.exists()) {
			result_dir.mkdir();
		}
		getSuspiciousFileOfDefects();
	
		logger.info(d4jdefect_suspiciousfiles.keySet().toString());
		logger.info("total number of suspicious files considered: " + d4jdefect_suspiciousfiles.get(defect).size());
		String stmt_dir = ConfigurationParameters.statementDocumentsDirectory + "/" + defect;
		File dir = new File(stmt_dir);
		logger.info("storing docs in " + stmt_dir);
		// if the directory does not exist, create it
		if (!dir.exists()) {
			logger.info("creating directory: " + dir.getName());
			try{
				dir.mkdir();
			} 
			catch(SecurityException se){
				logger.error("cannot create directory: " + dir.getName());
			}        
		}
		
		ArrayList<JavaStatement> javastatements = null;
		for(String suspfile: d4jdefect_suspiciousfiles.get(defect)){
			suspfile = suspfile.split("::::")[0].replace(".", "/");
			suspfile = suspfile.substring(0, suspfile.lastIndexOf('/')) + "." + "java";
			logger.info(suspfile);
			String filename = src_path + "/" + suspfile;
			logger.info("Processing susps file: " + filename);
			javastatements = StatementLevelFL.parseJavaFile(filename);
			logger.info("#statements extracted: " + javastatements.size());
			if (javastatements != null){
				StatementLevelFL.createDocsForStatements(defect, filename, javastatements, 
						ConfigurationParameters.statementDocumentsDirectory, project, defect);
			}
			javastatements.clear();
		}
		
		logger.info("Rank Top-10000 (all) suspicious Java statements of the top-50 suspicious Java files"); 
	    Blues.getTopKRelevantJavaStatements(defect, ConfigurationParameters.All);
	    
	    logger.info("Re-ranking statement results by combining file and stmt scores using ScoreFn and m values");
		RerankStatements.rerankStatementsConsideringFileScore(defect, ConfigurationParameters.m, ConfigurationParameters.scoringStrategy);
	
		    logger.info("Dump BLUiR statement results without re-ranking");
	    RerankStatements.dumpBLUiRStatementResultsWithoutReranking(defect);
					
		logger.info("cleaning up intermediate results");
		deleteIndriDocs(defect);
	}
	
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();
		setParametersFromSettingsFile();
		
		
		if (args.length > 1) {					// to localize any arbitrary defect with bug report
			
			String defect = args[0].trim();         // Chart_98
			String src_path = args[1];				// /home/manish/BluesReleased/jfreechart/src/main/java
			String bugreport_url = args[2];			// https://github.com/jfree/jfreechart/issues/98
			runBluesStandAlone(defect, src_path, bugreport_url);
			
		}else { 								// To localize Defects4J defects
		
		String defectinfo = args[0];

		if (defectinfo.contentEquals("all")) {
		
			logger.info("Fetching bug reports and creating XML queries"); 
			Blues.fetchBugReportsAndCreateQuery(defectinfo);
			
			File defectfile = new File("d4j-defects.txt");
			boolean exists = defectfile.exists();
			if (exists) {
				d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
			}
			logger.info("total #defects:" + d4jdefects.size());
	
			for (String defect : d4jdefects) {
				
				logger.info("Processing Defect: " + defect);
				
				logger.info("Extracting Java source files and creating docs"); 
				Blues.extractJavaFilesAndCreateDocs(defect);
				
				logger.info("Computing Top-50 suspicious Java source files based on bug report"); 
				Blues.getTopKRelevantJavaFiles(defect, ConfigurationParameters.k);
				
				logger.info("Extracting Java source statements from the suspicious Java source files and creating docs."); 
				Blues.extractJavaStatementsAndCreateDocs(defect, ConfigurationParameters.All);
	
				logger.info("Rank Top-10000 (all) suspicious Java statements of the top-50 suspicious Java files"); 
			    Blues.getTopKRelevantJavaStatements(defect, ConfigurationParameters.All);
				
				logger.info("Re-ranking statement results by considering file scores based on set m values");
				RerankStatements.rerankStatementsConsideringFileScore(defect, ConfigurationParameters.m, ConfigurationParameters.scoringStrategy);
			
				logger.info("Dump BLUiR statement results without re-ranking");
				RerankStatements.dumpBLUiRStatementResultsWithoutReranking(defect);
							
				logger.info("cleaning up intermediate results");
				deleteIndriDocs(defect);
			}
		}else{
			
			logger.info("Processing Defect: " + defectinfo);
			
			logger.info("Fetching bug report and creating XML query"); 
			Blues.fetchBugReportsAndCreateQuery(defectinfo);
			
			logger.info("Extracting Java source files and creating docs"); 
			Blues.extractJavaFilesAndCreateDocs(defectinfo);
			
			logger.info("Computing Top-50 suspicious Java source files based on bug report"); 
			Blues.getTopKRelevantJavaFiles(defectinfo, ConfigurationParameters.k);
			
			logger.info("Extracting Java source statements from the suspicious Java source files and creating docs."); 
			Blues.extractJavaStatementsAndCreateDocs(defectinfo, ConfigurationParameters.All);

			logger.info("Rank Top-10000 (all) suspicious Java statements of the top-50 suspicious Java files"); 
		    Blues.getTopKRelevantJavaStatements(defectinfo, ConfigurationParameters.All);
			
			logger.info("Re-ranking statement results by combining file and stmt scores using ScoreFn and m values");
			RerankStatements.rerankStatementsConsideringFileScore(defectinfo, ConfigurationParameters.m, ConfigurationParameters.scoringStrategy);
		
   		    logger.info("Dump BLUiR statement results without re-ranking");
		    RerankStatements.dumpBLUiRStatementResultsWithoutReranking(defectinfo);
						
			logger.info("cleaning up intermediate results");
			deleteIndriDocs(defectinfo);
			
		}
	 }
	}
}

