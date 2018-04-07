<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.io.File"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
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
<script type="text/javascript">

function hide(version){
	
	document.getElementById("div1").style.display="none";
	document.getElementById("div2").style.display="";
	
	document.getElementById("atag").href="###";
	document.getElementById("atag").click();
}
</script>
</head>
<body>
<div id="div1">
	<%
		String rootPath = this.getServletContext().getRealPath("/");
		String[] fileNamesTemp = new File(rootPath + "/excels").list();
		
		String[] fileNames = new String[fileNamesTemp.length];
		for(int i=0; i<fileNamesTemp.length; i++){
			fileNames[i] = fileNamesTemp[i].replace(".xls", "");
		}
		
		Arrays.sort(fileNames);
		for (String fileName : fileNames) {
			%>
			<a class="atag" href="/PerfAutoTest/pages/result/excleMessage.jsp?fileName=<%=fileName %>.xls" 
			style="font-size:16px;" 
			target="interfacepage"
			onclick='hide("<%=fileName.replace(".xls", "")%>")'>
			<%=fileName %>
			</a><br/>
			<%
		}
	%>
	<br />
	<br />
	<p style="font-family: 宋体; font-size: 20px; color: #4A6EAA">上传excel文件</p>
	<form action="/PerfAutoTest/UpLoad" method="POST"
		enctype="multipart/form-data" accept="application/vnd.ms-excel">
		<input type="file" name="uploadFile" /><br /> <br /> <input
			type="submit" value="上传" /><br />
	</form>
	<br />
	<br />
</div>
<div id="div2" style="display:none">
	<a id="atag" href="###" target="detailpage" ></a>
</div>
</body>
</html>