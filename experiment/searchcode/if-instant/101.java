package org.dftproject.genesis.ui.pages.summary.details;

import org.dftproject.genesis.data.IAttribute;
import org.dftproject.genesis.data.genealogy.GenealogyConstants;
import org.dftproject.genesis.data.genealogy.GenealogyUtils;
import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.INote;
import org.dftproject.genesis.ui.SharedImages;
import org.dftproject.genesis.ui.figures.TooltipLabel;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;

public class DateFigure extends Figure {

	private final Label label;
	
	public DateFigure(IInstant instant) {
		ToolbarLayout layout = new ToolbarLayout(true);
		layout.setSpacing(5);
		setLayoutManager(layout);
		
		if (instant.getMillis() == null) {
			ImageFigure warningFigure = new ImageFigure(SharedImages.getDefault().getImage(SharedImages.IMG_WARNING));
			warningFigure.setToolTip(new TooltipLabel("The date could not be parsed"));
			add(warningFigure);
		}
		
		label = new Label(GenealogyUtils.stringFromInstant(instant));
		add(label);
		
		for (IAttribute attribute : instant.getAttributes()) {
			if (GenealogyConstants.note.equals(attribute.getName())) {
				Object value = attribute.getValue();
				if (value instanceof INote) {
					label.setText(label.getText() + " (" + ((INote) value).getContents() + ")");
				}
			}
		}
	}

}

