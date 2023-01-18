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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.JsonElement;


public class BugReportParser {
	
	protected static Logger logger = Logger.getLogger(BugReportParser.class);

	public static BugReport getBugDetails(Document document, String link, String project, String defectid) throws MalformedURLException, IOException {
		String bugsummary = "";
		String bugdescription = "";
		if (link.contains("github")) {
			String description = "";
			Element summary = document.select("bdi.js-issue-title").first();
			Elements comments = document.select("div#discussion_bucket");
		
			for (Element comment : comments) {
				description += comment.text();
			}
			description = description.replace("Copy link Quote reply", "");
			bugsummary = PreProcessor.process(summary.text());
			bugdescription = PreProcessor.process(description);

		} else if (link.contains("issues.apache")) {
			String description = "";
			Element summary = document.select("h1#summary-val").first();
			Elements comments = document.select("div#descriptionmodule");
			for (Element comment : comments) {
				description += comment.text();
			}
			description = description.toLowerCase().replace("description", "");
			bugsummary = PreProcessor.process(summary.ownText());
			bugdescription = PreProcessor.process(description);

		} else if (link.contains("json")) {
			InputStream input = new URL(link).openStream();
			Reader reader = new InputStreamReader(input, "UTF-8");

			Gson gson = new Gson();
			JsonElement json = gson.fromJson(reader, JsonElement.class);
			String result = gson.toJson(json);

			JSONObject obj = new JSONObject(result);
			String summary = obj.getString("summary");
			String description = "";
			JSONArray arr = obj.getJSONArray("comments");
			for (int i = 0; i < arr.length(); i++) {
				String comment = arr.getJSONObject(i).getString("content");
				description += comment;
			}

			bugsummary = PreProcessor.process(summary);
			bugdescription = PreProcessor.process(description);

		} else if (link.contains("sourceforge")) {
			String description = "";
			Element summary = document.select("h2.dark.title").first();
			Elements comments = document.select("div#ticket_content");
			for (Element comment : comments) {
				description += comment.text();
			}
			bugsummary = PreProcessor.process(summary.text());
			bugdescription = PreProcessor.process(description);
		} else {
			logger.error("FORMAT NOT SUPPORTED");
		}

		if (link.contains("github")) {
			String processed_bugdesc = "";
			String[] words = bugdescription.split(" ");
			Boolean flag = false;
			int ct = 0;
			for (String w : words) {

				if (w.length() < 1)
					continue;
				if (w.equalsIgnoreCase("commented")) {
					flag = true;
				}
				if (flag) {
					ct++;
				}
				if (ct > 3) {
					processed_bugdesc += w + " ";
				}
			}
			bugdescription = processed_bugdesc;
		}

		BugReport br = new BugReport(bugsummary, bugdescription, link, project, defectid, null);
		return br;
	}

	public static String ConvertToXML(BugReport br, String project, String defectid, ArrayList<String> fixedfiles) {

		String XML = "<bug id=\"" + project + "_" + defectid + "\" link=\"" + br.link + "\">\n";
		XML += "<buginformation>\n";
		XML += "<summary>" + br.summary.trim() + "</summary>\n";
		XML += "<description>" + br.description.trim() + "</description>\n";
		XML += "</buginformation>\n";
		XML += "<fixedfiles>\n";
		if (fixedfiles != null) {
			for (String f : fixedfiles) {
				XML += "<file>" + f.trim() + "</file>\n";
			}
		}
		XML += "</fixedfiles>\n";
		XML += "</bug>\n";
		return XML;
	}

}
