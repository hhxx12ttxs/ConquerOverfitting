int pos= 0;
int flag= 0;
if(isComplex)flag = flag | 0x80;
if(isBid) flag = flag | 0x40;
int dtx; 
if (!isComplex)
dtx= ((Integer)o[1]).intValue();// non-complex data is just an integer
 * First part of an OPT record is an array of FOPTEs (propertyId, fBid, fComplex, data) 
 * If fComplex is set, the actual data (Unicode strings, arrays, etc.) is stored AFTER the last FOPTE (sorted by property id???);
 * the length of the complex data is stored in the data field.
 * if fComplex is not set, the meaining of the data field is dependent upon the propertyId
 * if fBid is set and fComplex is not set, the data = a BLIP id (= an index into the BLIP store)
 * The number of FOPTES is the inst field read above
byte[] tmp= new byte[inst*6];// basic property table
byte[] complexData= new byte[0];// extra complex data, if any, after basic property table

