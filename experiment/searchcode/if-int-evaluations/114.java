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
package de.infoasset.echordC2.assets.experiment;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.infoasset.echordC2.EchordC2Plugin;
import de.infoasset.echordC2.assets.C2ApplicationExtension;
import de.infoasset.echordC2.assets.C2PersonExtension;
import de.infoasset.echordC2.assets.C2ApplicationExtension.CallStateProperty;
import de.infoasset.echordC2.assets.evaluation.C2AbstractEvaluation;
import de.infoasset.echordC2.assets.evaluation.C2AreaEditor;
import de.infoasset.echordC2.assets.evaluation.C2ConsensusReport;
import de.infoasset.echordC2.assets.evaluation.C2Evaluation;
import de.infoasset.echordC2.assets.evaluation.C2Evaluator;
import de.infoasset.echordC2.domains.evaluation.C2CallState;
import de.infoasset.echordC2.handler.c2.evaluation.ViewForExperimentHandler;
import de.infoasset.echordC2.handler.c2.evaluator.EvaluationOfExperimentHandler;
import de.infoasset.echordC2.handler.c2.evaluator.ReportOfExperimentHandler;
import de.infoasset.echordC2.handler.c2.experiment.DeleteHandler;
import de.infoasset.echordC2.handler.c2.experiment.Edit1Handler;
import de.infoasset.echordC2.handler.c2.experiment.Edit2Handler;
import de.infoasset.echordC2.handler.c2.experiment.Edit3Handler;
import de.infoasset.echordC2.handler.c2.experiment.EditAreaEditorHandler;
import de.infoasset.echordC2.handler.c2.experiment.EvaluationsHandler;
import de.infoasset.echordC2.handler.c2.experiment.ViewHandler;
import de.infoasset.echordC2.handler.c2.experimentParticipant.NewHandler;
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
import de.infoasset.platform.services.asset.UrlNameProperty;
import de.infoasset.platform.services.asset.Validator;
import de.infoasset.platform.services.asset.UniquenessQuery.FixViolation;
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
import de.infoasset.toro.assets.Searchable;
import de.infoasset.toro.assets.ReadProtected.RefreshReadAccessListener;
import de.infoasset.toro.assets.group.Group;
import de.infoasset.toro.assets.group.Person;
import de.infoasset.toro.assets.group.Principal;
import de.infoasset.toro.assets.links.ILinkable;
import de.infoasset.toro.assets.links.Linkable;
import de.infoasset.toro.assets.links.UrlNameQuery;

public class C2Experiment extends PersistentEntity implements ILinkable {

    public static final PersistentSchema<C2Experiment> SCHEMA = new PersistentSchema();

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
                CallStateProperty experimentCallStateProperty = C2ApplicationExtension.getC2Application().c2callForExperimentState;
                if (experimentCallStateProperty.isEquals(C2CallState.open) || experimentCallStateProperty.isEquals(C2CallState.closed)) {
                    copy.set(new OneIterable<Principal>(submitter.get().getEntity()));
                } else {
                    copy.set(EmptyIterable.<Principal> instance());
                }

                if (C2ApplicationExtension.getC2Application().c2panelState.isEquals(C2CallState.open)) {
                    copy.add(EchordC2Plugin.getPanelMembersGroup());
                }

