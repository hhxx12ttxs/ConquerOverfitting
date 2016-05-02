package cn.org.act.sdp.bpmnRepository.portal.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.org.act.sdp.bpmnRepository.entity.AnnotationBean;
import cn.org.act.sdp.bpmnRepository.entity.DomainBean;
import cn.org.act.sdp.bpmnRepository.entity.MetaBean;
import cn.org.act.sdp.bpmnRepository.entity.TagBean;
import cn.org.act.sdp.bpmnRepository.entity.TitleBean;
import cn.org.act.sdp.bpmnRepository.entity.UserBean;
import cn.org.act.sdp.bpmnRepository.portal.config.Config;
import cn.org.act.sdp.bpmnRepository.portal.constants.Message;
import cn.org.act.sdp.bpmnRepository.tool.AnnotationTool;
import cn.org.act.sdp.bpmnRepository.tool.BpmnTool;
import cn.org.act.sdp.bpmnRepository.tool.DomainTool;
import cn.org.act.sdp.bpmnRepository.tool.MetaTool;
import cn.org.act.sdp.bpmnRepository.tool.TagTool;
import cn.org.act.sdp.bpmnRepository.tool.TitleTool;
import cn.org.act.sdp.bpmnRepository.tool.UserTool;

public class AProcess extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public AProcess() {
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
		// check parameters
		String titleIdStr = request.getParameter("id");
		if (titleIdStr == null) {
			error(request, response, Message.MSG_NO_TITLE_ID);
			return;
		}
		Integer titleId;
		try {
			titleId = Integer.valueOf(titleIdStr);
		} catch (NumberFormatException e) {
			error(request, response, Message.MSG_WRONG_TITLE_ID);
			return;
		}
		// get version
		Integer version;
		String versionStr = request.getParameter("version");
		if (versionStr == null) {
			version = 0;
		} else {
			try {
				version = Integer.valueOf(versionStr);
			} catch (NumberFormatException e) {
				error(request, response, Message.MSG_WRONG_VERSION);
				return;
			}
		}

		// initialize the database tools
		TitleTool titleTool = new TitleTool();
		DomainTool domainTool = new DomainTool();
		MetaTool metaTool = new MetaTool();
		BpmnTool bpmnTool = new BpmnTool();
		TagTool tagTool = new TagTool();
		AnnotationTool annotationTool = new AnnotationTool();
		UserTool userTool = new UserTool();

		// get the title bean
		TitleBean title = titleTool.getById(titleId);
		if (title == null) {
			error(request, response, Message.MSG_NO_BPMN_BY_ID);
			return;
		}
		// get the tags of the title
		List<TagBean> tagList = tagTool.getByTitleId(title.getId());

		// get the latest version model of the title
		if (version == 0) {
			metaTool.getLatestByTitle(title);
		} else {
			metaTool.getVersionByTitle(title, version);
		}
		MetaBean meta = title.getMetas().get("v" + version);
		bpmnTool.getByMeta(meta);
		// get the domain which including the title
		DomainBean domain = domainTool.getDomainById(title.getDomainId());

		// get the annotation list
		List<AnnotationBean> annotationList = null;
		if (meta != null) {
			annotationList = annotationTool.getByBpmn(meta.getId());
			UserBean userTemp;
			if (annotationList != null) {
				for (AnnotationBean bean : annotationList) {
					userTemp = userTool.getUserById(bean.getUserId());
					if (userTemp != null) {
						bean.setAuthor(userTemp.getName());
					}
				}
			}
		}

		// set the information what is going to be shown
		request.setAttribute("meta", meta);
		request.setAttribute("title", title);
		request.setAttribute("domain", domain);
		request.setAttribute("tagList", tagList);
		request.setAttribute("annotationList", annotationList);
		if (meta != null && meta.getBpmn() != null)
			request.setAttribute("modelPath", Config.imgFolderPath
					+ meta.getBpmn().getFlexLocation());
		request.getRequestDispatcher("a_process.jsp")
				.forward(request, response);
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

	private void error(HttpServletRequest request,
			HttpServletResponse response, String msg) {
		request.setAttribute("msg", msg);
		try {
			request.getRequestDispatcher("error.jsp")
					.forward(request, response);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}
}

