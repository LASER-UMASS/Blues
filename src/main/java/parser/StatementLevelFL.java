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

package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import configuration.ConfigurationParameters;

public class StatementLevelFL {
	
	protected static Logger logger = Logger.getLogger(StatementLevelFL.class);

    public static ArrayList<JavaStatement> parseJavaFile(String filepath) throws IOException{
  
 		ASTParser parser = ASTParser.newParser(AST.JLS10);
		String contents = new String(Files.readAllBytes(Paths.get(filepath)));
		parser.setSource(contents.toCharArray());		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<JavaStatement> javastatements = MethodVisitor.getStatements(cu, filepath.split("/")[filepath.split("/").length-1].trim());
		return javastatements;
    }
    
	public static void createDocsForStatements(String defect, String javafilename, ArrayList<JavaStatement> javastatements, String destinationDirectory, 
			String project, String defectid) throws Exception{

		String file_stmt_dir =  ConfigurationParameters.statementDocumentsDirectory + "/" + defect; //+ "/" + javafilename.split("buggy/")[1].replace("/", ".");
		File theDir = new File(file_stmt_dir);
		logger.info(file_stmt_dir);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.getName());
			try{
				theDir.mkdir();
			} 
			catch(SecurityException se){
				//handle it
			}        
		}
		
		int index = 1;
		for(JavaStatement stmt : javastatements){
			//StmtCount++;
    	    String key = "";
    	    String fileName = "stmtdoc-" + stmt.filename + "-" + stmt.linenumber + "-" + index;
    	   
    		File file = new File(file_stmt_dir + "/" + fileName);
    		//logger.info(stmt.linenumber);
    		// if file doesnt exists, then create it
    		if (!file.exists()) {
    			file.createNewFile();
    		}

    		FileWriter fw = new FileWriter(file.getAbsoluteFile());
    		BufferedWriter bw = new BufferedWriter(fw);	
	
    		key = stmt.filename + "-" + stmt.linenumber + "-" + index;
    	    bw.write("<DOC>\n<DOCNO>"+key+" </DOCNO>\n<text>");
        	bw.newLine();
        	
        	String[] idNames = PreProcessor.process(stmt.content).split(" ");
        	
        	bw.write("<identifier>");   
        	bw.newLine();
        	for(String idName:idNames){
            	bw.write(PreProcessor.transform(idName));
            	bw.newLine();
        	}       	
        	bw.write("</identifier>");
        	bw.newLine();
        	
        	String[] list = PreProcessor.process(stmt.comment.toString()).split(" ");
        	bw.write("<comments>");   
        	bw.newLine();
        	for(int i=0;i<list.length;i++){        		
    	    	bw.write(list[i]);
    	    	bw.newLine();
    	    }
           
        	bw.write("</comments>");   
        	bw.newLine();       	
        	bw.write("</text>\n</DOC>");
        	bw.newLine();
    	    bw.close();
    	    index++;
	    }
 	
	    logger.info("End");
	    
	}
}


