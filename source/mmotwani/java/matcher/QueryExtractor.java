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

import parser.BugReport;
import parser.XMLParser;
import parser.PreProcessor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

public class QueryExtractor
{
  public QueryExtractor() {}
  protected static Logger logger = Logger.getLogger(QueryExtractor.class);
 
  public static void extractSumDesField(String XMLPath, String outputPath)
    throws IOException
  {
    XMLParser parser = new XMLParser();
    List<BugReport> bugRepo = parser.createRepositoryList(XMLPath);
    
    FileWriter fw = new FileWriter(outputPath);
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write("<parameters>");
    bw.newLine();
    for (int i = 0; i < bugRepo.size(); i++)
    {

      BugReport bug = (BugReport)bugRepo.get(i);
      
      bw.write("\t<query>\n\t\t<number>" + bug.getBugId() + "</number>");
      bw.newLine();
      
      bw.write("\t\t<text> #weight(" + 
      
        addField(PreProcessor.process(bug.getSummary()), "class", 1.0D) + " " + 
        addField(PreProcessor.process(bug.getDescription()), "class", 1.0D) + " " + 
        
        addField(PreProcessor.process(bug.getSummary()), "method", 1.0D) + " " + 
        addField(PreProcessor.process(bug.getDescription()), "method", 1.0D) + " " + 
        
        addField(PreProcessor.process(bug.getSummary()), "identifier", 1.0D) + " " + 
        addField(PreProcessor.process(bug.getDescription()), "identifier", 1.0D) + " " + 
        
        addField(PreProcessor.process(bug.getSummary()), "comments", 1.0D) + " " + 
        addField(PreProcessor.process(bug.getDescription()), "comments", 1.0D) + " " + 
        
        ")</text>\n\t</query>");
      bw.newLine();
    }
    
    bw.write("</parameters>");
    bw.newLine();
    
    bw.close();
    logger.info(bugRepo.size() + " created successfully :-)");
  }
  

  static String addField(String str, String fieldName)
  {
    String addedStr = "";
    
    String[] queryParts = str.split(" ");
    for (String eachPart : queryParts) {
      if (!eachPart.equals("")) {
        eachPart = eachPart + ".(" + fieldName + ")";
        addedStr = addedStr + eachPart + " ";
      }
    }
    
    return addedStr;
  }
  
  static String addField(String str, String fieldName, double weight)
  {
    String addedStr = "";
    
    String[] queryParts = str.split(" ");
    for (String eachPart : queryParts) {
      if (!eachPart.equals("")) {
        eachPart = weight + " " + eachPart + ".(" + fieldName + ")";
        addedStr = addedStr + eachPart + " ";
      }
    }
    
    return addedStr;
  }
  

  static String splitCamelCase(String s)
  {
    return s.replaceAll(
      String.format("%s|%s|%s", new Object[] {
      "(?<=[A-Z])(?=[A-Z][a-z])", 
      "(?<=[^A-Z])(?=[A-Z])", 
      "(?<=[A-Za-z])(?=[^A-Za-z])" }), 
      " ");
  }
  


  static void calculateRatio(String XMLPath)
  {
    XMLParser parser = new XMLParser();
    List<BugReport> bugRepo = parser.createRepositoryList(XMLPath);
    
    double ratioSum = 0.0D;
    
    int sumTotal = 0;
    int desTotal = 0;
    
    for (int i = 0; i < bugRepo.size(); i++) {
      BugReport bug = (BugReport)bugRepo.get(i);
      
      int summaryLength = countWord(bug.getSummary());
      int desLength = countWord(bug.getDescription());
      sumTotal += summaryLength;
      desTotal += desLength;
      double ratio = summaryLength / desLength;
      ratioSum += ratio;
      logger.info(summaryLength + "\t" + desLength + "\t" + ratio);
    }
    logger.info(ratioSum / bugRepo.size() + "\t" + sumTotal / desTotal);
  }
  

  static int countWord(String subject)
  {
    String[] word = PreProcessor.process(subject).split(" ");
    int c = 0;
    for (int j = 0; j < word.length; j++) {
      if (word[j].length() > 2) {
        c++;
      }
    }
    
    return c;
  }
}
