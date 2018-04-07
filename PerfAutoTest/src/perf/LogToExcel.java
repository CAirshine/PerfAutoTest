package perf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import utils.FileUtils;

@SuppressWarnings("unused")
public class LogToExcel {

	private Logger logger = Logger.getLogger(JmeterUtils.class);

	public void logToExcel(Interface interFace) {

		System.out.println("开始转化" + interFace.desc + "日志");
		try {

			Pattern pattern = Pattern.compile("((\\[)(.+)(\\]))");

			ArrayList<String> TPSs = new ArrayList<String>();
			ArrayList<String> AvgDelays = new ArrayList<String>();
			TreeMap<String, ArrayList<String>> sarReceives = new TreeMap<String, ArrayList<String>>();
			TreeMap<String, ArrayList<String>> sarSends = new TreeMap<String, ArrayList<String>>();
			TreeMap<String, ArrayList<String>> topcpuIdle = new TreeMap<String, ArrayList<String>>();
			TreeMap<String, ArrayList<String>> topcpuUsed = new TreeMap<String, ArrayList<String>>();
			TreeMap<String, ArrayList<String>> topvirtUsed = new TreeMap<String, ArrayList<String>>();
			TreeMap<String, ArrayList<String>> topmemUsed = new TreeMap<String, ArrayList<String>>();
			TreeSet<String> boards = new TreeSet<String>();

			// 读取日志文件
			BufferedReader reader = new BufferedReader(
					new FileReader(new File(ConfigUtils.rootPath + "/logs/" + interFace.script + ".log")));
			String res = "";
			while ((res = reader.readLine()) != null) {
				if (res.startsWith("TPS")) {
					TPSs.add(res.split(":")[1].trim());
				} else if (res.startsWith("AvgDelay")) {
					AvgDelays.add(res.split(":")[1].trim());
				} else if (res.contains("cpuIdle")) {
					Matcher matcher = pattern.matcher(res);
					if (matcher.find()) {
						boards.add(matcher.group(3));
						if (topcpuIdle.get(matcher.group(3)) == null) {
							ArrayList<String> listTemp = new ArrayList<String>();
							listTemp.add(res.split(":")[1].trim());
							topcpuIdle.put(matcher.group(3), listTemp);
						} else {
							topcpuIdle.get(matcher.group(3)).add(res.split(":")[1].trim());
						}
					} else {
						logger.error("log格式错误 " + res);
					}
				} else if (res.contains("cpuUsed")) {
					Matcher matcher = pattern.matcher(res);
					if (matcher.find()) {
						boards.add(matcher.group(3));
						if (topcpuUsed.get(matcher.group(3)) == null) {
							ArrayList<String> listTemp = new ArrayList<String>();
							listTemp.add(res.split(":")[1].trim());
							topcpuUsed.put(matcher.group(3), listTemp);
						} else {
							topcpuUsed.get(matcher.group(3)).add(res.split(":")[1].trim());
						}
					} else {
						logger.error("log格式错误 " + res);
					}
				} else if (res.contains("virtUsed")) {
					Matcher matcher = pattern.matcher(res);
					if (matcher.find()) {
						boards.add(matcher.group(3));
						if (topvirtUsed.get(matcher.group(3)) == null) {
							ArrayList<String> listTemp = new ArrayList<String>();
							listTemp.add(res.split(":")[1].trim());
							topvirtUsed.put(matcher.group(3), listTemp);
						} else {
							topvirtUsed.get(matcher.group(3)).add(res.split(":")[1].trim());
						}
					} else {
						logger.error("log格式错误 " + res);
					}
				} else if (res.contains("memUsed")) {
					Matcher matcher = pattern.matcher(res);
					if (matcher.find()) {
						boards.add(matcher.group(3));
						if (topmemUsed.get(matcher.group(3)) == null) {
							ArrayList<String> listTemp = new ArrayList<String>();
							listTemp.add(res.split(":")[1].trim());
							topmemUsed.put(matcher.group(3), listTemp);
						} else {
							topmemUsed.get(matcher.group(3)).add(res.split(":")[1].trim());
						}
					} else {
						logger.error("log格式错误 " + res);
					}
				} else if (res.contains("rxkBList")) {
					Matcher matcher = pattern.matcher(res);
					if (matcher.find()) {
						boards.add(matcher.group(3));
						if (sarReceives.get(matcher.group(3)) == null) {
							ArrayList<String> listTemp = new ArrayList<String>();
							listTemp.add(res.split(":")[1].trim());
							sarReceives.put(matcher.group(3), listTemp);
						} else {
							sarReceives.get(matcher.group(3)).add(res.split(":")[1].trim());
						}
					} else {
						logger.error("log格式错误 " + res);
					}
				} else if (res.contains("txkBList")) {
					Matcher matcher = pattern.matcher(res);
					if (matcher.find()) {
						boards.add(matcher.group(3));
						if (sarSends.get(matcher.group(3)) == null) {
							ArrayList<String> listTemp = new ArrayList<String>();
							listTemp.add(res.split(":")[1].trim());
							sarSends.put(matcher.group(3), listTemp);
						} else {
							sarSends.get(matcher.group(3)).add(res.split(":")[1].trim());
						}
					} else {
						logger.error("log格式错误 " + res);
					}
				}
			}
			reader.close();





			////// 写入到excel中//////
			File file = null;
			if (RunJmeter.isCI) {
				file = new File(ConfigUtils.rootPath + "/excels/" + "[" + ConfigUtils.getCongByName("prefix") + "]"
						+ ConfigUtils.getCongByName("platform") + "_" + ConfigUtils.getCongByName("version") + "("
						+ new SimpleDateFormat("yyyyMMdd").format(new Date()) + ")" + ".xls");
				
				BufferedWriter CIlog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(ConfigUtils.rootPath + "/logs/CI.info"), true), "utf-8"));
				CIlog.write(interFace.desc + "=" + TPSs.get(TPSs.size()-1) + "\r\n");
				CIlog.flush();
				CIlog.close();
			} else {
				file = new File(ConfigUtils.rootPath + "/excels/" + "[" + ConfigUtils.getCongByName("prefix") + "]"
						+ ConfigUtils.getCongByName("platform") + "_" + ConfigUtils.getCongByName("version") + "("
						+ ConfigUtils.getCongByName("suffix") + ")" + ".xls");
			}

			HSSFWorkbook workbook;
			if (!file.exists()) {
				logger.info("excel文件不存在，创建新文件");
				
				file.createNewFile();
				workbook = new HSSFWorkbook();
			} else {
				logger.info("excel文件存在，直接读取老文件");
				
				// 每次转换日志到excel之前先备份老的excel文件
				// 以免日志转换过程中文件损坏导致信息丢失
				// 定期上服务器手动删除备份文件
				FileUtils.CopyFileFile(
						ConfigUtils.rootPath + "/excels/" + "[" + ConfigUtils.getCongByName("prefix") + "]"
								+ ConfigUtils.getCongByName("platform") + "_" + ConfigUtils.getCongByName("version")
								+ "(" + ConfigUtils.getCongByName("suffix") + ")" + ".xls",
						ConfigUtils.rootPath + "/excels_back/" + "[" + ConfigUtils.getCongByName("prefix") + "]"
								+ ConfigUtils.getCongByName("platform") + "_" + ConfigUtils.getCongByName("version")
								+ "(" + ConfigUtils.getCongByName("suffix") + ")_"
								+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls",
						false);

				workbook = new HSSFWorkbook(new FileInputStream(file));
			}

			if (workbook.getSheet(interFace.desc) != null) {
				workbook.removeSheetAt(workbook.getSheetIndex(interFace.desc));
			}

			HSSFSheet sheet = workbook.createSheet(interFace.desc);
			// 先把行列创建了，最大只能到255
			for (int i = 0; i < 255; i++) {
				HSSFRow row = sheet.createRow(i);
				for (int j = 0; j < 255; j++) {
					row.createCell(j);
				}
			}

			HSSFCellStyle titleStyle = titleStyle(workbook);
			HSSFCellStyle contentStyle = contentStyle(workbook);

			////// 填写数据//////
			int numRow = 0;// 起始行，从0开始
			int numCell = 0;// 起始列，从0开始

			mergeCells(sheet, numRow, numRow + 1, numCell, numCell);
			mergeCells(sheet, numRow, numRow + 1, numCell + 1, numCell + 1);
			setValueandStyle(sheet.getRow(numRow).getCell(numCell), "TPS", titleStyle);
			setValueandStyle(sheet.getRow(numRow).getCell(numCell + 1), "AvgDelay", titleStyle);

			for (int i = 0; i < TPSs.size(); i++) {
				setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell), TPSs.get(i), contentStyle);
				setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 1), AvgDelays.get(i), contentStyle);
			}

			numCell = numCell + 2;

			// 解决不按顺序填写的问题
			// for (Iterator<String> iterator = boards.iterator(); iterator.hasNext();) {
			for (int iii = 1; iii < interFace.boards.size() + 1; iii++) {
				String board = interFace.boards.get(iii).desc;

				mergeCells(sheet, numRow, numRow, numCell, numCell + 5);
				setValueandStyle(sheet.getRow(numRow).getCell(numCell), board, titleStyle);

				ArrayList<String> topcpuIdleslistTemp = topcpuIdle.get(board);
				for (int i = 0; i < topcpuIdleslistTemp.size(); i++) {
					setValueandStyle(sheet.getRow(numRow + 1).getCell(numCell + 0), "CPU idle", titleStyle);
					setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 0), topcpuIdleslistTemp.get(i),
							contentStyle);
				}

				ArrayList<String> topcpuUsedlistTemp = topcpuUsed.get(board);
				for (int i = 0; i < topcpuUsedlistTemp.size(); i++) {
					setValueandStyle(sheet.getRow(numRow + 1).getCell(numCell + 1), "CPU used", titleStyle);
					setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 1), topcpuUsedlistTemp.get(i),
							contentStyle);
				}

				ArrayList<String> topvirtUsedlistTemp = topvirtUsed.get(board);
				for (int i = 0; i < topvirtUsedlistTemp.size(); i++) {
					setValueandStyle(sheet.getRow(numRow + 1).getCell(numCell + 2), "VIRT", titleStyle);
					setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 2), topvirtUsedlistTemp.get(i),
							contentStyle);
				}

				ArrayList<String> topmemUsedlistTemp = topmemUsed.get(board);
				for (int i = 0; i < topmemUsedlistTemp.size(); i++) {
					setValueandStyle(sheet.getRow(numRow + 1).getCell(numCell + 3), "mem", titleStyle);
					setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 3), topmemUsedlistTemp.get(i),
							contentStyle);
				}

				ArrayList<String> sarReceiveslistTemp = sarReceives.get(board);
				for (int i = 0; i < sarReceiveslistTemp.size(); i++) {
					setValueandStyle(sheet.getRow(numRow + 1).getCell(numCell + 4), "Receive", titleStyle);
					setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 4), sarReceiveslistTemp.get(i),
							contentStyle);
				}

				ArrayList<String> sarSendslistTemp = sarSends.get(board);
				for (int i = 0; i < sarSendslistTemp.size(); i++) {
					setValueandStyle(sheet.getRow(numRow + 1).getCell(numCell + 5), "Send", titleStyle);
					setValueandStyle(sheet.getRow(i + 2 + numRow).getCell(numCell + 5), sarSendslistTemp.get(i),
							contentStyle);
				}
				numCell = numCell + 6;
			}

			workbook.write(file);
			workbook.close();
			System.out.println("转化日志完成");
		} catch (Exception e) {
			logger.error(interFace.desc + " log转excel异常 : " + e.getMessage());
			e.printStackTrace();
			System.out.println("转化日志异常");
		}
	}

	// 合并单元格
	public void mergeCells(HSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		CellRangeAddress cra = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		sheet.addMergedRegion(cra);
	}

	// 设置表头部分的单元格样式
	@SuppressWarnings("deprecation")
	public HSSFCellStyle titleStyle(HSSFWorkbook wb) {
		HSSFCellStyle style = wb.createCellStyle();

		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);

		style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框

		style.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直
		style.setAlignment(HorizontalAlignment.CENTER);// 水平

		return style;
	}

	// 设置内容部分的单元格样式
	@SuppressWarnings("deprecation")
	public HSSFCellStyle contentStyle(HSSFWorkbook wb) {
		HSSFCellStyle style = wb.createCellStyle();

		style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框

		// style.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直
		// style.setAlignment(HorizontalAlignment.CENTER);// 水平

		return style;
	}

	// 设置格式&添加内容&字符转数字
	public void setValueandStyle(HSSFCell cell, String value, HSSFCellStyle style) {
		try {
			cell.setCellStyle(style);
			cell.setCellValue(Double.parseDouble(value));
		} catch (NumberFormatException e) {
			cell.setCellValue(value);
			logger.warn("[WARN]转换数字格式失败 : " + value);
		}

	}

	public static void main(String[] args) {

		// System.out.println(ConfigUtils.rootPath);
		// new LogToExcel().lofToExcel("userabilityv1cardsproductsquery");
	}
}
