/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.evernote.edam.notestore;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.evernote.thrift.*;
import com.evernote.thrift.protocol.*;

/**
 *  This structure is returned from calls to the findNotesMetadata function to
 *  give the high-level metadata about a subset of Notes that are found to
 *  match a specified NoteFilter in a search.
 * 
 * <dl>
 *  <dt>startIndex</dt>
 *    <dd>
 *    The starting index within the overall set of notes.  This
 *    is also the number of notes that are "before" this list in the set.
 *    </dd>
 * 
 *  <dt>totalNotes</dt>
 *    <dd>
 *    The number of notes in the larger set.  This can be used
 *    to calculate how many notes are "after" this note in the set.
 *    (I.e.  remaining = totalNotes - (startIndex + notes.length)  )
 *    </dd>
 * 
 *  <dt>notes</dt>
 *    <dd>
 *    The list of metadata for Notes in this range.  The set of optional fields
 *    that are set in each metadata structure will depend on the
 *    NotesMetadataResultSpec provided by the caller when the search was
 *    performed.  Only the 'guid' field will be guaranteed to be set in each
 *    Note.
 *    </dd>
 * 
 *  <dt>stoppedWords</dt>
 *    <dd>
 *    If the NoteList was produced using a text based search
 *    query that included words that are not indexed or searched by the service,
 *    this will include a list of those ignored words.
 *    </dd>
 * 
 *  <dt>searchedWords</dt>
 *    <dd>
 *    If the NoteList was produced using a text based search
 *    query that included viable search words or quoted expressions, this will
 *    include a list of those words.  Any stopped words will not be included
 *    in this list.
 *    </dd>
 * 
 *  <dt>updateCount</dt>
 *    <dd>
 *    Indicates the total number of transactions that have
 *    been committed within the account.  This reflects (for example) the
 *    number of discrete additions or modifications that have been made to
 *    the data in this account (tags, notes, resources, etc.).
 *    This number is the "high water mark" for Update Sequence Numbers (USN)
 *    within the account.
 *    </dd>
 *  </dl>
 */
