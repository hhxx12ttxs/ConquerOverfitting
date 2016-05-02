package cn.org.act.sdp.bpmnRepository.portal.servlet.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

public class UploadModel extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public UploadModel() {
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
		this.doPost(request, response);
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
		Result xml = new Result();
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			xml.setItem("ret", "false");
			xml.setItem("errMsg", Message.MSG_NOT_MULTIPART_CONTENT);
			Return.print(response, xml.asXML());
			return;
		}
		String action = null;
		int titleId = 0;
		String filename = null;
		String originalUrl = null;
		File imgFile = null;
		int bpmnId = 0;
		String flexFile = "";
		String bpmnFile = "";
		FileItem imgIs = null;
		try {
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Parse the request
			// FileItem
			List items = upload.parseRequest(request);
			// Process the uploaded items
			Iterator iter = items.iterator();

			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					String name = item.getFieldName();
					// get the URL to go back
					if (name.equalsIgnoreCase("bpmnId")) { // get bpmn
						// id
						try {
							bpmnId = Integer.valueOf(item.getString());
						} catch (NumberFormatException e) {
							xml.setItem("ret", "false");
							xml.setItem("errMsg", Message.MSG_WRONG_BPMN_ID);
							Return.print(response, xml.asXML());
							return;
						}
					} else if (name.equalsIgnoreCase("action")) {
						action = item.getString();
					} else if (name.equalsIgnoreCase("titleId")) {
						try {
							titleId = Integer.valueOf(item.getString());
						} catch (NumberFormatException e) {
							xml.setItem("ret", "false");
							xml.setItem("errMsg", Message.MSG_WRONG_TITLE_ID);
							Return.print(response, xml.asXML());
							return;
						}
					} else if (name.equalsIgnoreCase("flex")) {
						flexFile = item.getString();
					} else if (name.equalsIgnoreCase("bpmn")) {
						bpmnFile = item.getString();
					}
				} else {
					String name = item.getFieldName();
					if (name.equalsIgnoreCase("img")) {
						// tempFile = File.createTempFile("temp", "temp");
						imgIs = item;
						// item.write(tempFile);
					}
					// String fileName = item.getName();
					// String contentType = item.getContentType();
					// boolean isInMemory = item.isInMemory();
					// long sizeInBytes = item.getSize();

					// filename = createFileName();
					// File uploadedFile = new File(Config
					// .getPath(Config.bpmnFolderPath)
					// + filename);
					// item.write(uploadedFile);

				}
			}

		} catch (FileUploadException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// store data
		TitleTool titleTool = new TitleTool();
		MetaTool metaTool = new MetaTool();
		BpmnTool bpmnTool = new BpmnTool();
		MetaBean meta = null;
		BpmnBean bpmn = null;
		TitleBean title = null;
		if (action == null) {
			xml.setItem("ret", "false");
			xml.setItem("errMsg", Message.MSG_NO_ACTION);
			Return.print(response, xml.asXML());
			return;
		}
		if (action.equalsIgnoreCase("new")) {
			// title
			title = titleTool.getById(titleId);
			if (title == null) {
				xml.setItem("ret", "false");
				xml.setItem("errMsg", Message.MSG_NO_TITLE_BY_ID);
				Return.print(response, xml.asXML());
				return;
			}
			title.setVersions(title.getVersions() + 1);
			title.setLatest(title.getLatest() + 1);
			titleTool.update(title);
			// bpmn
			bpmn = new BpmnBean();
			bpmn.setBpmnSize(bpmnFile.length());
			bpmn.setFlexSize(flexFile.length());
			bpmn.setFlexStream(new ByteArrayInputStream(flexFile.getBytes()));
			bpmn.setBpmnStream(new ByteArrayInputStream(bpmnFile.getBytes()));
			bpmn.setFlexLocation(new Date().getTime() + ".jpg");
			bpmn.setFlexImgStream(imgIs.getInputStream());
			int id = bpmnTool.saveAll(bpmn);
			// meta
			meta = new MetaBean();
			meta.setId(id);
			meta.setDomainId(title.getDomainId());
			meta.setCreDate(new Date());
			meta.setModDate(new Date());
			meta.setTitleId(title.getId());
			meta.setVersion(title.getLatest());
			metaTool.save(meta);

			if (id > 0) {
				xml.setItem("ret", "true");
				xml.setItem("id", String.valueOf(id));
				Return.print(response, xml.asXML());
				return;
			} else {
				xml.setItem("ret", "false");
				xml.setItem("errMsg", Message.MSG_SAVE_ERROR);
				Return.print(response, xml.asXML());
				return;
			}
			// img
			// imgFile = new File(Config.getPath(Config.imgFolderPath
			// + bpmn.getFlexLocation()));
		} else if (action.equalsIgnoreCase("modify")) {
			// meta
			meta = metaTool.getById(bpmnId);
			meta.setModDate(new Date());
			boolean b = metaTool.update(meta);
			// bpmn
			bpmn = bpmnTool.getBpmnByID(bpmnId);
			bpmn.setBpmnSize(bpmnFile.length());
			bpmn.setFlexSize(flexFile.length());
			bpmn.setFlexStream(new ByteArrayInputStream(flexFile.getBytes()));
			bpmn.setBpmnStream(new ByteArrayInputStream(bpmnFile.getBytes()));
			bpmn.setFlexImgStream(imgIs.getInputStream());
			b &= bpmnTool.updateAll(bpmn);

			if (b) {
				xml.setItem("ret", "true");
				xml.setItem("id", String.valueOf(bpmnId));
				Return.print(response, xml.asXML());
				return;
			} else {
				xml.setItem("ret", "false");
				xml.setItem("errMsg", Message.MSG_SAVE_ERROR);
				Return.print(response, xml.asXML());
				return;
			}
			// imgFile = new File(Config.getPath(Config.imgFolderPath
			// + bpmn.getFlexLocation()));
		} else {
			xml.setItem("ret", "false");
			xml.setItem("errMsg", Message.MSG_UNKNOWN_ACTION);
			Return.print(response, xml.asXML());
			return;
		}
		// save the image with into a file on disk
		// try {
		// imgIs.write(imgFile);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
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

}

