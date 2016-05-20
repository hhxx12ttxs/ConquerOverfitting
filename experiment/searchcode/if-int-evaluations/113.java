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
package de.infoasset.echordC3.assets.experiment;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.infoasset.echordC3.EchordC3Plugin;
import de.infoasset.echordC3.assets.C3ApplicationExtension;
import de.infoasset.echordC3.assets.C3ApplicationExtension.CallStateProperty;
import de.infoasset.echordC3.assets.C3PersonExtension;
import de.infoasset.echordC3.assets.evaluation.C3AbstractEvaluation;
import de.infoasset.echordC3.assets.evaluation.C3AreaEditor;
import de.infoasset.echordC3.assets.evaluation.C3ConsensusReport;
import de.infoasset.echordC3.assets.evaluation.C3Evaluation;
import de.infoasset.echordC3.assets.evaluation.C3Evaluator;
import de.infoasset.echordC3.domains.evaluation.C3CallState;
import de.infoasset.echordC3.handler.c3.evaluation.ViewForExperimentHandler;
import de.infoasset.echordC3.handler.c3.evaluator.EvaluationOfExperimentHandler;
import de.infoasset.echordC3.handler.c3.evaluator.ReportOfExperimentHandler;
import de.infoasset.echordC3.handler.c3.experiment.DeleteHandler;
import de.infoasset.echordC3.handler.c3.experiment.Edit1Handler;
import de.infoasset.echordC3.handler.c3.experiment.Edit2Handler;
import de.infoasset.echordC3.handler.c3.experiment.Edit3Handler;
import de.infoasset.echordC3.handler.c3.experiment.EditAreaEditorHandler;
import de.infoasset.echordC3.handler.c3.experiment.EvaluationsHandler;
import de.infoasset.echordC3.handler.c3.experiment.ViewHandler;
import de.infoasset.echordC3.handler.c3.experimentParticipant.NewHandler;
import de.infoasset.file.assets.Attachments;
import de.infoasset.file.assets.Directory;
import de.infoasset.imf.util.EmptyIterable;
import de.infoasset.imf.util.OneIterable;
import de.infoasset.platform.client.SessionLocal;
import de.infoasset.platform.handler.Action;
import de.infoasset.platform.handler.ConfirmationRequiredAction;
import de.infoasset.platform.handler.Forwarder;
import de.infoasset.platform.handler.HandlerPattern;
import de.infoasset.platform.handler.View;
import de.infoasset.platform.services.asset.AssetListener;
import de.infoasset.platform.services.asset.BooleanProperty;
import de.infoasset.platform.services.asset.ChangeListener;
import de.infoasset.platform.services.asset.DomainValueProperty;
import de.infoasset.platform.services.asset.Feature;
import de.infoasset.platform.services.asset.InstantChangeListener;
import de.infoasset.platform.services.asset.MandatoryMixin;
import de.infoasset.platform.services.asset.ManyRole;
import de.infoasset.platform.services.asset.OneRole;
import de.infoasset.platform.services.asset.PersistentEntity;
import de.infoasset.platform.services.asset.PersistentSchema;
import de.infoasset.platform.services.asset.Role;
import de.infoasset.platform.services.asset.StringProperty;
import de.infoasset.platform.services.asset.UniquenessQuery;
import de.infoasset.platform.services.asset.UniquenessQuery.FixViolation;
import de.infoasset.platform.services.asset.UrlNameProperty;
import de.infoasset.platform.services.asset.Validator;
import de.infoasset.platform.services.asset.diff.Diff;
import de.infoasset.platform.services.asset.propertyValidators.MinimalLengthValidator;
import de.infoasset.platform.services.asset.propertyValidators.NotNullValidator;
import de.infoasset.platform.services.asset.propertyValidators.UniqueStringValidator;
import de.infoasset.platform.services.domains.Domain;
import de.infoasset.platform.services.internationalization.Message;
import de.infoasset.platform.services.internationalization.SimpleMessage;
import de.infoasset.platform.services.internationalization.SimpleParameterizedMessage;
import de.infoasset.platform.store.Query;
import de.infoasset.platform.store.QueryEquals;
import de.infoasset.platform.store.Store;
import de.infoasset.platform.util.FilterIterable;
import de.infoasset.platform.util.Fun;
import de.infoasset.platform.util.NameValue;
import de.infoasset.platform.util.WrapperIterable;
import de.infoasset.toro.assets.ReadProtected;
import de.infoasset.toro.assets.ReadProtected.RefreshReadAccessListener;
import de.infoasset.toro.assets.Searchable;
import de.infoasset.toro.assets.group.Group;
import de.infoasset.toro.assets.group.Person;
import de.infoasset.toro.assets.group.Principal;
import de.infoasset.toro.assets.links.ILinkable;
import de.infoasset.toro.assets.links.Linkable;
import de.infoasset.toro.assets.links.UrlNameQuery;

