package edu.isnap.rating;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.isnap.rating.data.GoldStandard;
import edu.isnap.rating.data.HintSet;
import edu.isnap.rating.data.TutorHint.Validity;

public class RunHintRater {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// The threshold for counting hints as valid
		Validity validity = Validity.MultipleTutors;
		// Whether to print verbose debugging information when rating hints
		boolean debug = false;
		// Whether to write the hint ratings to a file
		boolean writeRatingsToFile = true;

		// Create the HintRater object
		HintRater hintRater = new HintRater(validity, debug);
		// If present, rate the isnapF16F17 and itapS16 datasets
		if (hasDataset(HintRater.isnapF16F17Dir(), "isnapF16F17")) {
			hintRater.rateDir(HintRater.isnapF16F17Dir(), RatingConfig.Snap, writeRatingsToFile);
		}
		if (hasDataset(HintRater.itapS16Dir(), "itapS16")) {
			hintRater.rateDir(HintRater.itapS16Dir(), RatingConfig.Python, writeRatingsToFile);
		}
		if (hasDataset(HintRater.blackBoxDir(), "blackBox")) {
			hintRater.rateDir(HintRater.blackBoxDir(), RatingConfig.Java, writeRatingsToFile);
		}
		// You can also rate specific hint algorithms, rather than a whole directory
//		hintRater.rateOneDir(HintRater.isnapF16F17Dir(), "SourceCheck", RatingConfig.Snap, true);
//		hintRater.rateOneDir(HintRater.itapS16Dir(), "SourceCheck", RatingConfig.Python, true);

		// Or just print the hints in a directory to see what they look like
//		printHints(HintRater.isnapF16F17Dir(), "SourceCheck", RatingConfig.Snap);
	}

	protected static void printHints(String dataset, String algorithm, RatingConfig config) {
		try {
			HintSet hints = HintSet.fromFolder(algorithm, config, String.format("%s%s/%s",
					dataset, HintRater.ALGORITHMS_DIR, algorithm));
			GoldStandard standard = GoldStandard.parseSpreadsheet(
					dataset + HintRater.GS_SPREADSHEET);
			hints.printHints(standard);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean hasDataset(String dataset, String name) {
		if (new File(dataset).exists()) return true;
		System.err.printf("Please download the %s dataset. See go.ncsu.edu/hint-quality-data\n",
				name);
		return false;
	}
}