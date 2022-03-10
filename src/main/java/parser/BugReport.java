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

import java.util.Set;

public class BugReport {

	String summary;
	String description;
	String link;
	String project;
	String defectid;
	String bugId;
	Set<String> fixedFiles;

	public BugReport(String summary, String description, String link, String project, String defectid,
			Set<String> fixedFiles) {
		this.summary = summary;
		this.description = description;
		this.link = link;
		this.project = project;
		this.defectid = defectid;
		this.fixedFiles = fixedFiles;
		this.bugId = project + "_" + defectid;
	}

	public BugReport() {
	}

	public String getBugId() {
		return bugId;
	}

	public void setBugId(String bugId) {
		this.bugId = bugId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getFixedFiles() {
		return fixedFiles;
	}

	public void setFixedFiles(Set<String> fixedFiles) {
		this.fixedFiles = fixedFiles;
	}

	public String toString() {
		return "BugReport [bugId=" + bugId + ", summary=" + summary + ", description=" + description + ", fixedFiles="
				+ fixedFiles + "]";
	}
}
