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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import parser.BugReportParser;

import org.apache.log4j.Logger;

import configuration.ConfigurationParameters;

public class BugReportCrawler {

	private HashMap<String, String> defect_link;
	private HashSet<BugReport> brlist;
	protected static Logger logger = Logger.getLogger(BugReportCrawler.class);

	public BugReportCrawler() {
		defect_link = new HashMap<String, String>();
		brlist = new HashSet<BugReport>();
	}

	// Find all URLs of bug reports from commit-DB files and add them to the
	// HashSets. Return the array list containing the defects processed
	public ArrayList<String> getBugReportLinks(String path, String def) throws IOException {
		ArrayList<String> d4jdefects = new ArrayList<String>();
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			String[] record = line.split(",");
			if (record.length < 5)
				continue;

			String project = path.split("/")[path.split("/").length - 2];
			String defectID = record[0];
			String actualID = record[3];
			String link = record[4];
			String defect = project + "_" + defectID;
			d4jdefects.add(defect);
			if (!def.contentEquals("all") && !def.contentEquals(defect)) continue;
			if (link.length() > 0) {
				if (!defect_link.containsKey(defect)) {
					defect_link.put(defect, link);
				}
				logger.info(line);
				logger.info(project + "_" + defectID + "_" + actualID + " =>> " + link);
			}
			if (!def.contentEquals("all")) break;
		}
		br.close();
		return d4jdefects;
	}

	// Connect to each bug report link and fetch information about summary,
	// description, and files modified
	public void getBugReportDetails(ArrayList<String> d4jdefects) throws InterruptedException {
		logger.info("Total #defects with bug reports: " + defect_link.keySet().size());
		int sleepSeconds = 2;
		for (String defect : defect_link.keySet()) {
			
			File xmlfile = new File(ConfigurationParameters.XMLQueryPath + "/" + defect + ".xml");
			if(xmlfile.exists()) return;
			
			String link = defect_link.get(defect);
			logger.info("DEFECT: " + defect);
			logger.info("LINK: " + link);
			Thread.sleep(sleepSeconds * 1000);
			if (!d4jdefects.contains(defect)) {
				d4jdefects.add(defect);
			}
			String project = defect.split("_")[0];
			String defectid = defect.split("_")[1];
			Document document;
			try {
				document = Jsoup.connect(link)
						.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
						.ignoreContentType(true).get();
				BugReport bugreport = BugReportParser.getBugDetails(document, link, project, defectid);
				brlist.add(bugreport);
			} catch (IOException e) {
				System.err.println(e.toString());
			}
		}
	}

	// Connect to input bug report link and fetch information about summary,
	// description, and files modified
	public void getBugReportDetails(String project, String defectid, String link) throws InterruptedException {
		logger.info("Crawling bug report from : " + link);
		int sleepSeconds = 2;
		Thread.sleep(sleepSeconds * 1000);
		Document document;
		try {
			document = Jsoup.connect(link)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
					.ignoreContentType(true).get();
			BugReport bugreport = BugReportParser.getBugDetails(document, link, project, defectid);
			brlist.add(bugreport);
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
	
	
	// write the extracted bug report context to an XML file
	public void writeToXML(String filesdir, String defect) {

		File dir = new File(filesdir);
		if (!dir.exists()) {
			logger.info("creating dir to store XML queries at " + filesdir);
			dir.mkdir();
		}
		File xmlfile = new File(ConfigurationParameters.XMLQueryPath + "/" + defect + ".xml");
		if(xmlfile.exists()) return;
		
		logger.info(brlist.size());
		for (BugReport br : brlist) {
			try {
				// String brlink = br.link;
				String defectid = br.defectid; // defects =
												// link_defect.get(brlink);
				String proj = br.project;
				String def = proj + "_" + defectid;
				if (!defect.contentEquals("all") && !def.equalsIgnoreCase(defect)) {
					continue;
				}
				logger.info("creating XML query for " + def);
				FileWriter writer = new FileWriter(filesdir + "/" + proj + "_" + defectid + ".xml");
				writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
				writer.write("<bugrepository name=\"Defects4J\">\n");

				ArrayList<String> fixedfiles = getFixedFilesForDefect(proj, defectid);
				String xmlbr = BugReportParser.ConvertToXML(br, proj, defectid, fixedfiles);

				writer.write(xmlbr);
				writer.write("</bugrepository>\n");
				writer.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	public static ArrayList<String> getFixedFilesForDefect(String project, String defectid) throws IOException {

		ArrayList<String> filelist = new ArrayList<String>();

		String path = ConfigurationParameters.defects4JHome + "/framework/projects/" + project + "/modified_classes/"
				+ defectid + ".src";
		File srcfile = new File(path);
		if (!srcfile.exists()){
			return null;
		}
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while ((st = br.readLine()) != null) {
			//logger.info(st);
			filelist.add(st);
		}
		br.close();
		return filelist;
	}

//	public static void main(String[] args) throws IOException {
//		// BugReportCrawler brc = new BugReportCrawler();
//		// brc.getBugReportLinks(ConfigurationParameters.CommitDBPaths);
//		// brc.getBugDetails();
//		// brc.writeToXML(ConfigurationParameters.XMLQueryPath, "Chart");
//		String project = "JacksonDatabind";
//		String defectid = "47";
//		String link = "https://github.com/FasterXML/jackson-databind/issues/1231";
//		try {
//			Document document = Jsoup.connect(link).userAgent("Mozilla").ignoreContentType(true).get();
//			logger.info(link);
//			BugReport bugreport = BugReportParser.getBugDetails(document, link, project, defectid);
//		} catch (IOException e) {
//			System.err.println(e.getMessage());
//		}
//	}

}
