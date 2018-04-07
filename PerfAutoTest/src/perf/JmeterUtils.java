package perf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.log4j.Logger;

public class JmeterUtils {

	// 修改Jmeter脚本中的TPS控制器，实现以指定的TPS运行
	
	private static Logger logger = Logger.getLogger(JmeterUtils.class);

	/**
	 * Jmeter脚本只能有一个线程组，且需要添加一个TPS控制器：Constant Throughput Timer
	 * 
	 * @scriptName 需要修改的Jmeter脚本名
	 * @TPS 设置TPS
	 */
	public static void TPSContral(String scriptName, int TPS, String trueORfalse) {

		try {
			int threadNum = Integer.parseInt(ConfigUtils.getCongByName("threadNum"));// 线程数

			String jmxPath = ConfigUtils.getCongByName("jmxPath");// Jmeter脚本的位置
			File file = new File(jmxPath + "/" + scriptName + ".jmx");// 创建Jmeter脚本对象

			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line = "";
			StringBuffer buffer = new StringBuffer();

			while ((line = reader.readLine()) != null) {
				// 修改线程数配置
				if (line.contains("name=\"ThreadGroup.num_threads\"")) {
					line = line.replaceAll("\\d+", threadNum + "");
					logger.info("修改线程设置：" + line);
				}

				// 修改TPS控制器
				if (line.trim().startsWith("<value>") && line.trim().endsWith("</value>")) {
					line = line.replaceAll("\\d+\\.\\d+", TPS * 60 + ".0");
					logger.info("修改TPS设置：" + line);
				}

				// 修改TPS控制器是否生效
				if (line.contains("testclass=\"ConstantThroughputTimer\"")) {
					line = line.replaceAll("((true)|(false))", trueORfalse);
					logger.info("修改TPS控制器为：" + trueORfalse);
				}
				
				buffer.append(line + "\r\n");
			}
			reader.close();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(buffer.toString());
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error("修改Jmeter脚本: " + scriptName + " 异常" + e.getMessage());
		}
	}
	
	public static void main(String[] args) {

		System.out.println(System.currentTimeMillis());
	}
}
