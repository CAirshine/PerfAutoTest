<%@page import="perf.ConfigUtils"%>
<%@page import="java.io.File"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
body {
	color: #4A6EAA;
	font-size: 15px;
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
</head>
<body>
	<%
		String fileName = request.getParameter("fileName");
		if (new File(ConfigUtils.rootPath + "/excels/" + fileName).delete()) {
			out.print(fileName + "文件删除成功");
		} else {
			out.print(fileName + "文件删除失败");
		}
	%>
	<br>
	<br>
	<a class="atag" href="excels.jsp" target="detailpage">返回</a>
</body>
</html>