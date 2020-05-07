package org.emoflon.cep.engine;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import org.emoflon.cep.util.ProcessRunner;

public class ApamaCorrelator {
	
	private final String correlatorLocation;
	private String licenseLocation = null;
	private int port = -1;
	private boolean debug;
	// TODO: Add other exec arguments, e.g., queue-size, etc..
	
	private ProcessRunner runner;
	
	public ApamaCorrelator(final String correlatorLocation) {
		this.correlatorLocation = correlatorLocation;
	}
	
	public void setLicenseLocation(String licenseLocation) throws IOException {
		File file = new File(licenseLocation);
		if(!file.exists())
			throw new IOException("License file: "+licenseLocation+" does not exist!");
		this.licenseLocation = licenseLocation;
	}
	
	public void setPort(int port) throws SocketException {
		if(port > 65535 || port < 0)
			throw new SocketException("Port number must GEQ to 0 and LEQ to 65535. Given: "+port);
		this.port = port;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public String[] buildExecArgs() throws IOException {
		List<String> args = new LinkedList<>();
		File file = new File(correlatorLocation);
		if(!file.exists())
			throw new IOException("Correlator executable: "+correlatorLocation+" does not exist!");
		
		args.add(correlatorLocation);
		if(licenseLocation != null) {
			args.add("-l");
			args.add(licenseLocation);
		}
		if(port > -1) {
			args.add("-p");
			args.add(""+port);
		}
		
		String[] buffer = new String[args.size()];
		return args.toArray(buffer);
	}
	
	public void runCorrelator() throws IOException {
		runner = new ProcessRunner(buildExecArgs());
		runner.setDebug(debug);
		runner.start();
	}
	
	public void disposeCorrelator() throws InterruptedException {
		runner.join();
	}
}
