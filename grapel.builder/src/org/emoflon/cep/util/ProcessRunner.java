package org.emoflon.cep.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utilities for creating and stopping a process via command
 */
public class ProcessRunner extends Thread{
	/**
	 * Process which the runner should control
	 */
	private Process pr = null;
	/**
	 * The process execution arguments
	 */
	private final String[] execArgs;
	/**
	 * If the process is currently running
	 */
	private boolean running = false;
	/**
	 * If debug mode is enabled
	 */
	private boolean debug = false;
	
	/**
	 * Constructor for a process runner for given exec arguments
	 * @param execArgs as executable arguments to run the Process
	 */
	public ProcessRunner(final String[] execArgs) {
		this.execArgs = execArgs;
	}
	
	/**
	 * Constructor for a process runner for given exec arguments and debug mode
	 * @param execArgs as executable arguments to run the Process
	 * @param debug if the debug mode should be enabled
	 */
	public ProcessRunner(final String[] execArgs, boolean debug) {
		this.execArgs = execArgs;
		this.debug = debug;
	}
	
	@Override
	public void run() {
		Runtime runtime = Runtime.getRuntime();
		try
		{
			pr = runtime.exec(execArgs);
			running = true;
//			System.out.println("Running: " + pr.toString()+" status: "+pr.isAlive());
			
			BufferedReader stdInput = new BufferedReader(new 
				     InputStreamReader(pr.getInputStream()));

				BufferedReader stdError = new BufferedReader(new 
				     InputStreamReader(pr.getErrorStream()));
				while(running && pr.isAlive()) {
					// Read the output from the command
					String s = null;
					while ((s = stdInput.readLine()) != null) {
					    if(debug)
					    	System.out.println(s);
					}

					// Read any errors from the attempted command
					while ((s = stdError.readLine()) != null) {
					    if(debug)
					    	System.out.println(s);
					}
				}
				close();
				
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the debug parameter
	 * @param debug parameter for enabling/disabling debug
	 */
	public synchronized void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Closes the process and updates the running state
	 */
	public synchronized void close() {
		// if process is already terminated set running to false and exit
		if(!pr.isAlive()) {
			running = false;
			return;
		}
		
		// terminate process and wait until process has died
		pr.destroy();
		while(pr.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		running = false;
	}
}
