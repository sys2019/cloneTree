package main;

import java.io.File;

public class CliParam {
	
	private String in_file = null;
	private String out_label=null;
	private int itera=1000;
	private int db_min = 1;
	private int num_cluster = 0;
	private double db_r= 0.05;
	private double db_p= 0.0;
	private String clone_file = null;
	private String cluster_method = "D";
	private String sample_name = "Default";
	private boolean std_flag = false;
	private double bootc = 0.5;
	private double boots = 0.5;
	private boolean effective = true;

	public CliParam(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String param = args[i].toLowerCase();
			if (param.equals("-i")) {
				i++;
				if (i < args.length) {
					in_file = args[i];
				}
			}
			else if (param.equals("-o")) {
				i++;
				if (i < args.length) {
					out_label = args[i];
				}
			}
			else if (param.equals("-s")) {
				i++;
				if (i < args.length) {
					sample_name = args[i];
				}
			}
			else if(param.equals("-mono")) {
				i++;
				if (i < args.length) {
					clone_file=args[i];
				}
			}
			else if(param.equals("-mclu")) {
				i++;
				if (i < args.length) {
					cluster_method=args[i];
				}
			}
			else if (param.equals("-dbm")) {
				i++;
				if (i < args.length) {
					db_min = Integer.parseInt(args[i]);
				}
			}
			else if (param.equals("-n")) {
				i++;
				if (i < args.length) {
					itera = Integer.parseInt(args[i]);
				}
			}
			else if (param.equals("-nclu")) {
				i++;
				if (i < args.length) {
					num_cluster = Integer.parseInt(args[i]);
				}
			}
			else if (param.equals("-dbp")) {
				i++;
				if (i < args.length) {
					db_p = Double.parseDouble(args[i]);
				}
			}
			else if (param.equals("-dbr")) {
				i++;
				if (i < args.length) {
					db_r = Double.parseDouble(args[i]);
				}
			}
			else if(param.equals("-bootc")) {
				i++;
				if (i < args.length) {
					bootc = Double.parseDouble(args[i]);
				}
			}
			else if(param.equals("-boots")) {
				i++;
				if (i < args.length) {
					boots = Double.parseDouble(args[i]);
				}
			}
			else if(param.equals("-std")) {
				std_flag = true;
			}
			else {
				System.err.println("Unkown param: " + args[i]);
				effective = false;
			}
		}
	}
	
	public String getSample_name(){
		return sample_name;
	}
	
	public String getCluster_method() {
		return cluster_method;
	}

	public String getIn_file() {
		return in_file;
	}

	public String getClone_file() {
		return clone_file;
	}

	public boolean isStd_flag() {
		return std_flag;
	}

	public double getBootc() {
		return bootc;
	}

	public double getBoots() {
		return boots;
	}

	public String getOut_label() {
		return out_label;
	}

	public int getItera() {
		return itera;
	}

	public int getDB_min() {
		return db_min;
	}
	
	public int getNum_cluster() {
		return num_cluster;
	}
	
	public double getDB_r() {
		return db_r;
	}
	
	public double getDb_p() {
		return db_p;
	}
	
	public boolean checkParam(){
		if (effective) {
			if (in_file != null){
				File in = new File(in_file);
				if (in.isFile()){
					if (out_label != null){
						return true;
					}
					else {
						System.err.println("No output path£¬ using \"-o path\" to give output path");
					}
				}
				else {
					System.err.println("Input " + in_file + " is not a existing file");
				}
			}
			else {
				System.err.println("No input file£¬ using \"-i file path\" to give input data");
			}
		}
		return false;
	}
}
