package perf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ConfigUtils {

	private static Logger logger = Logger.getLogger(ConfigUtils.class);

	public static String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath()
			.split("PerfAutoTest")[0] + "PerfAutoTest";
	public static File config = new File(rootPath + "/config.xml");
	public static File boardsInfo = new File(rootPath + "/boardsInfo.xml");
	public static File interfacesFile = new File(rootPath + "/interfaces.xml");

	// 获取config.xml中指定元素的信息
	public synchronized static String getCongByName(String confName) {

		String result = "";
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(config);
			result = document.selectSingleNode("//root/" + confName).getText();

		} catch (Exception e) {
			logger.error("解析" + confName + "异常" + e.getMessage());
		}
		return result;
	}

	// 设置config.xml中指定元素的信息(已废弃)
	public synchronized static void setConfigByName(String confName, String value) {

		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(config);
			document.selectSingleNode("//root/" + confName).setText(value);

			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter xmlWriter = new XMLWriter(new FileWriter(config), format);
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (Exception e) {
			logger.error("设置" + confName + "异常" + e.getMessage());
		}
	}

	// 获取boardsInfo.xml指定boardName的单板信息
	public synchronized static Board getBoardByName(String boardName) {
		Board board = new Board();
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(boardsInfo);
			Element element = (Element) document.selectSingleNode("//boards/" + boardName);

			board.desc = element.element("desc").getText();
			board.host = element.element("host").getText();
			board.user = element.element("user").getText();
			board.port = Integer.parseInt(element.element("port").getText());
			board.pw = element.element("pw").getText();
			board.top_keyword_1 = element.element("top_keyword_1").getText();
			board.top_keyword_2 = element.element("top_keyword_2").getText();

		} catch (Exception e) {
			logger.error("获取Board异常" + e.getMessage());
		}
		return board;
	}

	// 获取interfaces.xml指定interfaceName的信息
	public synchronized static Interface getInterfaceByName(String interfaceName) {
		Interface interFace = new Interface();
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(interfacesFile);

			Element interNode = (Element) document.selectSingleNode("//interfaces/" + interfaceName);

			interFace.tag = interNode.getName();
			interFace.desc = interNode.selectSingleNode("./desc").getText();
			interFace.script = interNode.selectSingleNode("./script").getText();
			interFace.boards = new TreeMap<Integer, Board>();

			for (Node boardNode : interNode.selectNodes("./boards/board")) {
				Element boardEle = (Element) boardNode;

				interFace.boards.put(Integer.parseInt(boardEle.attribute("index").getText()),
						getBoardByName(boardEle.getText()));
			}

		} catch (Exception e) {
			logger.error("获取Interface异常" + e.getMessage());
		}
		return interFace;
	}

	// 调用getInterfaceByName获取interfaces.xml中所有的接口信息
	public synchronized static ArrayList<Interface> getInterfaces() {
		ArrayList<Interface> interfaces = new ArrayList<Interface>();

		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(interfacesFile);

			List<Node> nodes = document.selectNodes("//interfaces/*");
			for (Node node : nodes) {
				Element element = (Element) node;

				Interface interFace = getInterfaceByName(element.getName());

				interfaces.add(interFace);
			}

			document = saxReader.read(boardsInfo);

		} catch (Exception e) {
			logger.error("获取Interfaces异常" + e.getMessage());
		}

		return interfaces;
	}

	// 获取excel文件中所有的sheet名称信息
	public synchronized static ArrayList<String> getExcelSheets(String excelName) {
		ArrayList<String> sheetList = new ArrayList<String>();
		try {
			HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(rootPath + "/excels/" + excelName)));
			Iterator<Sheet> iterator = workbook.sheetIterator();
			while (iterator.hasNext()) {
				sheetList.add(iterator.next().getSheetName());
			}
			workbook.close();
		} catch (Exception e) {
			logger.error("获取ExcelSheets异常" + e.getMessage());
		}
		return sheetList;
	}

	// 获取所有的收件人信息
	public synchronized static ArrayList<String> getReceives () {
		
		ArrayList<String> resList = new ArrayList<String>();
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(config);
			List<Node> nodes = document.selectSingleNode("//root/receives").selectNodes("./receive");
			for (Node node : nodes) {
				resList.add(node.getText());
			}
		} catch (Exception e) {
			logger.error("获取收件人信息异常" + e.getMessage());
		}
		return resList;
	}
	
	/** --测试-- **/
	public static void main(String[] args) {

		System.out.println(Thread.currentThread().getContextClassLoader().getResource("").getPath());
	}
}