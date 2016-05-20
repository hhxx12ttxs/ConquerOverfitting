package pl.zgora.uz.wmie.fe.tag;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import pl.zgora.uz.wmie.fe.bean.ButtonBean;
import pl.zgora.uz.wmie.fe.bean.ColumnBean;
import pl.zgora.uz.wmie.fe.bean.FilterBean;
import pl.zgora.uz.wmie.fe.statics.StaticFIelds;
import pl.zgora.uz.wmie.fe.util.MessageUtil;
import pl.zgora.uz.wmie.fe.util.ValidatorUtil;
import pl.zgora.uz.wmie.fe.action.ColumnLabels;

public class Table implements Tag {
	private PageContext pageContext;
	private Tag parent;
	private String id;
	private List<FilterBean> filters = new ArrayList<FilterBean>();
	private String filtersArrayJS = "";
	private String tableAjaxActionClass;
	private String tableAjaxActionMethod;
	private String defaultColumnSortDir;
	private String defaultColumnSort;
	private String buttons;
	private String title;
	private List<ButtonBean> buttonList = new ArrayList<ButtonBean>();
	private List<ColumnBean> columnList = new ArrayList<ColumnBean>();

	int i = 0;

	public Table() {
		super();
	}
	public int doStartTag() throws JspException {
		System.out.println("pocztek");
		this.filters.clear();
		this.buttonList.clear();
		this.columnList.clear();
		return EVAL_BODY_INCLUDE;
	}

	public int doEndTag() throws JspException {
		String findClickAction = "";
		ColumnLabels label=new ColumnLabels();
		String labelText="";
		String findClickActionSort = "";
		try {
			Writer out = pageContext.getOut();
			out.write("<div id=\"" + id
					+ "ContentField\" class=\"tableContentField\">");
			
			out.write("<input type=\"hidden\" id=\"" + this.id
					+ "Sort\" value=\""+this.defaultColumnSort+"\"> "
					+ "<input type=\"hidden\" id=\"" + this.id
					+ "SortDir\" value=\""+this.defaultColumnSortDir+"\">");
			
			out.write("<div id=\"" + id
					+ "FilterField\" class=\"tableFilterField\">");

			for (FilterBean element : filters) {
				String filerHtml =element.getBody().replace(element.getName(), element.getName()+id);
				String filterId = element.getName() + id;
				out.write(filerHtml);
				if (!"".equals(filtersArrayJS)) {
					filtersArrayJS = filtersArrayJS + ",'" + filterId
							+ "'";
				} else {
					filtersArrayJS = "'" + filterId + "'";
				}
			}
			
			
			String projections = "";
			for (ColumnBean columnBean : columnList) {
				if (!ValidatorUtil.isBlankOrNull(projections)) {
					projections += ",";
				}
				projections = projections + "'" + columnBean.getColumn() + "'";
			}
			findClickAction = "\"tableRefresh('" + id + "',new Array("
					+ filtersArrayJS + "),new Array(" + projections + "), "
					+ tableAjaxActionClass + "." + tableAjaxActionMethod
					+ ")\"";
			out.write("<button id=\"" + id + "FindButton\" onclick="
					+ findClickAction + ">znajdz</button>");
			out.write("</div>");
			String filterParameter = "div#" + id + "ContentField div#" + id
					+ "FilterField";
			out.write("<table id=\"" + id
					+ "\" border=\"1\" class=\"standardTable\">");
			if (title != null) {
				out.write("<caption onclick=\"switchFilter('" + filterParameter
						+ "')\">" + title);
				out.write("<button  class=\"filterTableButton\"></button>");
				out.write("</caption>");
			}
			out.write("<tr>");
			for (ColumnBean columnBean : columnList) {
				//wyswietla label jesli nie ma w tablicy wartosci odpowiadajacych
				//parametrom. Mozna zmodyfikowac, do wyswietlania komunikatu.
				if (columnBean.getLabel()==null) {
						labelText=label.getLabelByName(columnBean.getColumn(),columnBean.getSource());
					}
					else {
						labelText=(columnBean.getLabel());	
					}
				
				if (StaticFIelds.NO.equals(columnBean.getVisible())) {
					out.write("<th class=\"hideTableColumn\">"
							+ columnBean.getLabel() + "</th>");
				} else {
					// OBSLUGA SORTOWANIA
					if (StaticFIelds.NO.equals(columnBean.getSort())
							|| columnBean.getSort() == null) {
						out.write("<th style=\"width:" + columnBean.getWidth()
								+ "\">");
						out.write(labelText);
						out.write("</th>");
					} else {
						findClickActionSort = "tableRefresh('" + id
								+ "',new Array(" + filtersArrayJS
								+ "),new Array(" + projections + "),"
								+ tableAjaxActionClass + "."
								+ tableAjaxActionMethod + ")";
						out
								.write("<th onclick=\"sortBy('"
										+ columnBean.getColumn()
										+ "','"
										+ this.id
										+ ""
										+ "'),"
										+ findClickActionSort
										+ "\" style=\"cursor:pointer; width:"
										+ columnBean.getWidth()
										+ "\">");
						out
								.write(labelText);
						String dnSortIconClass=null;
						if (this.defaultColumnSort!=null && this.defaultColumnSortDir=="desc" && this.defaultColumnSort==columnBean.getColumn()) {
							dnSortIconClass="sortIconVisible";
						}
						else {
							dnSortIconClass="sortIconHide";
						}
						out
								.write("<div class=\""+dnSortIconClass+"\" id=\"dn"
										+ this.id
										+ columnBean.getColumn()
										+ "\">");
						out
								.write("<img src=\"/FE.presentation/pages/include/css/images/sort_dn.gif\" />");
						out
								.write("</div>");
						String upSortIconClass=null;
						if ((this.defaultColumnSort!=null && this.defaultColumnSortDir=="asc" && this.defaultColumnSort==columnBean.getColumn()) ||
						    (this.defaultColumnSort!=null && this.defaultColumnSortDir==null && this.defaultColumnSort==columnBean.getColumn()))
						{
							upSortIconClass="sortIconVisible";
						}
						else {
							upSortIconClass="sortIconHide";
						}
						out
								.write("<div class=\""+upSortIconClass+"\" id=\"up"
										+ this.id
										+ columnBean.getColumn()
										+ "\">");
						out
								.write("<img src=\"/FE.presentation/pages/include/css/images/sort_up.gif\" />");
						out
								.write("</div>");
						out		
								.write("</th>");
					}
				}
			}
			out.write("</tr>");

			out.write("</table>");
			out.write("<div class=\"tableFooter\">");
				out.write("<div class=\"tableButtonField\">");
				if (StaticFIelds.YES.equals(buttons)) {
					for (ButtonBean buttonBean : buttonList) {
						out.write("<button onclick=\"" + buttonBean.getAction()
								+ "\">" + MessageUtil.getMessage(buttonBean.getLabel(),pageContext.getRequest().getLocale()) + "</button>");
					}
				}else{
					out.write("&nbsp;");
				}
				out.write("</div>");
			out.write("<div class=\"tableNavigation\">");
			out.write("<button class=\"firstPage\" onclick=\"changePage('" + id
					+ "','first')\"></button>");
			out.write("<button class=\"prevPage\" onclick=\"changePage('" + id
					+ "','prev')\"></button>");
			out.write("<input type=\"text\" id=\"" + id
					+ "PageNum\" class=\"tablePageNum\" value=\"1\" disabled=\"disabled\">");
			out.write(" <b>\\</b> <input type=\"text\" id=\"" + id
					+ "PageLastNum\" class=\"tablePageNum\" value=\"1\" disabled=\"disabled\">");
			out.write("<button class=\"nextPage\" onclick=\"changePage('" + id
					+ "','next')\"></button>");
			out.write("<button class=\"lastPage\" onclick=\"changePage('" + id
					+ "','last')\"></button>");
			out.write("</div>");

			out.write("</div>");

			out.write("</div>");

			out.write("<script>");
			out.write(findClickAction.replace("\"", ""));
			out.write("</script>");
		} catch (Exception e) {
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		filters.clear();
		filtersArrayJS = "";
		return EVAL_PAGE;
	}


	public Tag getParent() {
		return parent;
	}

	public void release() {
		System.out.println("wypuszczenie");

	}

	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;

	}

