package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class FileRW {
	public static int threshold = 5;
	public static double min_value = 0.05;
	
	/**
	 * read file with lines and add in list
	 * @param fi input file;
	 * @return a list of file lines
	 */
	public static ArrayList<String> readFile(String fi){
		ArrayList<String> out = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fi));
			String line = null;
			while ((line = reader.readLine()) != null) {
				out.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}
	
	public static ArrayList<OriginalCluster> stdRead(String file_name){
		ArrayList<OriginalCluster> out = new ArrayList<>();
		File fi = new File(file_name);
		BufferedReader reader = null;
		FileRW.printNow("Scaning " + file_name + " at ");
		try {
			reader = new BufferedReader(new FileReader(fi));
	
			String line = null;
			HashMap<String, HashMap<String, ArrayList<Double>>> v_map = new HashMap<>();
			HashMap<String, HashMap<String, ArrayList<Double>>> e_map = new HashMap<>();
			HashMap<String, HashSet<String>> g_map = new HashMap<>();
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				if (cols[0].contains("id")) {
					continue;
				}
				String cluster_id = cols[2];
				String sample_id = cols[1];
				double value = Double.parseDouble(cols[3]);
				double error = Double.parseDouble(cols[4]);
				double frequency = Double.parseDouble(cols[5]);
				if (cols.length > 6) {
					HashSet<String> genes = null;
					if (g_map.containsKey(cluster_id)) {
						genes = g_map.get(cluster_id);
					}
					else {
						genes = new HashSet<>();
						g_map.put(cluster_id, genes);
					}
					genes.add(cols[6]);
				}
				HashMap<String, ArrayList<Double>> sample_value = null;
				HashMap<String, ArrayList<Double>> sample_error = null;
				ArrayList<Double> values = null;
				ArrayList<Double> errors = null;
				if (v_map.containsKey(cluster_id)) {
					sample_value = v_map.get(cluster_id);
					sample_error = e_map.get(cluster_id);
					if (sample_value.containsKey(sample_id)) {
						values = sample_value.get(sample_id);
						errors = sample_error.get(sample_id);
					}
					else {
						values = new ArrayList<>();
						errors = new ArrayList<>();
						sample_value.put(sample_id, values);
						sample_error.put(sample_id, errors);
					}
				}
				else {
					sample_value = new HashMap<>();
					sample_error = new HashMap<>();
					v_map.put(cluster_id, sample_value);
					e_map.put(cluster_id, sample_error);
					values = new ArrayList<>();
					errors = new ArrayList<>();
					sample_value.put(sample_id, values);
					sample_error.put(sample_id, errors);
				}
				if (frequency == 0.0) {
					value = 0.0;
					error = 0.0;
				}
				values.add(value);
				errors.add(error);
			}
			double max_value = 0.0;
			OriginalCluster max_c = null;
			for (Entry<String, HashMap<String, ArrayList<Double>>> entry : v_map.entrySet()) {
				int size = 0;
				double value_sum = 0.0;
				OriginalCluster c = new OriginalCluster();
				for (Entry<String, ArrayList<Double>> sample_entry : entry.getValue().entrySet()) {
					ArrayList<Double> values = sample_entry.getValue();
					ArrayList<Double> errors = e_map.get(entry.getKey()).get(sample_entry.getKey());
					ArrayList<Double> errorsn = new ArrayList<>();
					size = values.size();
					for (int i = 0; i < size; ++i) {
						errorsn.add(-errors.get(i));
					}
					c.addSample(sample_entry.getKey(), values, errorsn, errors);
					double value = Method.median(values);
					if (value < min_value) {
						value = 0.0;
					}
					value_sum += value;
				}
				if (size >= threshold && value_sum >= min_value) {
					c.setSize(size);
					c.setId(entry.getKey());
					c.setGenes(g_map.get(entry.getKey()));
					out.add(c);
				}
				if (value_sum > max_value) {
					max_value = value_sum;
					max_c = c;
				}
			}
			if (!out.contains(max_c) && max_c.getAverage() > 0.9) {
				out.add(max_c);
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
	
	public static ArrayList<OriginalCluster> highLowRead(String file_name){
		ArrayList<OriginalCluster> out = new ArrayList<>();
		File fi = new File(file_name);
		BufferedReader reader = null;
		FileRW.printNow("Scaning " + file_name + " at ");
		try {
			reader = new BufferedReader(new FileReader(fi));

			String line = null;
			HashMap<String, HashMap<String, ArrayList<Double>>> v_map = new HashMap<>();
			HashMap<String, HashMap<String, ArrayList<Double>>> ep_map = new HashMap<>();
			HashMap<String, HashMap<String, ArrayList<Double>>> en_map = new HashMap<>();
			HashMap<String, HashSet<String>> g_map = new HashMap<>();
			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				if (cols[0].contains("id")) {
					continue;
				}
				String cluster_id = cols[2];
				String sample_id = cols[1];
				double value = Double.parseDouble(cols[3]);
				double error_negtive = value - Double.parseDouble(cols[4]);
				double error_positive = Double.parseDouble(cols[5]) - value;
				double frequency = Double.parseDouble(cols[6]);
				if (cols.length > 7) {
					HashSet<String> genes = null;
					if (g_map.containsKey(cluster_id)) {
						genes = g_map.get(cluster_id);
					}
					else {
						genes = new HashSet<>();
						g_map.put(cluster_id, genes);
					}
					genes.add(cols[7]);
				}
				HashMap<String, ArrayList<Double>> sample_value = null;
				HashMap<String, ArrayList<Double>> sample_errorp = null;
				HashMap<String, ArrayList<Double>> sample_errorn = null;
				ArrayList<Double> values = null;
				ArrayList<Double> errorsp = null;
				ArrayList<Double> errorsn = null;
				if (v_map.containsKey(cluster_id)) {
					sample_value = v_map.get(cluster_id);
					sample_errorp = ep_map.get(cluster_id);
					sample_errorn = en_map.get(cluster_id);
					if (sample_value.containsKey(sample_id)) {
						values = sample_value.get(sample_id);
						errorsp = sample_errorp.get(sample_id);
						errorsn = sample_errorn.get(sample_id);
					}
					else {
						values = new ArrayList<>();
						errorsp = new ArrayList<>();
						errorsn = new ArrayList<>();
						sample_value.put(sample_id, values);
						sample_errorp.put(sample_id, errorsp);
						sample_errorn.put(sample_id, errorsn);
					}
				}
				else {
					sample_value = new HashMap<>();
					sample_errorp = new HashMap<>();
					sample_errorn = new HashMap<>();
					v_map.put(cluster_id, sample_value);
					ep_map.put(cluster_id, sample_errorp);
					en_map.put(cluster_id, sample_errorn);
					values = new ArrayList<>();
					errorsp = new ArrayList<>();
					errorsn = new ArrayList<>();
					sample_value.put(sample_id, values);
					sample_errorp.put(sample_id, errorsp);
					sample_errorn.put(sample_id, errorsn);
				}
				if (frequency == 0.0) {
					value = 0.0;
					error_positive = 0.0;
					error_negtive = 0.0;
				}
				values.add(value);
				errorsp.add(error_positive);
				errorsn.add(error_negtive);
			}
			double max_value = 0.0;
			OriginalCluster max_c = null;
			for (Entry<String, HashMap<String, ArrayList<Double>>> entry : v_map.entrySet()) {
				double value_sum = 0.0;
				int size = 0;
				OriginalCluster c = new OriginalCluster();
				for (Entry<String, ArrayList<Double>> sample_entry : entry.getValue().entrySet()) {
					ArrayList<Double> values = sample_entry.getValue();
					ArrayList<Double> errorsp = ep_map.get(entry.getKey()).get(sample_entry.getKey());
					ArrayList<Double> errorsn = en_map.get(entry.getKey()).get(sample_entry.getKey());
					size = values.size();
					double value = Method.median(values);
					if (value < min_value) {
						value = 0.0;
					}
					value_sum += value;
					c.addSample(sample_entry.getKey(), values, errorsn, errorsp);
				}
				if (size >= threshold && value_sum >= min_value) {
					c.setSize(size);
					c.setId(entry.getKey());
					c.setGenes(g_map.get(entry.getKey()));
					out.add(c);
				}
				if (value_sum > max_value) {
					max_value = value_sum;
					c.setSize(size);
					c.setId(entry.getKey());
					c.setGenes(g_map.get(entry.getKey()));
					max_c = c;
				}
			}
			if (!out.contains(max_c) && max_c.getAverage() > 0.9) {
				out.add(max_c);
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
	
	public static Cluster buildClusterTree(HashMap<Cluster, ArrayList<Cluster>> child, HashMap<Cluster, ArrayList<Cluster>> no_child, ArrayList<Cluster> all) {
		Cluster root = all.get(0);
		ArrayList<Cluster> left = new ArrayList<>();
		left.addAll(all);
		HashMap<Cluster, Cluster> parent = new HashMap<>();
		for (int i = 0; i < child.get(root).size(); i++) {
			Cluster the_cluster = child.get(root).get(i);
			if (child.containsKey(the_cluster)) {
				child.get(the_cluster).remove(root);
				for (int j = 0; j < child.get(the_cluster).size(); j++) {
					Cluster the_child = child.get(the_cluster).get(j);
					if (child.containsKey(the_child) && child.get(the_child).contains(the_cluster)) {
						if (the_cluster.getAvarage() > the_child.getAvarage()) {
							child.get(the_child).remove(the_cluster);
						}
						else {
							child.get(the_cluster).remove(j);
							j--;
						}
					}
				}
			}
		}
		for (Entry<Cluster, ArrayList<Cluster>> entry : child.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				Cluster the_cluster = entry.getValue().get(i);
				if (parent.containsKey(the_cluster)) {
					if (!entry.getValue().contains(parent.get(the_cluster))) {
						parent.put(the_cluster, entry.getKey());
					}
				}
				else {
					parent.put(the_cluster, entry.getKey());
				}
			}
		}
		
		buildClusterTree(root, parent);
		cleanClusterTree(root, left);
		HashMap<Cluster, ArrayList<Cluster>> parents = Method.possibleParent(left, all, no_child);
		root = expandClusterTree(root, parents, left);
		if (root != null)
			root = addAllInTree(root, left, parents, all);
		if (root != null)
			Method.changeCID(root, new int[] {0});
		return root;
	}
	
	private static void buildClusterTree(Cluster root, HashMap<Cluster, Cluster> parent) {
		for (Entry<Cluster, Cluster> entry : parent.entrySet()) {
			entry.getKey().setParent(entry.getValue());
			entry.getValue().getChildren().add(entry.getKey());
		}
	}
	
	private static void cleanClusterTree(Cluster root, ArrayList<Cluster> left) {
		left.remove(root);
		for (int i = 0; i < root.getChildren().size(); i++) {
			Cluster parent = root.getChildren().get(i);
			if (parent.getChildren().size() == 0) {
				root.getChildren().remove(i);
				i--;
			}
			else {
				cleanClusterTree(parent, left);
			}
		}
	}
	
	public static Cluster expandClusterTree(Cluster root, HashMap<Cluster, ArrayList<Cluster>> parents, ArrayList<Cluster> left) {
		for (Entry<Cluster, ArrayList<Cluster>> entry : parents.entrySet()) {
			switch (entry.getValue().size()) {
			case 0:
				entry.getKey().setParent(root);
				root.getChildren().add(entry.getKey());
				left.remove(entry.getKey());
				break;
			case 1:
				entry.getKey().setParent(entry.getValue().get(0));
				entry.getValue().get(0).getChildren().add(entry.getKey());
				left.remove(entry.getKey());
				break;
			default:
				break;
			}
		}
		return root;
	}
	
	public static Cluster addAllInTree(Cluster root, ArrayList<Cluster> left, HashMap<Cluster, ArrayList<Cluster>> parents, ArrayList<Cluster> all) {
		Cluster out = root;
		double max = -1000.0;
		int total = 1;
		int suc_tree = 0;
		HashMap<Cluster, Cluster> old_new = new HashMap<>();
		ArrayList<Integer> choose = new ArrayList<>();
		ArrayList<Cluster> new_all = new ArrayList<>();
		for (int i = 0; i < left.size(); i++) {
			if (parents.containsKey(left.get(i))) {
				int to_choose = parents.get(left.get(i)).size();
				choose.add(to_choose);
				total *= to_choose;
			}
			else {
				System.out.println("Cluster " + left.get(i).getId() + " can not be added in tree");
				out = null;
				return out;
			}
		}
		for (int i=0; i < total; i++) {
			Cluster root_clone = root.deepClone(null, old_new);
			for (int j=0; j < left.size(); j++) {
				left.get(j).deepClone(null, old_new);
			}
			for (int j=0; j < left.size(); j++) {
				Cluster clone = old_new.get(left.get(j));
				int id = i;
				for (int k=0; k < j; k++) {
					 id /= choose.get(k);
				}
				id %= choose.get(j);
				Cluster parent = old_new.get(parents.get(left.get(j)).get(id));
				if (parent == null) {
					System.out.println(old_new.size() + "\t" + left.size());
					System.out.println(left.get(j).getId());
				}
				parent.getChildren().add(clone);
				clone.setParent(parent);
			}
			if (root_clone.size() == all.size()) {
				double value = addThisInTree(root_clone);
				if (value > -1000.0) {
					suc_tree++;
				}
				if (value > max) {
					max = value;
					out = root_clone;
					new_all.clear();
					for (int j=0; j < all.size(); j++) {
						new_all.add(old_new.get(all.get(j)));
					}
				}
				else if (value == max) {
					stableCluster(out, root_clone);
				}
			}
		}
		if (suc_tree > 0) {
			for (int j=0; j < all.size(); j++) {
				all.set(j, new_all.get(j));
			}
		}
		else {
			out = null;
		}
		System.out.println("Total Tree Possible: " + suc_tree);
		return out;
	}
	
	public static double addThisInTree(Cluster root) {
		double out = 0.0;
		for (int i = 0; i < root.getChildren().size(); i++) {
			Cluster child = root.getChildren().get(i);
			out += addThisInTree(child);
			out += adjustSample(root, null);
		}
		return out;
	}
	
	public static boolean addInTree(Cluster root, ArrayList<Cluster> left, HashMap<Cluster, ArrayList<Cluster>> parents) {
		boolean out = true;
		for (int i = 0; i < left.size(); i++) {
			if (parents.containsKey(left.get(i))) {
				for (int j=0; j < parents.get(left.get(i)).size(); j++) {
					out = true;
					Cluster parent  = parents.get(left.get(i)).get(j);
					for (String key : parent.getSamples().keySet()) {
						ArrayList<Sample> s = new ArrayList<>();
						s.add(left.get(i).getSamples().get(key));
						for(int k=0; k < parent.getChildren().size(); k++) {
							s.add(parent.getChildren().get(k).getSamples().get(key));
						}
//						out &= adjustSample(parent.getSamples().get(key), s) >= 0.0;
					}
					if (out) {
						parent.getChildren().add(left.get(i));
						left.get(i).setParent(parent);
						left.remove(i);
						i--;
						break;
					}
				}
			}
		}
		return out;
	}
	
	public static void adjustTree(Cluster root) {
		for (int i = 0; i < root.getChildren().size(); i++) {
			Cluster c = root.getChildren().get(i);
			for (Entry<String, Sample> entry : root.getSamples().entrySet()) {
				Sample s = entry.getValue();
				Sample cs = c.getSamples().get(entry.getKey());
				if (s.getValue() + s.getAdjust_value() < cs.getAdjust_value() + cs.getValue() + 0.01) {
					cs.setAdjust_value(s.getValue() + s.getAdjust_value() - 0.01 - cs.getValue());
				}
			}
			adjustTree(c);
		}
	}
	
	private static double adjustSample(Cluster parent, Cluster child) {
		double out = 0.0;
		for (Entry<String, Sample> entry : parent.getSamples().entrySet()) {
			Sample top = entry.getValue();
			ArrayList<Sample> follow = new ArrayList<>();
			for (int i=0; i < parent.getChildren().size(); i++) {
				follow.add(parent.getChildren().get(i).getSamples().get(entry.getKey()));
			}
			if (child != null) {
				follow.add(child.getSamples().get(entry.getKey()));
			}
			double value_sum = 0.0;
			double error_sum = 0.0;
			double adjust_sum = 0.0;
			for (int i=0; i < follow.size(); i++) {
				value_sum += follow.get(i).getValue();
				error_sum += follow.get(i).getError_negative();
				adjust_sum += follow.get(i).getAdjust_value();
			}
			if(top.getAdjust_value() + top.getValue() < value_sum + adjust_sum) {
				if (top.getAdjust_value() + top.getValue() + top.getError_positive() < value_sum + adjust_sum + error_sum) {
					out -= 1000.0;
				}
				else {
					double to_adjust = value_sum + adjust_sum - top.getValue() - top.getAdjust_value();
					out -= to_adjust;
					for (int i=0; i < follow.size(); i++) {
						double this_adjust = to_adjust * follow.get(i).getError_negative() / (top.getError_positive() - error_sum);
//						out += adjustSample(entry.getKey(), parent.getChildren().get(i), this_adjust);
						follow.get(i).setAdjust_value(follow.get(i).getAdjust_value() + this_adjust);
						out += adjustSample(entry.getKey(), parent.getChildren().get(i));
					}
					top.setAdjust_value(top.getAdjust_value() + to_adjust * top.getError_positive() / (top.getError_positive() - error_sum));
					top.setError_negative(Math.max(top.getError_negative() - top.getAdjust_value(), error_sum  - to_adjust * error_sum / (top.getError_positive() - error_sum)));
					top.setError_positive(top.getError_positive() - top.getAdjust_value());
				}
			}
		}
		return out;
	}
	
//	private static double adjustSample(String id, Cluster c, double to_adjust) {
//		double out = 0.0;
//		double error_sum = 0.0;
//		for (int i=0; i < c.getChildren().size(); i++) {
//			Cluster child = c.getChildren().get(i);
//			Sample s = child.getSamples().get(id);
//			error_sum += s.getError_negative();
//		}
//		for (int i=0; i < c.getChildren().size(); i++) {
//			Cluster child = c.getChildren().get(i);
//			Sample s = child.getSamples().get(id);
//			double this_adjust = to_adjust * s.getError_negative() / error_sum;
//			if (s.getAdjust_value() > 0.0) {
//				out += adjustSample(id, child, this_adjust);
//			}
//			s.setAdjust_value(s.getAdjust_value() + this_adjust);
//		}
//		return out;
//	}
	
	private static double adjustSample(String id, Cluster parent) {
		double out = 0.0;
		Sample top = parent.getSamples().get(id);
		ArrayList<Sample> follow = new ArrayList<>();
		for (int i=0; i < parent.getChildren().size(); i++) {
			follow.add(parent.getChildren().get(i).getSamples().get(id));
		}
		double value_sum = 0.0;
		double error_sum = 0.0;
		double adjust_sum = 0.0;
		for (int i=0; i < follow.size(); i++) {
			value_sum += follow.get(i).getValue();
			error_sum += follow.get(i).getError_negative();
			adjust_sum += follow.get(i).getAdjust_value();
		}
		if(top.getAdjust_value() + top.getValue() < value_sum + adjust_sum) {
			if (top.getAdjust_value() + top.getValue() < value_sum + adjust_sum + error_sum) {
				out -= 1000.0;
			}
			else {
				double to_adjust = value_sum + adjust_sum - top.getValue() - top.getAdjust_value();
				out -= to_adjust;
				for (int i=0; i < follow.size(); i++) {
					double this_adjust = to_adjust * follow.get(i).getError_negative() / (-error_sum);
					follow.get(i).setAdjust_value(follow.get(i).getAdjust_value() + this_adjust);
					out += adjustSample(id, parent.getChildren().get(i));
				}
			}
		}
		return out;
	}
	
	public static void adjustValue(String out_prefix, String in, HashMap<String, Cluster> clusters) {
		String out = out_prefix + "_fixed" + threshold + in.substring(in.lastIndexOf('.'));
		ArrayList<String> out_list = new ArrayList<>();
		File fi = new File(in);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fi));
			String line = reader.readLine();
			out_list.add(line);
			while((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				if (clusters.containsKey(cols[2])) {
					Cluster the_cluster = clusters.get(cols[2]);
					cols[2] = the_cluster.getId_fix();
					Sample sample = the_cluster.getSamples().get(cols[1]);
					double value = Double.parseDouble(cols[3]);
					value += sample.getAdjust_value();
					if (value < 0 || Double.parseDouble(cols[5]) == 0.0 || sample.getAdjust_value() + sample.getValue() <= 0.0) {
						cols[3] = String.valueOf(0.0);
					}
					else {
						cols[3] = String.valueOf(value);
					}
					StringBuilder sb = new StringBuilder();
					sb.append(cols[0]);
					for (int i=1; i < cols.length; i++) {
						sb.append('\t');
						sb.append(cols[i]);
					}
					out_list.add(sb.toString());
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
		fileWrite(out, out_list, false);
	}
	
	public static void notadjustValue(String out_prefix, String in, HashMap<String, Cluster> clusters) {
		String out = out_prefix + "_" + threshold + in.substring(in.lastIndexOf('.'));
		ArrayList<String> out_list = new ArrayList<>();
		File fi = new File(in);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fi));
			String line = reader.readLine();
			out_list.add(line);
			while((line = reader.readLine()) != null) {
				String[] cols = line.split("\t");
				if (clusters.containsKey(cols[2])) {
					Cluster the_cluster = clusters.get(cols[2]);
					cols[2] = the_cluster.getId_fix();
					StringBuilder sb = new StringBuilder();
					sb.append(cols[0]);
					for (int i=1; i < cols.length; i++) {
						sb.append('\t');
						sb.append(cols[i]);
					}
					out_list.add(sb.toString());
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
		fileWrite(out, out_list, false);
	}
	
	public static void stableCluster(Cluster out, Cluster same) {
		for (int i = 0; i < out.getChildren().size(); i++) {
			boolean stable = false;
			Cluster out_c = out.getChildren().get(i);
			if (same != null && same.getChildren() != null) {
				for (int j = 0; j < same.getChildren().size(); j++) {
					Cluster same_c = same.getChildren().get(j);
					if (out_c.getId().equals(same_c.getId())) {
						stableCluster(out_c, same_c);
						stable = true;
						break;
					}
				}
			}
			if (!stable) {
				out_c.setStable_flag(false);
				stableCluster(out_c, null);
			}
		}
	}
	
	public static void printTree(Cluster root, ArrayList<ArrayList<String>> out, ArrayList<String> samples, double[] to_one, ArrayList<Cluster> used, double div, HashMap<String, HashSet<String>> sample_clusters) {
		ArrayList<String> lines = root.toMergeLine(samples, to_one, used, div, sample_clusters);
		for (int j = 0; j < lines.size() && j < out.size(); j++) {
			out.get(j).add(lines.get(j));
		}
		for (int i = 0; i < root.getChildren().size(); i++) {
			Cluster c = root.getChildren().get(i);
			printTree(c, out, samples, to_one, used, div, sample_clusters);
		}
		
	}
	
	public static void fileWrite(String out, ArrayList<String> out_list, boolean append_flag) {
		File fo = new File(out);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fo, append_flag));
			for (int i=0; i < out_list.size(); i++) {
				writer.write(out_list.get(i));
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			if (writer != null){
				try{
					writer.close();
				}
				catch(IOException e1){
				}
			}
		}
	}
	
	public static void writeTable(String out, ArrayList<String> out_ids, ArrayList<ArrayList<String>> out_table) {
		File fo = new File(out);
		if(!fo.exists()) {
			fo.mkdirs();
		}
		for (int i = 0; i < out_ids.size(); i++) {
			fo = new File(out + out_ids.get(i) + ".txt");
			BufferedWriter writer = null;
			try {
				ArrayList<String> out_list = out_table.get(i);
//				writer = new BufferedWriter(new FileWriter(fo));
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fo), "GBK"));
				for (int j=0; j < out_list.size(); j++) {
					writer.write(out_list.get(j));
					writer.newLine();
				}
				writer.flush();
				writer.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			finally{
				if (writer != null){
					try{
						writer.close();
					}
					catch(IOException e1){
					}
				}
			}
		}
	}
	
	/**
	 * print some tips with time
	 * @param prefix the tip of time prefix
	 */
	public static void printNow(String prefix) {
		Date time = new Date();
		System.out.printf("%s%tF %tT\n", prefix, time, time);
	}
	
}
