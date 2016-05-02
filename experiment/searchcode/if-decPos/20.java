/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Jun 13, 2004
 *
 */
package edu.mit.csail.relo.jdt.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import edu.mit.csail.relo.figures.CodeUnitFigure;
import edu.mit.csail.relo.jdt.CodeUnit;
import edu.mit.csail.relo.jdt.RJCore;
import edu.mit.csail.relo.jdt.ReloJDTPlugin;
import edu.mit.csail.relo.jdt.util.JDTUISupport;
import edu.mit.csail.relo.modelBridge.Artifact;
import edu.mit.csail.relo.modelBridge.DerivedArtifact;
import edu.mit.csail.relo.modelBridge.DirectedRel;
import edu.mit.csail.relo.parts.AbstractReloEditPart;
import edu.mit.csail.relo.parts.ArtifactEditPart;
import edu.mit.csail.relo.parts.NavAidsEditPolicy;
import edu.mit.csail.relo.parts.MoreItemsEditPart;
import edu.mit.csail.relo.parts.NavAidsSpec;
import edu.mit.csail.relo.parts.NavAidsEditPolicy.NavAidsEditPart;
import edu.mit.csail.relo.store.ReloRdfRepository;

/**
 * Java Related Functionality
 * 
 * @author vineet
 */
public abstract class CodeUnitEditPart extends MoreItemsEditPart implements NavAidsEditPart {
	static final Logger logger = ReloJDTPlugin.getLogger(CodeUnitEditPart.class);

    public final static String REQ_REDUCE = "minimize";
    public final static String REQ_EXPAND = "expand";
    
    // tooltips
    public final static String COLLAPSE = "collapse";
    public final static String EXPAND = "expand";
    public final static String HIDE = "hide";
    
	public CodeUnit getCU() {
	    return (CodeUnit) getModel();
	}
    
	
	@Override
    public void setModel(Object model) {
		// make sure that the model is of an easy to hanle type
		if (!(model instanceof CodeUnit))
			model = new CodeUnit(((Artifact) model).elementRes);
		super.setModel(model);
	}

	
    @Override
    public String getLabel(Artifact art, Artifact contextArt) {
        return CodeUnit.getLabel(getRepo(), art, contextArt);
    }
    
    @Override
    protected ImageDescriptor getIconDescriptor(Artifact art, org.openrdf.model.Resource resType) {
    	return CodeUnit.getIconDescriptor(getRepo(), art, resType);
    }
	
