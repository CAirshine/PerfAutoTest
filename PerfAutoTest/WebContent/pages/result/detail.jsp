<%@page import="perf.ConfigUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.io.File"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>接口性能详情页</title>
<style type="text/css">
.picDEV {
	font-size: 15px;
	color: #4A6EAA;
	display: none;
}

.infoDEV {
	font-size: 15px;
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
}

table {
	font-size: 12px;
	border-collapse: collapse;
}
</style>
<script type="text/javascript" src="/PerfAutoTest/jquery.js"></script>
<script src="g2.js"></script>
<script src="my.js"></script>
<script type="text/javascript">
	function showhide(id) {
		var o = document.getElementById(id);
		o.style.display = o.style.display == 'block' ? 'none' : 'block';
	}
</script>
</head>

<body>
	<Script language="javascript">
		var sheetName = getParamByName('sheet_index');
		var excelName = getParamByName('version');

		/*------------------------当前版本------------------------*/

		document.write("<div class='infoDEV'>");
		document.write('<br/>当前版本: <br/>' + excelName + '<br/><br/>');
		document.write('</div>');

		/*------------------------接口名称------------------------*/

		document.write("<div class='infoDEV'>");
		document.write('<br/>接口名称: <br/>' + sheetName + '<br/><br/>');
		document.write('</div>');
		
		/*-------------------------获取sheet页json数据------------*/

		var body = $.ajax({
			url : '/PerfAutoTest/getDetail?excelName=' + excelName
					+ '&sheetName=' + sheetName,
			async : false
		});

		var jsonObj = JSON.parse(body.responseText);
		var boards = getBoards(jsonObj);
		
		/*-------------------------绘制表格-----------------------*/
		
		document.write("<div class='infoDEV'>");
		document.write('<br/>详细数据: <br/><br/>');
		makeTable(jsonObj);
		document.write('<br/></div>');

		/*-------------------------绘制折线图---------------------*/
		
		document.write("<div class='infoDEV'>");
		document.write('<br/>变化趋势: <br/><br/>');

		// 绘制delay&tps折线图
		document
				.write("<a href=\"javascript:showhide('delayLine')\" class='atag'>AvgDelay</a>");
		document.write("<div id='delayLine' class='picDEV'></div><br/>");
		var src = getPerfInfoByBoard(jsonObj, boards[0]);
		delayLine(src);

		// 绘制各个单板性能折线图
		for (j = 0; j < boards.length; j++) {
			document.write("<a href=\"javascript:showhide('" + boards[j]
					+ "')\" class='atag'>" + boards[j] + "</a>");
			document
					.write("<div id=" + boards[j] + " class='picDEV'></div><br/>");

			// 获取源数据，绘图
			var src = getPerfInfoByBoard(jsonObj, boards[j]);
			boardLine(src);
		}

		document.write('<br/></div>');
	</Script>

	<!-------------------------历史版本比对--------------------->
	<div class='infoDEV'>
		<br />历史版本比对: <br /> <br />
		<%
			String sheetName = request.getParameter("sheet_index");
		%>
		<form method="post" action="compare.jsp?sheet_index=<%=sheetName%>">
			<%
				File[] files = new File(ConfigUtils.rootPath + "/excels").listFiles();
				for (int i = 0; i < files.length; i++) {

					out.print("<input type=\"checkbox\" name=\"verCom\" value=\"" + files[i].getName().replace(".xls", "") + "\">"
							+ files[i].getName().replace(".xls", "") + "</input><br/>");
				}
				out.print("<br/>");
				out.print("<input type=\"submit\" value=\"OK\"/>");
			%>
		</form>

	</div>

</body>

</html>