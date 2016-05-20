/**
 * Copyright 2009 infoAsset AG
 * 
 * This file is part of Tricia - Open Source Web Collaboration and Knowledge Management Software.
 * 
 * Tricia is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Tricia is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Tricia. If not, see http://www.gnu.org/licenses/.
 * 
 */
package de.infoasset.echordC1.assets.experiment;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Lists;

import de.infoasset.echordC1.EchordC1Plugin;
import de.infoasset.echordC1.assets.C1ApplicationExtension;
import de.infoasset.echordC1.assets.C1DirectoryExtension;
import de.infoasset.echordC1.assets.C1PersonExtension;
import de.infoasset.echordC1.assets.evaluation.C1AreaEditor;
import de.infoasset.echordC1.assets.evaluation.C1Evaluation;
import de.infoasset.echordC1.assets.evaluation.C1Evaluator;
import de.infoasset.echordC1.assets.evaluation.C1Report;
import de.infoasset.echordC1.assets.evaluation.C1SingleEvaluation;
import de.infoasset.echordC1.assets.logBook.C1ExperimentBlog;
import de.infoasset.echordC1.handler.evaluation.ViewForExperimentHandler;
import de.infoasset.echordC1.handler.evaluator.EvaluationOfExperimentHandler;
import de.infoasset.echordC1.handler.experiment.DeleteHandler;
import de.infoasset.echordC1.handler.experiment.Edit1Handler;
import de.infoasset.echordC1.handler.experiment.Edit2Handler;
import de.infoasset.echordC1.handler.experiment.Edit3Handler;
import de.infoasset.echordC1.handler.experiment.EditAreaEditorHandler;
import de.infoasset.echordC1.handler.experiment.EvaluationsHandler;
import de.infoasset.echordC1.handler.experiment.ViewHandler;
import de.infoasset.echordC1.handler.experimentParticipant.NewHandler;
import de.infoasset.file.assets.Directory;
import de.infoasset.file.assets.Path;
import de.infoasset.imf.util.EmptyIterable;
import de.infoasset.platform.client.SessionLocal;
import de.infoasset.platform.handler.Action;
import de.infoasset.platform.handler.ConfirmationRequiredAction;
import de.infoasset.platform.handler.Forwarder;
import de.infoasset.platform.handler.View;
import de.infoasset.platform.services.asset.BooleanProperty;
import de.infoasset.platform.services.asset.DomainValueProperty;
import de.infoasset.platform.services.asset.Feature;
import de.infoasset.platform.services.asset.MandatoryMixin;
import de.infoasset.platform.services.asset.ManyRole;
import de.infoasset.platform.services.asset.OneRole;
import de.infoasset.platform.services.asset.PersistentEntity;
import de.infoasset.platform.services.asset.PersistentSchema;
import de.infoasset.platform.services.asset.Role;
import de.infoasset.platform.services.asset.StringProperty;
import de.infoasset.platform.services.asset.Validator;
import de.infoasset.platform.services.asset.propertyValidators.MinimalLengthValidator;
import de.infoasset.platform.services.asset.propertyValidators.NotNullValidator;
import de.infoasset.platform.services.domains.Domain;
import de.infoasset.platform.services.internationalization.Message;
import de.infoasset.platform.services.internationalization.SimpleMessage;
import de.infoasset.platform.services.internationalization.SimpleParameterizedMessage;
import de.infoasset.platform.store.Store;
import de.infoasset.platform.util.FilterIterable;
import de.infoasset.platform.util.NameValue;
import de.infoasset.platform.util.WrapperIterable;
import de.infoasset.toro.assets.Application;
import de.infoasset.toro.assets.ReadProtected;
import de.infoasset.toro.assets.Searchable;
import de.infoasset.toro.assets.group.Person;
import de.infoasset.toro.assets.group.Principal;
import de.infoasset.toro.assets.links.ILinkable;
import de.infoasset.toro.assets.links.Linkable;

public class C1Experiment extends PersistentEntity implements ILinkable {

    public static final PersistentSchema<C1Experiment> SCHEMA = new PersistentSchema() {
        public boolean mayCreateThis() {
            return SessionLocal.getUser() != null && Application.getApplication().adapt(C1ApplicationExtension.class).c1callForExperimentIsOpen.get();
        };
    };

