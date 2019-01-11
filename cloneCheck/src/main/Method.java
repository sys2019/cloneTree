package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;

public class Method {
	
	public final static String[] color= {"#cccccc","#a6cee3","#b2df8a",
			"#cab2d6","#ff99ff","#fdbf6f","#fb9a99","#bbbb77","#cf8d30",
			"#41ae76","#ff7f00","#3de4c5","#ff1aff","#9933ff","#3690c0",
			"#8c510a","#666633","#54278f","#e6e600","#e31a1c","#00cc00",
			"#0000cc","#252525","#fccde5","#d9d9d9","#f0ecd7","#ffffb3",
//			"#b3b3b3","#999999","#bf812d","#aaaa55","#c51b7d","#ef6548","#33a02c","#3f007d","#1f78b4","#ffcc00","#fca27e","#fb8072",
			"#ffff00"};
	
	public static double median(ArrayList<Double> list) {
		ArrayList<Double> temp = new ArrayList<>();
		temp.addAll(list);
		Collections.sort(temp);
		if (temp.size() % 2 == 0) {
			return (temp.get(temp.size()/2) + temp.get(temp.size() / 2 - 1)) / 2;
		}
		else {
			return temp.get(temp.size() / 2);
		}
	}
	
	public static <T> void getNoneRepeat(ArrayList<T> list, int count, HashSet<T> out) {
		if (list.size() > count) {
			while(out.size() < count) {
				out.add(list.get(Method.randIntReach(0, list.size() - 1)));
			}
		}
		else if (list.size() == count) {
			out.addAll(list);
		}
		else {
			System.out.println("Warning: Not enough elements for picking");
		}
	}
	
	public static int randIntReach(int low, int high) {
		int out = 0;
		if(high >= low) {
			out = (int) (Math.random() * (high - low + 1)) + low;
		}
		else {
			System.out.println("Warning: " + low + " is greater than " + high);
		}
		return out;
	}
	
	public static double average(ArrayList<Double> list){
		double out = 0;
		for (int i = 0; i < list.size(); i++) {
			out += list.get(i);
		}
		return out/list.size();
	}
	
	public static double[] getError(ArrayList<Double> value, ArrayList<Double> error, double median){
		double[] out = {0,0};
		double max = value.get(0);
		double min = value.get(0);
		for (int i = 1; i < value.size(); i++) {
			if (value.get(i) > max) {
				max = value.get(i);
			}
			else if (value.get(i) < min){
				min = value.get(i);
			}
		}
		for (int i = 0; i < value.size(); i++) {
//			double distance = value.get(i) - median;
//			if (distance >= 0) {
//				out[0] += 2 * (0.75 - 0.25 * distance/(max - median)) * error.get(i) / value.size();
//			}
//			else {
//				out[1] -= 2 * (0.75 - 0.25 * distance/(min - median)) * error.get(i) / value.size();
//			}
			out[0] += error.get(i) * error.get(i);
		}
		out[0] /= value.size();
		out[0] = Math.sqrt(out[0]);
//		out[0] *= 3;
		out[1] = - out[0];
		if (out[0] + median > 1.0) {
			out[0] = 1.0 - median;
		}
		if (out[1] + median < 0.0) {
			out[1] = 0.0 - median;
		}
		return out;
	}
	
	public static double setAdjust(HashMap<String, Cluster> all, String cluster, String sample, double value) {
		double out = all.get(cluster).getSamples().get(sample).getAdjust_value();
		all.get(cluster).getSamples().get(sample).setAdjust_value(value);
		return out;
	}
	
