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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import parser.NodeVisitor;
import main.Blues;
import parser.PreProcessor;
import parser.GetSourceCodeFiles;

public class ParseSourceCode {
	
	private static Document fDocument;
	private static final int BUFFER_LENGTH = 1024;
	protected static Logger logger = Logger.getLogger(ParseSourceCode.class);
    
    public static String readFile(String filePath) throws CoreException, FileNotFoundException {
        char[] b = new char[BUFFER_LENGTH];
        InputStreamReader isr = null;
        isr = new InputStreamReader(new FileInputStream(filePath));
        StringBuffer sb = new StringBuffer();
        int n;
        try {
            while ((n = isr.read(b)) > 0) {
                sb.append(b, 0, n);
            }
            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
	
	public static void extractEclipseFacts(String codeDirectory, String destinationDirectory, String project, String defectid) throws Exception{
		GetSourceCodeFiles fs = new GetSourceCodeFiles();
		
		List<String> files = fs.getAllJavaFiles(codeDirectory, codeDirectory.length());
		Iterator <String> it = files.iterator();
		
		File dir = new File(destinationDirectory);
		if (!dir.exists()) {
			logger.info("creating folder to store docs at " + destinationDirectory);
			dir.mkdir();
		}
			
		int count = 1;
		int fileCount = 0;
		logger.info("Processing " + project + defectid);
		logger.info("Total files: " + files.size());
		while(it.hasNext()){
			fileCount++;
			String filePath = it.next();	
			logger.info(fileCount+"\t"+filePath);
			fDocument = new Document(readFile(filePath));
    	    String key = "";
    	    String fileName = "doc-" + count;
    		File file = new File(destinationDirectory+"/"+fileName);
    		logger.info("creating" + destinationDirectory+"/"+fileName);
    		// if file doesnt exists, then create it
    		if (!file.exists()) {
    			file.createNewFile();
    		}

    		FileWriter fw = new FileWriter(file.getAbsoluteFile());
    		BufferedWriter bw = new BufferedWriter(fw);
    		String fqn = filePath.substring(codeDirectory.length());
			fqn = fqn.substring(1).replaceAll("/", ".");

    		key = fqn;
    	    bw.write("<DOC>\n<DOCNO>"+key+" </DOCNO>\n<text>");
        	bw.newLine();
        
    		ASTParser parser = ASTParser.newParser(AST.JLS10);		
    		String source = GetSourceCodeFiles.readFile(filePath);
    		
    		parser.setSource(source.toCharArray());
    		parser.setKind(ASTParser.K_COMPILATION_UNIT);
    		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    		
    		NodeVisitor visitor = new NodeVisitor();
    		cu.accept(visitor);
    		
    		List<String> classes =  visitor.getClassNames();
    		
        	bw.write("<class>");        
        	bw.newLine();
        	for(String cls:classes){
            	bw.write(PreProcessor.transform(cls));
            	bw.newLine();
        	}       	
        	bw.write("</class>");
        	bw.newLine();
        	
        	List<String> methods = visitor.getMethodNames();
        	
        	bw.write("<method>");  
        	bw.newLine();
        	
        	if(methods.size() == 0){
	        	bw.write("NONE");  
	        	bw.newLine();
        	}
        	for(String methodName:methods){
            	bw.write(PreProcessor.transform(methodName));
            	bw.newLine();
        	}       	
        	bw.write("</method>");
        	bw.newLine();
        	
        	List<String> idNames = visitor.getIdentifierNames();
        	
        	bw.write("<identifier>");   
        	bw.newLine();
        	for(String idName:idNames){
            	bw.write(PreProcessor.transform(idName));
            	bw.newLine();
        	}       	
        	bw.write("</identifier>");
        	bw.newLine();
        	
        	List<ASTNode> list = new ArrayList<ASTNode>();
			for(Object element: cu.getCommentList())	{
        		list.add((ASTNode) element);
        	}
        	bw.write("<comments>");   
        	bw.newLine();
        	for(int i=0;i<list.size();i++){        		
    	    	bw.write(PreProcessor.process(getCommentString(list.get(i))));
    	    	bw.newLine();
    	    }
           
        	bw.write("</comments>");   
        	bw.newLine();       	
        	bw.write("</text>\n</DOC>");
        	bw.newLine();
    	    bw.close();
    	    count++;
	    }
 
	    Blues.deleteDefects4jDefect(project, defectid);
	}

	
	public static void extractFacts(String codeDirectory, String destinationDirectory) throws IOException, CoreException, org.eclipse.jface.text.BadLocationException{
		GetSourceCodeFiles fs = new GetSourceCodeFiles();
		
		int offset = codeDirectory.length();
		List<String> files = fs.getAllJavaFiles(codeDirectory, codeDirectory.length());
		Iterator <String> it = files.iterator();
		
		int count = 1;
		int fileCount = 0;
		logger.info("Processing");
		while(it.hasNext()){
			fileCount++;

			String filePath = it.next();
			logger.info(fileCount+"\t"+filePath);
	
			fDocument = new Document(readFile(filePath));
	
			String classFQN = filePath.substring(offset+1).replaceAll("/",".");
			//String part[]= classFQN.split("\\.");
			//String className = part[part.length-2];

    	    String key = "";
    	    
    	    String fileName = "doc/file"+count;
    	    
    		File file = new File(destinationDirectory+"/"+fileName);

    		// if file doesnt exists, then create it
    		if (!file.exists()) {
    			file.createNewFile();
    		}

    		FileWriter fw = new FileWriter(file.getAbsoluteFile());
    		BufferedWriter bw = new BufferedWriter(fw);
    		key = classFQN.substring(4); //for swt
    	    bw.write("<DOC>\n<DOCNO>"+key+" </DOCNO>\n<text>");
        	bw.newLine();
        

    		ASTParser parser = ASTParser.newParser(AST.JLS10);		
    		String source = GetSourceCodeFiles.readFile(filePath);
    		
    		parser.setSource(source.toCharArray());
    		parser.setKind(ASTParser.K_COMPILATION_UNIT);
    		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    		
    		NodeVisitor visitor = new NodeVisitor();
    		cu.accept(visitor);
    		
    		List<String> classes =  visitor.getClassNames();
    		
        	bw.write("<class>");        
        	bw.newLine();
        	for(String cls:classes){
            	bw.write(cls);
            	bw.newLine();
        	    
            	bw.write(PreProcessor.transform(cls));
            	bw.newLine();
        	}       	
        	bw.write("</class>");
        	bw.newLine();
        	
        	List<String> methods = visitor.getMethodNames();
        	
        	bw.write("<method>");  
        	bw.newLine();
        	
        	if(methods.size() == 0){
	        	bw.write("NONE");  
	        	bw.newLine();
        	}
        	for(String methodName:methods){
            	bw.write(methodName);
            	bw.newLine();
            	bw.write(PreProcessor.transform(methodName));
            	bw.newLine();
        	}       	
        	bw.write("</method>");
        	bw.newLine();
        	
        	List<String> idNames = visitor.getIdentifierNames();
        	
        	bw.write("<identifier>");   
        	bw.newLine();
        	for(String idName:idNames){
            	bw.write(idName);
            	bw.newLine();
            	bw.write(PreProcessor.transform(idName));
            	bw.newLine();
        	}       	
        	bw.write("</identifier>");
        	bw.newLine();
        	
        	List<ASTNode> list = new ArrayList<ASTNode>();
			for(Object element: cu.getCommentList())	{
        		list.add((ASTNode) element);
        	}
        	bw.write("<comments>");   
        	bw.newLine();
        	for(int i=0;i<list.size();i++){        		
    	    	bw.write(PreProcessor.process(getCommentString(list.get(i))));
    	    	bw.newLine();
    	    }
           
        	bw.write("</comments>");   
        	bw.newLine();       	
        	bw.write("</text>\n</DOC>");
        	bw.newLine();
    	    bw.close();
    	    count++;
	    }
 	
	    logger.info("End");
	}
	
	public static void extractFactsDefault(String codeDirectory, String destinationDirectory) throws IOException, CoreException, org.eclipse.jface.text.BadLocationException{
		GetSourceCodeFiles fs = new GetSourceCodeFiles();
		
		int offset = codeDirectory.length();
		List<String> files = fs.getAllJavaFiles(codeDirectory, codeDirectory.length());
		Iterator <String> it = files.iterator();
		
		int count = 1;
		int fileCount = 0;
		logger.info("Processing");
		while(it.hasNext()){
			fileCount++;

			String filePath = it.next();
			//logger.info(fileCount+"\t"+filePath);
	
			fDocument = new Document(readFile(filePath));
	
			String classFQN = filePath.substring(offset+1).replaceAll("/",".");
    	    String key = "";
    	    String fileName = "doc_default/file"+count;
    	 
    		File file = new File(destinationDirectory+"/"+fileName);

    		// if file doesnt exists, then create it
    		if (!file.exists()) {
    			file.createNewFile();
    		}

    		FileWriter fw = new FileWriter(file.getAbsoluteFile());
    		BufferedWriter bw = new BufferedWriter(fw);

    		key = classFQN;
    	    bw.write("<DOC>\n<DOCNO>"+key+" </DOCNO>\n<text>");
        	bw.newLine();

    		ASTParser parser = ASTParser.newParser(AST.JLS10);		
    		String source = GetSourceCodeFiles.readFile(filePath);
    		
    		parser.setSource(source.toCharArray());
    		parser.setKind(ASTParser.K_COMPILATION_UNIT);
    		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
    		
    		NodeVisitor visitor = new NodeVisitor();
    		cu.accept(visitor);
    		
    		List<String> classes =  visitor.getClassNames();
    		
        	bw.write("<class>");        
        	bw.newLine();
        	for(String cls:classes){
            	bw.write(PreProcessor.transform(cls).trim());
            	bw.newLine();
        	}       	
        	bw.write("</class>");
        	bw.newLine();
        	
        	List<String> methods = visitor.getMethodNames();
        	
        	bw.write("<method>");  
        	bw.newLine();
        	
        	if(methods.size() == 0){
	        	bw.write("NONE");  
	        	bw.newLine();
        	}
        	for(String methodName:methods){
            	bw.write(PreProcessor.transform(methodName));
            	bw.newLine();
        	}       	
        	bw.write("</method>");
        	bw.newLine();
        	
        	List<String> idNames = visitor.getIdentifierNames();
        	
        	bw.write("<identifier>");   
        	bw.newLine();
        	for(String idName:idNames){
            	bw.write(PreProcessor.transform(idName));
            	bw.newLine();
        	}       	
        	bw.write("</identifier>");
        	bw.newLine();
        	
        	List<ASTNode> list = new ArrayList<ASTNode>();
			for(Object element: cu.getCommentList())	{
        		list.add((ASTNode) element);
        	}
        			
        	bw.write("<comments>");   
        	bw.newLine();
        	for(int i=0;i<list.size();i++){        		
    	    	bw.write(PreProcessor.process(getCommentString(list.get(i))));
    	    	bw.newLine();
    	    }
           
        	bw.write("</comments>");   
        	bw.newLine();       	
        	bw.write("</text>\n</DOC>");
        	bw.newLine();
    	    bw.close();
    	    count++;
	    }
 	
	    logger.info("End");
	}


	private static String getCommentString(ASTNode node) throws org.eclipse.jface.text.BadLocationException {
		try {
			return fDocument.get(node.getStartPosition(), node.getLength());
		} catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}

}