package efi.unleashed.workflow.impl;


import efi.platform.TestingUtils;
import efi.platform.service.ErrorMessageService;
import efi.platform.web.controller.WorkflowErrors;
import efi.unleashed.TestConstants;
import efi.unleashed.domain.Address;
import efi.unleashed.domain.Contact;
import efi.unleashed.security.SessionUserContext;
import efi.unleashed.service.ContactService;
import efi.unleashed.service.EnterpriseService;
import efi.unleashed.view.web.AddressView;
import efi.unleashed.view.web.ContactAccountView;
import efi.unleashed.view.web.ContactView;
import efi.unleashed.workflow.ContactWorkflow;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "/spring-context-testing.xml" } )
@TransactionConfiguration( defaultRollback = true )
@Transactional
public class ContactWorkflowTest {

    @Resource
    ContactWorkflow contactWorkflow;

    @Resource( name = "scopedTarget.sessionUserContext" )
    SessionUserContext userContext;

    @Resource
    ContactService contactService;

    @Resource
    ErrorMessageService errorMessageService;

    @Resource
    EnterpriseService enterpriseService;

    @Before
    public void setupUserContext() {

        userContext.setUserId( TestConstants.TEST_USER_ID );
        userContext.setEnterpriseId( TestConstants.TEST_ENTERPRISE_ID );
    }

    @Test
    public void testInvalidFindContact() throws Exception {

        WorkflowErrors errorsUnusedIdTest = new WorkflowErrors();
        ContactView contactViewUnusedIdTest =
                contactWorkflow.findContact( TestConstants.TEST_UNUSED_ID, errorsUnusedIdTest );
        assertNotNull( errorsUnusedIdTest );
        assertTrue( TestingUtils.errorsContains( errorsUnusedIdTest,
                                                 errorMessageService,
                                                 "The selected contact could not be found." ) );

        WorkflowErrors errorsNullTest = new WorkflowErrors();
        ContactView contactViewNullTest = contactWorkflow.findContact( null, errorsNullTest );
        assertNotNull( errorsNullTest );
        assertTrue( TestingUtils.errorsContains( errorsNullTest,
                                                 errorMessageService,
                                                 "Please select a contact." ) );
    }

    @Test
    public void testFindContact() throws Exception {

        WorkflowErrors errors = new WorkflowErrors();
        ContactView contactView = contactWorkflow.findContact( TestConstants.TEST_CONTACT_ID_NO_ACCOUNT, errors );
        assertNotNull( contactView );
        assertEquals( contactView.getFirstName(), "Dan" );
    }

    @Test
    public void testInvalidEmptyAddContact() throws Exception {

        ContactView contactView = new ContactView();
        WorkflowErrors errors = new WorkflowErrors();
        ContactView response = contactWorkflow.addContact( contactView, errors );
        assertTrue( errors.hasErrors() );
        List<String> messages = errorMessageService.getDisplayableErrors( errors );
        assertEquals( 2, messages.size() );
        Integer messageContainsMayNotBeEmpty = 0;
        Integer messageContainsMayNotBeNull = 0;
        for ( String message : messages ) {
            if ( message.contains( "may not be empty" ) ) {
                messageContainsMayNotBeEmpty++;
            }
        }
        assertTrue( messageContainsMayNotBeEmpty.equals( 2 ) );
    }

