package be.tumbador.services.impl;

import be.tumbador.dao.IEventDao;
import be.tumbador.dao.IShiftDao;
import be.tumbador.dao.ISubscriptionDao;
import be.tumbador.model.*;
import be.tumbador.services.IEventService;
import be.tumbador.services.IPersonService;
import be.tumbador.services.IShiftService;
import org.rhok.foodmovr.exceptions.NotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class EventService implements IEventService {

    @Autowired
    private IEventDao eventDao;

    @Autowired
    private IShiftDao shiftDao;

    @Autowired
    private ISubscriptionDao subscriptionDao;

    @Autowired
    private IPersonService personService;

    @Autowired
    private IShiftService shiftService;

    public void setEventDao(IEventDao eventDao) {
		this.eventDao = eventDao;
	}

	@Override
	public Event create(Event entity) throws NotValidException {	
		Event event = this.eventDao.create(entity);
		
		for (Shift shift : entity.getShifts()) {
			shift.setEvent(event);
			this.shiftDao.update(shift);
		}
		
		return event;
	}

	@Override
	public Event get(int id) {
		return this.eventDao.get(id);
	}

	@Override
	public void edit(Event entity) throws NotValidException {
		this.eventDao.update(entity);
	}

	@Override
	public void delete(int id) {
		this.eventDao.delete(id);
	}

	@Override
	public Collection<Event> getAll() {
		return this.eventDao.getAll();
	}



    @Override
    public Map<Person, Double> getSubscribeBonusPerShift(Shift shift) {
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForShift(shift);
        Map<Person, Double> subscribeBonusMap = new HashMap<Person, Double>();
        Collections.sort(subscriptions, new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        int counter = 1;
        for (Subscription subscription : subscriptions) {
            subscribeBonusMap.put(subscription.getPerson(), ((double)(subscriptions.size() - 1) / (double)(subscriptions.size() - counter) ));
        }
        return null;
    }

    @Override
    public void planEvents() {
        List<Event> events = new ArrayList<Event> (getAll());
        List<Shift> shifts = new ArrayList<Shift>();
        for (Event event : events) {
            List<Shift> eventShifts = event.getShifts();
            for (Shift eventShift : eventShifts) {
                shifts.add(eventShift);
            }
        }
        Collections.sort(shifts, new Comparator<Shift>() {
            @Override
            public int compare(Shift o1, Shift o2) {
                return o1.getStartTime().compareTo(o1.getStartTime());
            }
        });
        Map<Shift, Integer> popularityBonusMap = shiftService.getShiftPopularityPoints();
        for (Shift shift : shifts) {
            Map<Person, Double> subscribeBonusMap = getSubscribeBonusPerShift(shift);
            List<Person> persons = new ArrayList<Person>();
            for (Person person : subscribeBonusMap.keySet()) {
                double subscribeBonus = subscribeBonusMap.get(person);
                double tagBonus = personService.getPersonTagScore(person);
                double beginnerBonus = personService.getBeginnerBonus(person);
                double popularityBonus = personService.getPopularityScore(person, popularityBonusMap);
                double abscencyMinus = personService.getAbscencyMinus(person);
                person.setScore(new Score(person, subscribeBonus, popularityBonus, beginnerBonus, tagBonus, abscencyMinus));
                persons.add(person);
            }
            List<Subscription> subscriptions = shift.getSubscriptions();
            for (Subscription subscription : subscriptions) {
                for (Person person : persons) {
                    if (subscription.getPerson().getId() == person.getId()){
                        subscription.setPerson(person);
                    }
                }
            }
            Collections.sort(subscriptions, new Comparator<Subscription>() {
                @Override
                public int compare(Subscription o1, Subscription o2) {
                    return ((int) (o1.getPerson().getScore().getScore() * 1000000.0)) - ((int)(o2.getPerson().getScore().getScore() * 1000000.0));
                }
            });
            int ctrSubscription = 0;
            for (Subscription subscription : subscriptions) {
                if(ctrSubscription < shift.getMaxVolunteers()){
                    subscription.setAccepted(AcceptedStatus.Accepted);
                }else{
                    if(ctrSubscription < shift.getEvent().getMaxReserves() + shift.getMaxVolunteers()){
                        subscription.setAccepted(AcceptedStatus.Reserve);
                    }else {
                        subscription.setAccepted(AcceptedStatus.NotAccepted);
                    }
                }
                subscriptionDao.update(subscription);
            }
        }
    }
}