    @Override
    public Message getLabel() {
        return new SimpleMessage("Experiment Proposal");
    }

    @Override
    protected MandatoryMixin[] mandatoryMixins() {
        return new MandatoryMixin[] {

        new Linkable(),

        new Searchable(),

        new ReadProtected() {
            protected void refreshReadAccess(ReadProtected copy) {
                copy.add(EchordC1Plugin.getExcCcGroup());
            };
        }

        };
    }

    public final OneRole<C1PersonExtension> submitter = new OneRole<C1PersonExtension>() {
        protected Role oppositeRole() {
            return C1PersonExtension.prototype.experiments;
        };

        public boolean showInGenericViews() {
            return false;
        };
    };

    public final StringProperty fullTitle = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Proposal Full Title");
        };

            final Validator minimalLength = new MinimalLengthValidator(3);
    };

    public final StringProperty acronym = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Proposal Acronym");
        };
    };

    public final DomainValueProperty typeOfExperiment = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Type of experiment");
        }

        @Override
        public Domain getDomain() {
            return EchordC1Plugin.experimentType;
        };

            final Validator notNull = new NotNullValidator();
    };

    public final DomainValueProperty scenario = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Main Scenario");
        }

        @Override
        public Domain getDomain() {
            return EchordC1Plugin.experimentScenario;
        };

            final Validator notNull = new NotNullValidator();
    };

    public final DomainValueProperty researchFocus = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Main Research Focus");
        }

        @Override
        public Domain getDomain() {
            return EchordC1Plugin.experimentFocus;
        };

            final Validator notNull = new NotNullValidator();
    };

    public final StringProperty keywords = new StringProperty() {

        public Message getLabel() {
            return new SimpleMessage("Keywords");
        };

        public Message getShortHelp() {
            return new SimpleMessage(
                    "Please enter up to 3 keywords. If appropriate, you may choose them from the following list: dextrous manipulation, mobile platforms, navigation, cooperative manipulation or coop. transport, aerial robots, Augmented Reality HRI, human-robot interfacing, human-robot cooperation, networked robots, motion control and planning, force control, 3D perception.");
        };
    };

    public final StringProperty experimentAbstract = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Abstract");
        };

        public int getMaxLength() {
            return Store.TEXT;
        };
    };

    public final ManyRole<C1ExperimentParticipant> participants = new ManyRole<C1ExperimentParticipant>() {
        public Message getLabel() {
            return new SimpleMessage("List of Partners");
        };

        protected Role oppositeRole() {
            return C1ExperimentParticipant.SCHEMA.prototype().experiment;
        };

        protected java.util.Comparator<C1ExperimentParticipant> getDefaultSortingComparator() {
            return new Comparator<C1ExperimentParticipant>() {

                @Override
                public int compare(C1ExperimentParticipant o1, C1ExperimentParticipant o2) {
                    return o1.number.getInteger().compareTo(o2.number.get());
                }
            };
        };

        public boolean isCascadeDelete() {
            return true;
        };
    };

    public final BooleanProperty ethChildren = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve children?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethPatients = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve patients or persons not able to give consent?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethVolunteers = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve adult healthy volunteers?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethHumanGenet = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve Human Genetic Material?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethHumanBiologicalSamples = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve Human biological samples?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethHumanDataCollection = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve Human data collection?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethHumanEmbryos = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve Human Embryos?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethHumanFoetalCells = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve Human Foetal Tissue / Cells?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethHumanEmbryonicStemCells = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve Human Embryonic Stem Cells?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethPersonalData = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage(
                    "Does the proposal involve processing of genetic information or personal data (e.g. health, sexual lifestyle, ethnicity, political opinion, religious or philosophical conviction)?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethTracking = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve tracking the location or observation of people?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethResearchOnAnimals = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve research on animals?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethTransgenicSmallLabAnimals = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Are those animals transgenic small laboratory animals?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethTransgenicFarmAnimals = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Are those animals transgenic farm animals?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethClonedFarmAnimals = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Are those animals cloned farm animals?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethDevCountriesLocalResources = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Use of local resources (genetic, animal, plant etc)");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethDevCountriesImpactCommunity = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Impact on local community");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethMilitaryApplication = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Research having direct military application");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethPotentialForTerroristAbuse = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Research having the potential for terrorist abuse");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty ethIctImplants = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Does the proposal involve clinical trials of ICT implants?");
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final StringProperty ethExplanation = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("If you selected any of the issues above, please explain");
        };

        public int getMaxLength() {
            return Store.MEDIUM_TEXT;
        };
    };

    public final ManyRole<C1ExperimentItem> experimentItems = new ManyRole<C1ExperimentItem>() {
        public Message getLabel() {
            return new SimpleMessage("Equipment Items");
        };

        protected Role oppositeRole() {
            return C1ExperimentItem.SCHEMA.prototype().experiment;
        };

        public boolean isCascadeDelete() {
            return true;
        };
    };

    public final OneRole<C1DirectoryExtension> directory = new OneRole<C1DirectoryExtension>() {
        protected Role oppositeRole() {
            return C1DirectoryExtension.prototype.experiment;
        };

        public Message getLabel() {
            return new SimpleMessage("Files");
        };

        @Override
        public boolean isCascadeDelete() {
            return true;
        }

        protected boolean isOwner() {
            return true;
        };
    };

    public final ManyRole<C1SingleEvaluation> evaluations = new ManyRole<C1SingleEvaluation>() {
        public Message getLabel() {
            return new SimpleMessage("Evaluations");
        };

        protected Role oppositeRole() {
            return C1SingleEvaluation.SCHEMA.prototype().experiment;
        };
    };

    public final OneRole<C1AreaEditor> areaEditor = new OneRole<C1AreaEditor>() {
        public Message getLabel() {
            return new SimpleMessage("Area Editor");
        };

        protected Role oppositeRole() {
            return C1AreaEditor.prototype.experiments;
        };

        public String getColumnNameHint() {
            return "areaeditor";
        };
    };

    public final BooleanProperty eligibleForEvaluation = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Eligible for Evaluation");
        };

        protected Boolean getDefaultValueHook() {
            return true;
        };
    };

    public final OneRole<C1Report> report = new OneRole<C1Report>() {
        public Message getLabel() {
            return new SimpleMessage("Report");
        };

        protected Role oppositeRole() {
            return C1Report.SCHEMA.prototype().experiment;
        };
    };

    public final OneRole<C1ExperimentBlog> logBlog = new OneRole<C1ExperimentBlog>() {
        public Message getLabel() {
            return new SimpleMessage("Experiment Log");
        };

        protected Role oppositeRole() {
            return C1ExperimentBlog.prototype.c1Experiment;
        };

        protected boolean isOwner() {
            return true;
        };
    };

    @Override
    public String getName() {
        return acronym.get();
    }

    @Override
    public void specifyUrl(Forwarder f) {
        f.goWithId(ViewHandler.class, C1Experiment.this);
    }

    @Override
    protected boolean mayEditThis() {
        C1PersonExtension pe = submitter.get();
        boolean callIsOpenAndSelf = ObjectUtils.equals(SessionLocal.getUser(), pe.getEntity())
                && Application.getApplication().adapt(C1ApplicationExtension.class).c1callForExperimentIsOpen.get();
        return callIsOpenAndSelf || C1Evaluation.isEvaluationCoordinatorOrExCc();
    }

    public static Directory getExperimentsDir() {
        return (Directory) Path.findPath(EXPERIMENTS_PATH);
    }

    public Iterable<C1Evaluator> getEvaluators() {
        C1Report c1Report = report.get();
        if(c1Report != null) {
            final Person rapporteur = c1Report.rapporteur.get().getEntity();
            WrapperIterable<C1SingleEvaluation, C1Evaluator> result = new WrapperIterable<C1SingleEvaluation, C1Evaluator>(
                    new FilterIterable<C1SingleEvaluation>(evaluations.getAssets()) {

                        @Override
                        public boolean filter(C1SingleEvaluation o) {
                            return !o.evaluator.get().getEntity().equals(rapporteur);
                        }
                    }) {

                @Override
                public C1Evaluator wrap(C1SingleEvaluation o) {
                    return o.evaluator.get();
                }
            };
            return result;
        } else {
            return EmptyIterable.instance();
        }
    }

    public List<BooleanProperty> getEthicalIssues() {
        List<BooleanProperty> result = Lists.newArrayList();
        for (Feature c : getFeatures()) {
            if (c.getName().startsWith("eth")) {
                if (c instanceof BooleanProperty) {
                    BooleanProperty bp = (BooleanProperty) c;
                    if (bp.get()) {
                        result.add(bp);
                    }
                }
            }
        }
        return result;
    }

    private static final String EXPERIMENTS_PATH = "/Experiments/Call1/Proposals";

    public void addReadAccess(Principal p) {
        adapt(ReadProtected.class).add(p);
        Directory dir = (Directory) directory.get().getEntity().createWritableCopy();
        dir.readers.create(p);
        dir.persist();
        for (C1ExperimentParticipant ep : participants.getAssets()) {
            ep.adapt(ReadProtected.class).add(p);
        }
        for (C1ExperimentItem ep : experimentItems.getAssets()) {
            ep.adapt(ReadProtected.class).add(p);
        }
    }

    public void removeReadAccess(Principal p) {
        adapt(ReadProtected.class).remove(p);
        Directory dir = directory.get().getEntity();
        dir = (Directory) dir.createWritableCopy();
        dir.readers.remove(p);
        dir.persist();
        for (C1ExperimentParticipant ep : participants.getAssets()) {
            ep = (C1ExperimentParticipant) ep.createWritableCopy();
            ep.adapt(ReadProtected.class).remove(p);
            ep.persist();
        }
        for (C1ExperimentItem ep : experimentItems.getAssets()) {
            ep = (C1ExperimentItem) ep.createWritableCopy();
            ep.adapt(ReadProtected.class).remove(p);
            ep.persist();
        }
    }

    public void setReadAccess(Iterable<Principal> ps) {
        adapt(ReadProtected.class).set(ps);
        Directory dir = directory.get().getEntity();
        dir = (Directory) dir.createWritableCopy();
        dir.readers.set(ps);
        dir.persist();
        for (C1ExperimentParticipant ep : participants.getAssets()) {
            ep = (C1ExperimentParticipant) ep.createWritableCopy();
            ep.adapt(ReadProtected.class).set(ps);
            ep.persist();
        }
        for (C1ExperimentItem ep : experimentItems.getAssets()) {
            ep = (C1ExperimentItem) ep.createWritableCopy();
            ep.adapt(ReadProtected.class).set(ps);
            ep.persist();
        }
    }

    final View VIEW = new View() {

        @Override
        public void target(Forwarder f) {
            f.go(C1Experiment.this);
        }
    };

    final View MY_PROPSALS = new View() {

        @Override
        public Message overrideHandlerTargetLabel() {
            return new SimpleMessage("My Proposals");
        }

        @Override
        public void target(Forwarder f) {
            f.go("/wikis/home-wiki/call-1-experiment-proposal-submission");
        }
    };

    final View EVALUATIONS = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationsHandler.class, C1Experiment.this);
        }
    };

    final View EVALUATION = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationOfExperimentHandler.class, C1Experiment.this);
        }
    };

    final View FINAL_EVALUATION = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ViewForExperimentHandler.class, C1Experiment.this);
        }
    };

    final Action NEW_PARTICIPANT = new Action() {

        @Override
        public void target(Forwarder f) {
            f.go(NewHandler.class, new NameValue(NewHandler.EXPERIMENT_ID, id.get()));
        }
    };

    final Action EDIT = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit1Handler.class, C1Experiment.this);

        }
    };

    final Action EDIT_2 = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit2Handler.class, C1Experiment.this);

        }
    };

    final Action EDIT_3 = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit3Handler.class, C1Experiment.this);

        }
    };

    final Action DELETE = new ConfirmationRequiredAction() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(DeleteHandler.class, C1Experiment.this);
        }

        @Override
        public Message getConfirmationMessage() {
            return new SimpleParameterizedMessage("Do you really want to delete {0}?").setParameters(getName());
        }
    };

    final Action NEW_EVALUATION = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(de.infoasset.echordC1.handler.evaluation.NewHandler.class, C1Experiment.this);
        }
    };

    final Action NEW_REPORT = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(de.infoasset.echordC1.handler.report.NewHandler.class, C1Experiment.this);
        }
    };

    final Action EDIT_AREA_EDITOR = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EditAreaEditorHandler.class, C1Experiment.this);

        }
    };
}

