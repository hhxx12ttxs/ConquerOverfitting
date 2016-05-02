package su.nsk.iae.reflexdt.ui.editors;

import java.util.ArrayList;

import org.antlr.runtime.tree.TreeIterator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import su.nsk.iae.reflexdt.core.dom.ASTNode;
import su.nsk.iae.reflexdt.core.dom.ASTNodeTreeAdapter;
import su.nsk.iae.reflexdt.core.dom.ControlProcessStatement;
import su.nsk.iae.reflexdt.core.dom.ReflexParser;
import su.nsk.iae.reflexdt.core.dom.RegisterDeclaration;
import su.nsk.iae.reflexdt.core.dom.StateDeclaration;
import su.nsk.iae.reflexdt.core.dom.VariableDeclaration;

public class ReflexCompletionProcessor implements IContentAssistProcessor
{
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];
	
	private int currentOffset;

	//final String[] proposals = IReflexLanguageConstants.KEYWORDS;

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset)
	{
		IDocument document = viewer.getDocument();
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();		
		ReflexModel model = ReflexModel.getModel();
		ASTNode tree = model.getAST();
				
		String prefix = prefix(document, offset);
		char lastChar = '\0';
		try
		{
			lastChar = document.getChar(offset-1);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		
		if (lastChar != ' ' && lastChar != ',' && lastChar != '{' && prefix.isEmpty())
			return NO_COMPLETIONS;
		
		String prevWord1 = lastWord(document, offset);
		String prevWord2 = lastWord(document, currentOffset);
		String indent = null;//lastIndent(document, offset);
			
		if (tree == null)
		{
			System.out.println("tree==null");
			return NO_COMPLETIONS;
		}
		
		ASTNode offsetNode = tree.getElementOnOffset(offset-2);		
		System.out.println(offset + " " + offsetNode);
		System.out.println(prefix);
		System.out.println(prevWord1);
		System.out.println(prevWord2);
		
		//System.out.println(tree.toStringTree());
		
		if (offsetNode.getParent() == null)
			return NO_COMPLETIONS;
		/*
		if ((prevWord1.equals("ÄËß") || prevWord1.equals("FOR"))
				&& offsetNode.getType() == ReflexParser.FOR_ALL_MODIFIER)
		{
			
		}*/
		
		if ((prevWord1.equals("STATE") || prevWord1.equals("ŃÎŃŇ"))
				&& (prevWord2.equals("IN") || prevWord2.equals("Â"))
				&& offsetNode.getType() == ReflexParser.SITUATION)
		{
			System.out.println(33);
			
			result.add(new CompletionProposal("ŔĘŇČÂÍÎĹ", offset, 0, "ŔĘŇČÂÍÎĹ".length()));
			result.add(new CompletionProposal("ĎŔŃŃČÂÍÎĹ", offset, 0, "ĎŔŃŃČÂÍÎĹ".length()));
			result.add(new CompletionProposal("ŃŇÎĎ", offset, 0, "ŃŇÎĎ".length()));
			result.add(new CompletionProposal("ÎŘČÁĘŔ", offset, 0, "ÎŘČÁĘŔ".length()));
			
			ASTNode procDeclNode = null;
			ASTNode sitNode = (ASTNode) offsetNode;			
			String procName = sitNode.getChild(0).getText();
			if (procName.isEmpty())
				return NO_COMPLETIONS;
		
			
			TreeIterator it = new TreeIterator(new ASTNodeTreeAdapter(), tree);
			while(it.hasNext())
			{
				ASTNode t = (ASTNode) it.next();
				if ((t.getType() == ReflexParser.PROCESS_DECL)
						&& (t.getChild(0) != null && t.getChild(0).getText().equals(procName)))
				{
					procDeclNode = t;
					break;
				}
			}
			
			int childCount = procDeclNode.getChildCount(); 
			if (childCount == 1)
				return NO_COMPLETIONS;
			for (int i = 1; i < childCount; i++)
			{						
				ASTNode ch = (ASTNode) procDeclNode.getChild(i);
				if (ch.getType() == ReflexParser.VAR_DECL)
					continue;
				if (ch.getType() == ReflexParser.STATE_DECL)
				{
					String stateName = ((StateDeclaration)ch).getIdentifier().getText();
					result.add(new CompletionProposal(stateName, offset, 0, stateName.length()));							
				}
			}
			
		}
		else if ((prevWord1.equals("STATE") || prevWord1.equals("ŃÎŃŇ"))
				&& (prevWord2.equals("IN") || prevWord2.equals("Â")))
		{
			System.out.println(11);
			ASTNode processDeclNode = offsetNode;
			while(processDeclNode.getType() != ReflexParser.PROCESS_DECL)
			{
				processDeclNode = (ASTNode) processDeclNode.getParent();
			}
			
			int childCount = processDeclNode.getChildCount(); 
			if (childCount == 1)
				return NO_COMPLETIONS;
			for (int i = 1; i < childCount; i++)
			{						
				ASTNode ch = (ASTNode) processDeclNode.getChild(i);
				if (ch.getType() == ReflexParser.VAR_DECL)
					continue;
				if (ch.getType() == ReflexParser.STATE_DECL)
				{
					String stateName = ((StateDeclaration)ch).getIdentifier().getText();
					result.add(new CompletionProposal(stateName, offset, 0, stateName.length()));							
				}
			}
		}		
		else if ((lastChar == '{'
					&& offsetNode.getType() == ReflexParser.PORT_LINK_LIST)
				|| (lastChar == ','
					&& offsetNode.getParent().getType() == ReflexParser.PORT_LINK_LIST))
		{			
			TreeIterator it = new TreeIterator(new ASTNodeTreeAdapter(), tree);
			while(it.hasNext())
			{
				ASTNode t = (ASTNode) it.next();
				if (t.getType() == ReflexParser.REGISTER_DECL)
				{
					String name = ((RegisterDeclaration)t).getIdentifier().getText() + "[]";
					result.add(new CompletionProposal(name, offset - prefix.length(), 
							prefix.length(), name.length() - 1));
				}
			}
		}
		else
		{			
			TreeIterator it = new TreeIterator(new ASTNodeTreeAdapter(), tree);
			while(it.hasNext())
			{
				ASTNode t = (ASTNode) it.next();
				
				if (((prevWord1.equals("PROC") || prevWord1.equals("ĎĐÎÖ"))
						&& (prevWord2.equals("FROM") || prevWord2.equals("ČÇ")
								|| prevWord2.equals("ÄËß") || prevWord2.equals("FOR")))
					|| (lastChar == ',' 
						&& offsetNode.getParent().getType() == ReflexParser.FOR_PROC_LIST_MODIFIER)
					|| ((prevWord1.equals("PROC") || prevWord1.equals("ĎĐÎÖ"))
							&& offsetNode instanceof ControlProcessStatement)
					|| ((prevWord1.equals("PROC") || prevWord1.equals("ĎĐÎÖ"))
							&& offsetNode.getType() == ReflexParser.SITUATION))
				{
					String tokenString = t.getText();
					
					if (t.getParent() != null && t.getParent().getType() == ReflexParser.PROCESS_DECL
							&& t.getType() == ReflexParser.PROCESS_NAME)
					{
						result.add(new CompletionProposal(tokenString, offset - prefix.length(), 
								prefix.length(), tokenString.length()));
					}
				}
				else if ((offsetNode.getParent().getType() == ReflexParser.VAR_REF 
							&& (prevWord2.equals("PROC") || prevWord2.equals("ĎĐÎÖ")))
						|| (lastChar == ',' 
							&& offsetNode.getParent().getType() == ReflexParser.VAR_REF))
				{
					//String processName = offsetNode.getParent().getChild(0).getText();
					
					if (t.getParent() != null && t.getParent().getType() == ReflexParser.PROCESS_DECL
							 && t.getType() == ReflexParser.PROCESS_NAME 
							 // verify process name
							 && t.getText().equals(offsetNode.getParent().getChild(0).getText()))
					{
						ASTNode processDeclNode = (ASTNode) t.getParent();
						int childCount = processDeclNode.getChildCount(); 
						if (childCount == 1)
							return NO_COMPLETIONS;
						for (int i = 1; i < childCount; i++)
						{						
							ASTNode ch = (ASTNode) processDeclNode.getChild(i);
							if (ch.getType() == ReflexParser.STATE_DECL)
								break;
							if (ch.getType() == ReflexParser.VAR_DECL)
							{
								String varName = ((VariableDeclaration)ch).getIdentifier().getText();
								result.add(new CompletionProposal(varName, offset, 0, varName.length()));							
							}
						}
						
						break;
					}
				}
			}
		}		
		
		return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);
	}

	private String prefix(IDocument doc, int offset)
	{
		try
		{
			for (int n = offset - 1; n >= 0; n--)
			{
				char c = doc.getChar(n);
				if (!Character.isJavaIdentifierPart(c))
				{
					return doc.get(n + 1, offset - n - 1);
				}
			}
		}
		catch (BadLocationException e)
		{
			
		}
		return "";
	}
	
	private String lastWord(IDocument doc, int offset)
	{
		try
		{
			int i = offset - 1; 
			for (; i >= 0 && !Character.isJavaIdentifierPart(doc.getChar(i)); i--);
			
			int end = i;
			for (int j = end; j >= 0; j--)
			{
				char c = doc.getChar(j);
				if (!Character.isJavaIdentifierPart(c))
				{
					currentOffset = j;
					return doc.get(j + 1, end - j);
				}
			}
		}
		catch (BadLocationException e)
		{
			
		}
		return "";
	}
	
	private String lastIndent(IDocument doc, int offset) {
        try {
           int start = offset-1; 
           while (start >= 0 && 
              doc.getChar(start)!= '\n') start--;
           int end = start;
           while (end < offset && 
              Character.isSpaceChar(doc.getChar(end))) end++;
           return doc.get(start+1, end-start-1);
        } catch (BadLocationException e) {
           e.printStackTrace();
        }
        return "";
     }


	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters()
	{		
		return new char[]{' ', ',', '{'};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator()
	{
		// TODO Auto-generated method stub
		return null;
	}

}

