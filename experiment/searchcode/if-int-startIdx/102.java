package contacts.service;

import contacts.domain.ContactModel;
import contacts.domain.ContactSearchResult;
import contacts.domain.PageState;
import contacts.fakename.FakeNameGenerator;
import contacts.fakename.FakeNameRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
@Path("/backend")
public class ContactsService {

    private static List<ContactModel> contacts = findAllContacts();

    @GET
    @Path("/contact")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public ContactSearchResult fetchByCriteria(@QueryParam("name") String name, @QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {

        // adjust any negative pageNumbers
        if(pageNumber < 0) {
            pageNumber = 0;
        }

        // pageNumbers start from page zero, even though  they have 1 on the ui
        int startIdx = (pageNumber * pageSize) + 1;
        if(startIdx > contacts.size() - 1) {

            // if pSz = 3, and contacts.size = 8, and pageNumber = 10, then startIdx = 7
            int lastPage = contacts.size() / pageSize;
            startIdx = (lastPage * pageSize) + 1;

            // update which pageNumber to be the lastPage
            pageNumber = lastPage;
        }

        int endIdx = Math.min(startIdx + pageSize, contacts.size() - 1);

        List<ContactModel> list = new ArrayList<ContactModel>(pageSize);
        for(int i = startIdx; i < endIdx; i++) {
            list.add(contacts.get(i));
        }

        int totalPageCount = new Double(Math.ceil((double)contacts.size() / pageSize)).intValue();
        PageState pageState = new PageState();
        pageState.setPageNumber(pageNumber);
        pageState.setPageSize(pageSize);
        pageState.setTotalPageCount(totalPageCount);

        ContactSearchResult result = new ContactSearchResult();
        result.setPageState(pageState);
        result.setContacts(list);
        return result;
    }

    @GET
    @Path("/contact/{cid}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public ContactModel fetchOne(@PathParam("cid") Long contactId) {
        ContactModel contact = contacts.get(findContactIndex(contactId));
        return contact;
    }

    private int findContactIndex(long contactId) {
        int idx = -1;
        for(int i = 0; i < contacts.size(); i++) {
            ContactModel nextContact = contacts.get(i);
            if(contactId == nextContact.getContactId().intValue()) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    @POST
    @Path("/contact")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public void save(ContactModel contactModel)
    {
        System.out.println("Save contactModel = " + contactModel);
        if(contactModel != null) {
            System.out.println(contactModel.getContactId() + ": " + contactModel.getGivenName() + " " + contactModel.getSurname());

            if(contactModel.getContactId() == null) {
                contactModel.setContactId(contacts.size());
                contacts.add(contactModel);
            }
            else {
                contacts.set(findContactIndex(contactModel.getContactId()), contactModel);
            }
        }
    }


    private static List<ContactModel> findAllContacts() {
        FakeNameGenerator fakeNameGenerator = new FakeNameGenerator();
        FakeNameRecord[] fakeNameRecords = fakeNameGenerator.generateRecords(50);

        List<ContactModel> list = new ArrayList<ContactModel>();
        for(int i = 0; i < fakeNameRecords.length; i++) {
            FakeNameRecord r = fakeNameRecords[i];

            ContactModel c = new ContactModel();
            c.setContactId(r.getNumber());
            c.setGivenName(r.getGivenName());
            c.setSurname(r.getSurname());
            c.setStreetAddress(r.getStreetAddress());
            c.setCity(r.getCity());
            c.setZipCode(r.getZipCode());
            c.setCountry(r.getCountry());
            c.setEmailAddress(r.getEmailAddress());
            c.setTelephoneNumber(r.getTelephoneNumber());
            c.setBirthday(r.getBirthday());

            list.add(c);
        }

        Collections.sort(list, new Comparator<ContactModel>() {
            @Override
            public int compare(ContactModel o1, ContactModel o2) {
                String s1 = o1.getSurname() + o1.getGivenName() + o1.getContactId();
                String s2 = o2.getSurname() + o2.getGivenName() + o2.getContactId();
                return s1.compareTo(s2);
            }
        });
        return list;
    }

}

