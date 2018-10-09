package edu.isnap.rating.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.isnap.node.ASTNode;
import edu.isnap.node.ASTSnapshot;
import edu.isnap.node.CodeAlignment;
import edu.isnap.node.CodeAlignment.NodePairs;
import edu.isnap.rating.HintRater;
import edu.isnap.rating.RatingConfig;
import edu.isnap.rating.data.HintGenerator;
import edu.isnap.rating.data.HintOutcome;
import edu.isnap.rating.data.HintRequest;
import edu.isnap.rating.data.HintSet;
import edu.isnap.rating.data.Trace;
import edu.isnap.rating.data.TutorHint.Validity;

public class OneNNAlgorithm extends HintGenerator {

	@Override
	public String name() {
		// Probably best not to use spaces
		return "OneNN";
	}

	private final static CodeAlignment Align = new CodeAlignment(1, 2);

	// This method actually generates the hints
	@Override
	protected List<HintOutcome> generateHints(List<Trace> trainingData, HintRequest request) {

		// Look for the closest node in the training dataset (correct or not)
		double highestSimilarity = Double.MIN_VALUE;
		// Make sure not to modify the original request's code
		ASTNode requestCode = request.code.copy();
		ASTNode nearestNeighbor = requestCode;
		for (Trace trace : trainingData) {
			// Don't count traces that didn't get to a correct solution
			if (!trace.getFinalSnapshot().isCorrect) continue;
			for (ASTSnapshot snapshot : trace) {
				// Use a simple similarity metric
				int similarity = Align.align(nearestNeighbor, requestCode).getReward();
				if (similarity > highestSimilarity) {
					highestSimilarity = similarity;
					nearestNeighbor = snapshot;
				}
			}
		}
		// Use this nearest neighbor to extract a top-leve edit
		makeTopLevelEdit(Align.align(requestCode, nearestNeighbor),
				nearestNeighbor);
		// Return this as a single hint, with a weight of 1. You can return multiple hints and
		// weight them to reflect the confidence that it's a good hint.
		double weight = 1;
		return Collections.singletonList(
				new HintOutcome(requestCode, request.assignmentID, request.id, weight));
	}

	private boolean makeTopLevelEdit(NodePairs pairs, ASTNode to) {
		ASTNode fromPair = pairs.getTo(to);

		// If this Node does not have a pair or has a different pair...
		if (to.parent() != null && (fromPair == null || !fromPair.shallowEquals(to, false))) {
			// Insert it and return that as an edit
			ASTNode parentPair = pairs.getTo(to.parent());
			// Remove any node this replaces
			if (fromPair != null) parentPair.removeChild(fromPair.index());
			ASTNode toInsert = to.copy();
			// First remove all children
			while (toInsert.children().size() > 0) toInsert.removeChild(0);
			parentPair.addChild(to.index(), toInsert);
			return true;
		}



		for (ASTNode child : to.children()) {
			if (makeTopLevelEdit(pairs, child)) return true;
		}
		return false;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// If you want to see the hints that the algorithm generated, set debug=true
		boolean debug = false;
		// Use the ITAP dataset (it's easier, and this algorithm needs all the help it can get...)
		String dataset = HintRater.itapS16Dir();
		// Since we're using ITAP, make sure to use the Python rating config.
		RatingConfig config = RatingConfig.Python;
		HintGenerator generator = new OneNNAlgorithm();

		// Train and test the algorithm to create a set of hints
		HintSet hintSet = HintRater.createHintSet(generator, dataset, config);
		// Then rate them
		new HintRater(Validity.MultipleTutors, debug).rate(dataset, hintSet);

		// We can also write these hints to the algorithms directory, to analyze them offline
		hintSet.writeToFolder(dataset + HintRater.ALGORITHMS_DIR + "/" + generator.name(), true);

	}
}
