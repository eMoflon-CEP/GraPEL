package org.emoflon.cep.engine;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import org.emoflon.cep.util.ProcessRunner;

/**
 * Interface for running the Apama correlator process
 */
public class ApamaCorrelator {
	
	/**
	 * Location to the correlator
	 */
	private final String correlatorLocation;
	/**
	 * Location to the license file
	 */
	private String licenseLocation = null;
	/**
	 * Port number for the correlator
	 */
	private int port = -1;
	/**
	 * Debug flag
	 */
	private boolean debug;
	// TODO: Add other exec arguments, e.g., queue-size, etc..
	
	/**
	 * Runner for the correlator
	 */
	private ProcessRunner runner;
	
	/**
	 * Constructor for the GrapeL Apama correlator interface
	 * @param correlatorLocation where the executable Apama correlator can be found
	 */
	public ApamaCorrelator(final String correlatorLocation) {
		this.correlatorLocation = correlatorLocation;
	}
	
	/**
	 * Sets the location to the license file
	 * @param licenseLocation where the Apama license file is placed
	 * @throws IOException if no file at the given location exists
	 */
	public void setLicenseLocation(String licenseLocation) throws IOException {
		File file = new File(licenseLocation);
		if(!file.exists())
			throw new IOException("License file: "+licenseLocation+" does not exist!");
		this.licenseLocation = licenseLocation;
	}
	
	/**
	 * Sets the port, which should be used to access the correlator
	 * @param port for the correlator
	 * @throws SocketException if a non valid port number is used
	 */
	public void setPort(int port) throws SocketException {
		if(port > 65535 || port < 0)
			throw new SocketException("Port number must GEQ to 0 and LEQ to 65535. Given: "+port);
		this.port = port;
	}
	
	/**
	 * Sets debug flag
	 * @param debug flag
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Creates the exec arguments used to run the correlator
	 * @return the exec string to run the correlator with the given parameters
	 * @throws IOException if the no file at the given correlator path exists
	 */
	public String[] buildExecArgs() throws IOException {
		List<String> args = new LinkedList<>();
		// check if file at correlator location exsits
		File file = new File(correlatorLocation);
		if(!file.exists())
			throw new IOException("Correlator executable: "+correlatorLocation+" does not exist!");
		
		// add correlator executable path
		args.add(correlatorLocation);
		// add parameter with path to license location
		if(licenseLocation != null) {
			args.add("-l");
			args.add(licenseLocation);
		}
		// add parameter with the port
		if(port > -1) {
			args.add("-p");
			args.add(""+port);
		}
		
		// convert string to buffer
		String[] buffer = new String[args.size()];
		return args.toArray(buffer);
	}
	
	/**
	 * Runs the correlator with the given parameters/flags
	 * @throws IOException if the no file at the given correlator path exists
	 */
	public void runCorrelator() throws IOException {
		runner = new ProcessRunner(buildExecArgs());
		runner.setDebug(debug);
		runner.start();
	}
	
	/**
	 * Closes the correlator runner and wait for the thread to die
	 * @throws InterruptedException if any thread has interrupted the current thread
	 */
	public void disposeCorrelator() throws InterruptedException {
		runner.close();
		runner.join();
	}
}
