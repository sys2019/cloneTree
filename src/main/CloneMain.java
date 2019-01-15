package main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class CloneMain {
	
	public static void main (String[] args){
		CliParam in_args = new CliParam(args);
		if (in_args.checkParam()){
			Weka.run(in_args);
		}
	}
	
	/**
	 *	run clusters into tree and test the tree for its robust
	 * @param args the command line parameters
	 * @param in_file cluster result file
	 * @param clu_method the cluster result using
	 */
	public static void run(CliParam args, String in_file, String clu_method) {
		String out_prefix = args.getOut_label();
		boolean std_flag = args.isStd_flag();
		int bootstrap = args.getItera();
		double cluster_up = args.getBootc();
		double remove_up = args.getBoots();
		String clone_file = args.getClone_file();
		String name = args.getSample_name() + "_" + clu_method;// get command line parameters
		
		ArrayList<OriginalCluster> all_origin = null;
		ArrayList<Cluster> all = null;
		if (std_flag) {
			all_origin = FileRW.stdRead(in_file);
		}
		else {
			all_origin = FileRW.highLowRead(in_file);
		}
		all = all_origin.get(0).toCluster(all_origin);
		System.out.println("Total Clone: " + all.size());
		if (all.size() == 0) {
			System.out.println("No Clones");
			return;
		}
		Cluster top = new Cluster(all.get(0).getSamples().keySet());
		all.add(0, top);
		Cluster root = null;
		Sample s = new Sample();
		ArrayList<Cluster> error = new ArrayList<>();
		HashMap<Cluster, ArrayList<Cluster>> child = new HashMap<>();
		HashMap<Cluster, ArrayList<Cluster>> no_child = new HashMap<>();
		boolean stat = true;
		for (String id : all.get(0).getSamples().keySet()) {
			stat &= s.getRelation(error, child, no_child, all, id);
		}
		if (stat) {
			root = FileRW.buildClusterTree(child, no_child, all);
			if (root != null) {
				HashMap<String, Cluster> all_map = new HashMap<>();
				double div = 0.0;
				for (int i=0; i < all.size(); i++) {
					div = Math.max((double)all.get(i).getSize() / 7.0, div);
					all_map.put(all.get(i).getId(), all.get(i));
				}
				root.setId_fix("-1");
				ArrayList<ArrayList<String>> out = new ArrayList<>();
				ArrayList<String> sample_ids = new ArrayList<>();
				sample_ids.addAll(root.getSamples().keySet());
				for (int i = 0; i < root.getSamples().size(); i++) {
					ArrayList<String> temp = new ArrayList<>();
					temp.add("lab	color	vaf	occupied	free	num.subclones	excluded	ancestors	parent	free.mean	free.lower	free.upper	free.confident.level	free.confident.level.non.negative	p.value	is.zero	is.subclone	is.founder	sample.group	sample.group.color");
					out.add(temp);
				}
				ArrayList<String> temp = new ArrayList<>();
				temp.add("lab	color	parent	excluded	sample	num.samples	leaf.of.sample	is.term	leaf.of.sample.count	sample.with.cell.frac.ci	sample.with.nonzero.cell.frac.ci	clone.ccf.combined.p	events	branches	blengths	samples.with.nonzero.cell.frac	node.border.color	node.border.width	branch.border.color	branch.border.linetype	branch.border.width	sample.group	sample.group.color	location.type");
				out.add(temp);
				ArrayList<Cluster> used = new ArrayList<>();
				HashMap<String, HashSet<String>> sample_clusters = new HashMap<>();
				if (root.getChildren().size() == 1) {
					boolean top_flag=false;
					double[] to_one = new double[sample_ids.size()];
					for (int j = 0; j < sample_ids.size(); ++j) {
						s = root.getChildren().get(0).getSamples().get(sample_ids.get(j));
						to_one[j] = s.getValue() + s.getAdjust_value();
						top_flag |= to_one[j]<=0.05;
					}
					if (top_flag) {
						for (int j = 0; j < sample_ids.size(); ++j) {
							to_one[j] = 1.0;
						}
						root.setId_fix("0");
						FileRW.printTree(root, out, sample_ids, to_one, used, div, sample_clusters);
					}
					else {
						for (int i = 0; i < root.getChildren().size(); ++i) {
							Cluster c = root.getChildren().get(i);
							FileRW.printTree(c, out, sample_ids, to_one, used, div, sample_clusters);
						}
					}
				}
				else {
					double[] to_one = new double[sample_ids.size()];
					for (int j = 0; j < sample_ids.size(); ++j) {
						to_one[j] = 1.0;
					}
					root.setId_fix("0");
					FileRW.printTree(root, out, sample_ids, to_one, used, div, sample_clusters);
				}
				sample_ids.add("Merge");
//				Method.callStats(all);
				FileRW.writeTable(out_prefix + "/" + name + "/", sample_ids, out);
				FileRW.notadjustValue(out_prefix + "/" + name + "/" + name, in_file, all_map);
				FileRW.adjustValue(out_prefix + "/" + name + "/" + name, in_file, all_map);
				HashMap<Cluster, Integer> tree_stats = new HashMap<>();
				HashMap<String, Integer> mono_stats = new HashMap<>();
				HashMap<String, String> mono_records = new HashMap<>();
				if (clone_file != null) {
					ArrayList<String> records = FileRW.readFile(clone_file);
					for (int i = 0; i < records.size(); ++i) {
						String[] cols = records.get(i).split("\t");
						if (cols[0].contains(name) && cols[0].charAt(name.length()) > '9') {
							int index = cols[1].indexOf('/');
							String from = cols[1];
							if (index != -1) {
								from = from.substring(0,index);
							}
							from = from.substring(name.length());
							String key = cols[0].substring(name.length()) + "\t" + from;
							mono_stats.put(key, 0);
							mono_records.put(key, records.get(i));
						}
					}
				}
				for (Entry<String, Integer> entry : mono_stats.entrySet()) {
					String[] ids = entry.getKey().split("\t");
					if (root.isMonoClone(ids[1], ids[0])) {
						entry.setValue(1);
					}
				}
				tree_stats.put(root, 1);
				for (int i = bootstrap; i > 1; --i) {
					all = all_origin.get(0).bootStrapCluster(all_origin, cluster_up, remove_up);
					top = new Cluster(all.get(0).getSamples().keySet());
					all.add(0, top);
					Cluster c = null;
					error = new ArrayList<>();
					child = new HashMap<>();
					no_child = new HashMap<>();
					stat = true;
					for (String id : all.get(0).getSamples().keySet()) {
						stat &= s.getRelation(error, child, no_child, all, id);
					}
					if (stat) {
						c = FileRW.buildClusterTree(child, no_child, all);
						if (c != null) {
							boolean same_tree = false;
							for (Entry<Cluster, Integer> entry : tree_stats.entrySet()) {
								if (c.treeEquals(entry.getKey())) {
									same_tree = true;
									entry.setValue(entry.getValue() + 1);
								}
							}
							if (!same_tree) {
								tree_stats.put(c, 1);
							}
							for (Entry<String, Integer> entry : mono_stats.entrySet()) {
								String[] ids = entry.getKey().split("\t");
								if (c.isMonoClone(ids[1], ids[0])) {
									entry.setValue(entry.getValue() + 1);
								}
							}
						}
					}
				}
				ArrayList<String> stats = new ArrayList<>();
				stats.add("TreeNO\tNum\tPossibility");
				DecimalFormat df = new DecimalFormat("#0.0000");
				StringBuffer stat_line = new StringBuffer();
				stat_line.append("0\t");
				stat_line.append(tree_stats.get(root));
				stat_line.append('\t');
				stat_line.append(df.format((double)tree_stats.get(root)/(double)bootstrap));
				tree_stats.remove(root);
				stats.add(stat_line.toString());
				int no = 0;
				for (Entry<Cluster, Integer> entry : tree_stats.entrySet()) {
					stat_line.setLength(0);
					stat_line.append(++no);
					stat_line.append('\t');
					stat_line.append(entry.getValue());
					stat_line.append('\t');
					stat_line.append(df.format((double)entry.getValue()/(double)bootstrap));
					stats.add(stat_line.toString());
				}
				FileRW.fileWrite(out_prefix + "/" + name + "/tree_stat.tsv", stats, false);
				stats.clear();
				for (Entry<String, Integer> entry : mono_stats.entrySet()) {
					stat_line.setLength(0);
					stat_line.append(mono_records.get(entry.getKey()));
					stat_line.append('\t');
					if (mono_records.get(entry.getKey()).contains("Mono")) {
						stat_line.append(df.format((double)entry.getValue()/(double)bootstrap));
					}
					else {
						stat_line.append(df.format(1.0 - (double)entry.getValue()/(double)bootstrap));
					}
					stats.add(stat_line.toString());
				}
				if (stats.size() > 0) {
					FileRW.fileWrite(out_prefix + "/monoclone_stat.tsv", stats, true);
				}
			}
			else {
				System.out.println("No Tree Built");
			}
		}
		else {
			System.out.println(name + " Error: ");
			for (int i=0; i < error.size(); i++) {
				error.get(i).callErrors();
			}
		}
		System.out.println("END");
	}
	
}