public class C3Experiment extends PersistentEntity implements ILinkable {

    public static final PersistentSchema<C3Experiment> SCHEMA = new PersistentSchema();

    @Override
    public Message getLabel() {
        return new SimpleMessage("Experiment Proposal");
    }

    @Override
    protected MandatoryMixin[] mandatoryMixins() {
        return new MandatoryMixin[] { new Linkable(), new Searchable(),

        new ReadProtected() {
            @Override
            protected void refreshReadAccess(ReadProtected copy) {
                CallStateProperty experimentCallStateProperty = C3ApplicationExtension.getC3Application().c3callForExperimentState;
                if (experimentCallStateProperty.isEquals(C3CallState.open) || experimentCallStateProperty.isEquals(C3CallState.closed)) {
                    copy.set(new OneIterable<Principal>(submitter.get().getEntity()));
                } else {
                    copy.set(EmptyIterable.<Principal> instance());
                }

                if (C3ApplicationExtension.getC3Application().c3panelState.isEquals(C3CallState.open)) {
                    copy.add(EchordC3Plugin.getPanelMembersGroup());
                }

                copy.add(C3AbstractEvaluation.getEvaluationCoordinators());
                CallStateProperty evaluationStateProperty = C3ApplicationExtension.getC3Application().c3evaluationState;
                if (evaluationStateProperty.isEquals(C3CallState.open)) {
                    for (C3Evaluation e : evaluations.getAssets()) {
                        copy.add(e.evaluator.get().getEntity());
                    }
                    C3ConsensusReport consensusReport = report.get();
                    if (consensusReport != null) {
                        copy.add(consensusReport.rapporteur.get().getEntity());
                    }
                }
                Attachments.refreshAttachments(C3Experiment.this);
                Directory dir = adapt(Attachments.class).directory.get();
                if (dir != null) {
                    final Directory dirCopy = (Directory) dir.createWritableCopy();
                    if (C3ApplicationExtension.getC3Application().c3callForExperimentState.isEquals(C3CallState.open)) {
                        dirCopy.writers.set(new OneIterable(submitter.get().getEntity()));
                    } else {
                        dirCopy.writers.set(new OneIterable(Group.getAdminGroup()));
                    }
                    AssetListener.noWriteAccessCheck(new Fun() {
                        @Override
                        public void fun() {
                            dirCopy.persist();
                        }
                    });
                }
            }

            @Override
            protected void afterRefreshReadAccess() {
                refresh(experimentItems);
                refresh(participants);
            }
        },

        new Attachments() {
            @Override
            public boolean alwaysCreateDirectory() {
                return true;
            }

            @Override
            public Iterable<Principal> getReaders() {
                return new OneIterable<Principal>(submitter.get().getEntity());
            }

            @Override
            public Iterable<Principal> getWriters() {
                return new OneIterable<Principal>(submitter.get().getEntity());
            }
        }

        };
    }

    public final OneRole<C3PersonExtension> submitter = new OneRole<C3PersonExtension>() {

        public Message getLabel() {
            return new SimpleMessage("Submitter");
        };

        protected Role oppositeRole() {
            return C3PersonExtension.prototype.experiments;
        };

        public boolean showInGenericViews() {
            return false;
        };

        final ChangeListener refreshAccess = new RefreshReadAccessListener();

        @Override
        public boolean isInPlaceEditable() {
            return false;
        }
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

        final Validator isUnique = new UniqueStringValidator();

        final ChangeListener adaptUrlName = new InstantChangeListener() {

            @Override
            public void change(Diff diff) {
                UrlNameProperty.updateUrlName(urlNameQuery(), urlName, get());
            }
        };
    };

