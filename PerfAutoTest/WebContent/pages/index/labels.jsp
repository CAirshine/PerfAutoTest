<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Hiboard</title>
<style type="text/css">
a {
	text-decoration: none;
	font-family: 宋体;
	font-size: 20px;
	color: #4A6EAA;
}

a.atag:hover {
	color: red;
}
</style>
</head>
<body bgcolor="#F5F5F5">

	<a
		href="/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename=<%=getServletContext().getRealPath("").replaceAll("\\\\", "/")%>"
		target="detailpage">文件助手</a>
	<br />
	<br />
	<a class="atag" href="/PerfAutoTest/pages/runtests/selectScript.jsp"
		target="detailpage">测试执行</a>
	<br />
	<br />
	<a class="atag" href="/PerfAutoTest/pages/result/excels.jsp"
		target="detailpage">结果查看</a>
	<br />
	<br />
	<a class="atag" href="/PerfAutoTest/runLog.html"
		target="detailpage">执行进度</a>
	<br />
	<br />
</body>
</html>