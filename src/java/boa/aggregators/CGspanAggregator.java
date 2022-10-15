package boa.aggregators;

import java.io.IOException;
import java.util.HashMap;

import boa.functions.BoaCasts;
import boa.io.EmitKey;

import boa.aggregators.GspanAggregator;

/**
 * A Boa aggregator to count through subpatterns.
 * 
 * @author DavidMOBrien
 * @author rdyer
 */
@AggregatorSpec(name = "cgSpanAgg", formalParameters = { "double" }, canCombine = true)
public class CGspanAggregator extends GspanAggregator {
	
	public CGspanAggregator(double n) {
		super(n);
	}
	
	public HashMap<String, Integer> filter() {
		
		System.out.println("here");
		
		HashMap<String, Integer> subpatterns = super.filter();
		
		//TODO: find something not O(n^2)?
		HashMap<String, Integer> filtered = new HashMap<String, Integer>();
		
		for (String key : subpatterns.keySet()) {
			
			boolean keep = true;
			
			for (String compare : subpatterns.keySet()) {
				
				if (key.equals(compare)) {
					continue;
				}
				
				//CGspan removes any subpatterns which is a subset of another pattern that has the
				//same frequency. This provides more fruitful results.
				if (compare.startsWith(key) && subpatterns.get(key).equals(subpatterns.get(compare))) {
					keep = false;
				}
			}
			
			if (keep) {
				filtered.put(key, subpatterns.get(key));
			}
		}
		
		return filtered;
		
	}
	
}