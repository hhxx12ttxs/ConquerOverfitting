package de.infoasset.echordC3.assets.evaluation;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import de.infoasset.echordC3.EchordC3Plugin;
import de.infoasset.echordC3.handler.c3.evaluator.EvaluationsHandler;
import de.infoasset.echordC3.handler.c3.evaluator.OpenHandler;
import de.infoasset.echordC3.handler.c3.evaluator.ReplyHandler;
import de.infoasset.echordC3.handler.c3.evaluator.ViewHandler;
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

public class C3Evaluator extends OptionalMixin<Person> {
    public static final C3Evaluator prototype = createPrototype(C3Evaluator.class);

    public final ManyRole<C3AbstractEvaluation> evaluations = new ManyRole<C3AbstractEvaluation>() {
        public Message getLabel() {
            return new SimpleMessage("Evaluations");
        };

        protected Role oppositeRole() {
            return C3AbstractEvaluation.SCHEMA.prototype().evaluator;
        };
    };

    public final OneRole<C3AreaEditor> areaEditor = new OneRole<C3AreaEditor>() {
        public Message getLabel() {
            return new SimpleMessage("Area Editor");
        };

        protected Role oppositeRole() {
            return C3AreaEditor.prototype.evaluators;
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

    public final BooleanProperty avForCall3 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for remote evaluation of experiments Call 3.");
        };

        public Message getShortHelp() {
            return new SimpleMessage("In the time frame October till December 2010.");
        };
    };

    public final BooleanProperty avForEvalPanel3 = new BooleanProperty() {
        public Message getLabel() {
            return new SimpleMessage("I am available for the evaluation panel meeting of Call 3.");
        };

        public Message getShortHelp() {
            return new SimpleMessage("Time frame beginning end of November till beginning of December 2010.");
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
                for (C3AbstractEvaluation evaluation : evaluations.getAssets()) {
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
                return C3Evaluator.this;
            };
        };
    }

    public final StringProperty alias = new StringProperty() {
        public Message getLabel() {
            return new SimpleMessage("Alias");
        };
    };

    public final ManyRole<C3FinalEvaluation> agreedEvaluation = new ManyRole<C3FinalEvaluation>() {
        protected Role oppositeRole() {
            return C3FinalEvaluation.SCHEMA.prototype().agrees;
        };
    };

    @Override
    public boolean allowManualAssignment() {
        return false;
    }

    protected void assignHook() {
        QueryEquals q = new QueryEquals(C3Evaluator.prototype.hasType, true);
        q.addSortingCriterion(new Descending(C3Evaluator.prototype.alias));
        Iterator<Person> it = Person.SCHEMA.queryEntitiesIterator(q);
        int max = 0;
        if (it.hasNext()) {
            C3Evaluator p = it.next().adapt(C3Evaluator.class);
            String strippedAlias = StringUtils.stripStart(p.alias.get(), "0");
            if (strippedAlias.length() > 0) {
                max = Integer.parseInt(strippedAlias);
            }
        }
        alias.set(StringUtils.leftPad(Integer.toString(++max), 3, "0"));
    };

    public void checkIsEvaluationCoordinatorOrSelf() {
        if (!(C3AbstractEvaluation.isEvaluationCoordinatorOrExCc() || ObjectUtils.equals(getEntity().getReadOnlyEntity(), SessionLocal.getUser()))) {
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
            return EchordC3Plugin.calls;
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
            f.goWithId(ReplyHandler.class, C3Evaluator.this);
        }
    };

    final Action OPEN = new Action() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(OpenHandler.class, C3Evaluator.this);
        }
    };

    final View VIEW = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(ViewHandler.class, C3Evaluator.this);
        }
    };

    final View EVALUATIONS = new View() {

        @Override
        public void target(Forwarder f) {
            f.goWithId(EvaluationsHandler.class, C3Evaluator.this);
        }
    };
}

