package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import perf.ConfigUtils;

public class MyOutputStream extends OutputStream {

	private static Logger logger = Logger.getLogger(MyOutputStream.class);

	private static MyOutputStream myOutputStream;

	public static MyOutputStream getInstance() {

		if (myOutputStream == null) {
			myOutputStream =  new MyOutputStream();
		}
		return myOutputStream;
	}

	Writer writer;

	private MyOutputStream() {
		
		init();
	}

	public void init() {
		try {
			this.writer = new OutputStreamWriter(new FileOutputStream(new File(ConfigUtils.rootPath + "/runLog.html")),
					"utf-8");
			System.setOut(new PrintStream(this));
			System.setErr(new PrintStream(this));
			
			writer.write("<head><meta charset=\"UTF-8\"><title>执行日志</title></head><br/>\r\n");
			writer.write("<style type=\"text/css\">p{color:black;font-size:15px;}</style><p>\r\n");
			writer.flush();
			
			logger.info("初始化MyOutputStream");
		} catch (Exception e) {
			logger.error("初始化MyOutputStream失败 : " + e.getMessage());
		}
	}

	@Override
	public void write(int arg0) throws IOException {

	}

	@Override
	public void write(byte[] bs, int start, int end) throws IOException {
		String s = new String(bs, start, end);
		if (!(s.equals("\r\n") || s.equals("\n"))) {
			writer.write(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:SSS]	").format(new Date()));
			writer.write(s);
			writer.write("<br/>\r\n");
			writer.flush();
		}
	}

}
