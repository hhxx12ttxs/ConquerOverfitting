public class RemoveAbilityEffect extends ContinuousEffect {
protected String	namekey;
protected Ability	ability;

public RemoveAbilityEffect(String namekey) {
this.namekey = namekey;
}

@Override
public boolean applyThis() {