	public void setParent(Tag parent) {
		this.parent = parent;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<FilterBean> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterBean> filters) {
		this.filters = filters;
	}

	protected String getFiltersArrayJS() {
		return filtersArrayJS;
	}

	protected void setFiltersArrayJS(String filtersArrayJS) {
		this.filtersArrayJS = filtersArrayJS;
	}

	public String getTableAjaxActionClass() {
		return tableAjaxActionClass;
	}

	public void setTableAjaxActionClass(String tableAjaxActionClass) {
		this.tableAjaxActionClass = tableAjaxActionClass;
	}

	public String getButtons() {
		return buttons;
	}

	public void setButtons(String buttons) {
		this.buttons = buttons;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<ButtonBean> getButtonList() {
		return buttonList;
	}

	public List<ColumnBean> getColumnList() {
		return columnList;
	}

	public String getTableAjaxActionMethod() {
		return tableAjaxActionMethod;
	}

	public void setTableAjaxActionMethod(String tableAjaxActionMethod) {
		this.tableAjaxActionMethod = tableAjaxActionMethod;
	}

	public String getDefaultColumnSort() {
		return defaultColumnSort;
	}

	public void setDefaultColumnSort(String defaultColumnSort) {
		this.defaultColumnSort = defaultColumnSort;
	}

	public String getDefaultColumnSortDir() {
		return defaultColumnSortDir;
	}

	public void setDefaultColumnSortDir(String defaultColumnSortDir) {
		this.defaultColumnSortDir = defaultColumnSortDir;
	}

}