public class NotesMetadataList implements TBase<NotesMetadataList>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("NotesMetadataList");

  private static final TField START_INDEX_FIELD_DESC = new TField("startIndex", TType.I32, (short)1);
  private static final TField TOTAL_NOTES_FIELD_DESC = new TField("totalNotes", TType.I32, (short)2);
  private static final TField NOTES_FIELD_DESC = new TField("notes", TType.LIST, (short)3);
  private static final TField STOPPED_WORDS_FIELD_DESC = new TField("stoppedWords", TType.LIST, (short)4);
  private static final TField SEARCHED_WORDS_FIELD_DESC = new TField("searchedWords", TType.LIST, (short)5);
  private static final TField UPDATE_COUNT_FIELD_DESC = new TField("updateCount", TType.I32, (short)6);

  private int startIndex;
  private int totalNotes;
  private List<NoteMetadata> notes;
  private List<String> stoppedWords;
  private List<String> searchedWords;
  private int updateCount;


  // isset id assignments
  private static final int __STARTINDEX_ISSET_ID = 0;
  private static final int __TOTALNOTES_ISSET_ID = 1;
  private static final int __UPDATECOUNT_ISSET_ID = 2;
  private boolean[] __isset_vector = new boolean[3];

  public NotesMetadataList() {
  }

  public NotesMetadataList(
    int startIndex,
    int totalNotes,
    List<NoteMetadata> notes)
  {
    this();
    this.startIndex = startIndex;
    setStartIndexIsSet(true);
    this.totalNotes = totalNotes;
    setTotalNotesIsSet(true);
    this.notes = notes;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public NotesMetadataList(NotesMetadataList other) {
    System.arraycopy(other.__isset_vector, 0, __isset_vector, 0, other.__isset_vector.length);
    this.startIndex = other.startIndex;
    this.totalNotes = other.totalNotes;
    if (other.isSetNotes()) {
      List<NoteMetadata> __this__notes = new ArrayList<NoteMetadata>();
      for (NoteMetadata other_element : other.notes) {
        __this__notes.add(new NoteMetadata(other_element));
      }
      this.notes = __this__notes;
    }
    if (other.isSetStoppedWords()) {
      List<String> __this__stoppedWords = new ArrayList<String>();
      for (String other_element : other.stoppedWords) {
        __this__stoppedWords.add(other_element);
      }
      this.stoppedWords = __this__stoppedWords;
    }
    if (other.isSetSearchedWords()) {
      List<String> __this__searchedWords = new ArrayList<String>();
      for (String other_element : other.searchedWords) {
        __this__searchedWords.add(other_element);
      }
      this.searchedWords = __this__searchedWords;
    }
    this.updateCount = other.updateCount;
  }

  public NotesMetadataList deepCopy() {
    return new NotesMetadataList(this);
  }

  public void clear() {
    setStartIndexIsSet(false);
    this.startIndex = 0;
    setTotalNotesIsSet(false);
    this.totalNotes = 0;
    this.notes = null;
    this.stoppedWords = null;
    this.searchedWords = null;
    setUpdateCountIsSet(false);
    this.updateCount = 0;
  }

  public int getStartIndex() {
    return this.startIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
    setStartIndexIsSet(true);
  }

  public void unsetStartIndex() {
    __isset_vector[__STARTINDEX_ISSET_ID] = false;
  }

  /** Returns true if field startIndex is set (has been asigned a value) and false otherwise */
  public boolean isSetStartIndex() {
    return __isset_vector[__STARTINDEX_ISSET_ID];
  }

  public void setStartIndexIsSet(boolean value) {
    __isset_vector[__STARTINDEX_ISSET_ID] = value;
  }

  public int getTotalNotes() {
    return this.totalNotes;
  }

  public void setTotalNotes(int totalNotes) {
    this.totalNotes = totalNotes;
    setTotalNotesIsSet(true);
  }

  public void unsetTotalNotes() {
    __isset_vector[__TOTALNOTES_ISSET_ID] = false;
  }

  /** Returns true if field totalNotes is set (has been asigned a value) and false otherwise */
  public boolean isSetTotalNotes() {
    return __isset_vector[__TOTALNOTES_ISSET_ID];
  }

  public void setTotalNotesIsSet(boolean value) {
    __isset_vector[__TOTALNOTES_ISSET_ID] = value;
  }

  public int getNotesSize() {
    return (this.notes == null) ? 0 : this.notes.size();
  }

  public java.util.Iterator<NoteMetadata> getNotesIterator() {
    return (this.notes == null) ? null : this.notes.iterator();
  }

  public void addToNotes(NoteMetadata elem) {
    if (this.notes == null) {
      this.notes = new ArrayList<NoteMetadata>();
    }
    this.notes.add(elem);
  }

  public List<NoteMetadata> getNotes() {
    return this.notes;
  }

  public void setNotes(List<NoteMetadata> notes) {
    this.notes = notes;
  }

  public void unsetNotes() {
    this.notes = null;
  }

  /** Returns true if field notes is set (has been asigned a value) and false otherwise */
  public boolean isSetNotes() {
    return this.notes != null;
  }

  public void setNotesIsSet(boolean value) {
    if (!value) {
      this.notes = null;
    }
  }

  public int getStoppedWordsSize() {
    return (this.stoppedWords == null) ? 0 : this.stoppedWords.size();
  }

  public java.util.Iterator<String> getStoppedWordsIterator() {
    return (this.stoppedWords == null) ? null : this.stoppedWords.iterator();
  }

  public void addToStoppedWords(String elem) {
    if (this.stoppedWords == null) {
      this.stoppedWords = new ArrayList<String>();
    }
    this.stoppedWords.add(elem);
  }

  public List<String> getStoppedWords() {
    return this.stoppedWords;
  }

  public void setStoppedWords(List<String> stoppedWords) {
    this.stoppedWords = stoppedWords;
  }

  public void unsetStoppedWords() {
    this.stoppedWords = null;
  }

  /** Returns true if field stoppedWords is set (has been asigned a value) and false otherwise */
  public boolean isSetStoppedWords() {
    return this.stoppedWords != null;
  }

  public void setStoppedWordsIsSet(boolean value) {
    if (!value) {
      this.stoppedWords = null;
    }
  }

  public int getSearchedWordsSize() {
    return (this.searchedWords == null) ? 0 : this.searchedWords.size();
  }

  public java.util.Iterator<String> getSearchedWordsIterator() {
    return (this.searchedWords == null) ? null : this.searchedWords.iterator();
  }

  public void addToSearchedWords(String elem) {
    if (this.searchedWords == null) {
      this.searchedWords = new ArrayList<String>();
    }
    this.searchedWords.add(elem);
  }

  public List<String> getSearchedWords() {
    return this.searchedWords;
  }

  public void setSearchedWords(List<String> searchedWords) {
    this.searchedWords = searchedWords;
  }

  public void unsetSearchedWords() {
    this.searchedWords = null;
  }

  /** Returns true if field searchedWords is set (has been asigned a value) and false otherwise */
  public boolean isSetSearchedWords() {
    return this.searchedWords != null;
  }

  public void setSearchedWordsIsSet(boolean value) {
    if (!value) {
      this.searchedWords = null;
    }
  }

  public int getUpdateCount() {
    return this.updateCount;
  }

  public void setUpdateCount(int updateCount) {
    this.updateCount = updateCount;
    setUpdateCountIsSet(true);
  }

  public void unsetUpdateCount() {
    __isset_vector[__UPDATECOUNT_ISSET_ID] = false;
  }

  /** Returns true if field updateCount is set (has been asigned a value) and false otherwise */
  public boolean isSetUpdateCount() {
    return __isset_vector[__UPDATECOUNT_ISSET_ID];
  }

  public void setUpdateCountIsSet(boolean value) {
    __isset_vector[__UPDATECOUNT_ISSET_ID] = value;
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof NotesMetadataList)
      return this.equals((NotesMetadataList)that);
    return false;
  }

  public boolean equals(NotesMetadataList that) {
    if (that == null)
      return false;

    boolean this_present_startIndex = true;
    boolean that_present_startIndex = true;
    if (this_present_startIndex || that_present_startIndex) {
      if (!(this_present_startIndex && that_present_startIndex))
        return false;
      if (this.startIndex != that.startIndex)
        return false;
    }

    boolean this_present_totalNotes = true;
    boolean that_present_totalNotes = true;
    if (this_present_totalNotes || that_present_totalNotes) {
      if (!(this_present_totalNotes && that_present_totalNotes))
        return false;
      if (this.totalNotes != that.totalNotes)
        return false;
    }

    boolean this_present_notes = true && this.isSetNotes();
    boolean that_present_notes = true && that.isSetNotes();
    if (this_present_notes || that_present_notes) {
      if (!(this_present_notes && that_present_notes))
        return false;
      if (!this.notes.equals(that.notes))
        return false;
    }

    boolean this_present_stoppedWords = true && this.isSetStoppedWords();
    boolean that_present_stoppedWords = true && that.isSetStoppedWords();
    if (this_present_stoppedWords || that_present_stoppedWords) {
      if (!(this_present_stoppedWords && that_present_stoppedWords))
        return false;
      if (!this.stoppedWords.equals(that.stoppedWords))
        return false;
    }

    boolean this_present_searchedWords = true && this.isSetSearchedWords();
    boolean that_present_searchedWords = true && that.isSetSearchedWords();
    if (this_present_searchedWords || that_present_searchedWords) {
      if (!(this_present_searchedWords && that_present_searchedWords))
        return false;
      if (!this.searchedWords.equals(that.searchedWords))
        return false;
    }

    boolean this_present_updateCount = true && this.isSetUpdateCount();
    boolean that_present_updateCount = true && that.isSetUpdateCount();
    if (this_present_updateCount || that_present_updateCount) {
      if (!(this_present_updateCount && that_present_updateCount))
        return false;
      if (this.updateCount != that.updateCount)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(NotesMetadataList other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    NotesMetadataList typedOther = (NotesMetadataList)other;

    lastComparison = Boolean.valueOf(isSetStartIndex()).compareTo(typedOther.isSetStartIndex());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartIndex()) {      lastComparison = TBaseHelper.compareTo(this.startIndex, typedOther.startIndex);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTotalNotes()).compareTo(typedOther.isSetTotalNotes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTotalNotes()) {      lastComparison = TBaseHelper.compareTo(this.totalNotes, typedOther.totalNotes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNotes()).compareTo(typedOther.isSetNotes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNotes()) {      lastComparison = TBaseHelper.compareTo(this.notes, typedOther.notes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStoppedWords()).compareTo(typedOther.isSetStoppedWords());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStoppedWords()) {      lastComparison = TBaseHelper.compareTo(this.stoppedWords, typedOther.stoppedWords);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSearchedWords()).compareTo(typedOther.isSetSearchedWords());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSearchedWords()) {      lastComparison = TBaseHelper.compareTo(this.searchedWords, typedOther.searchedWords);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUpdateCount()).compareTo(typedOther.isSetUpdateCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUpdateCount()) {      lastComparison = TBaseHelper.compareTo(this.updateCount, typedOther.updateCount);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // START_INDEX
          if (field.type == TType.I32) {
            this.startIndex = iprot.readI32();
            setStartIndexIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // TOTAL_NOTES
          if (field.type == TType.I32) {
            this.totalNotes = iprot.readI32();
            setTotalNotesIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // NOTES
          if (field.type == TType.LIST) {
            {
              TList _list64 = iprot.readListBegin();
              this.notes = new ArrayList<NoteMetadata>(_list64.size);
              for (int _i65 = 0; _i65 < _list64.size; ++_i65)
              {
                NoteMetadata _elem66;
                _elem66 = new NoteMetadata();
                _elem66.read(iprot);
                this.notes.add(_elem66);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: // STOPPED_WORDS
          if (field.type == TType.LIST) {
            {
              TList _list67 = iprot.readListBegin();
              this.stoppedWords = new ArrayList<String>(_list67.size);
              for (int _i68 = 0; _i68 < _list67.size; ++_i68)
              {
                String _elem69;
                _elem69 = iprot.readString();
                this.stoppedWords.add(_elem69);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 5: // SEARCHED_WORDS
          if (field.type == TType.LIST) {
            {
              TList _list70 = iprot.readListBegin();
              this.searchedWords = new ArrayList<String>(_list70.size);
              for (int _i71 = 0; _i71 < _list70.size; ++_i71)
              {
                String _elem72;
                _elem72 = iprot.readString();
                this.searchedWords.add(_elem72);
              }
              iprot.readListEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 6: // UPDATE_COUNT
          if (field.type == TType.I32) {
            this.updateCount = iprot.readI32();
            setUpdateCountIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    oprot.writeFieldBegin(START_INDEX_FIELD_DESC);
    oprot.writeI32(this.startIndex);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(TOTAL_NOTES_FIELD_DESC);
    oprot.writeI32(this.totalNotes);
    oprot.writeFieldEnd();
    if (this.notes != null) {
      oprot.writeFieldBegin(NOTES_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.notes.size()));
        for (NoteMetadata _iter73 : this.notes)
        {
          _iter73.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.stoppedWords != null) {
      if (isSetStoppedWords()) {
        oprot.writeFieldBegin(STOPPED_WORDS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.stoppedWords.size()));
          for (String _iter74 : this.stoppedWords)
          {
            oprot.writeString(_iter74);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (this.searchedWords != null) {
      if (isSetSearchedWords()) {
        oprot.writeFieldBegin(SEARCHED_WORDS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.searchedWords.size()));
          for (String _iter75 : this.searchedWords)
          {
            oprot.writeString(_iter75);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
    }
    if (isSetUpdateCount()) {
      oprot.writeFieldBegin(UPDATE_COUNT_FIELD_DESC);
      oprot.writeI32(this.updateCount);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("NotesMetadataList(");
    boolean first = true;

    sb.append("startIndex:");
    sb.append(this.startIndex);
    first = false;
    if (!first) sb.append(", ");
    sb.append("totalNotes:");
    sb.append(this.totalNotes);
    first = false;
    if (!first) sb.append(", ");
    sb.append("notes:");
    if (this.notes == null) {
      sb.append("null");
    } else {
      sb.append(this.notes);
    }
    first = false;
    if (isSetStoppedWords()) {
      if (!first) sb.append(", ");
      sb.append("stoppedWords:");
      if (this.stoppedWords == null) {
        sb.append("null");
      } else {
        sb.append(this.stoppedWords);
      }
      first = false;
    }
    if (isSetSearchedWords()) {
      if (!first) sb.append(", ");
      sb.append("searchedWords:");
      if (this.searchedWords == null) {
        sb.append("null");
      } else {
        sb.append(this.searchedWords);
      }
      first = false;
    }
    if (isSetUpdateCount()) {
      if (!first) sb.append(", ");
      sb.append("updateCount:");
      sb.append(this.updateCount);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
    if (!isSetStartIndex()) {
      throw new TProtocolException("Required field 'startIndex' is unset! Struct:" + toString());
    }

    if (!isSetTotalNotes()) {
      throw new TProtocolException("Required field 'totalNotes' is unset! Struct:" + toString());
    }

    if (!isSetNotes()) {
      throw new TProtocolException("Required field 'notes' is unset! Struct:" + toString());
    }

  }

}


