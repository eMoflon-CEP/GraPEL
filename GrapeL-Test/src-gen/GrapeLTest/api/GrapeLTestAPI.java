package GrapeLTest.api;

import GrapeLTest.api.rules.P1Pattern;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.ibex.common.operational.IContextPatternInterpreter;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;
import org.emoflon.ibex.gt.api.GraphTransformationRule;
import org.emoflon.ibex.gt.arithmetics.Probability;

/**
 * The GrapeLTestAPI with 1 rules.
 */
public class GrapeLTestAPI extends GraphTransformationAPI {
	
	public static String patternPath = "GrapeLTest/src-gen/GrapeLTest/api/ibex-patterns.xmi";
	
	/**
	 * Map with all the rules and patterns of the model
	 */
	private Map<String, Supplier<? extends GraphTransformationPattern>> patternMap;
	
	/*
	 *Map with all the rules that can be applied to Gillespie and their probabilities;
	 * array[0] is the probability; array[1] is probability*matchCount
	 */
	private Map<GraphTransformationRule, double[]> gillespieMap;

	/**
	 * Creates a new GrapeLTestAPI.
	 *
	 * @param engine
	 *            the engine to use for queries and transformations
	 * @param model
	 *            the resource set containing the model file
	 * @param workspacePath
	 *            the path to the workspace which is concatenated with the project
	 *            relative path to the patterns
	 */
	public GrapeLTestAPI(final IContextPatternInterpreter engine, final ResourceSet model, final String workspacePath) {
		super(engine, model);
		URI uri = URI.createFileURI(workspacePath + patternPath);
		interpreter.loadPatternSet(uri);
		patternMap = initiatePatternMap();
		gillespieMap = initiateGillespieMap();
	}

	/**
	 * Creates a new GrapeLTestAPI.
	 *
	 * @param engine
	 *            the engine to use for queries and transformations
	 * @param model
	 *            the resource set containing the model file
	 * @param defaultResource
	 *            the default resource
	 * @param workspacePath
	 *            the path to the workspace which is concatenated with the project
	 *            relative path to the patterns
	 */
	public GrapeLTestAPI(final IContextPatternInterpreter engine, final ResourceSet model, final Resource defaultResource,
			final String workspacePath) {
		super(engine, model, defaultResource);
		URI uri = URI.createFileURI(workspacePath + patternPath);
		interpreter.loadPatternSet(uri);
		patternMap = initiatePatternMap();
		gillespieMap = initiateGillespieMap();
	}
	
	private Map<String, Supplier<? extends GraphTransformationPattern>> initiatePatternMap(){
		Map<String, Supplier<? extends GraphTransformationPattern>> map = new HashMap<String, Supplier<? extends GraphTransformationPattern>>();
		map.put("P1Pattern", () -> p1());
		return map;
	}
	
	
	private Map<GraphTransformationRule, double[]> initiateGillespieMap(){
		Map<GraphTransformationRule, double[]> map = 
			new HashMap<GraphTransformationRule, double[]>();
		return map;
	}
	 
	/**
	 * Returns the probability that the rule will be applied with the
	 * Gillespie algorithm; only works if the rules do not have parameters and the
	 * probability is static
	 */
	public double getGillespieProbability(GraphTransformationRule rule){
		if(gillespieMap.containsKey(rule)){
			double totalActivity = getTotalSystemActivity();
			if(totalActivity > 0){
				return gillespieMap.get(rule)[1]/totalActivity;	
			}								
		}
		return 0;
	}
	
	/**
	 * Applies a rule to the graph after the Gillerspie algorithm;
	 * only rules that do not have parameters are counted
	 * @return an {@link Optional} for the the match after rule application
	 */
	public final Optional<GraphTransformationMatch> applyGillespie(){
		double totalActivity = getTotalSystemActivity();
		if(totalActivity != 0){
			Random rnd = new Random();
			double randomValue = totalActivity*rnd.nextDouble();
			double currentActivity = 0;
			for(Entry<GraphTransformationRule, double[]> entries : gillespieMap.entrySet()){
			currentActivity += entries.getValue()[1];
				if(currentActivity >= randomValue){
					return entries.getKey().apply();
				}						
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Helper method for the Gillespie algorithm; counts all the possible matches
	 * for rules in the graph that have a static probability
	 */
	private double getTotalSystemActivity(){
		 gillespieMap.forEach((v,z) -> {
		 	z[0] =((Probability) v.getProbability().get()).getProbability();
			 z[1] = v.countMatches()*z[0];
			});
		double totalActivity = 0;
		for(double[] activity : gillespieMap.values()) {
			totalActivity += activity[1];
		}
		return totalActivity;
	}
					

	/**
	 * Creates a new instance of the pattern <code>p1()</code> which does the following:
	 * If this pattern is not self-explaining, you really should add some comment in the specification.
	 *
	 * @return the new instance of the pattern
	 */
	public P1Pattern p1() {
		return new P1Pattern(this, interpreter);
	}
/**
 * returns all the patterns and rules of the model that do not need an input parameter
 */
public Map<String, Supplier<? extends GraphTransformationPattern>> getAllPatterns(){
	return patternMap;
}
}
