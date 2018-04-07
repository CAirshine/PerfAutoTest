package web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class UpLoad
 */
@WebServlet("/UpLoad")
public class UpLoad extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UpLoad() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
		writer.write(
				"<header><style type=\"text/css\">"
				+ "body{color:#4A6EAA;font-size:15px;}"
				+ "a{text-decoration:none;color:#4A6EAA;font-size:15px;}"
				+ "a.tag:hover{color:red;}"
				+ "</style></header>");
		writer.write("<body>");
		
		// 检测是否为多媒体上传
		if (!ServletFileUpload.isMultipartContent(request)) {
			// 如果不是则停止?
			writer.println("<a class=\"tag\" href=\"/PerfAutoTest/pages/result/excels.jsp\">返回</a><br/><br/>");
			writer.println("Error: 表单必须包含 enctype=multipart/form-data");
			writer.flush();
			return;
		}

		// 上传文件存储目录
		String UPLOAD_DIRECTORY = "excels";

		// 上传配置
		int MEMORY_THRESHOLD = 1024 * 1024 * 3; // 3MB
		int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
		int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

		// 配置上传参数
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
		factory.setSizeThreshold(MEMORY_THRESHOLD);
		// 设置临时存储目录
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

		ServletFileUpload upload = new ServletFileUpload(factory);

		// 设置最大文件上传值?
		upload.setFileSizeMax(MAX_FILE_SIZE);

		// 设置最大请求值 (包含文件和表单数据)
		upload.setSizeMax(MAX_REQUEST_SIZE);

		// 中文处理
		upload.setHeaderEncoding("UTF-8");

		// 构造完整的上传路径：工程根目录+基于系统的路径分隔符+文件夹
		String uploadPath = getServletContext().getRealPath("/") + UPLOAD_DIRECTORY;

		// 如果目录不存在则创建
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}

		try {
			// 解析请求的内容提取文件数据
			List<FileItem> formItems = upload.parseRequest(request);

			if (formItems != null && formItems.size() > 0) {
				// 迭代表单数据
				for (FileItem item : formItems) {
					if (!item.getName().endsWith(".xls")) {
						writer.println("仅支持xls文件<br/><br/>");
						writer.println("<a class=\"tag\" href=\"/PerfAutoTest/pages/result/excels.jsp\">返回</a><br/><br/>");
						writer.write("</body>");
						writer.flush();
						return;
					}
					// 处理不在表单中的字段
					if (!item.isFormField()) {
						String fileName = new File(item.getName()).getName();
						String filePath = uploadPath + File.separator + fileName;
						File storeFile = new File(filePath);

						// 保存文件到硬盘
						item.write(storeFile);
						writer.println("文件上传成功<br/><br/>");
						writer.println("<a class=\"tag\" href=\"/PerfAutoTest/pages/result/excels.jsp\">返回</a><br/><br/>");
						writer.write("</body>");
						writer.flush();
					}
				}
			}

		} catch (Exception e) {
			writer.println("文件上传异常 : " + e.getMessage() + "<br/><br/>");
			writer.println("<a class=\"tag\" href=\"/PerfAutoTest/pages/result/excels.jsp\">返回</a><br/><br/>");
			writer.write("</body>");
			writer.flush();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
