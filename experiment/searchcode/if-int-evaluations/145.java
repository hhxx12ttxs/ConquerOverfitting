package de.infoasset.echordC2.assets.evaluation;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import de.infoasset.echordC2.EchordC2Plugin;
import de.infoasset.echordC2.handler.c2.evaluator.EvaluationsHandler;
import de.infoasset.echordC2.handler.c2.evaluator.OpenHandler;
import de.infoasset.echordC2.handler.c2.evaluator.ReplyHandler;
import de.infoasset.echordC2.handler.c2.evaluator.ViewHandler;
import de.infoasset.platform.client.SessionLocal;
import de.infoasset.platform.handler.Action;
import de.infoasset.platform.handler.Forwarder;
import de.infoasset.platform.handler.Mail;
import de.infoasset.platform.handler.View;
import de.infoasset.platform.services.asset.BooleanProperty;
import de.infoasset.platform.services.asset.ChangeListener;
import de.infoasset.platform.services.asset.DomainValueProperty;
import de.infoasset.platform.services.asset.ManyRole;
import de.infoasset.platform.services.asset.OnPersistentDeferredChangeListener;
import de.infoasset.platform.services.asset.OneRole;
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
import de.infoasset.toro.assets.ReadProtected;
import de.infoasset.toro.assets.group.Person;

public class C2Evaluator extends OptionalMixin<Person> {
    public static final C2Evaluator prototype = createPrototype(C2Evaluator.class);

    public final ManyRole<C2AbstractEvaluation> evaluations = new ManyRole<C2AbstractEvaluation>() {
        public Message getLabel() {
            return new SimpleMessage("Evaluations");
        };

        protected Role oppositeRole() {
            return C2AbstractEvaluation.SCHEMA.prototype().evaluator;
        };
    };

    public final OneRole<C2AreaEditor> areaEditor = new OneRole<C2AreaEditor>() {
        public Message getLabel() {
            return new SimpleMessage("Area Editor");
        };

        protected Role oppositeRole() {
            return C2AreaEditor.prototype.evaluators;
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

    public final BooleanProperty avForCall2 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for remote evaluation of experiments in Call 2");
        };

        public Message getShortHelp() {
            return new SimpleMessage(
                    "In the time frame 10th of May to end of June. This includes initial evaluations finished by 7th of June, furter evaluation steps finished by 21st of June (possibly 3rd evaluation and consensus finding including blogging, consensus report drafting and finalizing).");
        };
    };

    public final BooleanProperty avForEvalPanel2 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for the evaluation panel meeting of Call 2.");
        };

        public Message getShortHelp() {
            return new SimpleMessage("Planned for 25th of June 2010.");
        };
    };

    public final BooleanProperty avForCall3 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for remote evaluation of experiments Call 3.");
        };

        public Message getShortHelp() {
            return new SimpleMessage("In the time frame September till October 2010.");
        };
    };

    public final BooleanProperty avForEvalPanel3 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for the evaluation panel meeting of Call 3.");
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

        final ChangeListener refreshAccess = new OnPersistentDeferredChangeListener() {

            @Override
            public void change(Diff diff) {
                for (C2AbstractEvaluation evaluation : evaluations.getAssets()) {
                    evaluation.adapt(ReadProtected.class).refreshReadAccess();
                    evaluation.getExperiment().adapt(ReadProtected.class).refreshReadAccess();
                }
            }
        };

        final ChangeListener sendMail = new OnPersistentDeferredChangeListener() {

            @Override
            public void change(Diff diff) {
                if (get()) {
                    openedMail().sendMail();
                }
            }
        };
    };

    Mail openedMail() {
        return new Mail() {

            @Override
            public void init() {
                this.init(getEntity().getMailAddress());
            }

            public Object getScopeObject() {
                return C2Evaluator.this;
            };
        };
    }

    public final StringProperty alias = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Alias");
        };
    };

    public final ManyRole<C2FinalEvaluation> agreedEvaluation = new ManyRole<C2FinalEvaluation>() {
        protected Role oppositeRole() {
            return C2FinalEvaluation.SCHEMA.prototype().agrees;
        };
    };

    @Override
    public boolean allowManualAssignment() {
        return false;
    }

    protected void assignHook() {
        QueryEquals q = new QueryEquals(C2Evaluator.prototype.hasType, true);
        q.addSortingCriterion(new Descending(C2Evaluator.prototype.alias));
        Iterator<Person> it = Person.SCHEMA.queryEntitiesIterator(q);
        int max = 0;
        if (it.hasNext()) {
            C2Evaluator p = it.next().adapt(C2Evaluator.class);
            String strippedAlias = StringUtils.stripStart(p.alias.get(), "0");
            if (strippedAlias.length() > 0) {
                max = Integer.parseInt(strippedAlias);
            }
        }
        alias.set(StringUtils.leftPad(Integer.toString(++max), 3, "0"));
    };

    public void checkIsEvaluationCoordinatorOrSelf() {
        if (!(C2AbstractEvaluation.isEvaluationCoordinatorOrExCc() || ObjectUtils.equals(getEntity().getReadOnlyEntity(), SessionLocal.getUser()))) {
            throw new ProtectedActionException();
        }
    }

    public void checkIsSelf() {
        if (!ObjectUtils.equals(getEntity().getReadOnlyEntity(), SessionLocal.getUser())) {
            throw new ProtectedActionException();
        }
    }

    public final DomainValueProperty involvedIn = new DomainValueProperty() {
        public Message getLabel() {
            return new SimpleMessage("If involved in experiment proposal, this will be in");
        }

        @Override
        public Domain getDomain() {
            return EchordC2Plugin.calls;
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
            f.goWithId(ReplyHandler.class, C2Evaluator.this);
        }
    };

    final Action OPEN = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(OpenHandler.class, C2Evaluator.this);
        }
    };

    final View VIEW = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ViewHandler.class, C2Evaluator.this);
        }
    };

    final View EVALUATIONS = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationsHandler.class, C2Evaluator.this);
        }
    };
}