    @Test
    public void testAddContactNoAccount() throws Exception {

        ContactView contactView = new ContactView();
        WorkflowErrors errors = new WorkflowErrors();

        LocalDate anniversary = LocalDate.parse( "1984-10-23" );
        LocalDate birthday = LocalDate.parse( "1984-12-01" );

        contactView.setFirstName( "Charlie" );
        contactView.setLastName( "Testerson" );
        contactView.setHomeEmail( "charlie.home@reserveinteractive.com" );
        contactView.setWorkEmail( "charlie.work@reserveinteractive.com" );
        contactView.setOtherEmail( "charlie.other@reserveinteractive.com" );
        contactView.setOptOut( true );
        contactView.setFax( "4444444444" );
        contactView.setMobilePhone( "5555555555" );
        contactView.setHomePhone( "7777777777" );
        contactView.setWorkPhone( "8888888888" );
        contactView.setAnniversary( anniversary );
        contactView.setBirthday( birthday );

        String address1 = "12332 Test way";
        String address2 = "Suite A";
        String city = "Pleasanton";
        String state = "CA";
        String zipCode = "94551";
        String country = "United States";

        Address address = new Address();
        address.setAddress1( address1 );
        address.setAddress2( address2 );
        address.setCity( city );
        address.setState( state );
        address.setZipCode( zipCode );
        address.setCountry( country );

        contactView.setAddressView( new AddressView( address ) );

        //Attempt to add contact with no account
        ContactView contactViewLookedUp = contactWorkflow.addContact( contactView, errors );

        //Look up the contact in the database
        //ContactView contactViewLookedUp = contactWorkflow.findContact( response.getContactId(), errors );
        assertNotNull( contactViewLookedUp );
        assertNull( contactViewLookedUp.getAccountId() );
        assertEquals( contactViewLookedUp.getFirstName(), contactView.getFirstName() );
        assertEquals( contactViewLookedUp.getFirstName(), contactView.getFirstName() );
        assertEquals( contactViewLookedUp.getAnniversary(), contactView.getAnniversary() );
        assertEquals( contactViewLookedUp.getHomeEmail(), contactView.getHomeEmail() );
        assertEquals( contactViewLookedUp.getWorkEmail(), contactView.getWorkEmail() );
        assertEquals( contactViewLookedUp.getOtherEmail(), contactView.getOtherEmail() );
        assertEquals( contactViewLookedUp.getOptOut(), contactView.getOptOut() );
        assertEquals( contactViewLookedUp.getAddressView().getAddress1(), address1 );
        assertEquals( contactViewLookedUp.getAddressView().getAddress2(), address2 );
        assertEquals( contactViewLookedUp.getAddressView().getCity(), city );
        assertEquals( contactViewLookedUp.getAddressView().getState(), state );
        assertEquals( contactViewLookedUp.getAddressView().getZipCode(), zipCode );
        assertEquals( contactViewLookedUp.getAddressView().getCountry(), country );
    }

    @Test
    public void testInvalidAddContactWithAccount() throws Exception {

        ContactView contactView = new ContactView();
        contactView.setFirstName( "Charlie" );
        contactView.setLastName( "Testerson" );
        contactView.setHomeEmail( "charlie.testerson@reserveinteractive.com" );
        contactView.setFax( "4444444444" );
        contactView.setMobilePhone( "5555555555" );
        contactView.setHomePhone( "7777777777" );
        contactView.setWorkPhone( "8888888888" );
        LocalDate anniversary = LocalDate.parse( "1984-10-23" );
        contactView.setAnniversary( anniversary );
        LocalDate birthday = LocalDate.parse( "1984-12-01" );
        contactView.setBirthday( birthday );

        //Attempt to add contact with an account
        contactView.setAccountId( TestConstants.TEST_UNUSED_ID );
        WorkflowErrors errors = new WorkflowErrors();
        ContactView resultingContactFromAddContactWithInvalidAccount =
                contactWorkflow.addContact( contactView, errors );

        assertTrue( errors.hasErrors() );
        assertTrue( TestingUtils.errorsContains( errors,
                                                 errorMessageService,
                                                 "The selected account could not be found." ) );
    }