    UrlNameQuery urlNameQuery() {
        return new UrlNameQuery() {
            public int query(String sanitized) {
                return SCHEMA.countEntities(getUrlNameQuery(sanitized));
            }
        };
    }

    public final UrlNameProperty urlName = new UrlNameProperty() {
    };

    public static Query getUrlNameQuery(String name) {
        return new QueryEquals(SCHEMA.prototype().urlName, name);
    }

    public static C3Experiment getByUrlName(String urlName) {
        return SCHEMA.findSingleEntityNotNull(new UniquenessQuery(getUrlNameQuery(urlName), new FixViolation<C3Experiment>() {

            @Override
            public void fix(C3Experiment p) {
                UrlNameProperty.fixInvalidUrlName(p.urlNameQuery(), p.urlName);
            }
        }));
    }

    public final BooleanProperty resubmission = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Mark this checkbox if this proposal is a re-submission of one in a previous call.");
        };

        protected Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final DomainValueProperty typeOfExperiment = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Type of experiment");
        }

        @Override
        public Domain getDomain() {
            return EchordC3Plugin.experimentType;
        };

        final Validator notNull = new NotNullValidator();
    };

    public final DomainValueProperty scenario = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Main Scenario");
        }

        @Override
        public Domain getDomain() {
            return EchordC3Plugin.experimentScenario;
        };

        final Validator notNull = new NotNullValidator();
    };

    public final DomainValueProperty researchFocus = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Main Research Focus");
        }

        @Override
        public Domain getDomain() {
            return EchordC3Plugin.experimentFocus;
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

    public final StringProperty mainInnovation = new StringProperty() {

        public Message getLabel() {
            return new SimpleMessage("Main Innovation");
        };

        public Message getShortHelp() {
            return new SimpleMessage("Please enter up to 300 characters.");
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

    public final ManyRole<C3ExperimentParticipant> participants = new ManyRole<C3ExperimentParticipant>() {
        public Message getLabel() {
            return new SimpleMessage("List of Partners");
        };

        protected Role oppositeRole() {
            return C3ExperimentParticipant.SCHEMA.prototype().experiment;
        };

        protected java.util.Comparator<C3ExperimentParticipant> getDefaultSortingComparator() {
            return new Comparator<C3ExperimentParticipant>() {

                @Override
                public int compare(C3ExperimentParticipant o1, C3ExperimentParticipant o2) {
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

    public final ManyRole<C3ExperimentItem> experimentItems = new ManyRole<C3ExperimentItem>() {
        public Message getLabel() {
            return new SimpleMessage("Equipment Items");
        };

        protected Role oppositeRole() {
            return C3ExperimentItem.SCHEMA.prototype().experiment;
        };

        public boolean isCascadeDelete() {
            return true;
        };
    };

    public final ManyRole<C3Evaluation> evaluations = new ManyRole<C3Evaluation>() {
        public Message getLabel() {
            return new SimpleMessage("Evaluations");
        };

        protected Role oppositeRole() {
            return C3Evaluation.SCHEMA.prototype().experiment;
        };
    };

    public final OneRole<C3AreaEditor> areaEditor = new OneRole<C3AreaEditor>() {
        public Message getLabel() {
            return new SimpleMessage("Area Editor");
        };

        protected Role oppositeRole() {
            return C3AreaEditor.prototype.experiments;
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

    public final OneRole<C3ConsensusReport> report = new OneRole<C3ConsensusReport>() {
        public Message getLabel() {
            return new SimpleMessage("Report");
        };

        protected Role oppositeRole() {
            return C3ConsensusReport.SCHEMA.prototype().experiment;
        };
    };

    @Override
    public String getName() {
        return acronym.get();
    }

    public static final HandlerPattern EXPERIMENT = new HandlerPattern("/c3/experiments/{urlName}", ViewHandler.class) {
    };

    @Override
    public void specifyUrl(Forwarder f) {
        f.go(EXPERIMENT, urlName.get());
    }

    @Override
    protected boolean mayEditThis() {
        CallStateProperty callStateProperty = C3ApplicationExtension.getC3Application().c3callForExperimentState;
        if (callStateProperty.isEquals(C3CallState.open)) {
            return ObjectUtils.equals(SessionLocal.getUser(), submitter.get());
        } else if (callStateProperty.isEquals(C3CallState.closed)) {
            return C3AbstractEvaluation.isEvaluationCoordinatorOrExCc();
        } else {
            return false;
        }
    }

    public Iterable<C3Evaluator> getEvaluators() {
        final Person rapporteur = report.get().rapporteur.get().getEntity();
        WrapperIterable<C3Evaluation, C3Evaluator> result = new WrapperIterable<C3Evaluation, C3Evaluator>(new FilterIterable<C3Evaluation>(
                evaluations.getAssets()) {

            @Override
            public boolean filter(C3Evaluation o) {
                return !o.evaluator.get().getEntity().equals(rapporteur);
            }
        }) {

            @Override
            public C3Evaluator wrap(C3Evaluation o) {
                return o.evaluator.get();
            }
        };
        return result;
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

    public boolean thirdEvaluationNeeded() {
        Set<Boolean> bools = Sets.newHashSet();
        int count = evaluations.count();
        if (count != 2) {
            return false;
        }
        float[] sums = new float[count];
        int index = 0;
        for (C3AbstractEvaluation evaluation : evaluations.getAssets()) {
            bools.add(evaluation.toBeConsidered.get());
            sums[index++] = C3AbstractEvaluation.getFloat(evaluation.impact.getKey()) + C3AbstractEvaluation.getFloat(evaluation.efficiency.getKey())
                    + C3AbstractEvaluation.getFloat(evaluation.excellence.getKey());
        }
        if (bools.contains(Boolean.TRUE) && bools.contains(Boolean.FALSE)) {
            return true;
        } else if (bools.contains(Boolean.TRUE) && Math.abs(sums[0] - sums[1]) >= 3.0) {
            return true;
        } else {
            return false;
        }
    }

    final View VIEW = new View() {

        @Override
        public void target(Forwarder f) {
            f.go(C3Experiment.this);
        }
    };

    final View MY_PROPSALS = new View() {

        @Override
        public Message overrideHandlerTargetLabel() {
            return new SimpleMessage("My Proposals");
        }

        @Override
        public void target(Forwarder f) {
            f.go("/wikis/home-wiki/call-3-experiment-proposal-submission");
        }
    };

    final View EVALUATIONS = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationsHandler.class, C3Experiment.this);
        }
    };

    final View EVALUATION = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationOfExperimentHandler.class, C3Experiment.this);
        }
    };

    final View REPORT = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ReportOfExperimentHandler.class, C3Experiment.this);
        }
    };

    final View FINAL_EVALUATION = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ViewForExperimentHandler.class, C3Experiment.this);
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
            f.goWithId(Edit1Handler.class, C3Experiment.this);

        }
    };

    final Action EDIT_2 = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit2Handler.class, C3Experiment.this);

        }
    };

    final Action EDIT_3 = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit3Handler.class, C3Experiment.this);

        }
    };

    final Action NEW_EVALUATION = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(de.infoasset.echordC3.handler.c3.evaluation.NewHandler.class, C3Experiment.this);
        }
    };

    final Action NEW_REPORT = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(de.infoasset.echordC3.handler.c3.consensusReport.NewHandler.class, C3Experiment.this);
        }
    };

    final Action EDIT_AREA_EDITOR = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EditAreaEditorHandler.class, C3Experiment.this);

        }
    };

    final Action DELETE = new ConfirmationRequiredAction() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(DeleteHandler.class, C3Experiment.this);
        }

        @Override
        public Message getConfirmationMessage() {
            return new SimpleParameterizedMessage("Do you really want to delete {0}?").setParameters(getName());
        }
    };
}