	@Override
    public void activate() {
		super.activate();
		updateMembers(currDL);
	}

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(NavAidsEditPolicy.HANDLES_ROLE, new NavAidsEditPolicy());
    }


	
	
	// content pane
	@Override
    public IFigure getContentPane() {
		IFigure fig = getFigure();
		if (fig instanceof CodeUnitFigure) {
			return ((CodeUnitFigure)fig).getContentPane();
		} else {
			return super.getContentPane();
		}
	}
	
	public String getLabel() {
		EditPart parentEP = getParent();
		while (parentEP.getModel() instanceof DerivedArtifact) {
			parentEP = parentEP.getParent();
		}
		return this.getLabel(parentEP.getModel());
	}
	
	public String getLabel(Object context) {
		if (context instanceof CodeUnit) {
			return getCU().getLabel(getRepo(), (CodeUnit) context) + tag.getTrailer();
		} else {
			return getCU().getLabel(getRepo()) + tag.getTrailer();
		}
	}

	
	
	
	
	/*
	 * other misc. functionality
	 *
	 */



    public List<AbstractGraphicalEditPart> getSelectedAGEP() {
        List<ArtifactEditPart> retList = this.getRootController().getSelectedArtifactEditParts();
        return new ArrayList<AbstractGraphicalEditPart>(retList);
    }
    public String getRelModelLabel(Object model) {
        if (!(model instanceof Artifact)) return "{err}";
        return this.getLabel((Artifact)model, this.getArtifact());
    }
    public List<Object> listModel(ReloRdfRepository repo, DirectedRel rel, Predicate filter) {
        return new ArrayList<Object> (this.getArtifact().listArt(repo, rel, filter));
    }


	

    public List<NavAidsSpec> getSingleSelectHandlesSpecList(NavAidsEditPolicy bdec) {
		final List<NavAidsSpec> decorations = new ArrayList<NavAidsSpec> (5);
		decorations.add(new NavAidsSpec() {
	        @Override
            public void buildHandles() {
	            IFigure btn;
	            
	            btn = getReqButton(CodeUnitEditPart.this, "collapse.gif", REQ_REDUCE, COLLAPSE);
	            if (btn != null) decorationFig.add(btn);
	            
	            btn = getReqButton(CodeUnitEditPart.this, "expand.gif", REQ_EXPAND, EXPAND);
	            if (btn != null) decorationFig.add(btn);
	            
	            btn = getReqButton(CodeUnitEditPart.this, "remove.gif", RequestConstants.REQ_DELETE, HIDE);
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                if (containerFig instanceof CodeUnitFigure)
                    containerFig = ((CodeUnitFigure)containerFig).getLabel();
                
                return containerFig.getBounds().getTopRight(); 
            }
        });
	    decorations.add(new NavAidsSpec() {
	        NavAidsSpec firstDS = decorations.get(0); 
	        @Override
            public void buildHandles() {
	            IFigure btn = getRelation(CodeUnitEditPart.this, DirectedRel.getFwd(RJCore.calls));
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Point decPos = containerFig.getBounds().getTopRight();
                decPos.x = Math.max(
		                        decPos.x, 
		                        firstDS.decorationFig.getBounds().getTopRight().x);
                return decPos; 
            }
        });
	    decorations.add(new NavAidsSpec() {
	        @Override
            public void buildHandles() {
	            IFigure btn = getRelation(CodeUnitEditPart.this, DirectedRel.getRev(RJCore.calls));
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x - prefSize.width;
                int y = bounds.y;
                return new Point(x, y);
            }
        });
	    decorations.add(new NavAidsSpec() {
	        @Override
            public void buildHandles() {
	            IFigure btn = getRelation(CodeUnitEditPart.this, DirectedRel.getFwd(RJCore.overrides));
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x;
                int y = bounds.y - prefSize.height;
                return new Point(x, y);
            }
        });
	    decorations.add(new NavAidsSpec() {
	        @Override
            public void buildHandles() {
	            IFigure btn = getRelation(CodeUnitEditPart.this, DirectedRel.getRev(RJCore.overrides));
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                //Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x;
                int y = bounds.y + bounds.height;
                return new Point(x, y);
            }
        });
	    decorations.add(new NavAidsSpec() {
	        @Override
            public void buildHandles() {
	            IFigure btn = getRelation(CodeUnitEditPart.this, DirectedRel.getFwd(RJCore.inherits));
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x + (bounds.width - prefSize.width)/2;
                int y = bounds.y - prefSize.height;
                return new Point(x, y);
            }
        });
	    decorations.add(new NavAidsSpec() {
	        @Override
            public void buildHandles() {
	            IFigure btn = getRelation(CodeUnitEditPart.this, DirectedRel.getRev(RJCore.inherits));
	            if (btn != null) decorationFig.add(btn);
	        }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x + (bounds.width - prefSize.width)/2;
                int y = bounds.y + bounds.height;
                return new Point(x, y);
            }
        });
	    return decorations;
	}
	
    public List<NavAidsSpec> getMultiSelectHandlesSpecList(NavAidsEditPolicy bdec) {
		final List<NavAidsSpec> decorations = new ArrayList<NavAidsSpec> (5);

		decorations.add(new NavAidsSpec() {
			@Override
			public void buildHandles() {
				IFigure btn;
	            
	            btn = getReqButton(CodeUnitEditPart.this, "remove.gif", RequestConstants.REQ_DELETE, NavAidsEditPolicy.REMOVE_ALL_SELECTED);
	            if (btn != null) decorationFig.add(btn);
			}
            @Override
			public Point getHandlesPosition(IFigure containerFig) {
                if (containerFig instanceof CodeUnitFigure)
                    containerFig = ((CodeUnitFigure)containerFig).getLabel();
                
                return containerFig.getBounds().getTopRight(); 
            }			
		});
	    return decorations;
	}
	

	@Override
    protected void refreshVisuals() {
		if (getFigure() instanceof CodeUnitFigure) {
			CodeUnitFigure cuf = (CodeUnitFigure) getFigure();
			cuf.getLabel().setText(getLabel());
			
			Artifact art = getArtifact();
			ReloRdfRepository repo = getBrowseModel().getRepo();
			Image icon = CodeUnit.getIcon(repo, art, art.getType(repo));
			cuf.getLabel().setIcon(icon);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getCommand(org.eclipse.gef.Request)
	 */
	@Override
    public Command getCommand(Request request) {
	    if (request.getType().equals(REQ_EXPAND)) {
		    return getExpandCmd();
	    }
	    if (request.getType().equals(RequestConstants.REQ_OPEN) && !(this instanceof PackageEditPart)) {
	    	// this should be an action, not a command, i.e. it should not go on
			// the undo/redo stack
	    	return new Command("Open in Editor") {
                @Override
                public void execute() {
                    CodeUnitEditPart cuep = CodeUnitEditPart.this;
                    JDTUISupport.openInEditor(cuep.getCU(), cuep.getRepo());
                }
	    	};
	    }
	    if (request.getType().equals(RequestConstants.REQ_DELETE)) {
		    final CodeUnitEditPart cuep = CodeUnitEditPart.this;
			return new Command(NavAidsEditPolicy.REMOVE_NODE) {
                AbstractReloEditPart cuepParent = (AbstractReloEditPart) cuep.getParent();
                @Override
                public void execute() {
                    cuepParent.removeModelAndChild(cuep);
                }
                @Override
                public void undo() {
                    cuepParent.appendModelAndChild(cuep.getArtifact(), cuep);
                }
			};
	    }
	    if (request.getType().equals(REQ_REDUCE)) {
		    final CodeUnitEditPart cuep = CodeUnitEditPart.this;
	        if (cuep.currDL == cuep.getMinimalDL()) return null;
			return new Command() {
				@Override
                public void execute() {
				    cuep.suggestDetailLevelDecrease();
				}
			};
	    }
	    
	    //System.err.println("getCommad: request.getType()= " + request.getType());
	    //ConsoleView.log("getCommad: request.getType()= " + request.getType());
	    
		//if (request.getType().equals(RequestConstants.REQ_RESIZE_CHILDREN)) {
		//	ChangeBoundsRequest req = (ChangeBoundsRequest) request;
		//	getFigure().setBounds(req.getTransformedRectangle(new Rectangle()));
		//	System.err.println("RESIZE REQUEST");
		//}
		return super.getCommand(request);
	}

	protected Command getExpandCmd() {
		final CodeUnitEditPart cuep = CodeUnitEditPart.this;
		if (cuep.currDL == cuep.getMaximumDL()) return null;
		return new Command() {
			@Override
		    public void execute() {
			    //logger.info("COMMAND: Open execute start");
		        //logger.error("Opening: " + getCU());
		        //realizeParent();
		        CompoundCommand relaizeParentCmd = new CompoundCommand();
		        CodeUnitEditPart.this.realizeParent(relaizeParentCmd, /*inferring*/ true);
		        relaizeParentCmd.execute();
				suggestDetailLevelIncrease();
			    //logger.info("COMMAND: Open execute end");
			}
			@Override
		    public void undo() {
				suggestDetailLevelDecrease();
				logger.info("Trying to undo open request!!");
			}
		};
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(Request req) {
        //System.err.println("performRequest: req.getType()= " + req.getType());
        Command command = getCommand(req);
        if (command != null) {
            if (command.canExecute()) {
                execute(command);
            }
            return;
        }

        super.performRequest(req);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.gef.EditPart#refresh()
	 */
	@Override
    public void refresh() {
		//if (getModelSourceConnections().size()
		//		+ getModelTargetConnections().size() > 0) {
		//	System.err.println(this + ",s:"
		//			+ getModelSourceConnections().size() + ",t:"
		//			+ getModelTargetConnections().size());
		//}
		super.refresh();
	}

	public List getNonDerivedModelChildren() {
		List retVal = new ArrayList<Object> (getModelChildren());
		ListIterator li = retVal.listIterator();
		while (li.hasNext()) {
			if (li.next() instanceof DerivedArtifact) {
				li.remove();
			}
		}
		return retVal;
	}

    @Override
    public void buildContextMenu(IMenuManager menu) {
        super.buildContextMenu(menu);
        
        IAction action;
        action = new Action("Open in Editor") {
            @Override
            public void run() {
                CodeUnitEditPart cuep = CodeUnitEditPart.this;
                JDTUISupport.openInEditor(cuep.getCU(), cuep.getRepo());
            }
        };
        menu.appendToGroup("main", action);
    }

}

