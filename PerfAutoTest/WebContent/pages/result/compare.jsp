<%@page import="java.awt.BasicStroke"%>
<%@page import="org.jfree.chart.renderer.xy.XYLineAndShapeRenderer"%>
<%@page import="org.jfree.ui.TextAnchor"%>
<%@page import="org.jfree.chart.labels.ItemLabelAnchor"%>
<%@page import="org.jfree.chart.labels.ItemLabelPosition"%>
<%@page import="java.awt.Shape"%>
<%@page import="org.jfree.chart.renderer.xy.XYItemRenderer"%>
<%@page import="org.jfree.chart.plot.XYPlot"%>
<%@page import="org.jfree.chart.axis.CategoryAxis"%>
<%@page import="java.awt.Color"%>
<%@page import="java.util.HashSet"%>
<%@page import="perf.ConfigUtils"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.jfree.data.xy.XYSeries"%>
<%@page import="org.jfree.data.xy.XYSeriesCollection"%>
<%@page import="org.jfree.chart.ChartUtilities"%>
<%@page import="java.io.File"%>
<%@page import="org.jfree.chart.plot.CategoryPlot"%>
<%@page import="org.jfree.chart.plot.PlotOrientation"%>
<%@page import="org.jfree.chart.ChartFactory"%>
<%@page import="org.jfree.chart.JFreeChart"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Set"%>
<%@page import="com.alibaba.fastjson.JSONArray"%>
<%@page import="org.jfree.data.category.DefaultCategoryDataset"%>
<%@page import="com.alibaba.fastjson.JSONObject"%>
<%@page import="java.util.TreeMap"%>
<%@page import="perf.ExcelToJson"%>
<%@page import="java.net.URLDecoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
div {
	border: 1px #BBBBBB;
	border-bottom-style: solid;
	font-family: 宋体;
	color: #4A6EAA;
}
a {
	text-decoration: none;
	font-family: 宋体;
	font-size: 20px;
	color: #4A6EAA;
}

a.atag:hover {
	color: red;
}

div {
	border: 1px #BBBBBB;
	border-bottom-style: solid;
	color: #4A6EAA;
	display: none;
}
</style>
<script type="text/javascript">
	function showhide(id) {
		var o = document.getElementById(id);
		o.style.display = o.style.display == 'block' ? 'none' : 'block';
	}