	public static HashMap<Cluster, ArrayList<Cluster>> possibleParent(ArrayList<Cluster> left, ArrayList<Cluster> all, HashMap<Cluster, ArrayList<Cluster>> no_child) {
		HashMap<Cluster, ArrayList<Cluster>> out = new HashMap<>();
		ArrayList<Integer> parents = new ArrayList<>();
		for (int i=0; i < left.size(); i++) {
			Cluster key = left.get(i);
			ArrayList<Cluster> value = new ArrayList<>();
			value.addAll(all);
			Cluster parent = key.getParent();
			while(parent != null && parent.getParent() != null) {
				parent = parent.getParent();
				value.remove(parent);
				if (parent == all.get(0)) {
					break;
				}
			}
			if (no_child.containsKey(key)) {
				value.removeAll(no_child.get(key));
			}
			value.remove(key);
			out.put(key, value);
			parents.add(value.size());
		}
		ArrayList<Integer> sorted = new ArrayList<>();
		for (int i=0; i < parents.size(); i++) {
			int j = 0;
			for (; j < sorted.size(); j++) {
				if (sorted.get(j) > parents.get(i)) {
					break;
				}
			}
			sorted.add(j, parents.get(i));
			left.add(j, left.get(i));
			left.remove(i + 1);
		}
		return out;
	}
	
	public static void changeCID(Cluster root, int[] i){
		if(root != null) {
			root.setId_fix(String.valueOf(i[0]));
			i[0]++;
			if (root.getChildren() != null) {
				for (int j=0; j < root.getChildren().size(); j++) {
					changeCID(root.getChildren().get(j), i);
				}
			}
		}
		return;
	}
	
	public static void callStats(ArrayList<Cluster> c_list) {
		if (c_list == null || c_list.size() == 0) {
			System.err.println("No stat to print");
			return;
		}
		DecimalFormat df = new DecimalFormat("#0.0000");
//		StringBuffer error_line = new StringBuffer();
		StringBuffer value_line = new StringBuffer();
//		StringBuffer nega_line = new StringBuffer();
		for (int i = 0; i < c_list.size(); i++) {
			value_line.append("\t");
			value_line.append(c_list.get(i).getId_fix());
		}
		System.out.println(value_line);
		value_line.setLength(0);
		value_line.append("parent");
		for (int i = 0; i < c_list.size(); i++) {
			value_line.append('\t');
			if (c_list.get(i).getParent() != null) {
				value_line.append(c_list.get(i).getParent().getId_fix());
			}
			else {
				value_line.append("null");
			}
		}
		System.out.println(value_line);
		for (Entry<String, Sample> entry : c_list.get(0).getSamples().entrySet()) {
//			error_line.setLength(0);
			value_line.setLength(0);
//			nega_line.setLength(0);
			String s_id = entry.getKey();
			value_line.append(s_id);
			for (int i = 0; i < c_list.size(); i++) {
				Sample s = c_list.get(i).getSamples().get(s_id);
//				error_line.append("\t");
				value_line.append("\t");
//				nega_line.append("\t");
//				error_line.append(df.format(s.getError_positive()));
				value_line.append(df.format(s.getValue() + s.getAdjust_value()));
//				nega_line.append(df.format(s.getError_negative()));
			}
			System.out.println(value_line);
//			System.err.println(error_line);
//			System.err.println(nega_line);
		}
	}
	
