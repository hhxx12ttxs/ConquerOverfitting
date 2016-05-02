package lrg.insider.plugins.filters.memoria.classes;

import lrg.common.abstractions.entities.AbstractEntity;
import lrg.common.abstractions.entities.AbstractEntityInterface;
import lrg.common.abstractions.entities.GroupEntity;
import lrg.common.abstractions.plugins.Descriptor;
import lrg.common.abstractions.plugins.filters.FilteringRule;
import lrg.common.abstractions.plugins.filters.composed.AndComposedFilteringRule;
import lrg.common.abstractions.plugins.filters.composed.NotComposedFilteringRule;
import lrg.insider.plugins.core.filters.memoria.ModelClassFilter;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: 10.02.2005
 * Time: 19:48:29
 * To change this template use File | Settings | File Templates.
 */
class HeavyHierarchy extends FilteringRule {
    public HeavyHierarchy() {
        super(new Descriptor("Heavy Hierarchy", "class"));
    }

    public boolean applyFilter(AbstractEntityInterface aClass) {
        FilteringRule isModelNotInterface = new AndComposedFilteringRule(new ModelClassFilter(),
                new NotComposedFilteringRule(new IsInterface()));

        if (isModelNotInterface.applyFilter(aClass) == false) return false;

        double nrOfDescendants = ((Double) aClass.getProperty("NOD").getValue()).doubleValue();
        GroupEntity allClasses = aClass.getGroup("all descendants").union((AbstractEntity) aClass);

        double avgWMC = ((Double) allClasses.getProperty("WMC").aggregate("avg").getValue()).doubleValue();

        return ((nrOfDescendants >= 4) && (avgWMC >= 30));
    }
}