</script>
</head>
<body>
<!-- 废弃使用前端js渲染图片的形式，改用在服务器，使用jfreechat绘制静态图片 -->
	
	<%!
		// 字符串转double，无法转换返回0.0
		public double strToDouble(String s){
			double res = 0.0;
			try {
				res = Double.parseDouble(s);
			} catch (Exception e) {
				
				// e.printStackTrace();
			}
			
			return res;
		}
	
		public void categoryManager(JFreeChart chart){
			
	        XYPlot plot = chart.getXYPlot(); //获取绘图区对象  
	        
	        XYItemRenderer xyItemRenderer = plot.getRenderer();
	        XYLineAndShapeRenderer xyLineAndShapeRenderer = (XYLineAndShapeRenderer)xyItemRenderer;
	        
	        xyLineAndShapeRenderer.setBaseShapesVisible(true);
		}
	%>
	<%
		// 选择了以下几个版本
		String[] versions = request.getParameterValues("verCom");
		String sheetName = request.getParameter("sheet_index");
		
		String rootPath = ConfigUtils.rootPath;
		
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) {
			// 先注释掉，如果出现乱码的现象再放开
			// sheetName = new String(sheetName.getBytes("ISO-8859-1"), "utf-8");
		}
		
		// 解析json数据，保存到map文件
		TreeMap<String, TreeMap<String, ArrayList<Double>>> map = new TreeMap<String, TreeMap<String, ArrayList<Double>>>();
		HashSet<String> boardNames = new HashSet<String>();
		
		for (String version : versions) { // 遍历版本
			
			JSONArray jsonArray = (JSONArray)new ExcelToJson().excelToJson(version, sheetName).get("boards");
			for (int i = 0, len = jsonArray.size(); i < len; i++) { // 循环各个单板的信息，绘图
								
				JSONObject jsonObject = (JSONObject)jsonArray.get(i);
				jsonObject.entrySet();
				for (Entry<String, Object> entry : jsonObject.entrySet()) { // 这个for循环其实只有一行，不算是循环
					
					String boardName = entry.getKey();
					boardNames.add(boardName);
					JSONArray boardInfos = (JSONArray)entry.getValue();
					
					version = version.replace(".xls", "");
					
					if (map.get(version + "-" + boardName) == null) {
						TreeMap<String, ArrayList<Double>> mapTemp = new TreeMap<String, ArrayList<Double>>();
						mapTemp.put("TPS", new ArrayList<Double>());
						mapTemp.put("delay", new ArrayList<Double>());
						mapTemp.put("cpuused", new ArrayList<Double>());
						mapTemp.put("receive", new ArrayList<Double>());
						mapTemp.put("send", new ArrayList<Double>());
						map.put(version + "-" + boardName, mapTemp);
					}
					
					for (int j = 0, len_2 = boardInfos.size(); j < len_2; j++) {
						
						JSONObject boardInfo = (JSONObject)boardInfos.get(j);

						map.get(version + "-" + boardName).get("TPS").add(strToDouble(boardInfo.getString("tps")));
						map.get(version + "-" + boardName).get("delay").add(strToDouble(boardInfo.getString("delay")));
						map.get(version + "-" + boardName).get("cpuused").add(strToDouble(boardInfo.getString("cpuused")));
						map.get(version + "-" + boardName).get("receive").add(strToDouble(boardInfo.getString("receive")));
						map.get(version + "-" + boardName).get("send").add(strToDouble(boardInfo.getString("send")));
					}
				}
			}
		}

		// 根据map文件绘图
		for (String boardName : boardNames) {
			
			XYSeriesCollection delayCollection = new XYSeriesCollection();
			XYSeriesCollection cpuCollection = new XYSeriesCollection();
			XYSeriesCollection receiveCollection = new XYSeriesCollection();
			XYSeriesCollection sendCollection = new XYSeriesCollection();
			
			for (String version : versions) {
				
				XYSeries delaySeries = new XYSeries(version + boardName);
				XYSeries cpuSeries = new XYSeries(version + boardName);
				XYSeries receiveSeries = new XYSeries(version + boardName);
				XYSeries sendSeries = new XYSeries(version + boardName);	
				
				ArrayList<Double> TPSList = map.get(version + "-" + boardName).get("TPS");
				ArrayList<Double> delayList = map.get(version + "-" + boardName).get("delay");
				ArrayList<Double> cpuusedList = map.get(version + "-" + boardName).get("cpuused");
				ArrayList<Double> receiveList = map.get(version + "-" + boardName).get("receive");
				ArrayList<Double> sendList = map.get(version + "-" + boardName).get("send");
				
				int lenTmp = TPSList.size();
				for (int i = 0; i < lenTmp; i++) {
					
					delaySeries.add(TPSList.get(i), delayList.get(i));
					cpuSeries.add(TPSList.get(i), cpuusedList.get(i));
					receiveSeries.add(TPSList.get(i), receiveList.get(i));
					sendSeries.add(TPSList.get(i), sendList.get(i));
				}
				
				delayCollection.addSeries(delaySeries);
				cpuCollection.addSeries(cpuSeries);
				receiveCollection.addSeries(receiveSeries);
				sendCollection.addSeries(sendSeries);
			}
			JFreeChart Delay_chart = ChartFactory.createXYLineChart(
			        "Delay Info",
			        "TPS",
			        "Delay(ms)",				
			        delayCollection,
			        PlotOrientation.VERTICAL,
			        true, 
			        true, 
			        false);
			categoryManager(Delay_chart);
			JFreeChart CPU_chart = ChartFactory.createXYLineChart(
					boardName + " CPU Info",
			        "TPS",
			        "CPU(%)",				
			        cpuCollection,
			        PlotOrientation.VERTICAL,
			        true, 
			        true, 
			        false);
			categoryManager(CPU_chart);
			JFreeChart Receive_chart = ChartFactory.createXYLineChart(
					boardName + " Receive Info",
			        "TPS",
			        "Receive(KB/s)",				
			        receiveCollection,
			        PlotOrientation.VERTICAL,
			        true, 
			        true, 
			        false);
			categoryManager(Receive_chart);
			JFreeChart Send_chart = ChartFactory.createXYLineChart(
					boardName + " Send Info",
			        "TPS",
			        "Send(KB/s)",
			        sendCollection,
			        PlotOrientation.VERTICAL,
			        true,
			        true,
			        false);
			categoryManager(Send_chart);
			
			int width = 1100; /* Width of the image */
		    int height = 400; /* Height of the image */ 
			
			File TOP_pic = new File(rootPath + "/img/" + boardName + "_TOP.jpeg");
			File CPU_pic = new File(rootPath + "/img/" + boardName + "_CPU.jpeg");
			File Receive_pic = new File(rootPath + "/img/" + boardName + "_Receive.jpeg");
			File Send_pic = new File(rootPath + "/img/" + boardName + "_Send.jpeg");
			
			ChartUtilities.saveChartAsJPEG(TOP_pic ,Delay_chart, width ,height);
			ChartUtilities.saveChartAsJPEG(CPU_pic ,CPU_chart, width ,height);
			ChartUtilities.saveChartAsJPEG(Receive_pic ,Receive_chart, width ,height);
			ChartUtilities.saveChartAsJPEG(Send_pic ,Send_chart, width ,height);
		}
	%>
	
	<!-- 绘制页面 -->
	<%
		for (String boardName : boardNames) {
			%>
			<a href="javascript:showhide('<%=boardName %>')" class='atag'><%=boardName %></a><br>
			<div id=<%=boardName %>>
				<img src=<%="/PerfAutoTest/img/" + boardName + "_TOP.jpeg" %> /><br>
				<img src=<%="/PerfAutoTest/img/" + boardName + "_CPU.jpeg" %> /><br>
				<img src=<%="/PerfAutoTest/img/" + boardName + "_Receive.jpeg" %> /><br>
				<img src=<%="/PerfAutoTest/img/" + boardName + "_Send.jpeg" %> /><br>
			</div>
			<%
		}
	%>
</body>
</html>