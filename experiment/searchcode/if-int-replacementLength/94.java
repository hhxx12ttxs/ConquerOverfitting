package testcodeassistprocessor;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class StaticCompletionProposal implements ICompletionProposal {
	
private static ImageRegistry fgImageRegistry= null;
private static Map< String, ImageDescriptor > fgAvoidSWTErrorMap= null;
private String displayString;
private Image image;
private int replacementOffset;
private int replacementLength;
private int offset;
private String replacementString;
	
	public StaticCompletionProposal(String displayString, Image image, String replacementString, int replacementOffset, int replacementLength) {
		this.displayString= displayString;
		this.image=image;
		this.replacementString=replacementString;
		this.replacementOffset=replacementOffset;
		this.replacementLength=replacementLength;
		this.offset=offset;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(replacementOffset, replacementLength, replacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public String getAdditionalProposalInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayString() {
		return displayString;
	}

	@Override
	public Image getImage() {
		return image;
	}
	
	@Override
	public Point getSelection(IDocument document) {
		// TODO Auto-generated method stub
		return null;
	}
	
	static ImageRegistry getImageRegistry() {
		if (fgImageRegistry == null) {
			fgImageRegistry= new ImageRegistry();
			for (Iterator< String > iter= fgAvoidSWTErrorMap.keySet().iterator(); iter.hasNext();) {
				String key= iter.next();
				fgImageRegistry.put(key, fgAvoidSWTErrorMap.get(key));
			}
			fgAvoidSWTErrorMap= null;
		}
		return fgImageRegistry;
	}

}

