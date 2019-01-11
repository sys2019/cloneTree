package main;

public class CliParam {
	
	private String in_file = null;
	private String out_label=null;
	private int itera=10000;
	private int db_min = 1;
	private int num_cluster = 0;
	private double db_r= 0.05;
	private double db_p= 0.0;
	private String clone_file = null;
	private String out_prefix = null;
	private boolean std_flag = false;
	private int boot = 1000;
	private double bootc = 0.5;
	private double boots = 0.5;

	public CliParam(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String param = args[i];
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
			else if (param.equals("-m")) {
				i++;
				if (i < args.length) {
					db_min = Integer.parseInt(args[i]);
				}
			}
			else if (param.equals("-p")) {
				i++;
				if (i < args.length) {
					db_p = Double.parseDouble(args[i]);
				}
			}
			else if (param.equals("-r")) {
				i++;
				if (i < args.length) {
					db_r = Double.parseDouble(args[i]);
				}
			}
			else if (param.equals("-p")) {
				i++;
				if (i < args.length) {
					db_p = Double.parseDouble(args[i]);
				}
			}
		}
	}
	
	public String getIn_File() {
		return in_file;
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
}
