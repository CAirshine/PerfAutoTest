<%@page import="org.apache.log4j.Logger"%>
<%@page import="utils.EncodingDetect"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.File"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<style type="text/css">
textarea {
	width: 100%;
	height: 400px;
	resize: none;
	wrap: off
}

input {
	width: 100%;
}

p {
	color: #4A6EAA;
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
		Logger logger = Logger.getLogger(this.getClass());

		String filePath = (String) request.getAttribute("filename");
		File file = new File(filePath);

		//out.print(filePath + "<br/>");
		//out.print(filePath.substring(0, filePath.lastIndexOf("/", filePath.length() - 2) + 1) + "<br/>");

		out.print("<a href='/PerfAutoTest/pages/fileutils/viewFiles.jsp?filename="
				+ filePath.substring(0, filePath.lastIndexOf("/", filePath.length() - 2) + 1) + "'>返回上一级</a><br/>");

		/* 改成直接判断文件格式，只要是utf-8格式的就可以打开
		if (!(file.getName().endsWith(".xml") || file.getName().endsWith(".ini") || file.getName().endsWith(".log")
				|| file.getName().endsWith(".yaml") | file.getName().endsWith(".txt")
						| file.getName().endsWith(".html") | file.getName().endsWith(".jsp")
						| file.getName().endsWith(".java") | file.getName().endsWith(".sh")
						| file.getName().endsWith(".jmx") | file.getName().endsWith(".py")
						| file.getName().endsWith(".js") | file.getName().endsWith(".properties")
						| file.getName().endsWith(".bat") | file.getName().endsWith(".dat"))) {
			out.print("<p>仅支持查看&修改文本文件，请选择有效文件<p>");
			return;
		}
		*/

		logger.info("文件" + file.getName() + "大小为 : " + file.length());
		
		if (file.length() != 0) {
			
			String characterString = "NA";
			try {
				characterString = EncodingDetect.getJavaEncode(filePath);
			} catch (Exception e) {
				characterString = "解析文件格式异常 : " + e.getMessage();
			}
			
			logger.info("正在查看的文件" + file.getName() + "编码格式为 : " + characterString);
			if (!(characterString.equals("UTF-8") || characterString.equals("ASCII"))) {
				out.print("<p>正在查看的文件" + file.getName() + "编码格式为 : " + characterString + "</p>");
				out.print("<p>仅支持查看&修改utf-8格式的文件，请选择有效文件<p>");
				return;
			}
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
		String line = "";
		String data = "";

		while ((line = reader.readLine()) != null) {
			data = data + line + System.getProperty("line.separator");
		}
		reader.close();
	%>
	<form action="fileModify.jsp" method="post">
		<p>文件路径</p>
		<input type="text" name="filePath" value=<%=filePath%>>
		<p>文件内容</p>
		<textarea name="data"><%=data%></textarea>
		<br /> <input type="submit" value="确认修改"
			style="color: red; height: 40px">
	</form>
</body>
</html>