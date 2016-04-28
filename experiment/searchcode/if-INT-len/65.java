/*
 * Gap Data
 * Copyright (C) 2009 John Pritchard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package gap;

import com.google.appengine.api.datastore.Blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Object conversions for field types.
 * 
 * @author jdp
 */
public abstract class Objects {

    public final static java.lang.Object From(Primitive type, java.lang.Object object){
        if (null == object)
            return null;
        else {
            switch(type){
            case String:
                return StringFromObject(object);
            case Boolean:
                return BooleanFromObject(object);
            case Byte:
                return ByteFromObject(object);
            case Character:
                return CharacterFromObject(object);
            case Short:
                return ShortFromObject(object);
            case Integer:
                return IntegerFromObject(object);
            case Long:
                return LongFromObject(object);
            case Float:
                return FloatFromObject(object);
            case Double:
                return DoubleFromObject(object);
            case Date:
                return DateFromObject(object);
            case BigInteger:
                return BigIntegerFromObject(object);
            case BigDecimal:
                return BigDecimalFromObject(object);
            case Category:
                return CategoryFromObject(object);
            case Email:
                return EmailFromObject(object);
            case GeoPt:
                return GeoPtFromObject(object);
            case Key:
                return KeyFromObject(object);
            case Link:
                return LinkFromObject(object);
            case PhoneNumber:
                return PhoneNumberFromObject(object);
            case PostalAddress:
                return PostalAddressFromObject(object);
            case Rating:
                return RatingFromObject(object);
            case Text:
                return TextFromObject(object);
            case Serializable:
                return SerializableFromObject(object);
            default:
                throw new java.lang.IllegalStateException("Unrecognized type "+type.name());
            }
        }
    }
    public final static java.lang.Object ObjectFromObject(java.lang.Object object){
        if (object instanceof java.lang.String){
            String string = (String)object;
            try {
                com.google.appengine.api.datastore.Key key = Strings.KeyFromString(string);
                gap.data.Kind kind = gap.data.Kind.For(key.getKind());
                return kind.get(key);
            }
            catch (Exception any){
            }
        }
        return object;
    }
    public final static java.lang.String StringFromObject(java.lang.Object object){
        if (object instanceof java.lang.String)
            return (java.lang.String)object;
        else if (object instanceof gap.data.HasName)
            return ((gap.data.HasName)object).getName();
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Boolean BooleanFromObject(java.lang.Object object){
        if (object instanceof java.lang.Boolean)
            return (java.lang.Boolean)object;
        else if (object instanceof java.lang.String)
            return Strings.BooleanFromString( (String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Byte ByteFromObject(java.lang.Object object){
        if (object instanceof java.lang.Byte)
            return (java.lang.Byte)object;
        else if (object instanceof java.lang.String)
            return Strings.ByteFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.lang.Byte( ((java.lang.Number)object).byteValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Character CharacterFromObject(java.lang.Object object){
        if (object instanceof java.lang.Character)
            return (java.lang.Character)object;
        /*
         * Special storage type is Integer
         */
        else if (object instanceof java.lang.Number)
            return new java.lang.Character( (char)((java.lang.Number)object).intValue());
        else if (object instanceof java.lang.String)
            return Strings.CharacterFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Short ShortFromObject(java.lang.Object object){
        if (object instanceof java.lang.Short)
            return (java.lang.Short)object;
        else if (object instanceof java.lang.String)
            return Strings.ShortFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.lang.Short( ((java.lang.Number)object).shortValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Integer IntegerFromObject(java.lang.Object object){
        if (object instanceof java.lang.Integer)
            return (java.lang.Integer)object;
        else if (object instanceof java.lang.String)
            return Strings.IntegerFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.lang.Integer( ((java.lang.Number)object).intValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Long LongFromObject(java.lang.Object object){
        if (object instanceof java.lang.Long)
            return (java.lang.Long)object;
        else if (object instanceof java.lang.String)
            return Strings.LongFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.lang.Long( ((java.lang.Number)object).longValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Float FloatFromObject(java.lang.Object object){
        if (object instanceof java.lang.Float)
            return (java.lang.Float)object;
        else if (object instanceof java.lang.String)
            return Strings.FloatFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.lang.Float( ((java.lang.Number)object).floatValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Double DoubleFromObject(java.lang.Object object){
        if (object instanceof java.lang.Double)
            return (java.lang.Double)object;
        else if (object instanceof java.lang.String)
            return Strings.DoubleFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.lang.Double( ((java.lang.Number)object).doubleValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.util.Date DateFromObject(java.lang.Object object){
        if (object instanceof java.util.Date)
            return (java.util.Date)object;
        else if (object instanceof java.lang.String)
            return Strings.DateFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.util.Date( ((java.lang.Number)object).longValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.lang.Enum EnumFromObject(java.lang.Object object){
        if (object instanceof java.lang.Enum)
            return (java.lang.Enum)object;
        else if (object instanceof java.lang.String)
            return Strings.EnumFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.math.BigInteger BigIntegerFromObject(java.lang.Object object){
        if (object instanceof java.math.BigInteger)
            return (java.math.BigInteger)object;
        /*
         * Special storage type is ShortBlob
         */
        else if (object instanceof com.google.appengine.api.datastore.ShortBlob){
            com.google.appengine.api.datastore.ShortBlob blob = (com.google.appengine.api.datastore.ShortBlob)object;
            return new java.math.BigInteger(1,blob.getBytes());
        }
        else if (object instanceof java.lang.String)
            return Strings.BigIntegerFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.math.BigInteger( java.lang.Long.toString(((java.lang.Number)object).longValue(),16),16);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.math.BigDecimal BigDecimalFromObject(java.lang.Object object){
        if (object instanceof java.math.BigDecimal)
            return (java.math.BigDecimal)object;
        /*
         * Special storage type is String
         */
        else if (object instanceof java.lang.String)
            return Strings.BigDecimalFromString( (java.lang.String)object);
        else if (object instanceof java.lang.Number)
            return new java.math.BigDecimal( ((java.lang.Number)object).doubleValue());
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Blob BlobFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Blob)
            return (com.google.appengine.api.datastore.Blob)object;
        else if (object instanceof java.lang.String)
            return Strings.BlobFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Category CategoryFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Category)
            return (com.google.appengine.api.datastore.Category)object;
        else if (object instanceof java.lang.String)
            return Strings.CategoryFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Email EmailFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Email)
            return (com.google.appengine.api.datastore.Email)object;
        else if (object instanceof java.lang.String)
            return Strings.EmailFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.GeoPt GeoPtFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.GeoPt)
            return (com.google.appengine.api.datastore.GeoPt)object;
        else if (object instanceof java.lang.String)
            return Strings.GeoPtFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Key KeyFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Key)
            return (com.google.appengine.api.datastore.Key)object;
        else if (object instanceof java.lang.String)
            return Strings.KeyFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Link LinkFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Link)
            return (com.google.appengine.api.datastore.Link)object;
        else if (object instanceof java.lang.String)
            return Strings.LinkFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.PhoneNumber PhoneNumberFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.PhoneNumber)
            return (com.google.appengine.api.datastore.PhoneNumber)object;
        else if (object instanceof java.lang.String)
            return Strings.PhoneNumberFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.PostalAddress PostalAddressFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.PostalAddress)
            return (com.google.appengine.api.datastore.PostalAddress)object;
        else if (object instanceof java.lang.String)
            return Strings.PostalAddressFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Rating RatingFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Rating)
            return (com.google.appengine.api.datastore.Rating)object;
        else if (object instanceof java.lang.String)
            return Strings.RatingFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static com.google.appengine.api.datastore.Text TextFromObject(java.lang.Object object){
        if (object instanceof com.google.appengine.api.datastore.Text)
            return (com.google.appengine.api.datastore.Text)object;
        else if (object instanceof java.lang.String)
            return Strings.TextFromString( (java.lang.String)object);
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }
    public final static java.io.Serializable SerializableFromObject(java.lang.Object object){
        if (object instanceof Blob){
            Blob blob = (Blob)object;
            try {
                ByteArrayInputStream buffer = new ByteArrayInputStream(blob.getBytes());
                ObjectInputStream in = new ObjectInputStream(buffer);
                return (java.io.Serializable)in.readObject();
            }
            catch (java.lang.ClassNotFoundException exc){
                throw new java.lang.RuntimeException(exc);
            }
            catch (java.io.IOException exc){
                throw new java.lang.RuntimeException(exc);
            }
        }
        else if (object instanceof java.io.Serializable)
            return (java.io.Serializable)object;
        else if (null == object)
            return null;
        else
            throw new ClassCastException(object.getClass().getName());
    }

    public final static java.lang.Object[] Add(java.lang.Object[] list, java.lang.Object item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Object[]{item};
        else {
            final int len = list.length;
            java.lang.Object[] copier = new java.lang.Object[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.String[] Add(java.lang.String[] list, java.lang.String item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.String[]{item};
        else {
            final int len = list.length;
            java.lang.String[] copier = new java.lang.String[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Boolean[] Add(java.lang.Boolean[] list, java.lang.Boolean item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Boolean[]{item};
        else {
            final int len = list.length;
            java.lang.Boolean[] copier = new java.lang.Boolean[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Byte[] Add(java.lang.Byte[] list, java.lang.Byte item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Byte[]{item};
        else {
            final int len = list.length;
            java.lang.Byte[] copier = new java.lang.Byte[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Character[] Add(java.lang.Character[] list, java.lang.Character item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Character[]{item};
        else {
            final int len = list.length;
            java.lang.Character[] copier = new java.lang.Character[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Short[] Add(java.lang.Short[] list, java.lang.Short item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Short[]{item};
        else {
            final int len = list.length;
            java.lang.Short[] copier = new java.lang.Short[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Integer[] Add(java.lang.Integer[] list, java.lang.Integer item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Integer[]{item};
        else {
            final int len = list.length;
            java.lang.Integer[] copier = new java.lang.Integer[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Long[] Add(java.lang.Long[] list, java.lang.Long item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Long[]{item};
        else {
            final int len = list.length;
            java.lang.Long[] copier = new java.lang.Long[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Float[] Add(java.lang.Float[] list, java.lang.Float item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Float[]{item};
        else {
            final int len = list.length;
            java.lang.Float[] copier = new java.lang.Float[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Double[] Add(java.lang.Double[] list, java.lang.Double item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Double[]{item};
        else {
            final int len = list.length;
            java.lang.Double[] copier = new java.lang.Double[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.util.Date[] Add(java.util.Date[] list, java.util.Date item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.util.Date[]{item};
        else {
            final int len = list.length;
            java.util.Date[] copier = new java.util.Date[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.lang.Enum[] Add(java.lang.Enum[] list, java.lang.Enum item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.lang.Enum[]{item};
        else {
            final int len = list.length;
            java.lang.Enum[] copier = new java.lang.Enum[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.math.BigInteger[] Add(java.math.BigInteger[] list, java.math.BigInteger item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.math.BigInteger[]{item};
        else {
            final int len = list.length;
            java.math.BigInteger[] copier = new java.math.BigInteger[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.math.BigDecimal[] Add(java.math.BigDecimal[] list, java.math.BigDecimal item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.math.BigDecimal[]{item};
        else {
            final int len = list.length;
            java.math.BigDecimal[] copier = new java.math.BigDecimal[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Blob[] Add(com.google.appengine.api.datastore.Blob[] list, com.google.appengine.api.datastore.Blob item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Blob[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Blob[] copier = new com.google.appengine.api.datastore.Blob[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Category[] Add(com.google.appengine.api.datastore.Category[] list, com.google.appengine.api.datastore.Category item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Category[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Category[] copier = new com.google.appengine.api.datastore.Category[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Email[] Add(com.google.appengine.api.datastore.Email[] list, com.google.appengine.api.datastore.Email item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Email[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Email[] copier = new com.google.appengine.api.datastore.Email[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.GeoPt[] Add(com.google.appengine.api.datastore.GeoPt[] list, com.google.appengine.api.datastore.GeoPt item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.GeoPt[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.GeoPt[] copier = new com.google.appengine.api.datastore.GeoPt[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Key[] Add(com.google.appengine.api.datastore.Key[] list, com.google.appengine.api.datastore.Key item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Key[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Key[] copier = new com.google.appengine.api.datastore.Key[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Link[] Add(com.google.appengine.api.datastore.Link[] list, com.google.appengine.api.datastore.Link item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Link[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Link[] copier = new com.google.appengine.api.datastore.Link[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.PhoneNumber[] Add(com.google.appengine.api.datastore.PhoneNumber[] list, com.google.appengine.api.datastore.PhoneNumber item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.PhoneNumber[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.PhoneNumber[] copier = new com.google.appengine.api.datastore.PhoneNumber[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.PostalAddress[] Add(com.google.appengine.api.datastore.PostalAddress[] list, com.google.appengine.api.datastore.PostalAddress item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.PostalAddress[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.PostalAddress[] copier = new com.google.appengine.api.datastore.PostalAddress[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Rating[] Add(com.google.appengine.api.datastore.Rating[] list, com.google.appengine.api.datastore.Rating item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Rating[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Rating[] copier = new com.google.appengine.api.datastore.Rating[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static com.google.appengine.api.datastore.Text[] Add(com.google.appengine.api.datastore.Text[] list, com.google.appengine.api.datastore.Text item){
        if (null == item)
            return list;
        else if (null == list)
            return new com.google.appengine.api.datastore.Text[]{item};
        else {
            final int len = list.length;
            com.google.appengine.api.datastore.Text[] copier = new com.google.appengine.api.datastore.Text[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }
    public final static java.io.Serializable[] Add(java.io.Serializable[] list, java.io.Serializable item){
        if (null == item)
            return list;
        else if (null == list)
            return new java.io.Serializable[]{item};
        else {
            final int len = list.length;
            java.io.Serializable[] copier = new java.io.Serializable[len+1];
            System.arraycopy(list,0,copier,0,len);
            copier[len] = item;
            return copier;
        }
    }

}

