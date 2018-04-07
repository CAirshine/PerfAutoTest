<%@page import="utils.Utils"%>
<%@page import="perf.ConfigUtils"%>
<%@page import="perf.Interface"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="perf.RunJmeter"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
body {
	border: 1px #BBBBBB;
	border-bottom-style: solid;
	font-family: 宋体;
	color: #4A6EAA;
}

a {
	text-decoration: none;
	color: #4A6EAA;
	font-size: 20px;
}

a.atag:hover {
	color: red;
}
</style>
</head>
<body>
	<%!String[] interfaceNames;%>
	<%
		if (RunJmeter.runFlag) {
			out.println("ERROR : 测试已在进行中<br><br>");
		} else {
			interfaceNames = request.getParameterValues("interfaceName");
			if (interfaceNames == null) {
				out.println("INFO : 请至少选择一个接口测试<br><br>");
			}
			new Thread() {
				public void run() {
					
					RunJmeter.isCI = false;
					
					for (String interfaceName : interfaceNames) {
						new RunJmeter(ConfigUtils.getInterfaceByName(interfaceName)).start();
					}
				}
			}.start();
		}
	%>
	<br />
	<br />
	<!-- 新窗口打开 -->
	<a class="atag" href="/PerfAutoTest/runLog.html" target="view_window">执行进度</a>
	<br />
	<br />
	<a class="atag" href="selectScript.jsp" target="detailpage">返回</a>
	<br />
	<br />
</body>
</html>