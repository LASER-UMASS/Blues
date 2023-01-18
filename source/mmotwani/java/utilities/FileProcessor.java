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

package utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileProcessor {
	
	public static ArrayList<String> listFoldersInPath(String path) {
		ArrayList<String> folderlist = new ArrayList<String>();
		File dir = new File(path);
		for (final File fileEntry : dir.listFiles()) {
			if (fileEntry.isDirectory()) {
				folderlist.add(fileEntry.getName());
			}
		}
		return folderlist;
	}
	
	public static void listFilesInPath(String path, ArrayList<String> filelist) {
		try {
			File dir = new File(path); 
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					listFilesInPath(file.getCanonicalPath(), filelist);
				} else {
					filelist.add(file.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
