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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import configuration.ConfigurationParameters;

public class ExecuteCommandLine {
	
	protected static Logger logger = Logger.getLogger(ExecuteCommandLine.class);

	public static String executeCommandAndGetOutput(String command) throws Exception {
		logger.info("running command to get output:" + command);
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		String outputStream = "";
		BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
			     InputStreamReader(process.getErrorStream()));

			// Read the output from the command
			//System.out.println("Here is the standard output of the command:\n");
			String s = null;
			while ((s = stdInput.readLine()) != null) {
			 //   System.out.println(s);
			    outputStream = outputStream.concat(s);
			}

			// Read any errors from the attempted command
			//System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
			//    System.out.println(s);
				logger.error(s);
			}
		
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	    org.apache.commons.exec.CommandLine commandline = CommandLine.parse(command);
//	  //  ExecuteWatchdog watchdog = new ExecuteWatchdog(9600000);
//	    DefaultExecutor exec = new DefaultExecutor();
//	    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
//	    exec.setStreamHandler(streamHandler);
//	    exec.execute(commandline);
//	    exec.wait(9600000);
	 //   exec.setWatchdog(watchdog);
	    logger.info(outputStream);
	    return(outputStream.toString());
	}
	
	public static void executeCommand(String command) throws Exception {
		
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
//	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//	    org.apache.commons.exec.CommandLine commandline = CommandLine.parse(command);
//	//    ExecuteWatchdog watchdog = new ExecuteWatchdog(9600000);
//	    DefaultExecutor exec = new DefaultExecutor();
//	    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
//	    exec.setStreamHandler(streamHandler);
//	    exec.execute(commandline);
//	    exec.wait(9600000);
	//    exec.setWatchdog(watchdog);
	    logger.info("finised executing cmd:" + command);
	 //   exec.wait();
	}
	
	
//	public static void main(String[] args) throws Exception {
//		// TODO Auto-generated method stub
//		BasicConfigurator.configure();
//		String command = "rm -rf /home/manish/Documents/Blues/defects/chart1buggy";
//		logger.info("running cmd: " + command);
//		executeCommand(command);
//	
//		String command1 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j checkout -p Chart -v 1b -w /home/manish/Documents/Blues/defects/chart1buggy";
//		logger.info("running cmd: " + command1);
//		executeCommand(command1);
//		
//		String command3 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j compile -w /home/manish/Documents/Blues/defects/chart1buggy";
//		logger.info("running cmd: " + command3);
//		logger.info(executeCommandAndGetOutput(command3));
//		
//		String command4 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j test -w /home/manish/Documents/Blues/defects/chart1buggy";
//		logger.info("running cmd: " + command4);
//		logger.info(executeCommandAndGetOutput(command4));
//		
//		String command5 = ConfigurationParameters.defects4JHome + "framework/bin/defects4j export -p dir.src.classes -w /home/manish/Documents/Blues/defects/chart1buggy";
//		logger.info("running cmd: " + command5);
//		logger.info(executeCommandAndGetOutput(command5));
//
//	}
	

}
