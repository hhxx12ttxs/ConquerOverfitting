/*
 * Created on 1-nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.suijten.bordermaker;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import semantica.util.swing.EscapeDialog;
import semantica.util.swing.Gbc;
import semantica.util.swing.HeaderPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author OrbitZ
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ProgressDialog extends EscapeDialog implements BorderMakerListener {
	private JLabel from;

	private JLabel to;

	private JLabel icon;

//	private JLabel subMessage;
	private String submsg;
	
	private JLabel totalProgressLabel;
	private JLabel uploadProgressLabel;

	private JProgressBar totalProgress;
	private JProgressBar uploadProgress;

	private JButton cancel;
	private JButton pause;

	private BorderMakerBean bean;

	private BorderMakerProcessor worker = null;

	private Thread workerThread = null;

	private boolean paused = false;
	private ImageScollpane iconScroll;

	private int totalImages = 0;

	private JPanel p2;

	private int count;

	private int pending;

	public ProgressDialog(BorderMakerBean bean) throws HeadlessException {
		super(BorderMaker.mainFrame, true);
		this.bean = bean;
		initializeComponents();
	}

	private void initializeComponents() {
		totalProgressLabel = new JLabel();
		uploadProgressLabel = new JLabel();
//		subMessage = new JLabel();
		iconScroll = new ImageScollpane();
		totalProgress = new JProgressBar();
		uploadProgress = new JProgressBar();
		from = new JLabel();
		to = new JLabel();
		icon = new JLabel();
		cancel = new JButton();
		pause = new JButton();

		icon.setOpaque(true);
//		iconScroll
		iconScroll.setViewportView(icon);
		iconScroll.setPreferredSize(new Dimension(250, 250));
		iconScroll.setBorder(null);
		iconScroll.setViewportBorder(null);

		totalProgressLabel.setText(BorderMaker.getMessage("progress"));
		uploadProgressLabel.setText(BorderMaker.getMessage("upload"));
		
		
//		icon
		icon.setHorizontalAlignment(JLabel.CENTER);
		icon.setVerticalAlignment(JLabel.CENTER);
		
//		from
		from.setText(" ");
		
//		to
		to.setText(" ");
		
		
////		subMessage
//		subMessage.setText(" ");
		
//		pause
		pause.setText(BorderMaker.getMessage("pause"));
		pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pause();
			}
		});
		
//		cancel
		cancel.setText(BorderMaker.getMessage("cancel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
//		totalProgress
		totalProgress.setMinimum(0);
		totalProgress.setMaximum(0);
		totalProgress.setPreferredSize(new Dimension(400, 20));
//		totalProgress.setStringPainted(BorderMaker.STRING_PAINTED);
		totalProgress.setString(BorderMaker.getMessage("totalProgress"));
		
//		uploadProgress
		uploadProgress.setMinimum(0);
		uploadProgress.setMaximum(0);
		uploadProgress.setPreferredSize(new Dimension(400, 20));
//		uploadProgress.setStringPainted(BorderMaker.STRING_PAINTED);
		
////		subProgress
//		subProgress.setMinimum(0);
//		subProgress.setMaximum(100);
//		subProgress.setPreferredSize(new Dimension(400, 20));
//		subProgress.setStringPainted(BorderMaker.STRING_PAINTED);

		FormLayout layout = new FormLayout(
			    "r:p, 4dlu, 350dlu:g", // columns
			    "");      // rows

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.append(BorderMaker.getMessage("from") + ":", from);
		builder.append(BorderMaker.getMessage("to") + ":", to);
		builder.nextLine();
		builder.appendUnrelatedComponentsGapRow();
		builder.nextLine();
		builder.append(totalProgressLabel, 3);
		builder.append(totalProgress, 3);
		builder.nextLine();
		builder.appendUnrelatedComponentsGapRow();
		builder.nextLine();
		builder.append(uploadProgressLabel, 3);
		builder.append(uploadProgress, 3);
		builder.append(ButtonBarFactory.buildCenteredBar(cancel), 3);

		HeaderPanel gradientPanel = new HeaderPanel(ImageFactory.PROGRESS_32, BorderMaker.getMessage("progress"));
		gradientPanel.addSeparator();
		
		CollapsablePanel iconFrame = new CollapsablePanel(BorderMaker.getMessage("thumbnail"), null, iconScroll);
		iconFrame.setCollapsable(false);
		
		JPanel panel = builder.getPanel();
		JPanel p = new JPanel(new GridBagLayout());
		p.add(panel, Gbc.xy(0, 0).horizontal());
		p.setPreferredSize(p.getPreferredSize());

		uploadProgressLabel.setVisible(false);
		uploadProgress.setVisible(false);
		
		CollapsablePanel othersFrame = new CollapsablePanel(BorderMaker.getMessage("progress"), null, p);
		othersFrame.setCollapsable(false);

		JPanel p2 = new JPanel(new BorderLayout(0, 10));
		p2.setBorder(Borders.DIALOG_BORDER);
		p2.add(iconFrame, BorderLayout.CENTER);
		p2.add(othersFrame, BorderLayout.SOUTH);
		p2.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(gradientPanel, BorderLayout.NORTH);
		this.getContentPane().add(p2, BorderLayout.CENTER);
		this.getRootPane().setDefaultButton(cancel);
		this.setTitle(BorderMaker.getMessage("progress"));
	}	

	public void pause() {
		paused = !paused;
		if(paused) {
			p2.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			worker.setPaused(true);
			pause.setText(BorderMaker.getMessage("resume"));
		} else {
			p2.setCursor(Cursor.getDefaultCursor());
			worker.setPaused(false);
			pause.setText(BorderMaker.getMessage("pause"));
		}
	}

	public void setVisible(boolean b) {
		if(!b) {
			try {
				worker.stopProcessing();
			} catch (Exception e) {}
			try {
				workerThread.interrupt();
			} catch (Exception e) {}
		}
		super.setVisible(b);
	}

	public void go() {
		worker = new BorderMakerProcessor(bean, this);
		workerThread = new Thread(worker);
		workerThread.start();
		super.setVisible(true);
	}

	public void startProcessing(int count) {
		totalProgress.setMaximum(count);
		totalProgress.setValue(0);
		totalImages = count;
		if(count == 1) {
			totalProgress.setIndeterminate(true);
		}
	}

	public void stopProcessing() {
		totalProgress.setMaximum(totalProgress.getMaximum());
		setVisible(false);
	}

	public void fileProcessing(File source, String destination, int count, ImageIcon thumbnail) {
		if (thumbnail != null) {
			icon.setIcon(thumbnail);
		} else {
			icon.setIcon(ImageFactory.START);
		}
		totalProgressLabel.setText(BorderMaker.format("progressMessage", count + 1, totalImages));
		from.setText(SemanticaUtil.abbreviateFileName(source.getAbsolutePath(), 70));
		to.setText(destination);
	}

	public void fileProcessed(File source, String destination, long processingTime, int count) {
		this.count = count;
	}

	public synchronized void progress() {
		totalProgress.setValue(totalProgress.getValue() + 1);
	}
	
	@Override
	public synchronized void uploaded(Upload upload, long length) {
		uploadProgress.setValue(uploadProgress.getValue() + (int) (length / 100));
		uploadProgressLabel.setText(BorderMaker.format("uploadProgress", upload.getDestinationHandler().getName(), upload.getDestinationName(), pending));
	}

	@Override
	public synchronized void uploadStarted(Upload upload) {
		this.pending--;
		uploadProgressLabel.setText(BorderMaker.format("uploadProgress", upload.getDestinationHandler().getName(), upload.getDestinationName(), pending));
		uploadProgressLabel.setIcon(upload.getDestinationHandler().getIcon());
		uploadProgressLabel.setVisible(true);
		uploadProgress.setVisible(true);
		uploadProgress.setMaximum(uploadProgress.getMaximum() + (int) (upload.getSize() / 100));
	}

	@Override
	public synchronized void uploadStopped(Upload upload) {
		uploadProgress.setMaximum(uploadProgress.getMaximum() - (int) (upload.getSize() / 100));
		uploadProgress.setValue(uploadProgress.getValue() - (int) (upload.getSize() / 100));
	}

	@Override
	public synchronized void uploadedSubmitted(Upload upload) {
//		uploadProgress.setMaximum(uploadProgress.getMaximum() + (int) (upload.getSize() / 100));
		this.pending++;
	}

	@Override
	public void awaitingUploads() {
		totalProgress.setIndeterminate(true);
		totalProgressLabel.setText(BorderMaker.getMessage("awaitingUpload"));
	}
}
