package efi.unleashed.view.web;

import efi.platform.view.web.EntityView;
import efi.unleashed.domain.Contact;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;

public class ContactView extends EntityView {

    @Length(max = 32)
    private String firstName;

    @NotEmpty
    @Length(max = 32)
    private String lastName;

    @Length(max = 32)
    private String homePhone;

    @Length(max = 32)
    private String mobilePhone;

    @Length(max = 32)
    private String workPhone;

    @Length(max = 32)
    private String fax;

    @NotEmpty
    @Length(max = 128)
    private String homeEmail;

    @Length(max = 128)
    private String workEmail;

    @Length(max = 128)
    private String otherEmail;

    private Boolean optOut;

    private LocalDate birthday;

    private LocalDate anniversary;

    private Long accountId;

    private AddressView addressView;

    public ContactView() {

    }

    public ContactView( Contact contact ) {

        this.setId( contact.getId() );
        this.firstName = contact.getFirstName();
        this.lastName = contact.getLastName();
        this.homePhone = contact.getHomePhone();
        this.mobilePhone = contact.getMobilePhone();
        this.workPhone = contact.getWorkPhone();
        this.fax = contact.getFax();
        this.homeEmail = contact.getHomeEmail();
        this.workEmail = contact.getWorkEmail();
        this.otherEmail = contact.getOtherEmail();
        this.optOut = contact.getOptOut();
        this.birthday = contact.getBirthday();
        this.anniversary = contact.getAnniversary();
        if ( contact.getAccount() != null ) {
            this.accountId = contact.getAccount().getId();
        }

        if ( contact.getAddress() != null ) {
            AddressView addressView = new AddressView( contact.getAddress() );
            this.addressView = addressView;
        }
    }

    public String getFirstName() {

        return firstName;
    }

    public void setFirstName( String firstName ) {

        this.firstName = firstName;
    }

    public String getLastName() {

        return lastName;
    }

    public void setLastName( String lastName ) {

        this.lastName = lastName;
    }

    public String getHomePhone() {

        return homePhone;
    }

    public void setHomePhone( String homePhone ) {

        this.homePhone = homePhone;
    }

    public String getMobilePhone() {

        return mobilePhone;
    }

    public void setMobilePhone( String mobilePhone ) {

        this.mobilePhone = mobilePhone;
    }

    public String getWorkPhone() {

        return workPhone;
    }

    public void setWorkPhone( String workPhone ) {

        this.workPhone = workPhone;
    }

    public String getFax() {

        return fax;
    }

    public void setFax( String fax ) {

        this.fax = fax;
    }

    public String getHomeEmail() {

        return homeEmail;
    }

    public void setHomeEmail( String homeEmail ) {

        this.homeEmail = homeEmail;
    }

    public String getWorkEmail() {

        return workEmail;
    }

    public void setWorkEmail( String workEmail ) {

        this.workEmail = workEmail;
    }

    public String getOtherEmail() {

        return otherEmail;
    }

    public void setOtherEmail( String otherEmail ) {

        this.otherEmail = otherEmail;
    }

    public Boolean getOptOut() {

        return optOut;
    }

    public void setOptOut( Boolean optOut ) {

        this.optOut = optOut;
    }

    public LocalDate getBirthday() {

        return birthday;
    }

    public void setBirthday( LocalDate birthday ) {

        this.birthday = birthday;
    }

    public LocalDate getAnniversary() {

        return anniversary;
    }

    public void setAnniversary( LocalDate anniversary ) {

        this.anniversary = anniversary;
    }

    public Long getAccountId() {

        return accountId;
    }

    public void setAccountId( Long accountId ) {

        this.accountId = accountId;
    }

    public AddressView getAddressView() {

        return addressView;
    }

    public void setAddressView( AddressView addressView ) {

        this.addressView = addressView;
    }

    @Override public String toString() {

        return "ContactView {" +

               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", homePhone='" + homePhone + '\'' +
               ", mobilePhone='" + mobilePhone + '\'' +
               ", workPhone='" + workPhone + '\'' +
               ", fax='" + fax + '\'' +
               ", email='" + homeEmail + '\'' +
               ", birthday=" + birthday +
               ", anniversary=" + anniversary +
               ", accountId=" + accountId +
               "} " + super.toString();
    }
}

