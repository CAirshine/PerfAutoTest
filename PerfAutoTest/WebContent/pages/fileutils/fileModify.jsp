<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.OutputStreamWriter"%>
<%@page import="java.io.BufferedWriter"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
p {
	color: #4A6EAA;
	font-size: 20px;
}

a {
	text-decoration: none;
	font-family: 宋体;
	font-size: 20px;
	color: #4A6EAA;
}

a:hover {
	color: red;
}
</style>
</head>
<body>
	<%
		String data = request.getParameter("data");
		data = new String(data.getBytes("iso-8859-1"),"utf-8");
		String filePath = request.getParameter("filePath");
		out.print("<a href='/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename=" + filePath + "'>返回上一级</a><br/>");

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath), "utf-8"));
		writer.write(data);
		writer.flush();
		System.out.println(data);
		writer.close();
		out.print("<p>修改成功</p>");
	%>
</body>
</html>