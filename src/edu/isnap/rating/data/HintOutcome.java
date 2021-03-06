package edu.isnap.rating.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONObject;

import edu.isnap.node.ASTNode;
import edu.isnap.rating.RatingConfig;

public class HintOutcome implements Comparable<HintOutcome> {

	public final ASTNode result;
	public final String assignmentID;
	public final String requestID;
	/**
	 * Distinguishable ID for this hint outcome.
	 * This ID is a hash code and not guaranteed to be collision-free
	 * */
	public final int id;

	private final double weight;

	private final static Comparator<HintOutcome> comparator =
			Comparator.comparing((HintOutcome outcome) -> outcome.weight()).thenComparing(
					Comparator.comparing(outcome -> outcome.id));

	public double weight() {
		return weight;
	}

	public HintOutcome(ASTNode result, String assignmentID, String requestID, double weight) {
		this.result = result;
		this.assignmentID = assignmentID;
		this.requestID = requestID;
		this.weight = weight;
		HashCodeBuilder builder = new HashCodeBuilder(5, 13);
		builder.append(result);
		builder.append(assignmentID);
		builder.append(requestID);
		builder.append(weight);
		// Force the ID to be positive
		id = result == null ? 0 : builder.toHashCode() & 0x7FFFFFFF;
		if (weight <= 0 || Double.isNaN(weight)) {
			throw new IllegalArgumentException("All weights must be positive: " + weight);
		}
	}

	public String resultString(ASTNode from, RatingConfig config) {
		return ASTNode.diff(from, result, config, 1);
	}

	@Override
	public int compareTo(HintOutcome o) {
		return comparator.compare(this, o);
	}

	public static HintOutcome parse(File file, String assignmentID) throws IOException {
		String contents = new String(Files.readAllBytes(file.toPath()));
		JSONObject json = new JSONObject(contents);
		ASTNode root = ASTNode.parse(json);
		String name = file.getName().replace(".json", "");
		String snapshotID;
		try {
			int underscoreIndex = name.indexOf("_");
			snapshotID = name.substring(0, underscoreIndex);
		} catch (Exception e) {
			throw new RuntimeException("Invalid outcome file name: " + file.getName());
		}
		if (json.has("error")) {
			double error = json.getDouble("error");
			return new HintWithError(root, assignmentID, snapshotID, error);
		}
		double weight = 1;
		if (json.has("weight")) {
			weight = json.getDouble("weight");
		}
		return new HintOutcome(root, assignmentID, snapshotID, weight);
	}

	public static class HintWithError extends HintOutcome {

		public final double error;

		private double calculatedWeight = -1;

		@Override
		public double weight() {
			if (calculatedWeight == -1) {
				throw new RuntimeException("Must calculate weight before accessing it.");
			}
			return calculatedWeight;
		}

		public HintWithError(ASTNode result, String assignmentID, String snapshotID, double error) {
			super(result, assignmentID, snapshotID, 1);
			this.error = error;
		}

		public void calculateWeight(double minError, double beta) {
			calculatedWeight = Math.exp(-beta * (error - minError));
			if (Double.isNaN(calculatedWeight)) {
				throw new RuntimeException(String.format(
						"Weight is Nan: e=%.05f; beta=%.05f; e_min=%.05f",
						error, beta, minError));
			}
		}
	}

	public Map<String, String> getDebuggingProperties(ASTNode requestNode) {
		return new HashMap<>();
	}
}