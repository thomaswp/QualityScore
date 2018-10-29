package edu.isnap.rating.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isnap.node.ASTNode;
import edu.isnap.node.ASTSnapshot;
import edu.isnap.rating.RatingConfig;
import edu.isnap.util.Spreadsheet;
import edu.isnap.util.map.ListMap;

public class TraceDataset {

	protected final ListMap<String, Trace> traceMap = new ListMap<>(TreeMap::new);

	public final String name;

	public TraceDataset(String name) {
		this.name = name;
	}

	public Collection<String> getAssignmentIDs() {
		return traceMap.keySet();
	}

	public List<Trace> getTraces(String assignmentID) {
		return Collections.unmodifiableList(traceMap.get(assignmentID));
	}

	private void sort() {
		traceMap.values().forEach(Collections::sort);
	}

	public void addTrace(Trace trace) {
		traceMap.add(trace.assignmentID, trace);
		sort();
	}

	public void removeTrace(Trace trace) {
		traceMap.get(trace.assignmentID).remove(trace);
	}

	public void print(RatingConfig config) {
		System.out.println("#### " + name + " ####");
		for (String assignmentID : traceMap.keySet()) {
			System.out.println("------ " + assignmentID + " ------");
			for (Trace trace : traceMap.get(assignmentID)) {
				System.out.println("+++ " + trace.id + " +++");
				for (ASTSnapshot snapshot : trace) {
					System.out.println(snapshot.prettyPrint(true, config));
				}
			}
		}
	}

	protected void addSpreadsheet(String path) throws IOException {
		String lcPath = path.toLowerCase();
		boolean zip = lcPath.endsWith(".gz") || lcPath.endsWith(".gzip");
		InputStream in = new FileInputStream(path);
		if (zip) in = new GZIPInputStream(in);
		CSVParser parser = new CSVParser(new InputStreamReader(in), CSVFormat.DEFAULT.withHeader());
		Trace trace = null;
		for (CSVRecord record : parser) {
			String index = record.get("index");
			if ("0".equals(index)) {
				String assignmentID = record.get("assignmentID");
				String traceID = record.get("traceID");
				trace = new Trace(traceID, assignmentID);
				traceMap.add(assignmentID, trace);
			}

			String json = record.get("code");
			trace.add(ASTSnapshot.parse(json));
		}
		parser.close();
		sort();
	}

	public void writeToFolder(String rootDir) throws FileNotFoundException, JSONException {
		if (!rootDir.endsWith(File.separator)) rootDir += File.separator;
		for (String assignmentID : traceMap.keySet()) {
			for (Trace trace : traceMap.get(assignmentID)) {
				String shortID = trace.id;
				int order = 0;
				if (shortID.length() > 8) shortID = shortID.substring(0, 8);
				for (ASTNode snapshot : trace) {
					JSONObject json = snapshot.toJSON();
					write(
						String.format("%s%s/%s/%05d-%s.json",
							rootDir,
							assignmentID,
							trace.id,
							order++,
							shortID),
						json.toString(2));
				}
			}
		}
	}

	private static void write(String path, String text) throws FileNotFoundException {
		File file = new File(path);
		file.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(file);
		writer.println(text);
		writer.close();
	}

	public void writeToSpreadsheet(String path, boolean zip) throws IOException {
		String lcPath = path.toLowerCase();
		if (zip && !lcPath.endsWith(".gz") && !lcPath.endsWith(".gzip")) path += ".gz";
		new File(path).getParentFile().mkdirs();
		OutputStream out = new FileOutputStream(path);
		if (zip) out = new GZIPOutputStream(out);
		Spreadsheet spreadsheet = new Spreadsheet();
		spreadsheet.beginWrite(out);
		for (String assignmentID : traceMap.keySet()) {
			for (Trace trace : traceMap.get(assignmentID)) {
				int i = 0;
				for (ASTNode node : trace) {
					spreadsheet.newRow();
					spreadsheet.put("assignmentID", trace.assignmentID);
					spreadsheet.put("traceID", trace.id);
					spreadsheet.put("index", i++);

					boolean isCorrect = false;
					String source = "";
					if (node instanceof ASTSnapshot) {
						isCorrect = ((ASTSnapshot) node).isCorrect;
						source = ((ASTSnapshot) node).source;
					}

					spreadsheet.put("isCorrect", isCorrect);
					spreadsheet.put("source", source);
					spreadsheet.put("code", node.toJSON().toString());
				}
			}
		}
		spreadsheet.endWrite();

		out.close();
	}
}
