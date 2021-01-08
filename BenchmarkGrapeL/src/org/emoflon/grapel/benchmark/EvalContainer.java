package org.emoflon.grapel.benchmark;

import java.util.LinkedList;
import java.util.List;

public class EvalContainer {
	public PatternMatchingEngine engine;
	public List<EvalElement> apamaLoad;
	public List<EvalElement> emoflonLoad;
	public String modelInstanceName;
	public int expectedEvents;
	
	public EvalContainer(String modelInstanceName, int expectedEvents, PatternMatchingEngine engine) {
		this.modelInstanceName = modelInstanceName;
		this.expectedEvents = expectedEvents;
		this.engine = engine;
		
		apamaLoad = new LinkedList<EvalElement>();
		emoflonLoad = new LinkedList<EvalElement>();
	}
	
	public void printElements() {
		System.out.println("LoadBenchmark using " + engine.toString() + " engine on " + modelInstanceName + " expecting " + expectedEvents + " element(s)");
		System.out.println("EMolfon Load took: " + printTime(emoflonLoad) + "(ms) and got: " + printEvents(emoflonLoad) + " (events)");
		System.out.println("Apama Load took: " + printTime(apamaLoad) + "(ms) and got: " + printEvents(apamaLoad) + " (events)");
	}
	private String printTime(List<EvalElement> evals) {
		String s = "[";
		for(EvalElement ele: evals) {
			if(ele.equals(evals.get(0)))
				s += ele.time;
			else s += "," + ele.time;
		}
		return s + "]";
	}
	private String printEvents(List<EvalElement> evals) {
		String s = "[";
		for(EvalElement ele: evals) {
			if(ele.equals(evals.get(0)))
				s += ele.eventNumber;
			else s += "," + ele.eventNumber;
		}
		return s + "]";
	}
}
