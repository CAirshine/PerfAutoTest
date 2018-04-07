package perf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;



public class ExcelToJson {
	
	private Logger logger = Logger.getLogger(ExcelToJson.class);

	/**
	 * 将excel中的sheet数据转化为json对象，再由jsp发送给前端页面进行展示
	 * Excel文件的前两列一定是TPS和AvgDelay
	 * 后面每6列是一个单板的性能数据
	 * 
	 * 转换后的json格式
		{
		    "boards":[
		        {"SDS":[
		            {"tps":"500", "cpuused":"", "receive":"", "send":""},
		            {"tps":"1000", "cpuused":"", "receive":"", "send":""},
		            {"tps":"1500", "cpuused":"", "receive":"", "send":""},
		            {"tps":"2000", "cpuused":"", "receive":"", "send":""},
		            {"tps":"2500", "cpuused":"", "receive":"", "send":""}
		        ]},
		        {"DB":[
		            {"tps":"500", "cpuused":"", "receive":"", "send":""},
		            {"tps":"1000", "cpuused":"", "receive":"", "send":""},
		            {"tps":"1500", "cpuused":"", "receive":"", "send":""},
		            {"tps":"2000", "cpuused":"", "receive":"", "send":""},
		            {"tps":"2500", "cpuused":"", "receive":"", "send":""}
		        ]}
		}
	 * 
	 * 
	 * */
	public JSONObject excelToJson(String excelName, String sheetName) {
		
		logger.info("ExcelToJson: " + excelName + "  " + sheetName);
		
		JSONObject obj = new JSONObject();
		JSONArray boardsArray = new JSONArray();
		
		try {
			HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(ConfigUtils.rootPath + "/excels/" + excelName + ".xls")));
			// HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(ConfigUtils.rootPath + "/WebContent/excels/" + excelName + ".xls")));
			HSSFSheet sheet = workbook.getSheet(sheetName);
			DataFormatter formatter = new DataFormatter();
			
			// 遍历单板，起始列是2
			int cellNum = 0;
			while (sheet.getRow(0).getCell(2 + cellNum * 6) != null && !"".equals(sheet.getRow(0).getCell(2 + cellNum * 6).getStringCellValue().trim())) {
				
				JSONObject boardKey = new JSONObject();
				JSONArray boardValue = new JSONArray();
				
				// 遍历各个单板的TPS下的性能信息，起始行是2
				// 起始列 ->
				// 2 + num * 6 + 1 -- CPU used
				// 2 + num * 6 + 4 -- Receive
				// 2 + num * 6 + 5 -- Send
				int rowNum = 2;
				while (sheet.getRow(rowNum) != null && !"".equals(formatter.formatCellValue(sheet.getRow(rowNum).getCell(2 + cellNum * 6 + 1)))) {
					
					JSONObject perfInfo = new JSONObject();
					perfInfo.put("tps", formatter.formatCellValue(sheet.getRow(rowNum).getCell(0)));
					perfInfo.put("delay", formatter.formatCellValue(sheet.getRow(rowNum).getCell(1)));
					perfInfo.put("cpuused", formatter.formatCellValue(sheet.getRow(rowNum).getCell(2 + cellNum * 6 + 1)));
					perfInfo.put("receive", formatter.formatCellValue(sheet.getRow(rowNum).getCell(2 + cellNum * 6 + 4)));
					perfInfo.put("send", formatter.formatCellValue(sheet.getRow(rowNum).getCell(2 + cellNum * 6 + 5)));
					
					boardValue.add(perfInfo);
					rowNum++;
				}
				
				// boardKey.put("desc", sheet.getRow(0).getCell(2 + cellNum * 6).getStringCellValue());
				// boardKey.put("infos", boardValue);
				boardKey.put(sheet.getRow(0).getCell(2 + cellNum * 6).getStringCellValue(), boardValue);
				boardsArray.add(boardKey);
				cellNum++;
			}
			obj.put("boards", boardsArray);
			workbook.close();
			
			sheet.getRow(0).getCell(8);
			sheet.getRow(0).getCell(14);
			
			
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		logger.debug(obj.toJSONString());
		return obj;
	}
	
	public static void main(String[] args) {
		
		System.out.println(new ExcelToJson().excelToJson("[SDV1]HAGPerfTest_1.0.0.000(xxx)", "查询一个产品下所有Ability").toJSONString());
	}
}
