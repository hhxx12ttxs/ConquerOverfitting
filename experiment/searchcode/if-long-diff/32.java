/*Класс хранящий диф по разным событияям*/
public class EventDiff implements Serializable {
public EventType type;
public long money;
public ItemDiff itemDiff;

public EventDiff(EventType type, long money, StatsDiff statsDiff) {
this.money = money;

