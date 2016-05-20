package de.infoasset.echordC1.assets.evaluation;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import de.infoasset.echordC1.EchordC1Plugin;
import de.infoasset.echordC1.assets.experiment.C1Experiment;
import de.infoasset.echordC1.handler.evaluator.EvaluationsHandler;
import de.infoasset.echordC1.handler.evaluator.ReplyHandler;
import de.infoasset.echordC1.handler.evaluator.ViewHandler;
import de.infoasset.platform.client.SessionLocal;
import de.infoasset.platform.handler.Action;
import de.infoasset.platform.handler.Forwarder;
import de.infoasset.platform.handler.View;
import de.infoasset.platform.services.asset.BooleanProperty;
import de.infoasset.platform.services.asset.ChangeListener;
import de.infoasset.platform.services.asset.DomainValueProperty;
import de.infoasset.platform.services.asset.ManyRole;
import de.infoasset.platform.services.asset.OnPersistentDeferredChangeListener;
import de.infoasset.platform.services.asset.OptionalMixin;
import de.infoasset.platform.services.asset.Role;
import de.infoasset.platform.services.asset.StringProperty;
import de.infoasset.platform.services.asset.diff.Diff;
import de.infoasset.platform.services.domains.Domain;
import de.infoasset.platform.services.exceptions.ProtectedActionException;
import de.infoasset.platform.services.internationalization.Message;
import de.infoasset.platform.services.internationalization.SimpleMessage;
import de.infoasset.platform.store.Descending;
import de.infoasset.platform.store.QueryEquals;
import de.infoasset.platform.store.Store;
import de.infoasset.toro.assets.group.Person;

public class C1Evaluator extends OptionalMixin<Person> {
    public static final C1Evaluator prototype = createPrototype(C1Evaluator.class);

    public final ManyRole<C1Evaluation> evaluations = new ManyRole<C1Evaluation>() {
        public Message getLabel() {
            return new SimpleMessage("Evaluations");
        };

        protected Role oppositeRole() {
            return C1Evaluation.SCHEMA.prototype().evaluator;
        };
    };

    public final StringProperty keywords = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Keywords");
        };

        public int getMaxLength() {
            return Store.MEDIUM_TEXT;
        };
    };

    public final BooleanProperty hasReplied = new BooleanProperty() {
        public boolean showInGenericViews() {
            return false;
        };

        public Boolean getDefaultValueHook() {
            return false;
        };
    };

    public final BooleanProperty avForCall1 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for remote evaluation of experiments Call 1, Dec. 2009");
        };
    };

    public final BooleanProperty avForEvalPanel1 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for the evaluation panel of Call 1, Jan. 2010");
        };
    };

    public final BooleanProperty avForCall2 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for remote evaluation of experiments Call 2, April 2010");
        };
    };

    public final BooleanProperty avForEvalPanel2 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for the evaluation panel of Call 2, May 2010");
        };
    };

    public final BooleanProperty avForCall3 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for remote evaluation of experiments Call 3, Aug. 2010");
        };
    };

    public final BooleanProperty avForEvalPanel3 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for the evaluation panel of Call 3, Sep. 2010");
        };
    };

    public final StringProperty avComment = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Comment on availability");
        };
    };

    public final BooleanProperty conflict = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("My group is involved in a proposal or any other potential conflict of interest exists");
        };
    };

    public final BooleanProperty openForEvaluation = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("Open for Evaluation");
        };

        protected Boolean getDefaultValueHook() {
            return false;
        };

        final ChangeListener LISTENER = new OnPersistentDeferredChangeListener() {

            @Override
            public void change(Diff diff) {
                if (diff.isChanged()) {
                    for (C1Evaluation evaluation : evaluations.getAssets()) {
                        C1Experiment experiment = (C1Experiment) evaluation.getExperiment().createWritableCopy();
                        if (openForEvaluation.get()) {
                            experiment.addReadAccess(getEntity());
                        } else {
                            experiment.removeReadAccess(getEntity());
                        }
                        experiment.persist();
                    }
                }
            }
        };
    };

    public final StringProperty alias = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Alias");
        };
    };
    
    public final ManyRole<C1FinalEvaluation> agreedEvaluation = new ManyRole<C1FinalEvaluation>() {
        protected Role oppositeRole() {
            return C1FinalEvaluation.SCHEMA.prototype().agrees;
        };
    };

    @Override
    public boolean allowManualAssignment() {
        return false;
    }

    protected void assignHook() {
        QueryEquals q = new QueryEquals(C1Evaluator.prototype.hasType, true);
        q.addSortingCriterion(new Descending(C1Evaluator.prototype.alias));
        Iterator<Person> it = Person.SCHEMA.queryEntitiesIterator(q);
        int max = 0;
        if (it.hasNext()) {
            C1Evaluator p = it.next().adapt(C1Evaluator.class);
            String strippedAlias = StringUtils.stripStart(p.alias.get(), "0");
            if (strippedAlias.length() > 0) {
                max = Integer.parseInt(strippedAlias);
            }
        }
        alias.set(StringUtils.leftPad(Integer.toString(++max), 3, "0"));
    };

    public void checkIsEvaluationCoordinatorOrSelf() {
        if (!(C1Evaluation.isEvaluationCoordinatorOrExCc() || ObjectUtils.equals(getEntity().getReadOnlyEntity(), SessionLocal.getUser()))) {
            throw new ProtectedActionException();
        }
    }

    public final DomainValueProperty involvedIn = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("If involved in experiment proposal, this will be in");
        }

        @Override
        public Domain getDomain() {
            return EchordC1Plugin.calls;
        };
    };

    public final StringProperty invProposalName = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Name of the proposal or other conflict");
        };

        public int getMaxLength() {
            return Store.MEDIUM_TEXT;
        };
    };

    final Action EDIT = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ReplyHandler.class, C1Evaluator.this);
        }
    };

    final View VIEW = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ViewHandler.class, C1Evaluator.this);
        }
    };

    final View EVALUATIONS = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationsHandler.class, C1Evaluator.this);
        }
    };
}

