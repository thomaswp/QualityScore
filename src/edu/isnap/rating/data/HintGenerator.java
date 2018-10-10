package edu.isnap.rating.data;

import java.util.ArrayList;
import java.util.List;

import edu.isnap.rating.ColdStart.IHintGenerator;
import edu.isnap.rating.RatingConfig;

public abstract class HintGenerator implements IHintGenerator {


	/** This method should return the name of the hint generation algorithm. */
	public abstract String name();

	/**
	 * This method should take in the given training data and hint request and return a HintOutcome
	 * representing the hint state recommended by the algorithm.
	 */
	protected abstract List<HintOutcome> generateHints(
			List<Trace> trainingData, HintRequest request);

	private List<Trace> trainingData = new ArrayList<>();

	// The following methods are for use with the ColdStart class, where hints are generated
	// with varying levels of data.

	@Override
	public void clearTraces() {
		trainingData.clear();
	}

	@Override
	public void addTrace(Trace trace) {
		trainingData.add(trace);
	}

	@Override
	public HintSet generateHints(String name, RatingConfig config, List<HintRequest> hintRequests) {
		HintSet hintSet = new HintSet(name, config);
		for (HintRequest request : hintRequests) {
			generateHints(trainingData, request).forEach(hintSet::add);
		}
		return hintSet;
	}

	/**
	 * Use the hint generator to generate hints for one or more problems.
	 */
	public HintSet generateHints(RatingConfig config, TrainingDataset training,
			HintRequestDataset requests) {
		// This is a default implementation, but could be overwritten for some special cases
		HintSet hintSet = new HintSet(name(), config);
		// For each assignment...
		for (String assignmentID : requests.getAssignmentIDs()) {
			// Add all the training data
			clearTraces();
			training.getTraces(assignmentID).forEach(this::addTrace);
			// And generate hints for each request
			for (HintRequest request : requests.getRequestsForAssignmentID(assignmentID)) {
				generateHints(trainingData, request).forEach(hintSet::add);
			}
		}
		return hintSet;
	}
}
