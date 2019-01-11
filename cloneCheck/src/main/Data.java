package main;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

public class Data {

	String sample_id = null;
	Hashtable<String, double[]> ccfs= null;
	int label = -1;
	double min_distance = 100.0;
	String gene = null;
	
	public Data() {
		this.ccfs = new Hashtable<>();
	}
	
	public double log_p(double cell_value) {
		double out = cell_value;
		for (Entry<String, double[]> entry : this.ccfs.entrySet()) {
			out += entry.getValue()[0];
		}
		out = Math.log(out);
		return out;
	}
	
	public double get_Eula_Distance(Data data) {
		double distance = 0.0;
		int dimention = 0;
		for (Entry<String, double[]> entry : this.ccfs.entrySet()) {
			double d = 0;
			if (data.ccfs.containsKey(entry.getKey())) {
				d = Math.abs(data.ccfs.get(entry.getKey())[0] - entry.getValue()[0]);
			}
			else {
				d = entry.getValue()[0];
				System.out.println(entry.getKey() + " not contains " + data.sample_id);
			}
			distance += d * d;
			dimention++;
		}
		distance = Math.sqrt(distance / dimention);
		return distance;
	}
	
	public String ccfsToString() {
		StringBuffer out = new StringBuffer();
		for (Entry<String, double[]> entry : this.ccfs.entrySet()) {
			out.append('\t');
			out.append(entry.getValue());
		}
		return out.toString();
	}
	
	public static void writeCell(ArrayList<Data> data, String out_file) {
		ArrayList<String> out_put = new ArrayList<>();
		out_put.add("mutation_id\tsample_id\tcluster_id\tcellular_prevalence\tcellular_prevalence_low\tcellular_prevalence_high\tvariant_allele_frequency");
		for (int i = 0; i < data.size(); i++) {
			Data data_point = data.get(i);
			for (Entry<String, double[]> entry : data_point.ccfs.entrySet()) {
				if (data_point.label != -1) {
					StringBuffer out_line = new StringBuffer();
					out_line.append(data_point.sample_id);
					out_line.append('\t');
					out_line.append(entry.getKey());
					out_line.append('\t');
					out_line.append(data.get(i).label);
					out_line.append('\t');
					out_line.append(entry.getValue()[0]);
					out_line.append('\t');
					out_line.append(entry.getValue()[1]);
					out_line.append('\t');
					out_line.append(entry.getValue()[2]);
					out_line.append('\t');
					out_line.append(0.5);
					if (data_point.gene != null) {
						out_line.append('\t');
						out_line.append(data_point.gene);
					}
					out_put.add(out_line.toString());
				}
			}
		}
		FileRW.fileWrite(out_file, out_put, false);
	}
	
	public static void writeCluster(ArrayList<Data> data, String out_file) {
		ArrayList<String> out_put = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			StringBuffer out_line = new StringBuffer();
			out_line.append(data.get(i).sample_id);
			out_line.append('\t');
			out_line.append(data.get(i).label);
			out_line.append(data.get(i).ccfsToString());
			out_put.add(out_line.toString());
		}
		FileRW.fileWrite(out_file, out_put, false);
	}
}
