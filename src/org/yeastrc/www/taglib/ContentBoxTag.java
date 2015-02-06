package org.yeastrc.www.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public final class ContentBoxTag extends TagSupport {

	// The title to use in this text area
	private String title = null;
	
	private boolean innerBox = false;
	
	// Set the title to display for this text area
	public void setTitle(String title) {
		this.title = title;
	}

	public void setInnerBox(boolean innerBox) {
		this.innerBox = innerBox;
	}
	
	public int doStartTag() throws JspException {
		try{
			// Get our writer for the JSP page using this tag
			JspWriter writ = pageContext.getOut();
			

			if(!innerBox)
			{
				writ.print("<div id=\"wide-content\">");
				writ.print("<div class=\"box-style1\">");
			}
			else
			{
				writ.print("<div id=\"inner-wide-content\">");
				writ.print("<div class=\"box-style2\">");
			}
			
			
			// If we were supplied w/ a title, print the title box area
			if (this.title != null) {
				writ.print("<DIV CLASS=\"title\">");
				writ.print("<h2><span>");
				writ.print(this.title);
				writ.print("</span></h2>");
				writ.print("</DIV>");
			}
			
			writ.print("<div class=\"content\">");

		}
		catch (Exception e) {
			throw new JspException(e.toString());
		}
		return (EVAL_BODY_INCLUDE);
	}

	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().print("</DIV>");
			pageContext.getOut().print("</div>");
			pageContext.getOut().print("</div>");
		}
		catch (Exception e) {
			throw new JspException(e.toString());
		}
		return (EVAL_PAGE);
	}

	public void release() {
		super.release();
	}

}