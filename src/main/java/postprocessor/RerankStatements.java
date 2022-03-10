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

package postprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import configuration.ConfigurationParameters;
import main.Blues;
import utilities.FileProcessor;
import utilities.Serialize;

public class RerankStatements {

	static ArrayList<String> d4jdefects = new ArrayList<String>();
	static HashMap<String,ArrayList<String>> file_results = new HashMap<String,ArrayList<String>>();  // to store suspicious files for a defect
	static HashMap<String,ArrayList<String>> stmt_results = new HashMap<String,ArrayList<String>>();  // to store suspicious stmts for a defect
	protected static Logger logger = Logger.getLogger(RerankStatements.class);


	public static ArrayList<String> getSuspiciousFilesAndScoresOfDefect(String path) {
		ArrayList<String> file_score = new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {
				logger.info(line);
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

	public static ArrayList<String> getSuspiciousStmtsAndScoresOfDefect(String path) {
		ArrayList<String> stmt_score = new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {
				//	logger.info(line);
				String stmtAndScore = line.split(" ")[2].trim() + "::::" + line.split(" ")[4].trim();
				stmt_score.add(stmtAndScore);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stmt_score;
	}

	public static void getFileResults(){

		File defectfile = new File(ConfigurationParameters.file_level_results_path);
		boolean exists = defectfile.exists();
		if (exists) {
			d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
			file_results = Serialize.deserializeHashMap(ConfigurationParameters.file_level_results_path);
			logger.info("using stored src results " + file_results.size());
			return;
		}

		ArrayList<String> srcFilesPaths = new ArrayList<String>();
		FileProcessor.listFilesInPath(ConfigurationParameters.sourceResultPath + "/top50", srcFilesPaths);

		for (String filepath: srcFilesPaths){
			logger.info("process: " + filepath);
			String defect = filepath.split("/")[filepath.split("/").length-1].trim();
			ArrayList<String> suspiciousDefectFiles = new ArrayList<String>();
			suspiciousDefectFiles = getSuspiciousFilesAndScoresOfDefect(filepath);
			if (!file_results.containsKey(defect))
				file_results.put(defect, suspiciousDefectFiles);
			else
				logger.info("added file results for " + defect);
		}
		logger.info(srcFilesPaths.size());
		Serialize.serializeHashMap(file_results, ConfigurationParameters.file_level_results_path);
	}

	public static void getStatementResults(){
		File defectfile = new File(ConfigurationParameters.expr_level_results_path);
		boolean exists = defectfile.exists();
		if (exists) {
			d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
			stmt_results = Serialize.deserializeHashMap(ConfigurationParameters.expr_level_results_path);
			return;
		}
		ArrayList<String> stmtResultFilesPaths = new ArrayList<String>();
		FileProcessor.listFilesInPath(ConfigurationParameters.statementResultPath + "/top10000/" , stmtResultFilesPaths);

		for (String filepath: stmtResultFilesPaths){
			logger.info("process: " + filepath);
			String defect = filepath.split("/")[filepath.split("/").length-1].trim();
			ArrayList<String> suspiciousDefectStmts = new ArrayList<String>();
			suspiciousDefectStmts = getSuspiciousStmtsAndScoresOfDefect(filepath);
			
			if (!stmt_results.containsKey(defect))
				stmt_results.put(defect, suspiciousDefectStmts);
			else
				logger.info("added stmt results for " + defect);

		}
		logger.info(stmtResultFilesPaths.size());
		Serialize.serializeHashMap(stmt_results,ConfigurationParameters.expr_level_results_path);
	}

	public static void rerankStatementsConsideringFileScore(String defect, int[] Sm_list, String[] ScoreFns) throws IOException{

		File result_dir = new File(ConfigurationParameters.rootDirectory + "/" + ConfigurationParameters.resultsDirectory);
		if (!result_dir.exists()) {
			result_dir.mkdir();
		}
		
		if (!file_results.containsKey(defect)){
			getFileResults();
		}
		if (!stmt_results.containsKey(defect)){
			getStatementResults();
		}
		ArrayList<String> statements = new ArrayList<String>();
		logger.info("re-ranking statements for defect: " + defect);
		ArrayList<String> stmt_score = stmt_results.get(defect);
		ArrayList<String> file_score = file_results.get(defect);
		logger.info("#statements: " + stmt_score.size());
		logger.info("#files: " + file_score.size());

		for (String fn: ScoreFns){
			for (int S_m: Sm_list){
				if (fn.equals("wted") && S_m < ConfigurationParameters.All){
					continue;
				}
				
				String rootDirPath = "";
				if (fn.equals("high") && S_m < ConfigurationParameters.All){
					rootDirPath = ConfigurationParameters.RerankedSuspiciousStmtsPath  +  S_m  + "_stmts";
				}
				else if (fn.equals("high") && S_m == ConfigurationParameters.All){
					rootDirPath = ConfigurationParameters.RerankedSuspiciousStmtsPath  +  "All_stmts";
				}
				else if (fn.equals("wted") && S_m == ConfigurationParameters.All){
					rootDirPath = ConfigurationParameters.RerankedSuspiciousStmtsPath  +  "Wted_stmts";
				}
				
				File suspFileDir = new File(rootDirPath + "/" + defect.split("_")[0].toLowerCase() + "/" + defect.split("_")[1]);
				if (suspFileDir.exists()) {
					logger.info("Already re-ranked statements with m = " + S_m + " and scoring function = " + fn + " at " + suspFileDir.toString());
					continue;
				}
				
				logger.info("Reranking statements with m = " + S_m + " and scoring function = " + fn);
				ArrayList<String> statementsWithScores = new ArrayList<String>();
				if (statementsWithScores.size()==0){
					statementsWithScores.add("Statement,Suspiciousness"); 
				}

				//Double maxscore = Double.valueOf(file_score.size() * S_m);				// better scoring scheme
				Double maxscore = Double.valueOf(file_score.size() * stmt_score.size());   // to replicate results
				logger.info("CHECK SIZES: FS=" + file_score.size() + " SC=" + stmt_score.size());
				
				Double indexscore = maxscore;
				int stmt_per_file = 0;
				for(String fs: file_score){
					stmt_per_file = 0;
					for(String ss: stmt_score){
						if (fs.split("::::")[0].endsWith(ss.split("::::")[0].split("-")[0].trim())){
							String classname = fs.split("::::")[0].substring(0, fs.split("::::")[0].length()-5).trim();
							String linenumber = ss.split("::::")[0].split("-")[1].trim();
							String filescore = fs.split("::::")[1].trim();
							String stmtscore = ss.split("::::")[1].trim();
							Double score = 0.0;
							if (fn.equals("high")){
								score = indexscore/maxscore; 
							}else if (fn.equals("wted")){
							  // score = Double.parseDouble(filescore) + Double.parseDouble(stmtscore);  // my old method
								 score = Double.parseDouble(filescore) * Double.parseDouble(stmtscore);  // iFixR method
							}
							String statement = classname + "#" + linenumber;
							if(!statements.contains(statement)){
								statements.add(statement);
								if (stmt_per_file < S_m){
									statementsWithScores.add(statement + "," + score); 
									stmt_per_file++;
									
								}
								indexscore--;
							}	
							
						}
					//	if (stmt_per_file == S_m) break;
						
					}
				}

				File rootSuspFileDir = new File(rootDirPath);
				if (!rootSuspFileDir.exists()) {
					rootSuspFileDir.mkdir();
				}
				File subrootSuspFileDir = new File(rootDirPath + "/" + defect.split("_")[0].toLowerCase());
				if (!subrootSuspFileDir.exists()) {
					subrootSuspFileDir.mkdir();
				}

				if (!suspFileDir.exists()) {
					suspFileDir.mkdir();
					Path file = Paths.get(suspFileDir + "/stmt-susps.txt");
					Files.write(file, statementsWithScores, StandardCharsets.UTF_8);
				}
				statements.clear();
				statementsWithScores.clear();
			}
		}
	}


	
	public static void dumpBLUiRStatementResultsWithoutReranking(String defect) throws IOException{
		
		if (file_results.size() == 0){
			getFileResults();
		}
		if (stmt_results.size() == 0){
			getStatementResults();
		}
		ArrayList<String> statements = new ArrayList<String>();
		logger.info("dumping statement results without reranking for defect: " + defect);
		ArrayList<String> stmt_score = stmt_results.get(defect);
		ArrayList<String> file_score = file_results.get(defect);
		logger.info("#files: " + file_score.size());
		logger.info("#statements: " + stmt_score.size());
		String rootDirPath = ConfigurationParameters.rootDirectory + "/" + ConfigurationParameters.resultsDirectory + "/BLUiR_stmts"; 
		File suspFileDir = new File(rootDirPath + "/" + defect.split("_")[0].toLowerCase() + "/" + defect.split("_")[1]);
		if (suspFileDir.exists()) {
			logger.info("Already dumped results for defect " + defect);
			return;
		}
		
		ArrayList<String> statementsWithScores = new ArrayList<String>();
		if (statementsWithScores.size()==0){
			statementsWithScores.add("Statement,Suspiciousness"); 
		}
		
		for(String ss: stmt_score){
			for(String fs: file_score){
				if (fs.split("::::")[0].endsWith(ss.split("::::")[0].split("-")[0].trim())){
					String classname = fs.split("::::")[0].substring(0, fs.split("::::")[0].length()-5).trim();
					String linenumber = ss.split("::::")[0].split("-")[1].trim();
					String stmtscore = ss.split("::::")[1].trim();
					String statement = classname + "#" + linenumber;
					if(!statements.contains(statement)){
						//logger.info(classname + "#" + linenumber + "," + stmtscore);
						statements.add(statement);
						statementsWithScores.add(statement + "," + stmtscore); 
					}
				}
			}
		}
			
		File rootSuspFileDir = new File(rootDirPath);
		if (!rootSuspFileDir.exists()) {
			rootSuspFileDir.mkdir();
		}
		File subrootSuspFileDir = new File(rootDirPath + "/" + defect.split("_")[0].toLowerCase());
		if (!subrootSuspFileDir.exists()) {
			subrootSuspFileDir.mkdir();
		}

		if (!suspFileDir.exists()) {
			suspFileDir.mkdir();
			Path file = Paths.get(suspFileDir + "/stmt-susps.txt");
			Files.write(file, statementsWithScores, StandardCharsets.UTF_8);
		}
		statements.clear();
		statementsWithScores.clear();
		
	}
	
	
//	public static void main(String args[]) throws IOException{
//		
//		BasicConfigurator.configure();
//		Blues.setParametersFromSettingsFile();
//		
//		File defectfile = new File("d4j-defects.txt");
//		boolean exists = defectfile.exists();
//		if (exists) {
//			d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
//		}
//		logger.info("total #defects:" + d4jdefects.size());
//
//		for (String defect : d4jdefects) {
//			logger.info(defect);
//			rerankStatementsConsideringFileScore(defect, ConfigurationParameters.m, ConfigurationParameters.scoringStrategy);
//			logger.info("Applying heuristic to add mising intermittent statements to the re-ranked results");
//			ApplyHeuristic.addMissingStatements(defect, ConfigurationParameters.m, ConfigurationParameters.scoringStrategy);
//		}
//	}

}
