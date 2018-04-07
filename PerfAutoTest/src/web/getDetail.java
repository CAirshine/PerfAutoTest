package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import perf.ExcelToJson;

/**
 * Servlet implementation class getDetail
 */
@WebServlet("/getDetail")
public class getDetail extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getDetail.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public getDetail() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		response.setContentType("application/json");

		String excelName = request.getParameter("excelName");
		String sheetName = request.getParameter("sheetName");
		logger.info("getDetail url解码前: " + excelName + "  " + sheetName);
		if (!(java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(sheetName))) { // 判断字符串是否乱码，这个方法的可靠性有待考证
			excelName = new String(request.getParameter("excelName").getBytes("ISO-8859-1"), "utf-8");
			sheetName = new String(request.getParameter("sheetName").getBytes("ISO-8859-1"), "utf-8");
		}
		logger.info("getDetail url解码后: " + excelName + "  " + sheetName);

		PrintWriter writer = response.getWriter();
		writer.write(new ExcelToJson().excelToJson(excelName, sheetName).toJSONString());
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
