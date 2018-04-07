package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

public class FileUtils {

	private static Logger logger = Logger.getLogger(FileUtils.class);

	public static File[] getFileList(String path) {

		return new File(path).listFiles();
	}

	public static void fileModify(File file, String data) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.write(data);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error(file.getName() + " 文件修改异常 : " + e.getMessage());
		}
	}

	public static String fileReader(File file) {

		String res = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				res = line + "\r\n";
			}
			reader.close();
		} catch (Exception e) {
			logger.error(file.getName() + " 文件读取异常 : " + e.getMessage());
		}
		return res;
	}

	/**
	 * 复制/移动文件
	 * 
	 * @param oldFileName
	 *            老文件全路径
	 * @param newFileName
	 *            新文件全路径
	 * @param isRemove
	 *            true表示移动，false表示复制
	 * @return 是否成功
	 * @throws Exception
	 */
	public static boolean CopyFileFile(String oldFileName, String newFileName, boolean isRemove) {

		logger.info("Copy file " + oldFileName + "to new file " + newFileName);

		try {
			File oldfile = new File(oldFileName);
			File newfile = new File(newFileName);

			if (!oldfile.isFile() || !oldfile.exists()) {
				logger.warn("FileUtils INFO ：文件不存在，或是一个文件夹");
				return false;
			}

			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(oldfile));
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newfile));

			byte[] bs = new byte[1024 * 1024 * 10];
			int len = 0;

			while ((len = inputStream.read(bs)) != -1) {
				outputStream.write(bs, 0, len);
				outputStream.flush();
			}

			inputStream.close();
			outputStream.close();

			if (isRemove) {
				logger.info("删除老文件-" + oldfile.delete());
				logger.info("移动成功");
			} else {
				logger.info("FileUtils INFO ：复制成功");
			}

		} catch (Exception e) {
			logger.error("文件复制或移动失败 : " + e.getMessage());
		}

		return true;
	}
}
