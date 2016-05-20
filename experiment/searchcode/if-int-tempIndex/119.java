package com.blogfein.html.elements;
import com.blogfein.html.page.Page;

public class CommentNode extends Node {
	
	private String commentString;
	private boolean isTag = false;
	
	public CommentNode(Page page)
	{
		super();
		this.commentString = "";
		this.page = page;
	}
	
	public String getCommentString()
	{
		return this.commentString;
	}
	
	@Override
	public void parse() {
		char ch;
		StringBuffer sb = new StringBuffer();
		StringBuffer sbx = new StringBuffer();
		
		boolean sqActive = false, dqActive = false;
		while((ch = this.page.getNextChar()) != (char)Page.EOF)
		{
			//super.parsedToken += ch;
			sbx.append(ch);
			if(ch == '<')	// entry point
			{
				if(!sqActive && !dqActive)
				{
					if(this.page.getCurrentChar() == '!')
					{
						this.page.getNextChar();	// move cursor
						
						if(this.page.getCurrentChar() == '-')
						{
							while(this.page.getNextChar() == '-') ;
						}
						else
						{
							this.isTag = true;
							//this.commentString += this.page.getCurrentChar();
						}
						
					}
					else
					{
						//this.commentString += ch;//System.out.println("�̻��� ���� :" + ch);
						sb.append(ch);
					}
				}
				else
				{
					//this.commentString += ch;
					sb.append(ch);
				}
			}
			else if(ch == '\'')
			{
				if(sqActive) sqActive = false;
				else sqActive = true;
				//this.commentString += ch;
				sb.append(ch);
			}
			else if(ch == '\"')
			{
				if(dqActive) dqActive = false;
				else dqActive = true;
				//this.commentString += ch;
				sb.append(ch);
			}
			else if(ch == '>')
			{
				if(!sqActive && !dqActive)
				{
					//System.out.println("����..");
					if(!this.isTag)
					{
						if(this.page.getChar(this.page.getCursor()-2) == '-')
							break;	// end of comment
					}
					else	// Tagó��
					{
						break;
					}
					//this.commentString += ch;
					sb.append(ch);
				}
				else
				{
					//this.commentString += ch;
					sb.append(ch);
				}
			}
			else if(ch == '-')
			{
				if(sqActive || dqActive)
				{
					//this.commentString += ch;	// �ο빮 ���� �����ϴ� ���
					sb.append(ch);
				}
				else
				{
					int tempIndex = this.page.getCursor();
					int iCheck = 0, maxCheck = 5;
					while(this.page.getChar(tempIndex++) != '>' 
						&& iCheck++ < maxCheck)	;
					
					if(iCheck < maxCheck)	// end State
					{
						;
					}
					else	// not end State
					{
						//this.commentString += ch;
						sb.append(ch);
					}
				}
			}
			else
			{
				//this.commentString += ch;
				sb.append(ch);
			}
		}
		super.parsedToken = sbx.toString();
		//this.page.setCursor(-1);
	}
}

