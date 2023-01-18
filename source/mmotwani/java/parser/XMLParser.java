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

import parser.BugReport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XMLParser
{
  public XMLParser() {}
  
  public HashMap<String, BugReport> createRepositoryMap(String XMLPath)
  {
    HashMap<String, BugReport> bugRepository = new HashMap<String, BugReport> ();
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    

    try
    {
      DocumentBuilder db = dbf.newDocumentBuilder();
      

      Document doc = db.parse(XMLPath);
      
      doc.getDocumentElement().normalize();
      
      NodeList bugList = doc.getElementsByTagName("bug");
      

      for (int bugNumber = 0; bugNumber < bugList.getLength(); bugNumber++) {
        BugReport bugReport = new BugReport();
        Node bug = bugList.item(bugNumber);
        String bugId = bug.getAttributes().getNamedItem("id").getFirstChild().getNodeValue();
        bugReport.setBugId(bugId);
        
        Element bugInformation = (Element)bug.getChildNodes().item(1);
        String summary = getTagValue("summary", bugInformation);
        bugReport.setSummary(summary);
        String description = getTagValue("description", bugInformation);
        bugReport.setDescription(description);
        
        Element fileNode = (Element)bug.getChildNodes().item(3);
        
        NodeList fileNodeList = fileNode.getElementsByTagName("file");
        
        Set<String> files = new HashSet<String> ();
        
        for (int j = 0; j < fileNodeList.getLength(); j++) {
          String fileName = fileNodeList.item(j).getChildNodes().item(0).getNodeValue();
          
          fileName = fileName.replaceAll("/", ".");
          

          files.add(fileName);
        }
        bugReport.setFixedFiles(files);
        bugRepository.put(bugId, bugReport);
      }
    } catch (ParserConfigurationException pce) {
      pce.printStackTrace();
    } catch (SAXException se) {
      se.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    
    return bugRepository;
  }
  
  public List<BugReport> createRepositoryList(String XMLPath) {
    List<BugReport> bugRepository = new ArrayList<BugReport> ();
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    

    try
    {
      DocumentBuilder db = dbf.newDocumentBuilder();
      

      Document doc = db.parse(XMLPath);
      
      doc.getDocumentElement().normalize();
      
      NodeList bugList = doc.getElementsByTagName("bug");
      

      for (int bugNumber = 0; bugNumber < bugList.getLength(); bugNumber++) {
        BugReport bugReport = new BugReport();
        Node bug = bugList.item(bugNumber);
        String bugId = bug.getAttributes().getNamedItem("id").getFirstChild().getNodeValue();
        bugReport.setBugId(bugId);
        
        Element bugInformation = (Element)bug.getChildNodes().item(1);
        String summary = getTagValue("summary", bugInformation);
        bugReport.setSummary(summary);
        String description = getTagValue("description", bugInformation);
        bugReport.setDescription(description);
        
        Element fileNode = (Element)bug.getChildNodes().item(3);
        
        NodeList fileNodeList = fileNode.getElementsByTagName("file");
        
        Set<String> files = new HashSet<String>();
        
        for (int j = 0; j < fileNodeList.getLength(); j++) {
          String fileName = fileNodeList.item(j).getChildNodes().item(0).getNodeValue();
          fileName = fileName.replaceAll("/", ".");
          files.add(fileName);
        }
        bugReport.setFixedFiles(files);
        bugRepository.add(bugReport);
      }
    } catch (ParserConfigurationException pce) {
      pce.printStackTrace();
    } catch (SAXException se) {
      se.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    
    return bugRepository;
  }
  
  private static String getTagValue(String sTag, Element eElement){
    NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
    Node nValue = nlList.item(0);
    String str = "";
    if (nValue != null) {
      str = nValue.getNodeValue();
    }
    return str;
  }
}
