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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import configuration.ConfigurationParameters;
import utilities.FileProcessor;
import utilities.Serialize;

public class ApplyHeuristic {

	protected static Logger logger = Logger.getLogger(ApplyHeuristic.class);
	static HashMap<String,ArrayList<String>> defect_stmts = new HashMap<String,ArrayList<String>>();  // to store the mapping of defect and expressions
	static ArrayList<String> d4jdefects = new ArrayList<String>();
	
	public static ArrayList<String> getAllStatementsOfDefect(String defect){
		
		File defectexprfile = new File(ConfigurationParameters.defect_expressions_path + "/" + defect + "_expr.ser");
		boolean exists = defectexprfile.exists();
		if (exists) {
			defect_stmts = Serialize.deserializeHashMap(ConfigurationParameters.defect_expressions_path + "/" + defect + "_expr.ser");
			logger.info("using stored mapping of defect and expressions " + defect_stmts.size());
		}
		else{
			d4jdefects = Serialize.deserializeArrayList("d4j-defects.txt");
			for (String def : d4jdefects) {
				logger.info("fetching expr of defect: " + def);
				ArrayList<String> stmtsPaths = new ArrayList<String>();
				String exprpath = ConfigurationParameters.statementDocumentsDirectory + def + "/expression_docs/";
				logger.info(exprpath);
				FileProcessor.listFilesInPath(exprpath, stmtsPaths);
				if (!defect_stmts.containsKey(def))
					defect_stmts.put(def, stmtsPaths);
				logger.info(defect_stmts.get(def).size());
				Serialize.serializeHashMap(defect_stmts, ConfigurationParameters.defect_expressions_path + "/" + def + "_expr.ser");
				defect_stmts.clear();
			}
		}
		return defect_stmts.get(defect);
	}
	
	
	
	public static ArrayList<String> getRerankedFLResults(String path){

		ArrayList<String> stmt_results = new ArrayList<String> ();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {
				if (line.contains("#")){
					String classAndLine = line.split(",")[0].trim();
					double score = Double.parseDouble(line.split(",")[1].trim());
					if (score > 0.0)
						stmt_results.add(classAndLine + "," + score);
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stmt_results;
	}

	// method-1 (add missing statements regardless of suspiciousness scores)
	private static boolean statementExists(String defect, String classname, int lineno){

		String path = ConfigurationParameters.statementDocumentsDirectory + defect + "/expression_docs/"; 
		String[] pathnames;
		File f = new File(path);
		if (!f.exists()) {
			return false;
		}
		pathnames = f.list();
		for (String pathname : pathnames) {
			String searchStr = classname.split("\\.")[classname.split("\\.").length-1] + ".java-" + lineno;
			if (pathname.contains(searchStr)){
				return true;
			}
		}
		return false;
	}
	
	// method-2 (add missing statements with non-zero scores only. method-1 leads to better results)
	private static boolean checkIfStatementExists(String defect, String classname, int lineno){
		
		String project = defect.split("_")[0].trim();
		String bugid = defect.split("_")[1].trim();
		String allRankedResultsPath = ConfigurationParameters.RerankedSuspiciousStmtsPath  +  "All_stmts" + "/" + project.toLowerCase() + "/" + bugid + "/stmt-susps.txt";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(allRankedResultsPath));
			String line = reader.readLine();
			while (line != null) {
				//	logger.info(line);
				String classAndLineNo = line.split(",")[0].trim();
				if (classAndLineNo.equalsIgnoreCase(classname+"#"+lineno)){
					reader.close();
					return true;
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	

	// for each defect retrieve statements in statements results file
	// for each class c and line number l:
	//		if l-1 exists and l+2 exists and l-1 does not exists then
	//			add c#l+1 with score = average(score(l) and score(l+2)) 
	public static void addMissingStatements(String defect, int[] Sm_list, String[] ScoreFns) throws IOException{

		getAllStatementsOfDefect(defect);
		
		for (String fn: ScoreFns){
			for (int S_m: Sm_list){
				if (fn.equals("wted") && S_m < ConfigurationParameters.All){
					continue;
				}
				logger.info(defect + " " + fn + " " + S_m);
				String project = defect.split("_")[0].trim().toLowerCase();
				String bugid = defect.split("_")[1].trim();
				Boolean heuristicApplied = false;
	
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
				
				File suspFile = new File(rootDirPath + "/" + project + "/" + bugid + "/stmt-susps-heuristic.txt");
				if (suspFile.exists()) {
					logger.info("Already applied heuristics to defect with m = " + S_m + " and scoring function = " + fn);
					continue;
				}
				
				String flPath = rootDirPath + "/" + project + "/" + bugid + "/stmt-susps.txt"; 
				ArrayList<String> rankedStatements = getRerankedFLResults(flPath);
				ArrayList<String> statements = new ArrayList<String>();
				ArrayList<String> updatedresults = new ArrayList<String>();
				HashMap<String, Double> stmt_score = new HashMap<String, Double>();

				// add all ranked stmts to statements
				for(String stmt: rankedStatements){
					String classname = stmt.split(",")[0].split("#")[0];
					int lineNo = Integer.parseInt(stmt.split(",")[0].split("#")[1].trim());
					Double score_s = Double.parseDouble(stmt.split(",")[1].trim());
					String statement = classname + "#" + lineNo;
					statements.add(statement);
					stmt_score.put(statement, score_s);
				}

				updatedresults.add("Statement,Suspiciousness");
				for(String stmt: rankedStatements){
					String classname = stmt.split(",")[0].split("#")[0];
					int lineNo = Integer.parseInt(stmt.split(",")[0].split("#")[1].trim());	
					Double score_s = Double.parseDouble(stmt.split(",")[1].trim());
					updatedresults.add(stmt);
					int lMinusOne = lineNo - 1;
					int lPlusOne = lineNo + 1;
					int lPlusTwo = lineNo + 2;

					String sMinusOne = classname + "#" + lMinusOne;
					String sPlusOne = classname + "#" + lPlusOne;
					String sPlusTwo = classname + "#" + lPlusTwo;

					if (statements.contains(sMinusOne) && statements.contains(sPlusTwo) && !statements.contains(sPlusOne)){ // check if heuristic is applicable
						if (statementExists(defect, classname, lPlusOne)){
							Double score_sPlusOne = (stmt_score.get(sPlusTwo) + score_s)/2.0;
							updatedresults.add(sPlusOne + "," + score_sPlusOne);
							heuristicApplied = true;
						}
					}
				}
				logger.info(heuristicApplied);
				if (heuristicApplied == true){
					String file = rootDirPath + "/" + project + "/" + bugid + "/stmt-susps-heuristic.txt";
					FileWriter writer = new FileWriter(file); 
					logger.info("creating file " + file);
					for(String str: updatedresults) {
						writer.write(str + System.lineSeparator());
					}
					writer.close();
					logger.info("Updated FL file for " + defect + " under " + rootDirPath + "/" + project + "/" + bugid);
				}

			}
		}
	}
}
