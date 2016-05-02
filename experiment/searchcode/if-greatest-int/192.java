package com.github.capncanuck.smsstatistics.contacts;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.view.View.OnClickListener;

import com.github.capncanuck.smsstatistics.PhoneNumber;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * <h1>An SMS Contact.</h1>
 * 
 * Includes this name, the phone number, and the number of incoming and outgoing texts.<br>
 * 
 * <h1>TODO possible statistics:</h1>
 * <ul>
 *     <li>a list of incoming send dates</li>
 *     <li>a list of incoming receive dates</li>
 *     <li>a list of outgoing send dates</li>
 *     <li>a list of incoming lag times</li>
 *     <li>the average and sample standard deviation of incoming lag times</li>
 * </ul>
 * <h1>TODO Use the datetime stamp to implement the filter range feature</h1>
 */
public class Contact implements Comparable<Contact> {

    /**
     * For when the display_name is unknown
     */
    private static final String UNKNOWN = "Unknown";

    /**
     * Checks to see if a contact has the right number<br>
     * 
     * {@link PhoneNumber} -> {@link Contact} -> boolean
     * 
     * @param number the phone number to check
     * @return if the contact has the right number
     */
    public static Predicate<? super Contact> checkNumber(final PhoneNumber number) {
        return new Predicate<Contact>() {
            @Override
            public boolean apply(final Contact input) {
                return input.number.equals(number);
            }
        };
    }

    /**
     * The display_name.
     */
    private String display_name;

    /**
     * Uri to this contact's profile in the address book.
     */
    private Uri profileLink;

    /**
     * The phone number.
     */
    protected final PhoneNumber number;

    /**
     * The number of incoming messages.
     */
    private int incoming;

    /**
     * The number of outgoing messages.
     */
    private int outgoing;

    /**
     * The total number of incoming and outgoing messages.
     */
    private Integer total;

    /**
     * The uri to the contact's photo
     */
    private Uri photo_uri;

    /**
     * The percentage of messages that used this contact.
     */
    private double percentage;

    /**
     * The scale of the bar
     */
    private double barScale;

    /**
     * Instantiates a new contact.
     *
     * @param number the phone number
     * @param incoming the number of incoming messages
     * @param outgoing the number of outgoing messages
     */
    public Contact(final PhoneNumber number, final int incoming, final int outgoing) {
        this.number = number;
        this.incoming = incoming;
        this.outgoing = outgoing;
        this.total = this.incoming + this.outgoing;
    }

    /**
     * Set the display name of the contact.
     * 
     * @param display_name the display name
     */
    public void setName(final String display_name) {
        this.display_name = display_name;
    }

    /**
     * @param profileLink Uri to this contact's profile in the address book
     */
    public void setProfileLink(final Uri profileLink) {
        this.profileLink = profileLink;
    }

    /**
     * Increments the count of incomings by one.
     */
    public void incrIncomingCount() {
        this.incoming++;
        this.total++;
    }

    /**
     * Increments the count of outgoings by one.
     */
    public void incrOutgoingCount() {
        this.outgoing++;
        this.total++;
    }

    /**
     * @param photo the uri to the contact's photo
     */
    public void setPhoto(final Optional<String> photo) {
        if (photo.isPresent()) {
            this.photo_uri = Uri.parse(photo.get());
        }
    }

    /**
     * @param population the total number of messages
     */
    public void setPercentage(final int population) {
        this.percentage = 100.0 * this.total / population;
    }

    /**
     * Scale the bar length by the the greatest bar length
     * 
     * @param greatest the greatest bar length
     */
    public void setBarScale(final int greatest) {
        this.barScale = (double) this.total / greatest;
    }

    /**
     * @return this contact's display name
     */
    public CharSequence getDisplayName() {
        return this.display_name == null ? UNKNOWN : this.display_name;
    }

    /**
     * @param ctx where the click will happen
     * @return OnClickListener when clicked, the contact's profile opens
     */
    public OnClickListener getProfileLinkListener(final Context ctx) {
        return new ClickPhoto(ctx, new Intent(Intent.ACTION_VIEW).setDataAndType(this.profileLink, Contacts.CONTENT_ITEM_TYPE));
    }

    /**
     * @return the raw format of the phone number
     */
    public String getRawPhoneNumber() {
        return this.number.toRaw();
    }

    /**
     * @return the contact's phone number
     */
    public String getPhoneNumber() {
        return this.number.toString();
    }

    /**
     * @return the total number of incoming messages
     */
    public int getIncoming() {
        return this.incoming;
    }

    /**
     * @return the length of the incoming bar
     */
    public int getIncomingBar() {
        return (int) Math.round(100 * this.barScale / this.total * this.incoming);
    }

    /**
     * @return the total number of outgoing messages
     */
    public int getOutgoing() {
        return this.outgoing;
    }

    /**
     * @return the length of the outgoing bar
     */
    public int getOutgoingBar() {
        return (int) Math.round(100 * this.barScale / this.total * (this.incoming + this.outgoing));
    }

    /**
     * @return the total
     */
    public int getTotal() {
        return this.total;
    }

    /**
     * @return this contact's photo
     */
    public Uri getPhoto() {
        return this.photo_uri;
    }

    /**
     * @return how often this contact has been contacted
     */
    public CharSequence getPercentage() {
        return String.format(Locale.CANADA, "%2.1f%%", this.percentage);
    }

    /**
     * Sorting Contacts by:<br>
     * <ol>
     *     <li>total incoming and outgoing (greatest to least)</li>
     *     <li>lexical ordering of display name</li>
     *     <li>by phone number if display name is unknown</li>
     * </ol>
     *     
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final Contact other) {
        return ComparisonChain.start()
                .compare(other.total, this.total)
                .compare(this.display_name, other.display_name, Ordering.natural().nullsLast())
                .compare(this.number, other.number)
                .result();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Objects.toStringHelper("")
                .omitNullValues()
                .add("display_name", this.getDisplayName())
                .add("contact_uri", this.profileLink)
                .add("phone_number", this.number)
                .add("incoming", this.incoming)
                .add("outgoing", this.outgoing)
                .add("photo", this.photo_uri)
                .add("percentage", this.getPercentage()).toString();
    }
}
