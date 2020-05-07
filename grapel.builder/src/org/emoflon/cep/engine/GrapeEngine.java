package org.emoflon.cep.engine;

import java.io.IOException;

import com.apama.engine.beans.interfaces.EngineClientInterface;

public abstract class GrapeEngine {

	protected ApamaCorrelator correlator;
	protected TypeRegistry registry = new TypeRegistry();
	protected EngineClientInterface engineClient;
	
	public GrapeEngine() throws Exception {
		correlator = configureApamaCorrelator();
	}
	
	public abstract ApamaCorrelator configureApamaCorrelator() throws Exception;
	public abstract EngineClientInterface configureEngineClient() throws Exception;
	
	public void init() throws Exception {
		correlator.runCorrelator();
		engineClient = configureEngineClient();
	}
}
