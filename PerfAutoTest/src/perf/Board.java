package perf;

import java.util.TreeMap;

public class Board {

	public String desc;
	public String host;
	public String user;
	public int port;
	public String pw;
	public String top_keyword_1;
	public String top_keyword_2;
	
	public String topMessage;
	public String sarMessage;

	// key : TPS
	// value : 性能信息(key : 具体类别, value : 数值)
	public TreeMap<Integer, TreeMap<String, String>> topInfo;
	public TreeMap<Integer, TreeMap<String, String>> sarInfo;
	public TreeMap<Integer, TreeMap<String, String>> iostatInfo;
}
