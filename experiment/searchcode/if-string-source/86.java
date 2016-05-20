package pl.zgora.uz.wmie.fe.gui.common.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import pl.zgora.uz.wmie.fe.gui.common.bean.ColumnBean;

public class Column implements Tag {
	private PageContext pageContext;
	private Tag parent;
	private String label;
	private String sort;
	private String databaseColumn;
	private String visible;
	private String width;
	private String source;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public int doStartTag() throws JspException {
		Table table = (Table) getParent().getParent();
		ColumnBean columnBean = new ColumnBean();
		columnBean.setColumn(databaseColumn);
		columnBean.setLabel(label);
		columnBean.setVisible(visible);
		columnBean.setWidth(width);
		columnBean.setSort(sort);
		columnBean.setSource(source);
		table.getColumnList().add(columnBean);
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	@Override
	public Tag getParent() {
		return parent;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPageContext(PageContext arg0) {
		this.pageContext = arg0;

	}

	@Override
	public void setParent(Tag arg0) {
		this.parent = arg0;

	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDatabaseColumn() {
		return databaseColumn;
	}

	public void setDatabaseColumn(String databaseColumn) {
		this.databaseColumn = databaseColumn;
	}

	public String getVisible() {
		return visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}
	// private boolean contains(ColumnBean columnBean,List<ColumnBean> list){
	// boolean result = false;
	// for(ColumnBean f : list){
	// if(columnBean.getColumn().equals(f.getColumn())){
	// return true;
	// }
	// }
	// return result;
	//		
	// }

}

