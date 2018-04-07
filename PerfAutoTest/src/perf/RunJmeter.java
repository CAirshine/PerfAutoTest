package perf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 执行Jmeter脚本 采集性能信息使用top、sar、iostat
 */
public class RunJmeter {

	public static boolean runFlag = false;
	public static boolean isCI = false;

	private static Logger logger = Logger.getLogger(RunJmeter.class);

	public static ArrayList<Thread> threads;
	public static ArrayList<Session> sessions;

	public Interface interfaceName;

	public TreeMap<Integer, Board> boards;

	public String scriptName;
	public File jmeterFile;
	public BufferedWriter writer;
	public int seconds;

	public CountDownLatch latch;

	public String jmeterMessage = "";

	public String trueORfalse = "true";

	/**
	 * 初始化RunJmeter
	 */
	public RunJmeter(Interface interfaceName) {

		// 初始化
		try {

			this.interfaceName = interfaceName;

			RunJmeter.threads = new ArrayList<Thread>();
			RunJmeter.sessions = new ArrayList<Session>();

			boards = interfaceName.boards;

			this.scriptName = interfaceName.script;
			jmeterFile = new File(ConfigUtils.getCongByName("jmxPath") + "/" + scriptName + ".jmx");
			if (!jmeterFile.exists()) {
				logger.error("找不到Jmeter脚本");
				throw new RuntimeException("找不到Jmeter脚本");
			}

			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(ConfigUtils.rootPath + "/logs/" + scriptName + ".log")), "utf-8"));
			seconds = Integer.parseInt(ConfigUtils.getCongByName("seconds"));
		} catch (Exception e) {
			logger.error("初始化RunJmeter异常: " + e.getMessage());
			throw new RuntimeException("初始化RunJmeter异常: " + e.getMessage());
		}

	}

	
	/**
	 * 线程安全的写日志方法
	 */
	public synchronized void logWriter(String s) {
		try {
			writer.write(s + "\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * main函数
	 */
	public void start() {

		RunJmeter.runFlag = true;
		Thread tmpThread;

		try {
			for (int tps = 500; tps <= 10000; tps = tps + 500) {

				System.out.println("当前TPS : " + tps + " - " + scriptName);

				latch = new CountDownLatch(boards.size() * 2 + 1);
				final int tpsTmp = tps;

				// 多线程远程执行命令
				tmpThread = new Thread() {
					@Override
					public void run() {
						jmeterMessage = runScript(tpsTmp);
						latch.countDown();
					}
				};
				threads.add(tmpThread);
				tmpThread.start();

				for (Entry<Integer, Board> boardEntry : boards.entrySet()) {
					final Board boardTmp = boardEntry.getValue();
					tmpThread = new Thread() {
						@Override
						public void run() {
							boardTmp.topMessage = runTOP(boardTmp);
							latch.countDown();
						}
					};
					threads.add(tmpThread);
					tmpThread.start();

					tmpThread = new Thread() {
						@Override
						public void run() {
							boardTmp.sarMessage = runSAR(boardTmp);
							latch.countDown();
						}
					};
					threads.add(tmpThread);
					tmpThread.start();
				}

				logger.info("主线程等待");
				latch.await();
				logger.info("主线程执行");

				// 解析收集到的所有的top/sar/jmeter信息
				TreeMap<String, String> jmeterResult;
				jmeterResult = analyse(jmeterMessage);
				// 写日志
				logWriter("TPS : " + jmeterResult.get("tps"));
				logWriter("AvgDelay : " + jmeterResult.get("delay"));
				logWriter("Info : " + jmeterResult.get("info"));
				
				for (Entry<Integer, Board> boardEntry : boards.entrySet()) {

					TreeMap<String, String> topResult;
					TreeMap<String, String> sarResult;

					topResult = analyTopMessage(boardEntry.getValue());
					sarResult = analySarMessage(boardEntry.getValue());

					// 写日志
					logWriter("[" + boardEntry.getValue().desc + "]cpuIdle : " + topResult.get("cpuIdle"));
					logWriter("[" + boardEntry.getValue().desc + "]cpuUsed : " + topResult.get("cpuUsed"));
					logWriter("[" + boardEntry.getValue().desc + "]virtUsed : " + topResult.get("virtUsed"));
					logWriter("[" + boardEntry.getValue().desc + "]memUsed : " + topResult.get("memUsed"));
					logWriter("[" + boardEntry.getValue().desc + "]rxkBList : " + sarResult.get("rxkBList"));
					logWriter("[" + boardEntry.getValue().desc + "]txkBList : " + sarResult.get("txkBList"));
				}

				// log空两格
				logWriter("");
				logWriter("");
				System.out.println();
				System.out.println();

				// 检查一下是否继续
				if ("false".equals(jmeterResult.get("continue"))) {
					logger.info("时延超时或执行异常，结束执行");
					break;
				} else {
					
					// 如果本次执行的TPS比预期低了200以上，说明接近极限了，设置下一个循环时，jmx的TPS控制器状态，进行最后一轮测试
					logger.info("期望TPS : " + tpsTmp + " VS 实际TPS : " + jmeterResult.get("tps"));
					if ((tpsTmp - 200) < Double.parseDouble(jmeterResult.get("tps"))) {
						logger.info("TPS正常，继续执行......");
					} else {
						logger.info("TPS过低，关闭TPS控制器，进行最后测试......");
						trueORfalse = "false"; // 关闭TPS控制器
					}
				}
			}

			writer.close();

			// log转化为excel
			new LogToExcel().logToExcel(interfaceName);

			System.out.println("性能测试结束");
		} catch (Exception e) {
			logger.error("测试执行异常 : " + e.getMessage());
			RunJmeter.runFlag = false;
			throw new RuntimeException("测试执行异常 : " + e.getMessage());
		}
		RunJmeter.runFlag = false;
	}


	/**
	 * 执行Jmeter脚本
	 * 
	 * Jmeter的bin需要加入环境变量path中
	 */
	public String runScript(int TPS) {

		String res = "";

		JmeterUtils.TPSContral(scriptName, TPS, trueORfalse);

		String jmxPath = ConfigUtils.getCongByName("jmxPath");

		JSch jSch = new JSch();
		try {
			Session session = jSch.getSession(ConfigUtils.getCongByName("user"), ConfigUtils.getCongByName("host"),
					Integer.parseInt(ConfigUtils.getCongByName("port")));

			sessions.add(session);

			Properties properties = new Properties();
			properties.setProperty("StrictHostKeyChecking", "no");
			session.setConfig(properties);
			session.setPassword(ConfigUtils.getCongByName("pw"));
			session.connect();
			if (session.isConnected()) {
				ChannelShell shell = (ChannelShell) session.openChannel("shell");
				shell.connect();

				PrintWriter print = new PrintWriter(new OutputStreamWriter(shell.getOutputStream()), true);

				BufferedReader reader = new BufferedReader(new InputStreamReader(shell.getInputStream()));
				String line = "";
				StringBuffer buffer = new StringBuffer();

				System.out.println(reader.readLine());
				print.println("jmeter -n -t " + jmxPath + "/" + scriptName + ".jmx");

				long start = System.currentTimeMillis();
				long end = start + (seconds * 1000);
				while ((line = reader.readLine()) != null && (System.currentTimeMillis() < end)) {
					System.out.println(line);
					buffer.append(line + "\r\n");
				}
				reader.close();
				print.close();
				shell.disconnect();
				session.disconnect();

				res = buffer.toString();
			} else {
				logger.error("连接Client服务器异常");
				throw new RuntimeException("连接Client服务器异常");
			}
		} catch (Exception e) {
			logger.error("连接Client服务器异常 : " + e.getMessage());
			throw new RuntimeException("连接Client服务器异常 : " + e.getMessage());
		}

		return res;
	}


	/**
	 * 执行top命令
	 */
	public String runTOP(Board board) {

		String res = "";
		JSch jSch = new JSch();
		try {
			Session session = jSch.getSession(board.user, board.host, board.port);

			sessions.add(session);

			Properties properties = new Properties();
			properties.setProperty("StrictHostKeyChecking", "no");
			session.setConfig(properties);
			session.setPassword(board.pw);
			session.connect();
			if (session.isConnected()) {
				ChannelExec channel = (ChannelExec) session.openChannel("exec");
				channel.setCommand("top -d 1 -b -n 500000");
				channel.connect();

				BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
				String line = "";
				StringBuffer buffer = new StringBuffer();

				long start = System.currentTimeMillis();
				long end = start + ((seconds + 10) * 1000);
				while ((line = reader.readLine()) != null && (System.currentTimeMillis() < end)) {
					buffer.append(line + "\r\n");
				}
				reader.close();
				session.disconnect();

				res = buffer.toString();
			} else {
				logger.error("连接" + board.desc + "服务器异常");
				throw new RuntimeException("连接" + board.desc + "服务器异常");
			}
		} catch (Exception e) {
			logger.error("连接" + board.desc + "服务器异常 : " + e.getMessage());
			throw new RuntimeException("连接" + board.desc + "服务器异常 : " + e.getMessage());
		}

		return res;
	}

	
	/**
	 * 执行sar命令
	 */
	public String runSAR(Board board) {

		String res = "";
		JSch jSch = new JSch();
		try {
			Session session = jSch.getSession(board.user, board.host, board.port);

			sessions.add(session);

			Properties properties = new Properties();
			properties.setProperty("StrictHostKeyChecking", "no");
			session.setConfig(properties);
			session.setPassword(board.pw);
			session.connect();
			if (session.isConnected()) {
				ChannelExec channel = (ChannelExec) session.openChannel("exec");
				channel.setCommand("sar -n DEV 1 99999999");
				channel.connect();

				BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
				String line = "";
				StringBuffer buffer = new StringBuffer();

				long start = System.currentTimeMillis();
				long end = start + ((seconds + 10) * 1000);
				while ((line = reader.readLine()) != null && (System.currentTimeMillis() < end)) {
					buffer.append(line + "\r\n");
				}
				reader.close();
				session.disconnect();

				res = buffer.toString();
			} else {
				logger.error("连接" + board.desc + "服务器异常");
				throw new RuntimeException("连接" + board.desc + "服务器异常");
			}
		} catch (Exception e) {
			logger.error("连接" + board.desc + "服务器异常 : " + e.getMessage());
			throw new RuntimeException("连接" + board.desc + "服务器异常 : " + e.getMessage());
		}

		return res;
	}


	/**
	 * 解析JmeterInfo
	 * 
	 * @message 收集到的Jmeter执行信息
	 */
	public TreeMap<String, String> analyse(String message) {

		TreeMap<String, String> res = new TreeMap<String, String>();
		res.put("continue", "true");
		res.put("info", "执行正常");

		try {
			ArrayList<Double> tpss = new ArrayList<Double>();
			ArrayList<Double> delays = new ArrayList<Double>();

			String[] infos = message.split("\r\n");
			Pattern tpsPattern = Pattern.compile("((\\d+\\.\\d*)(/s))");
			Pattern delayPattern = Pattern.compile("((Avg:)(\\s*)(\\d+))");
			Pattern errPattern = Pattern.compile("((Err:)(\\s*)(\\d+))");

			// 时延超过250ms的次数，大于10次认为超时
			int numTemp = 0;

			for (String info : infos) {
				if (info.contains("summary +")) {
					Matcher tpsMatcher = tpsPattern.matcher(info);
					if (tpsMatcher.find()) {
						tpss.add(Double.parseDouble(tpsMatcher.group(2)));
					} else {
						logger.warn("日志格式错误 : " + info);
					}

					// 检查平均时延
					Matcher delayMatcher = delayPattern.matcher(info);
					if (delayMatcher.find()) {
						delays.add(Double.parseDouble(delayMatcher.group(4)));
						if (Double.parseDouble(delayMatcher.group(4)) > 250) {
							numTemp++;
							if (numTemp >= 10) {
								res.put("continue", "false");
								res.put("info", "时延超过250ms");
							}
						}
					} else {
						logger.warn("日志格式错误 : " + info);
					}

					// 检查错误计数
					Matcher errMatcher = errPattern.matcher(info);
					if (errMatcher.find()) {
						if (Integer.parseInt(errMatcher.group(4)) > 100) {
							res.put("continue", "false");
							res.put("info", "出现错误消息，请检查脚本执行情况");
						}
					} else {
						logger.warn("日志格式错误 : " + info);
					}
				}
			}
			
			// 判断当前执行是否为关闭TPS控制器，如果是则为最后一轮测试，将continue置为false
			if ("false".equals(trueORfalse)) {
				res.put("continue", "false");
				res.put("info", "关闭TPS控制器测试完成，结束执行");
			}

			res.put("delay", getAverage(delays) + "");
			res.put("tps", getAverage(tpss) + "");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	
	/**
	 * 解析topInfo
	 * 
	 * @message 收集到的top回显信息
	 * @board 执行的单板
	 */
	public TreeMap<String, String> analyTopMessage(Board board) {

		TreeMap<String, String> res = new TreeMap<String, String>();
		try {
			////// 解析top字符串，获取性能信息//////
			ArrayList<Double> cpuIdle = new ArrayList<Double>();
			ArrayList<Double> cpuUsed = new ArrayList<Double>();
			ArrayList<Double> virtUsed = new ArrayList<Double>();
			ArrayList<Double> memUsed = new ArrayList<Double>();

			String[] infos = board.topMessage.split("\r\n");

			for (int i = 0; i < infos.length; i++) {
				String info = infos[i].trim();
				String[] ms = null;
				if (info.startsWith("%Cpu(s)")) {
					try {
						cpuIdle.add(Double.parseDouble(info.trim().split("\\s+")[7].trim().replace("%id,", "")));
					} catch (Exception e) {
						logger.warn("解析top信息%Cpu(s)失败 " + info);
					}
				} else if (info.contains(board.top_keyword_1) && info.contains(board.top_keyword_2)) {
					try {
						ms = info.split("\\s+");
						cpuUsed.add(Double.parseDouble(ms[8].trim()));
						if (ms[4].trim().contains("g") || ms[4].trim().contains("m")) {
							virtUsed.add(Double.parseDouble(ms[4].trim().substring(0, ms[4].trim().length() - 1)));
						} else {
							virtUsed.add(Double.parseDouble(ms[4].trim()));
						}
						memUsed.add(Double.parseDouble(ms[9].trim()));
					} catch (Exception e) {
						logger.warn("解析top信息匹配[8][4][9]失败 " + info);
					}
				}
			}

			res.put("cpuIdle", getAverage(cpuIdle));
			res.put("cpuUsed", getAverage(cpuUsed));
			res.put("virtUsed", getAverage(virtUsed));
			res.put("memUsed", getAverage(memUsed));
		} catch (Exception e) {
			logger.error("解析top message 异常 : " + e.getMessage());
			throw new RuntimeException("解析top message 异常 : " + e.getMessage());
		}

		return res;
	}

	
	/**
	 * 解析sarInfo
	 * 
	 * @message 收集到的sar回显信息
	 */
	public TreeMap<String, String> analySarMessage(Board board) {

		TreeMap<String, String> res = new TreeMap<String, String>();
		try {
			////// 解析sar信息//////
			ArrayList<Double> rxkBList = new ArrayList<Double>();
			ArrayList<Double> txkBList = new ArrayList<Double>();

			String[] infos = board.sarMessage.split("\r\n");
			for (String info : infos) {
				if (!info.contains("Average") && info.contains("eth0")) {
					info = info.trim();
					String[] ms = info.split("\\s+");

					try {
						rxkBList.add(Double.parseDouble(ms[4]));
						txkBList.add(Double.parseDouble(ms[5]));
					} catch (Exception e) {
						logger.warn("解析sar信息失败 " + info);
					}
				}
			}

			res.put("rxkBList", getAverage(rxkBList));
			res.put("txkBList", getAverage(txkBList));
		} catch (Exception e) {
			logger.error("解析sar message 异常 : " + e.getMessage());
			throw new RuntimeException("解析sar message 异常 : " + e.getMessage());
		}
		return res;
	}

	
	/**
	 * @list 出入一个double的集合数组
	 */
	public String getAverage(ArrayList<Double> list) {

		try {
			if (list.size() > 3) {
				Collections.sort(list);

				ArrayList<Double> temp = new ArrayList<Double>();
				for (int i = 1; i < list.size() - 1; i++) {
					temp.add(list.get(i));
				}
				list = temp;
			}

			double sum = 0;
			int size = list.size();
			Iterator<Double> iterator = list.iterator();
			while (iterator.hasNext()) {
				Double d = iterator.next();
				sum = d + sum;
			}

			return Double.parseDouble(new DecimalFormat("#.00").format(sum / size)) + "";
		} catch (Exception e) {
			logger.warn("Double 集合均值异常 : " + e.getMessage());
			return "NA";
		}
	}

	
	/**
	 * 关闭所有的资源、线程、停止任务
	 */
	@SuppressWarnings("deprecation")
	public static void stop() {

		RunJmeter.runFlag = false;
		RunJmeter.isCI = false;

		if (threads != null) {
			for (Thread thread : threads) {
				if (thread != null) {
					while (thread.isAlive()) {
						logger.info("结束线程 : " + thread.getName());
						thread.stop();
					}
				}
			}
		}

		if (sessions != null) {
			for (Session session : sessions) {
				if (session != null) {
					while (session.isConnected()) {
						logger.info("断开session : " + session.getHost());
						session.disconnect();
					}
				}
			}
		}

		logger.info("已关闭全部资源，结束测试");
		System.out.println("已关闭全部资源，结束测试");
	}

	public static void main(String[] args) {

	}
}