    @Test
    public void testAddContactWithAccount() throws Exception {

        ContactView contactView = new ContactView();
        WorkflowErrors errors = new WorkflowErrors();

        LocalDate anniversary = LocalDate.parse( "1984-10-23" );
        LocalDate birthday = LocalDate.parse( "1984-12-01" );

        contactView.setFirstName( "Charlie" );
        contactView.setLastName( "Testerson" );
        contactView.setHomeEmail( "charlie.testerson@reserveinteractive.com" );
        contactView.setFax( "4444444444" );
        contactView.setMobilePhone( "5555555555" );
        contactView.setHomePhone( "7777777777" );
        contactView.setWorkPhone( "8888888888" );
        contactView.setAnniversary( anniversary );
        contactView.setBirthday( birthday );

        //Attempt to add contact with an account
        contactView.setAccountId( TestConstants.TEST_ACCOUNT_ID_NO_CONTACTS );
        ContactView response = contactWorkflow.addContact( contactView, errors );

        //Look up contact in database
        ContactView contactViewLookedUp = contactWorkflow.findContact( response.getId(), errors );
        assertNotNull( contactViewLookedUp );
        assertEquals( contactView.getAccountId(), contactViewLookedUp.getAccountId() );
        assertEquals( contactView.getFirstName(), contactViewLookedUp.getFirstName() );
        assertEquals( contactView.getFirstName(), contactViewLookedUp.getFirstName() );
        assertEquals( contactView.getAnniversary(), contactViewLookedUp.getAnniversary() );
        assertEquals( contactView.getAccountId(), contactViewLookedUp.getAccountId() );
    }

    @Test
    public void testAddAccountToContactAccountAndContactNotFound() throws Exception {

        WorkflowErrors errors = new WorkflowErrors();
        ContactView existingContact = contactWorkflow.findContact( TestConstants.TEST_CONTACT_ID_NO_ACCOUNT, errors );
        assertNotNull( existingContact );

        //First test with an invalid accountId
        ContactAccountView contactAccountView = new ContactAccountView();
        contactAccountView.setAccountId( TestConstants.TEST_UNUSED_ID );
        contactAccountView.setContactId( existingContact.getId() );
        errors = new WorkflowErrors();
        ContactView contactView = contactWorkflow.addAccountToContact( contactAccountView, errors );
        assertTrue( TestingUtils.errorsContains( errors,
                                                 errorMessageService,
                                                 "Please select an account." ) );
        //Second test with an invalid contactId
        contactAccountView.setAccountId( TestConstants.TEST_ACCOUNT_ID_NO_CONTACTS );
        contactAccountView.setContactId( TestConstants.TEST_UNUSED_ID );
        errors = new WorkflowErrors();
        contactView = contactWorkflow.addAccountToContact( contactAccountView, errors );
        assertTrue( TestingUtils.errorsContains( errors,
                                                 errorMessageService,
                                                 "The selected contact could not be found." ) );
    }

    @Test
    public void testInvalidAddAccountToContact() throws Exception {

        ContactAccountView contactAccountView = new ContactAccountView();
        WorkflowErrors errors = new WorkflowErrors();
        ContactView response = contactWorkflow.addAccountToContact( contactAccountView, errors );
        assertTrue( errors.hasErrors() );
        List<String> messages = TestingUtils.getDisplayableErrors( errors, errorMessageService );
        assertEquals( 2, messages.size() );
        for ( String message : messages ) {
            assertTrue( message.contains( "may not be null" ) );
        }
    }

    @Test
    public void testAddAccountToContact() throws Exception {

        WorkflowErrors errors = new WorkflowErrors();
        Contact existingContact = contactService.findContactForEnterprise( TestConstants.TEST_CONTACT_ID_NO_ACCOUNT,
                                                                           TestConstants.TEST_ENTERPRISE_ID );
        assertNotNull( existingContact );

        ContactAccountView contactAccountView = new ContactAccountView();
        contactAccountView.setAccountId( TestConstants.TEST_ACCOUNT_ID_NO_CONTACTS );
        contactAccountView.setContactId( existingContact.getId() );
        ContactView contactView = contactWorkflow.addAccountToContact( contactAccountView, errors );
        assertNotNull( contactView );
        assertEquals( contactView.getAccountId(), contactAccountView.getAccountId() );
        //Finally, look up the contact from the database and verify that the account was assigned to it.
        Contact contact = contactService.findContactForEnterprise( contactView.getId(),
                                                                   TestConstants.TEST_ENTERPRISE_ID );
        assertEquals( contact.getAccount().getId(), contactAccountView.getAccountId() );
    }

