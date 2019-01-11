package main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Cluster {
	
	private String id=null;
	private String id_fix=null;
	private Cluster parent=null;
	private ArrayList<Cluster> children=null;
	private HashMap<String, Sample> samples=null;
	private ArrayList<Cluster> errors=null;
	private HashSet<String> genes = null;
	private int size=0;
	private String branch=null;
	private String sample_group=null;
	private boolean stable_flag=true;

	public Cluster() {
		this.children = new ArrayList<>();
		this.samples = new HashMap<>();
		this.genes = new HashSet<>();
	}
	
	public Cluster(Set<String> sample_ids) {
		this.id = new String("TOP");
		this.samples = new HashMap<>();
		this.children = new ArrayList<>();
		for (String id : sample_ids) {
			this.samples.put(id, new Sample(id, 1.0, 0.0, 0.0, 0.0, 1.0));
		}
	}
	
	public double getAvarage() {
		double out = this.getValueSum();
		out /= samples.size();
		return out;
	}
	
	public double getValueSum() {
		double out = 0.0;
		for (Entry<String, Sample> entry : samples.entrySet()) {
			out += entry.getValue().getValue();
		}
		return out;
	}
	
	public Cluster deepClone(Cluster parent, HashMap<Cluster, Cluster> old_new) {
		Cluster out = new Cluster();
		old_new.put(this, out);
		out.id = this.id;
		out.id_fix = null;
		out.parent = parent;
		out.size = this.size;
		out.genes = this.genes;
		for(Entry<String, Sample> entry : this.samples.entrySet()) {
			out.samples.put(entry.getKey(), entry.getValue().clone());
		}
		for(int i=0; i < this.children.size(); i++) {
			Cluster child = this.children.get(i).deepClone(out, old_new);
			out.children.add(child);
		}
		return out;
	}
	
	public boolean treeEquals(Cluster c) {
		boolean out = true;
		out &= this.id.equals(c.id) && this.children.size() == c.children.size();
		for(int i = 0; out && i < this.children.size(); ++i) {
			Cluster child = this.children.get(i);
			if (child.id.equals(c.children.get(i).id)) {
				out &= child.treeEquals(c.children.get(i));
			}
			else {
				int j = 0;
				for (; j < c.children.size(); ++j) {
					if (child.id.equals(c.children.get(j).id)) {
						out &= child.treeEquals(c.children.get(j));
						break;
					}
				}
				out &= j < c.children.size();
			}
		}
		return out;
	}
	
	public int size() {
		int out = 0;
		out++;
//		System.out.println(this.id);
		for(int i=0; i < this.children.size(); i++) {
			out += this.children.get(i).size();
		}
		return out;
	}

	public void callErrors() {
		DecimalFormat df = new DecimalFormat("#0.0000");
		StringBuffer error_line = new StringBuffer();
		StringBuffer value_line = new StringBuffer();
		StringBuffer nega_line = new StringBuffer();
		error_line.append(this.id);
		error_line.append(" -");
		for (int i = 0; i < this.errors.size(); i++) {
			error_line.append("\t");
			error_line.append(this.errors.get(i).getId());
		}
		System.err.println(error_line);
		for (Entry<String, Sample> entry : this.samples.entrySet()) {
			error_line.setLength(0);
			value_line.setLength(0);
			nega_line.setLength(0);
			String s_id = entry.getKey();
			System.err.printf("%s :\n", s_id);
			Sample s = entry.getValue();
			error_line.append(df.format(s.getError_positive()));
			value_line.append(df.format(s.getValue()));
			nega_line.append(df.format(s.getError_negative()));
			for (int i = 0; i < this.errors.size(); i++) {
				s = this.errors.get(i).getSamples().get(s_id);
				error_line.append("\t");
				value_line.append("\t");
				nega_line.append("\t");
				error_line.append(df.format(s.getError_positive()));
				value_line.append(df.format(s.getValue()));
				nega_line.append(df.format(s.getError_negative()));
			}
			System.err.println(value_line);
			System.err.println(error_line);
			System.err.println(nega_line);
		}
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Cluster getParent() {
		return parent;
	}

	public void setParent(Cluster parent) {
		this.parent = parent;
	}

	public ArrayList<Cluster> getChildren() {
		return children;
	}
	
	public void setChildren(ArrayList<Cluster> children) {
		this.children = children;
	}
	
	public HashMap<String, Sample> getSamples() {
		return samples;
	}

	public void setSamples(HashMap<String, Sample> samples) {
		this.samples = samples;
	}

	public String getId_fix() {
		return id_fix;
	}

	public void setId_fix(String id_fix) {
		this.id_fix = id_fix;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public ArrayList<Cluster> getErrors() {
		return errors;
	}

	public void addError(Cluster error) {
		if (this.errors == null) {
			this.errors = new ArrayList<>();
		}
		if (!this.errors.contains(error))
			this.errors.add(error);
	}
	
	public void setErrors(ArrayList<Cluster> errors) {
		this.errors = errors;
	}
	
	public boolean isStable_flag() {
		return stable_flag;
	}

	public void setStable_flag(boolean stable_flag) {
		this.stable_flag = stable_flag;
	}

	public void addGene(String gene) {
		this.genes.add(gene);
	}
	
	public void setGenes(HashSet<String> genes) {
		this.genes = genes;
	}
	
	public boolean isMonoClone(String from, String to) {
		boolean out = true;
		Sample sf = samples.get(from);
		Sample st = samples.get(to);
		if (st.getAdjust_value() + st.getValue() > 0.05 && sf.getValue() + sf.getAdjust_value() > 0.05) {
			out &= "TOP".equals(id) || "TOP".equals(parent.id) || 
					parent.samples.get(to).getValue() + parent.samples.get(to).getAdjust_value() - st.getValue() - st.getAdjust_value() < 0.05;
		}
		for (int i = 0; out &&i < children.size(); ++i) {
			out &= children.get(i).isMonoClone(from, to);
		}
		return out;
	}
	
	public ArrayList<String> toMergeLine(ArrayList<String> sample_list, double[] to_one, ArrayList<Cluster> used, double div, HashMap<String, HashSet<String>> sample_clusters) {
//		String header = "lab\tcolor\tparent\texclude\tsample\tleaf.of.sample\tis.term\tsample.with.cell.frac.ci"
//				+ "\tsample.group\tsample.group.color\tnum.samples\tleaf.of.sample.count\tclone.cct.combined.p"
//				+ "\tevent\tbranches\tblengths\tsamples.with.nozero.cell.frac\tnode.border.color\tnode.border.width"
//				+ "\tbranch.border.color\tbranch.border.linetype\tbranch.border.width";
		ArrayList<String> lines = new ArrayList<>();
		if ("-1".equals(id_fix)) {
			return lines;
		}
		ArrayList<StringBuffer> line_buffers = new ArrayList<>();
		HashMap<String, Integer> sample_stats = new HashMap<>();
		StringBuffer merge = new StringBuffer();
		merge.append(id_fix);
		merge.append('\t');
		if (used.size() < Method.color.length) {
			if ("0".equals(id_fix)) {
				merge.append("white");
			}
			else {
				merge.append(Method.color[used.size()]);
			}
		}
		else {
			merge.append(Method.color[Method.color.length - 1]);
		}
		merge.append('\t');
		String label = merge.toString();
		int i = 0;
		for (i = 0; i < sample_list.size(); i++) {
			StringBuffer line = new StringBuffer();
			String sample_id = sample_list.get(i);
			line_buffers.add(line);
			line.append(label);
			Sample sample = samples.get(sample_id);
			double value_sum = 0.0;
			double vaf = (sample.getValue() + sample.getAdjust_value()) / 2.0;
			vaf /= to_one[i];
			line.append(vaf);
			line.append('\t');
			int sub_clone = 0;
			for (int j = 0; j < children.size(); j++) {
				Sample s = children.get(j).samples.get(sample_id);
				double s_vaf = s.getAdjust_value() + s.getValue();
				value_sum += s_vaf;
				if (s_vaf >= 0.025) {
					sub_clone++;
				}
			}
			value_sum /= 2.0;
			value_sum /= to_one[i];
			double free = vaf - value_sum;
			sample.setOccupied(value_sum);
			sample.setFree(free);
			if (free > 0.025) {
				HashSet<String> ids = null;
				if (sample_clusters.containsKey(sample_id)) {
					ids = sample_clusters.get(sample_id);
					ids.add(id_fix);
				}
				else {
					ids = new HashSet<>();
					ids.add(id_fix);
					sample_clusters.put(sample_id, ids);
				}
			}
			if (vaf >= 0.025) {
				sample_stats.put(sample_id, 0);
				if (free < 0.01) {
					sample_stats.put(sample_id, sample_stats.get(sample_id) + 1);
				}
				if (parent == null || parent.getSamples().get(sample_id).getFree() < 0.01) {
					sample_stats.put(sample_id, sample_stats.get(sample_id) + 2);
				}
				if (value_sum < 0.01) {
					sample_stats.put(sample_id, sample_stats.get(sample_id) + 4);
				}
			}
			line.append(value_sum);
			line.append('\t');
			line.append(free);
			line.append('\t');
			line.append(sub_clone);
			line.append('\t');
			if (vaf < 0.025) {
				line.append("TRUE");
				line.append('\t');
				line.append('-');
				line.append('\t');
				for (int k = 0; k < 10; k++) {
					line.append("NA");
					line.append('\t');
				}
			}
			else {
				line.append("FALSE");
				line.append('\t');
				line.append('-');
				int index = line.length();
				Cluster p = parent;
				while (p != null && !"-1".equals(p.id_fix)) {
					String ancestor = "#" + p.id_fix + "#";
					line.insert(index, ancestor);
					p = p.parent;
				}
				line.append('\t');
				if (parent == null ) {
					line.append("-1");
				}
				else {
					line.append(parent.id_fix);
				}
				line.append('\t');
				line.append(free);
				line.append('\t');
				line.append(free);
				line.append('\t');
				line.append(free);
				line.append('\t');
				line.append(0.95);
				line.append('\t');
				line.append(0.95);
				line.append('\t');
				line.append(0);
				line.append('\t');
				if (free < 0.01) {
					line.append("TRUE");
				}
				else {
					line.append("FALSE");
				}
				line.append('\t');
				if (parent == null || "-1".equals(parent.id_fix)) {
					line.append("FALSE");
				}
				else {
					line.append("TRUE");
				}
				line.append('\t');
				if (parent == null || parent.getSamples().get(sample_id).getFree() < 0.01) {
					line.append("FALSE");
				}
				else {
					line.append("TRUE");
				}
				line.append('\t');
			}
		}
		if (parent == null ) {
			merge.append("-1");
		}
		else {
			merge.append(parent.id_fix);
		}
		merge.append('\t');
		merge.append("FALSE");
		merge.append('\t');
		int num = 0;
		StringBuffer group = new StringBuffer();
		for (i = 0; i < sample_list.size(); i++) {
			String sample_id = sample_list.get(i);
			if (sample_stats.containsKey(sample_id)) {
				int stat = sample_stats.get(sample_id);
				if (num != 0) {
					group.append(',');
				}
				if ((stat & 1) > 0) {
					group.append('`');
				}
				else if ((stat & 2) > 0) {
					group.append('*');
				}
				group.append(sample_id);
				num++;
			}
		}
		if (group.length() == 0) {
			merge.append("NA");
			sample_group = "NA";
		}
		else {
			sample_group = group.toString();
			merge.append(group);
		}
		merge.append('\t');
		merge.append(num);
		merge.append('\t');
		num = 0;
		StringBuffer temp = new StringBuffer();
		for (i = 0; i < sample_list.size(); i++) {
			String sample_id = sample_list.get(i);
			if (sample_stats.containsKey(sample_id)) {
				int stat = sample_stats.get(sample_id);
				if ((stat & 4) > 0) {
					if (num != 0) {
						temp.append(',');
					}
					temp.append(sample_id);
					num++;
				}
			}
		}
		if (temp.length() == 0) {
			merge.append("NA");
			merge.append('\t');
			merge.append("FALSE");
		}
		else {
			merge.append(temp);
			merge.append('\t');
			merge.append("TRUE");
			temp.setLength(0);
		}
		merge.append('\t');
		merge.append(num);
		merge.append('\t');
		merge.append(sample_group);
		merge.append('\t');
		String[] cols = sample_group.split(",");
		for (i = 0; i < cols.length; i++) {
			if (cols[i].charAt(0) != '`') {
				temp.append(cols[i]);
				temp.append(',');
			}
		}
		if (temp.length() > 0) {
			temp.setLength(temp.length() - 1);
		}
		else {
			temp.append("NA");
		}
		merge.append(temp.toString());
		merge.append('\t');
		merge.append(0);
		merge.append('\t');
		if (this.genes == null || this.genes.size() <= 0) {
			merge.append("NULL");
		}
		else {
			for (String gene : genes) {
				merge.append(gene);
				merge.append(',');
			}
			merge.setLength(merge.length() - 1);
		}
		merge.append('\t');
		if (parent == null || "-1".equals(parent.id_fix)) {
			branch="Y";
		}
		else if ("Y".equals(parent.branch)) {
			if (parent.children.size()==1) {
				branch="0";
			}
			else {
				branch=String.valueOf(parent.children.indexOf(this)+1);
			}
		}
		else {
			if (parent.children.size()==1) {
				branch=parent.branch + "0";
			}
			else {
				branch=parent.branch + String.valueOf(parent.children.indexOf(this)+1);
			}
		}
		merge.append(branch);
		merge.append('\t');
		merge.append((double) (size) / div + 3.0);
		merge.append('\t');
		merge.append(temp);
		merge.append('\t');
		merge.append("black");
		merge.append('\t');
		merge.append(1);
		merge.append('\t');
		merge.append("white");
		merge.append('\t');
		merge.append("solid");
		merge.append('\t');
		merge.append(0.5);
		merge.append('\t');
		sample_group = temp.toString().replaceAll("\\*", "");
		merge.append(sample_group);
		for (i = 0; i < line_buffers.size(); i++) {
			line_buffers.get(i).append(sample_group);
			line_buffers.get(i).append('\t');
			if (stable_flag) {
				line_buffers.get(i).append("black");
			}
			else {
				line_buffers.get(i).append("red");
			}
		}
		if (stable_flag) {
			merge.append('\t');
			merge.append("black");
			merge.append('\t');
			merge.append("Unique");
		}
		else {
			merge.append('\t');
			merge.append("red");
			merge.append('\t');
			merge.append("Multi");
		}
		for (i = 0; i < line_buffers.size(); i++) {
			lines.add(line_buffers.get(i).toString());
		}
		lines.add(merge.toString());
		used.add(this);
		return lines;
	}
}
