package cn.org.act.sdp.bpmnRepository.portal.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import cn.org.act.sdp.bpmnRepository.entity.BpmnBean;
import cn.org.act.sdp.bpmnRepository.entity.MetaBean;
import cn.org.act.sdp.bpmnRepository.entity.TitleBean;
import cn.org.act.sdp.bpmnRepository.portal.constants.Message;
import cn.org.act.sdp.bpmnRepository.portal.util.Return;
import cn.org.act.sdp.bpmnRepository.tool.BpmnTool;
import cn.org.act.sdp.bpmnRepository.tool.MetaTool;
import cn.org.act.sdp.bpmnRepository.tool.TitleTool;

public class NewModel extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public NewModel() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			System.out.println("not");
			Return.error(request, response, Message.MSG_NOT_MULTIPART_CONTENT);
			return;
		} else
			System.out.println("yes");
		TitleBean title = null;
		String action = null;
		int version = 0;
		String filename = null;
		String originalUrl = null;
		File tempFile = null;
		try {
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Parse the request
			/* FileItem */
			List items = upload.parseRequest(request);
			// Process the uploaded items
			Iterator iter = items.iterator();

			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					String name = item.getFieldName();
					// get the URL to go back
					if (name.equalsIgnoreCase("url")) {
						originalUrl = item.getString();
					} else if (name.equalsIgnoreCase("bpmnId")) { // get bpmn
						// id
						title = getBPMN(Integer.valueOf(item.getString()));
						if (title == null) {
							Return.error(request, response,
									Message.MSG_NO_BPMN_ID);
							return;
						}
					} else if (name.equalsIgnoreCase("action")) {
						action = item.getString();
					} else if (name.equalsIgnoreCase("version")) {
						version = Integer.valueOf(item.getString());
					}
				} else {
					// String fieldName = item.getFieldName();
					// String fileName = item.getName();
					// String contentType = item.getContentType();
					// boolean isInMemory = item.isInMemory();
					// long sizeInBytes = item.getSize();

					// filename = createFileName();
					// File uploadedFile = new File(Config
					// .getPath(Config.bpmnFolderPath)
					// + filename);
					// item.write(uploadedFile);
					tempFile = File.createTempFile("temp", "temp");
					item.write(tempFile);
				}
			}

		} catch (FileUploadException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MetaTool metaTool = new MetaTool();
		BpmnTool bpmnTool = new BpmnTool();
		MetaBean meta = new MetaBean();
		BpmnBean bpmn = new BpmnBean();
		meta.setDomainId(title.getId());
		// bpmn.setBpmnLocation(filename);
		bpmn.setBpmnStream(new FileInputStream(tempFile));
		meta.setCreDate(new Date());
		meta.setModDate(new Date());
		meta.setTitleId(title.getId());
		meta.setVersion(0);
		bpmn.setBpmnSize(tempFile.length());

		if (action.equalsIgnoreCase("newVersion")) {
			title.setLatest(title.getLatest() + 1);
			title.setVersions(title.getVersions() + 1);
			meta.setVersion(title.getLatest());
			new TitleTool().update(title);
			int id = metaTool.save(meta);
			// metaTool.getVersionByTitle(title, meta.getVersion());
			// meta = title.getMetas().get("v"+meta.getVersion());
			bpmn.setId(id);
			bpmnTool.save(bpmn);
			bpmnTool.saveContent(bpmn, "bpmn");
			// delete temp file
			tempFile.delete();
		} else if (action.equalsIgnoreCase("modify")) {
			// TODO modify action
		}

		response.sendRedirect(originalUrl);
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

	private String createFileName() {
		Date date = new Date();
		return String.valueOf(date.getTime()) + ".bpmn";
	}

	private TitleBean getBPMN(int id) {
		TitleTool tool = new TitleTool();
		return tool.getById(id);
	}
}