    @Test
    public void testUpdateContact() throws Exception {

        WorkflowErrors errors = new WorkflowErrors();
        ContactView contactView = contactWorkflow.findContact( TestConstants.TEST_CONTACT_ID_WITH_ACCOUNT, errors );
        //Change values
        contactView.setFirstName( "Howard" );
        contactView.setLastName( "Mills" );
        contactView.setHomePhone( "0000000000" );
        contactView.setWorkPhone( "2222222222" );
        contactView.setFax( "3333333333" );
        contactView.setHomeEmail( "test@test.com" );
        contactView.setWorkEmail( "test@test.com" );
        contactView.setOtherEmail( "test@test.com" );
        contactView.setOptOut( false );
        contactView.setBirthday( LocalDate.parse( "1960-12-12" ) );
        contactView.setAnniversary( LocalDate.parse( "1970-12-10" ) );
        Address address = new Address();
        address.setAddress1( "Addy1" );
        address.setAddress2( "Addy2" );
        address.setCity( "CityUpdated" );
        address.setState( "StateUpdated" );
        address.setZipCode( "ZipUpdated" );
        address.setCountry( "CountryUpdated" );
        contactView.setAddressView( new AddressView( address ) );
        errors = new WorkflowErrors();

        contactWorkflow.updateContact( contactView, errors );

        ContactView lookedUpContactView = contactWorkflow.findContact( contactView.getId(), errors );
        assertEquals( lookedUpContactView.getFirstName(), contactView.getFirstName() );
        assertEquals( lookedUpContactView.getLastName(), contactView.getLastName() );
        assertEquals( lookedUpContactView.getHomePhone(), contactView.getHomePhone() );
        assertEquals( lookedUpContactView.getWorkPhone(), contactView.getWorkPhone() );
        assertEquals( lookedUpContactView.getFax(), contactView.getFax() );
        assertEquals( lookedUpContactView.getHomeEmail(), contactView.getHomeEmail() );
        assertEquals( lookedUpContactView.getWorkEmail(), contactView.getWorkEmail() );
        assertEquals( lookedUpContactView.getOtherEmail(), contactView.getOtherEmail() );
        assertEquals( lookedUpContactView.getOptOut(), contactView.getOptOut() );
        assertEquals( lookedUpContactView.getBirthday(), contactView.getBirthday() );
        assertEquals( lookedUpContactView.getAnniversary(), contactView.getAnniversary() );
        assertEquals( lookedUpContactView.getAccountId(), contactView.getAccountId() );
        assertNotNull( lookedUpContactView.getAddressView() );
        assertTrue( lookedUpContactView.getAddressView()
                            .getAddress1()
                            .equals( contactView.getAddressView().getAddress1() ) );
    }

    @Test
    public void testInvalidNullUpdateContact() throws Exception {

        WorkflowErrors errors = new WorkflowErrors();
        ContactView contactView = new ContactView();
        contactWorkflow.updateContact( contactView, errors );
        List<String> messages = TestingUtils.getDisplayableErrors( errors, errorMessageService );
        assertTrue( messages.size() == 2 );
        int mayNotBeEmptyCount = 0;
        for ( String message : messages ) {
            if ( message.contains( "may not be empty" ) ) {
                mayNotBeEmptyCount++;
            }
        }
        assertTrue( mayNotBeEmptyCount == 2 );
    }

    @Test
    public void testBadDataUpdateContact() throws Exception {

        WorkflowErrors errors = new WorkflowErrors();
        ContactView contactView = contactWorkflow.findContact( TestConstants.TEST_CONTACT_ID_WITH_ACCOUNT, errors );
        //Change values
        contactView.setId( TestConstants.TEST_UNUSED_ID );
        contactView.setLastName( "Mills" );
        contactView.setHomeEmail( "test@test.com" );

        errors = new WorkflowErrors();
        contactWorkflow.updateContact( contactView, errors );

        List<String> messages = TestingUtils.getDisplayableErrors( errors, errorMessageService );
        assertTrue( messages.size() == 1 );
        int messageMatchCount = 0;
        for ( String message : messages ) {
            if ( message.contains( "The selected contact could not be found." ) ) {
                messageMatchCount++;
            }
        }
        assertTrue( messageMatchCount == 1 );
    }
}