	public static ArrayList<Data> newmatRead(String fileName) {
		ArrayList<Data> out = new ArrayList<>();
		File fi = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fi));
			String line = null;
			line = reader.readLine();
			String[] samples = line.split("\t");
			int gene_col = samples.length - 1;
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				Data data = new Data();
				data.sample_id = cols[0];
				for (int i = 1; i < gene_col; i+=3) {
					double[] value = new double[3];
					value[1] = Double.parseDouble(cols[i + 1]);
					value[2] = Double.parseDouble(cols[i + 2]);
					if (!"NA".equals(cols[i])) {
						value[0] = Double.parseDouble(cols[i]);
					}
					else {
						value[0] = value[1] + value[2] / 2.0;
					}
					value[1] = Math.min(value[0], value[1]);
					value[2] = Math.max(value[0], value[2]);
					data.ccfs.put(samples[i], value);
				}
				if (cols.length > gene_col) {
					data.gene = cols[gene_col];
				}
				out.add(data);
			}
			reader.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			if (reader != null){
				try{
					reader.close();
				}
				catch(IOException e1){
				}
			}
		}
		return out;
	}
	
	public static ArrayList<Data> matRead(String fileName) {
		ArrayList<Data> out = new ArrayList<>();
		File fi = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fi));
			String line = null;
			line = reader.readLine();
			String[] samples = line.split("\t");
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				Data data = new Data();
				data.sample_id = cols[0];
				for (int i = 1; i < cols.length; i++) {
					double[] value = new double[3];
					value[0] = Double.parseDouble(cols[i]);
					data.ccfs.put(samples[i], value);
				}
				out.add(data);
			}
			reader.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			if (reader != null){
				try{
					reader.close();
				}
				catch(IOException e1){
				}
			}
		}
		return out;
	}
	
	public static void devRead(ArrayList<String> fileName, ArrayList<Data> data) {
		HashMap<String, Hashtable<String, double[]>> map = new HashMap<>();
		for (int i = 0; i < fileName.size(); i++) {
			File fi = new File(fileName.get(i));
			String ccf_id = fileName.get(i).substring(fileName.get(i).lastIndexOf('/') + 1, fileName.get(i).indexOf(".ccf"));
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(fi));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] cols = line.split("\t");
					if (map.containsKey(cols[0])) {
						Hashtable<String, double[]> dev = map.get(cols[0]);
						if (dev.containsKey(ccf_id)) {
							dev.get(ccf_id)[0] = Double.parseDouble(cols[2]);
							dev.get(ccf_id)[1] = Double.parseDouble(cols[3]);
						}
						else {
							double[] value = new double[2];
							value[0] = Double.parseDouble(cols[2]);
							value[1] = Double.parseDouble(cols[3]);
							dev.put(ccf_id, value);
						}	
					}
					else {
						String sample_id = cols[0];
						Hashtable<String, double[]> dev_table = new Hashtable<>(); 
						double[] value = new double[2];
						value[0] = Double.parseDouble(cols[2]);
						value[1] = Double.parseDouble(cols[3]);
						dev_table.put(ccf_id, value);
						map.put(sample_id, dev_table);
					}
				}
				reader.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			finally{
				if (reader != null){
					try{
						reader.close();
					}
					catch(IOException e1){
					}
				}
			}
		}
		for (int i = 0; i < data.size(); i++) {
			Data data_point = data.get(i);
			if (map.containsKey(data_point.sample_id) && map.get(data_point.sample_id).size() == data_point.ccfs.size()) {
				for (Entry<String, double[]> entry : data_point.ccfs.entrySet()) {
					double[] value = map.get(data_point.sample_id).get(entry.getKey());
					entry.getValue()[1] = value[0];
					entry.getValue()[2] = value[1];
				}
			}
			else {
				System.out.println(data_point.sample_id + " wrong with ccfs and devs");
			}
		}
	}
	
	public static ArrayList<Data> ccfRead(ArrayList<String> fileName) {
		ArrayList<Data> out = new ArrayList<>();
		HashMap<String, Data> map = new HashMap<>();
		for (int i = 0; i < fileName.size(); i++) {
			File fi = new File(fileName.get(i));
			String ccf_id = fileName.get(i).substring(fileName.get(i).lastIndexOf('/') + 1, fileName.get(i).indexOf(".ccf"));
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(fi));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] cols = line.split("\t");
					if (map.containsKey(cols[0])) {
						double[] value = new double[3];
						value[0] = Double.parseDouble(cols[1]);
						map.get(cols[0]).ccfs.put(ccf_id,value);
					}
					else {
						Data data = new Data();
						data.sample_id = cols[0];
						double[] value = new double[3];
						value[0] = Double.parseDouble(cols[1]);
						data.ccfs.put(ccf_id,value);
						map.put(cols[0], data);
					}
				}
				reader.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			finally{
				if (reader != null){
					try{
						reader.close();
					}
					catch(IOException e1){
					}
				}
			}
		}
		out.addAll(map.values());
		return out;
	}
}
