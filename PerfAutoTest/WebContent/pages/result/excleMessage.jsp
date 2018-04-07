<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.ArrayList"%>
<%@page import="perf.ConfigUtils"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeMap"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Hiboard性能测试</title>
<style type="text/css">
div {
	border: 1px #BBBBBB;
	border-bottom-style: solid;
	line-height: 30px;
}

a {
	text-decoration: none;
	color: #4A6EAA;
	font-size: 14px;
}

a.atag:hover {
	color: red;
}
</style>
<script>
	function aColorContral(id) {
		var divs = document.getElementsByTagName("div");
		for (var i = 0; i < divs.length; i++) {
			divs[i].style.backgroundColor = "#F5F5F5";
		}
		var div = document.getElementById(id);
		div.style.backgroundColor = "#87CEFF";
	}
</script>
</head>
<body bgcolor="#F5F5F5">
	<%
		String fileName = request.getParameter("fileName");
		String version = fileName.replace(".xls", "");
		
		ArrayList<String> sheetList = ConfigUtils.getExcelSheets(fileName);
	%>

	<br />
	<a class="atag" href="excels.jsp"
		target="detailpage">切换版本</a> &nbsp;
	<a class="atag" href="/PerfAutoTest/index.jsp" target="_parent">回到首页</a>

	<p style="font-family: 宋体; font-size: 20px">接口详细数据：</p>
	<div id="all" onclick="aColorContral(this.id)"
		style="border-top-style: solid;">
		<%
			String sss = "";
			// 组装一下sheetNames
			for (String sheetName : sheetList) {
				sss = sss + "--" + sheetName;
			}
			
			sss = sss.substring(2);
		%>
		<a class="atag" href="collectInfo.jsp?version=<%=version%>&sheetNames=<%=sss %>" target="detailpage">
			接口数据汇总</a><br>
	</div>

	<%
		for (String sheetName : sheetList) {
			String sheetNameTemp = URLEncoder.encode(URLEncoder.encode(sheetName, "utf-8"), "utf-8");
	%>
	<div id='<%=sheetName%>' onclick="aColorContral(this.id)">
		<a href='detail.jsp?sheet_index=<%=sheetNameTemp%>&version=<%=version%>'
			target="detailpage" class="atag"><%=sheetName%></a> <br />
	</div>
	<%
		}
	%>
	<br>
	<br>
	<p style="font-family: 宋体; font-size: 20px">excel源文件：</p>
	<div style="border-top-style: solid;">
		<a class="atag" href="/PerfAutoTest/excels/<%=version%>.xls"> 点击下载</a>
	</div>
	<br>
	<br>
	<a class="atag" href="excelDelete.jsp?fileName=<%=fileName%>"
		target="detailpage"><font color="red" size="2px">删除服务器上当前文件</font></a>
</body>
</html>