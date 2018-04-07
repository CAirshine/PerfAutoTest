<%@page import="java.io.File"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
a.a1 {
	font-family: 宋体;
	font-size: 20px;
	color: #4A6EAA;
}

a.a2 {
	text-decoration: none;
	font-family: 宋体;
	font-size: 20px;
	color: #4A6EAA;
}

a:hover {
	color: red;
}

p {
	color: #4A6EAA;
}
</style>
</head>
<body>
	<%
		String filePath = request.getParameter("filename");
		File file = new File(filePath);

		//out.print(filePath + "<br/>");
		//out.print(filePath.substring(0, filePath.lastIndexOf("/", filePath.length() - 2) + 1) + "<br/>");

		if (filePath.contains("/")) {
			out.print("<a class='a2' href='/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename="
					+ filePath.substring(0, filePath.lastIndexOf("/", filePath.length() - 2) + 1)
					+ "'>返回上一级</a><br/><br/>");
		}

		if (!file.exists()) {
			out.print("<p>已达到根目录</p>");
			out.print("<a class='a2' href='/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename="
					+ getServletContext().getRealPath("").replaceAll("\\\\", "/") + "'>返回</a>");
			return;
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					out.print("<a class='a1' href='/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename="
							+ f.getAbsolutePath().replaceAll("\\\\", "/") + "'>" + f.getName() + "</a><br/>");
				} else {
					out.print("<a class='a2' href='/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename="
							+ f.getAbsolutePath().replaceAll("\\\\", "/") + "'>" + f.getName() + "</a><br/>");
				}

			}
		} else {
			request.setAttribute("filename", file.getAbsolutePath().replaceAll("\\\\", "/"));
			request.getRequestDispatcher("fileDetail.jsp").forward(request, response);
		}
	%>
</body>
</html>