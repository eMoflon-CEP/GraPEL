package org.emoflon.cep.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessRunner extends Thread{
	private Process pr = null;
	private final String[] execArgs;
	private boolean running = false;
	private boolean debug = false;
	
	public ProcessRunner(final String[] execArgs) {
		this.execArgs = execArgs;
	}
	
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
	
	public synchronized void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public synchronized void close() {
		if(!pr.isAlive()) {
			running = false;
			return;
		}
//		System.out.println("Killing: "+pr.toString()+" status: "+pr.isAlive());
		
		pr.destroy();
		while(pr.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		System.out.println("Exited with: "+pr.exitValue());
		running = false;
	}
}
