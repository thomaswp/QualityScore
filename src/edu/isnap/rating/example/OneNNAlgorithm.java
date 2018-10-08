package edu.isnap.rating.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import edu.isnap.node.ASTNode;
import edu.isnap.node.ASTSnapshot;
import edu.isnap.node.CodeAlignment;
import edu.isnap.node.CodeAlignment.NodePairs;
import edu.isnap.rating.RateHints;
import edu.isnap.rating.RatingConfig;
import edu.isnap.rating.data.HintGenerator;
import edu.isnap.rating.data.HintOutcome;
import edu.isnap.rating.data.HintRequest;
import edu.isnap.rating.data.Trace;
import edu.isnap.rating.data.TutorHint.Validity;

public class OneNNAlgorithm extends HintGenerator {

	@Override
	public String name() {
		return "One Nearest Neighbor";
	}

	private final static CodeAlignment Align = new CodeAlignment(1, 2);

	@Override
	protected List<HintOutcome> generateHints(List<Trace> trainingData, HintRequest request) {
		double highestReward = Double.MIN_VALUE;
		// Make sure not to modify the original request's code
		ASTNode requestCode = request.code.copy();
		ASTNode nearestNeighbor = requestCode;
		for (Trace trace : trainingData) {
			for (ASTSnapshot snapshot : trace) {
				int reward = Align.align(nearestNeighbor, requestCode).getReward();
				if (reward > highestReward) {
					highestReward = reward;
					nearestNeighbor = snapshot;
				}
			}
		}
		makeTopLevelEdit(Align.align(requestCode, nearestNeighbor),
				nearestNeighbor);
		return Collections.singletonList(
				new HintOutcome(requestCode, request.assignmentID, request.id, 1));
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
		new RateHints(Validity.MultipleTutors, false).rateGenerator(new OneNNAlgorithm(),
				RateHints.ITAP_S16_DATA_DIR,
				RatingConfig.Python);
	}
}
