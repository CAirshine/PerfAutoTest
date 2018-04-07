<%@page import="utils.Utils"%>
<%@page import="utils.MyOutputStream"%>
<%@page import="perf.Interface"%>
<%@page import="java.util.List"%>
<%@page import="perf.ConfigUtils"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
<script type="text/javascript">
function CI(){
       document.forms.xxxFrom.action="CI.jsp";
       document.forms.xxxFrom.submit();
}
</script>
<style type="text/css">
div {
	border: 1px #BBBBBB;
	border-bottom-style: solid;
	font-family: 宋体;
	color: #4A6EAA;
}

p {
	color: #4A6EAA;
	font-size: 15px;
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
	<%
		List<Interface> interfaces = ConfigUtils.getInterfaces();
		MyOutputStream.getInstance();
	%>
	<div>
		<br />
		<!-- 改为新窗口打开 -->
		<a class="atag" href="clearLog.jsp">清空日志</a> <br /> <br />
	</div>

	<div>
		<br /> <a class="atag" href="kill.jsp">强制停止</a> <br /> <br />
	</div>
	<div>
		<br />
		<!-- 选择接口测试 -->
		<a>选择接口：</a>
		<form id="xxxFrom" action="execScript.jsp" method="post">
			<table>
				<%
					for (Interface interFace : interfaces) {
						out.print("<tr><td><input type=\"checkbox\" name=\"interfaceName\" value=" + interFace.tag + ">"
								+ interFace.desc + "</td></tr>");
					}
				%>
			</table>
			<input type="submit" value="Start"
				style="color: #000000; background: #FFFFFF;" />
			<input type="button" value="CI" 
			style="color: #000000; background: #FFFFFF;" onclick="CI();" />
		</form>
	</div>
</body>
</html>