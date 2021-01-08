package org.emoflon.grapel.benchmark.flights.karthesian;

import java.util.LinkedList;
import java.util.List;

import org.emoflon.grapel.benchmark.EvalContainer;
import org.emoflon.grapel.benchmark.EvalElement;
import org.emoflon.grapel.benchmark.PatternMatchingEngine;

import FlightGrapeLLoadApama.grapel.Flightapama.FlightapamaGrapeLAPI;
import FlightGrapeLLoadEMoflon.grapel.Flightemoflon.FlightemoflonGrapeLAPI;

public class FlightKarthesianBenchmark {
	private static final long sleepTime = 100;
	protected EvalElement runApamaLoad(String modelInstanceFileName, int expectedEvents, PatternMatchingEngine engine) {
		FlightApamaKarthesianMonitor monitor = new FlightApamaKarthesianMonitor();		
		FlightapamaGrapeLAPI api = monitor.init(modelInstanceFileName, engine);
		
		long tic = System.currentTimeMillis();
		api.update();
		while (expectedEvents > api.getConnectedFlightsEventHandler().getAllEvents().size())
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		long toc = System.currentTimeMillis();
		int eventNumber =  api.getConnectedFlightsEventHandler().getAllEvents().size();
		
		monitor.shutdown();
		
		return new EvalElement(toc-tic, eventNumber);		
	}
	protected EvalElement runEmoflonLoad(String modelInstanceFileName, int expectedEvents, PatternMatchingEngine engine) {
		FlightEMoflonKarthesianMonitor monitor = new FlightEMoflonKarthesianMonitor();		
		FlightemoflonGrapeLAPI api = monitor.init(modelInstanceFileName, engine);
		
		long tic = System.currentTimeMillis();
		api.update();
		while (expectedEvents > api.getConnectedFlightsEventHandler().getAllEvents().size())
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		long toc = System.currentTimeMillis();
		int eventNumber =  api.getConnectedFlightsEventHandler().getAllEvents().size();
		
		monitor.shutdown();
		
		return new EvalElement(toc-tic, eventNumber);		
	}
	protected EvalContainer runLoad(String modelInstanceFileName, int expectedEvents, PatternMatchingEngine engine, int numberOfRuns) {
		EvalContainer container = new EvalContainer(modelInstanceFileName, expectedEvents, engine);
		for(int i=0; i<numberOfRuns;i++) container.apamaLoad.add(runApamaLoad(modelInstanceFileName, expectedEvents, engine));
		for(int i=0; i<numberOfRuns;i++) container.emoflonLoad.add(runEmoflonLoad(modelInstanceFileName, expectedEvents, engine));
		
		return container;
	}
	protected List<EvalContainer> runLoad(String modelInstanceFileName, int expectedEvents, int numberOfRuns) {
		List<EvalContainer> eval = new LinkedList<EvalContainer>();
		eval.add(runLoad(modelInstanceFileName, expectedEvents, PatternMatchingEngine.HiPE, numberOfRuns));
		eval.add(runLoad(modelInstanceFileName, expectedEvents, PatternMatchingEngine.Viatra, numberOfRuns));
		eval.add(runLoad(modelInstanceFileName, expectedEvents, PatternMatchingEngine.Democles, numberOfRuns));
		
		return eval;
	}
	
	public static void main (String[] arg) {
		FlightKarthesianBenchmark benchmark = new FlightKarthesianBenchmark();
		
		String instance = "test1";
		int expectedEvents = 1;
		List<EvalContainer> eval = benchmark.runLoad(instance +".xmi", expectedEvents, 10);
		for(EvalContainer container: eval) container.printElements();
//		EvalElement apamaLoad = benchmark.runApamaLoad(instance +".xmi", expectedEvents, PatternMatcherEngines.HiPE);
//		EvalElement emoflonLoad = benchmark.runEmoflonLoad(instance + ".xmi", expectedEvents, PatternMatcherEngines.HiPE);
//		
//		System.out.println("Benchmark with load on Apama on " + instance + " with " + expectedEvents + " expected events and got " 
//			+ apamaLoad.eventNumber + " events took " + apamaLoad.time + " ms");
//		System.out.println("Benchmark with load on EMoflon on " + instance + " with " + expectedEvents + " expected events and got "
//			+ emoflonLoad.eventNumber + " events took " + emoflonLoad.time + " ms");
		
	}
}
