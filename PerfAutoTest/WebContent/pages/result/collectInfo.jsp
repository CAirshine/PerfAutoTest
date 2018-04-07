<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="perf.ConfigUtils"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript" src="/PerfAutoTest/jquery.js"></script>
<script src="g2.js"></script>
<script src="my.js"></script>
<style type="text/css">
p {
	font-size: 25px;
	text-align: center;
	color: #4A6EAA;
}

table {
	font-size: 18px;
	border-collapse: collapse;
}

div {
	font-size: 15px;
    color: #4A6EAA;
}
</style>
</head>
<body>
	<Script language="javascript">
	
		/*----------------------获取源数据-------------------------*/
	
		var excelName = getParamByName('version');
		var sheetNames = getParamByName('sheetNames').split('--');
		
		// 这两个参数要求一一对应，懒得用map了
		var interfaces = [];
		var tpss = [];
		
		// 为什么用ij，因为我怕和其他地方命名重了
		for (ij = 0; ij < sheetNames.length; ij++) {
			var body = $.ajax({
	            url: '/PerfAutoTest/getDetail?excelName=' + excelName + '&sheetName=' + sheetNames[ij],
	            async: false
	        });
			
			var jsonObj = JSON.parse(body.responseText);
			
			var perfInfo = getPerfInfoByBoard(jsonObj, getBoards(jsonObj)[0]);
			
			var length = perfInfo['tpss'].length;
			
			interfaces[ij] = sheetNames[ij];
			tpss[ij] = perfInfo['tpss'][length - 1];
		}
		
		
		/*----------------------绘制标题-------------------------*/
		
		document.write("<p>" + excelName + "</p>");
		
		
		/*----------------------绘制表格-------------------------*/
		document.write("<div>");
		document.write("<table border='1' align='center'>");
		document.write("<tr>");
		document.write("<td>接口描述</td>");
		document.write("<td>极限TPS</td>");
		document.write("</tr>");
		
		for (ij = 0; ij < interfaces.length; ij++) {
			document.write('<tr>');
			document.write('<td>' + interfaces[ij] + '</td>');
			document.write('<td>' + tpss[ij] + '</td>');
			document.write('</tr>');
		}
		document.write('</table>');
		document.write("</div>");
	</Script>
</body>
</html>