                copy.add(C2AbstractEvaluation.getEvaluationCoordinators());
                CallStateProperty evaluationStateProperty = C2ApplicationExtension.getC2Application().c2evaluationState;
                if (evaluationStateProperty.isEquals(C2CallState.open)) {
                    for (C2Evaluation e : evaluations.getAssets()) {
                        copy.add(e.evaluator.get().getEntity());
                    }
                    C2ConsensusReport consensusReport = report.get();
                    if (consensusReport != null) {
                        copy.add(consensusReport.rapporteur.get().getEntity());
                    }
                }
                Attachments.refreshAttachments(C2Experiment.this);
                Directory dir = adapt(Attachments.class).directory.get();
                if (dir != null) {
                    final Directory dirCopy = (Directory) dir.createWritableCopy();
                    if (C2ApplicationExtension.getC2Application().c2callForExperimentState.isEquals(C2CallState.open)) {
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

    public final OneRole<C2PersonExtension> submitter = new OneRole<C2PersonExtension>() {

        public Message getLabel() {
            return new SimpleMessage("Submitter");
        };

        protected Role oppositeRole() {
            return C2PersonExtension.prototype.experiments;
        };

        public boolean showInGenericViews() {
            return false;
        };

        final ChangeListener refreshAccess = new RefreshReadAccessListener();
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

    public static C2Experiment getByUrlName(String urlName) {
        return SCHEMA.findSingleEntityNotNull(new UniquenessQuery(getUrlNameQuery(urlName), new FixViolation<C2Experiment>() {

            @Override
            public void fix(C2Experiment p) {
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
            return EchordC2Plugin.experimentType;
        };

            final Validator notNull = new NotNullValidator();
    };

    public final DomainValueProperty scenario = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Main Scenario");
        }

        @Override
        public Domain getDomain() {
            return EchordC2Plugin.experimentScenario;
        };

            final Validator notNull = new NotNullValidator();
    };

    public final DomainValueProperty researchFocus = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("Main Research Focus");
        }

        @Override
        public Domain getDomain() {
            return EchordC2Plugin.experimentFocus;
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

    public final ManyRole<C2ExperimentParticipant> participants = new ManyRole<C2ExperimentParticipant>() {
        public Message getLabel() {
            return new SimpleMessage("List of Partners");
        };

        protected Role oppositeRole() {
            return C2ExperimentParticipant.SCHEMA.prototype().experiment;
        };

        protected java.util.Comparator<C2ExperimentParticipant> getDefaultSortingComparator() {
            return new Comparator<C2ExperimentParticipant>() {

                @Override
                public int compare(C2ExperimentParticipant o1, C2ExperimentParticipant o2) {
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

    public final ManyRole<C2ExperimentItem> experimentItems = new ManyRole<C2ExperimentItem>() {
        public Message getLabel() {
            return new SimpleMessage("Equipment Items");
        };

        protected Role oppositeRole() {
            return C2ExperimentItem.SCHEMA.prototype().experiment;
        };

        public boolean isCascadeDelete() {
            return true;
        };
    };

    public final ManyRole<C2Evaluation> evaluations = new ManyRole<C2Evaluation>() {
        public Message getLabel() {
            return new SimpleMessage("Evaluations");
        };

        protected Role oppositeRole() {
            return C2Evaluation.SCHEMA.prototype().experiment;
        };
    };

    public final OneRole<C2AreaEditor> areaEditor = new OneRole<C2AreaEditor>() {
        public Message getLabel() {
            return new SimpleMessage("Area Editor");
        };

        protected Role oppositeRole() {
            return C2AreaEditor.prototype.experiments;
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

    public final OneRole<C2ConsensusReport> report = new OneRole<C2ConsensusReport>() {
        public Message getLabel() {
            return new SimpleMessage("Report");
        };

        protected Role oppositeRole() {
            return C2ConsensusReport.SCHEMA.prototype().experiment;
        };
    };

    @Override
    public String getName() {
        return acronym.get();
    }

    public static final HandlerPattern EXPERIMENT = new HandlerPattern("/c2/experiments/{urlName}", ViewHandler.class) {
    };

    @Override
    public void specifyUrl(Forwarder f) {
        f.go(EXPERIMENT, urlName.get());
    }

    @Override
    protected boolean mayEditThis() {
        CallStateProperty callStateProperty = C2ApplicationExtension.getC2Application().c2callForExperimentState;
        if (callStateProperty.isEquals(C2CallState.open)) {
            return ObjectUtils.equals(SessionLocal.getUser(), submitter.get());
        } else if (callStateProperty.isEquals(C2CallState.closed)) {
            return C2AbstractEvaluation.isEvaluationCoordinatorOrExCc();
        } else {
            return false;
        }
    }

    public Iterable<C2Evaluator> getEvaluators() {
        final Person rapporteur = report.get().rapporteur.get().getEntity();
        WrapperIterable<C2Evaluation, C2Evaluator> result = new WrapperIterable<C2Evaluation, C2Evaluator>(new FilterIterable<C2Evaluation>(
                evaluations.getAssets()) {

            @Override
            public boolean filter(C2Evaluation o) {
                return !o.evaluator.get().getEntity().equals(rapporteur);
            }
        }) {

            @Override
            public C2Evaluator wrap(C2Evaluation o) {
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
        for (C2AbstractEvaluation evaluation : evaluations.getAssets()) {
            bools.add(evaluation.toBeConsidered.get());
            sums[index++] = C2AbstractEvaluation.getFloat(evaluation.impact.getKey()) + C2AbstractEvaluation.getFloat(evaluation.efficiency.getKey())
                    + C2AbstractEvaluation.getFloat(evaluation.excellence.getKey());
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
            f.go(C2Experiment.this);
        }
    };

    final View MY_PROPSALS = new View() {

        @Override
        public Message overrideHandlerTargetLabel() {
            return new SimpleMessage("My Proposals");
        }

        @Override
        public void target(Forwarder f) {
            f.go("/wikis/home-wiki/call-2-experiment-proposal-submission");
        }
    };

    final View EVALUATIONS = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationsHandler.class, C2Experiment.this);
        }
    };

    final View EVALUATION = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationOfExperimentHandler.class, C2Experiment.this);
        }
    };

    final View REPORT = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ReportOfExperimentHandler.class, C2Experiment.this);
        }
    };

    final View FINAL_EVALUATION = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ViewForExperimentHandler.class, C2Experiment.this);
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
            f.goWithId(Edit1Handler.class, C2Experiment.this);

        }
    };

    final Action EDIT_2 = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit2Handler.class, C2Experiment.this);

        }
    };

    final Action EDIT_3 = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(Edit3Handler.class, C2Experiment.this);

        }
    };

    final Action NEW_EVALUATION = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(de.infoasset.echordC2.handler.c2.evaluation.NewHandler.class, C2Experiment.this);
        }
    };

    final Action NEW_REPORT = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(de.infoasset.echordC2.handler.c2.consensusReport.NewHandler.class, C2Experiment.this);
        }
    };

    final Action EDIT_AREA_EDITOR = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EditAreaEditorHandler.class, C2Experiment.this);

        }
    };

    final Action DELETE = new ConfirmationRequiredAction() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(DeleteHandler.class, C2Experiment.this);
        }

        @Override
        public Message getConfirmationMessage() {
            return new SimpleParameterizedMessage("Do you really want to delete {0}?").setParameters(getName());
        }
    };
}

