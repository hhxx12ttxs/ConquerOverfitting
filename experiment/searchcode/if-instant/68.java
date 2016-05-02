package org.dftproject.genesis.ui.widgets;

import java.util.Set;

import org.dftproject.genesis.data.genealogy.GenealogyConstants;
import org.dftproject.genesis.data.genealogy.GenealogyUtils;
import org.dftproject.genesis.data.genealogy.IEvent;
import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.IName;
import org.dftproject.genesis.data.genealogy.IPerson;
import org.dftproject.genesis.data.genealogy.IPlace;
import org.dftproject.genesis.data.genealogy.IRole;
import org.dftproject.genesis.data.genealogy.Sex;
import org.dftproject.genesis.ui.SharedImages;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class PersonToolTip extends ToolTip {

	public static final String HEADER_FONT = Policy.JFACE + ".TOOLTIP_HEAD_FONT";

	static {
		JFaceResources.getFontRegistry().put(PersonToolTip.HEADER_FONT, JFaceResources.getFontRegistry().getBold(JFaceResources.getDefaultFont().getFontData()[0].getName()).getFontData());
	}

	private IPerson person;

	public PersonToolTip(Control control) {
		super(control);
	}

	public PersonToolTip(Control control, int style, boolean manualActivation) {
		super(control, style, manualActivation);
	}

	@Override
	public Point getLocation(Point tipSize, org.eclipse.swt.widgets.Event event) {
		if (event.widget instanceof Control) {
			Point location = ((Control) event.widget).toDisplay(0, 0);
			location.y += ((Control) event.widget).getSize().y;
			return location;
		}
		return new Point(event.x, event.y);
	}

	@Override
	protected Composite createToolTipContentArea(org.eclipse.swt.widgets.Event e, Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		container.setBackgroundMode(SWT.INHERIT_DEFAULT);

		GridLayout layout = new GridLayout();
		layout.marginTop = layout.marginRight = layout.marginBottom = layout.marginLeft = layout.marginWidth = layout.marginHeight = 2;
		container.setLayout(layout);

		CLabel clabel = new CLabel(container, SWT.NONE);
		clabel.setFont(JFaceResources.getFontRegistry().get(HEADER_FONT));
		clabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Sex sex = (Sex) GenealogyUtils.getValue(person, GenealogyConstants.sex, Sex.class);
		if (sex != null) {
			if (sex == Sex.Male)
				clabel.setImage(SharedImages.getDefault().getImage(SharedImages.IMG_MALE));
			else
				clabel.setImage(SharedImages.getDefault().getImage(SharedImages.IMG_FEMALE));
		}

		IName name = (IName) GenealogyUtils.getValue(person, GenealogyConstants.name, IName.class);
		if (name != null && !"".equals(name))
			clabel.setText(GenealogyUtils.stringFromName(name));
		else
			clabel.setText("(Unknown)");

		Composite eventsComposite = null;
		
		Set<IRole> roles = person.getRoles(new String[] { GenealogyConstants.Child, GenealogyConstants.Deceased });
		for (IRole role : roles) {
			String type = role.getType();
			if (GenealogyConstants.Child.equals(type)) {
				IEvent event = role.getEvent();
				if (event != null) {
					IInstant instant = GenealogyUtils.getDate(event);
					IPlace place = GenealogyUtils.getPlace(event);
	
					if (instant != null || place != null) {
						if (eventsComposite == null)
							eventsComposite = createEventsComposite(container);
			
						Label label = new Label(eventsComposite, SWT.NONE);
						label.setText("Born:");
						label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
						label = new Label(eventsComposite, SWT.NONE);
						label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
						if (instant != null)
							label.setText(GenealogyUtils.stringFromInstant(instant));
			
						label = new Label(eventsComposite, SWT.NONE);
						label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
						if (place != null)
							label.setText(place.toString());
					}
				}
			} else if (GenealogyConstants.Deceased.equals(type)) {
				IEvent event = role.getEvent();
				if (event != null) {
					IInstant instant = GenealogyUtils.getDate(event);
					IPlace place = GenealogyUtils.getPlace(event);
	
					if (instant != null || place != null) {
						if (eventsComposite == null)
							eventsComposite = createEventsComposite(container);
			
						Label label = new Label(eventsComposite, SWT.NONE);
						label.setText("Died:");
						label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
						label = new Label(eventsComposite, SWT.NONE);
						label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
						if (instant != null)
							label.setText(GenealogyUtils.stringFromInstant(instant));
			
						label = new Label(eventsComposite, SWT.NONE);
						label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
						if (place != null)
							label.setText(place.toString());
					}
				}
			}
		}
		
		Label helpLabel = new Label(container, SWT.NONE);
		helpLabel.setFont(JFaceResources.getFontRegistry().get(HEADER_FONT));
		helpLabel.setText(LegacyActionTools.findModifierString(SWT.CTRL) + "+Click to open in new tab");

		return container;
	}

	protected Composite createEventsComposite(Composite parent) {
		Composite events = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginTop = layout.marginRight = layout.marginBottom = layout.marginLeft = layout.marginWidth = layout.marginHeight = 0;
		layout.horizontalSpacing = 12;
		layout.verticalSpacing = 4;
		layout.numColumns = 3;
		events.setLayout(layout);
		events.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		return events;
	}

	public void setPerson(IPerson person) {
		this.person = person;
	}

}

