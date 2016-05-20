import java.math.*;
import java.security.*;
import java.util.*;
public class Ciphers {

   public static byte[] affineEncipher(byte[] msg,int blockSize,BigInteger a,BigInteger b) {
      if (blockSize<1||blockSize>255) throw new IllegalArgumentException("Block size must be between 1 and 255.");
      //Compute the modulus
      BigInteger modulus=BigInteger.valueOf(2).pow(8*blockSize);
      //Check the multiplier
      if (!(modulus.gcd(a).equals(BigIntegerMath.ONE))) throw new IllegalArgumentException("Enciphering key is not relatively prime to the modulus.");
      byte[][] ba=block(pad(msg,blockSize),blockSize);
      //Begin the enciphering
      for (int i=0;i<ba.length;i++) ba[i]=getBytes(a.multiply(new BigInteger(1,ba[i])).add(b).mod(modulus));
      return unBlock(ba,blockSize);
   }

   public static byte[] affineDecipher(byte[] msg,int blockSize,BigInteger a,BigInteger b) {
      if (blockSize<1||blockSize>255) throw new IllegalArgumentException("Block size must be between 1 and 255.");
      //Compute the modulus
      BigInteger modulus=BigInteger.valueOf(2).pow(8*blockSize);
      //Check the multiplier
      if (!(modulus.gcd(a).equals(BigIntegerMath.ONE))) throw new IllegalArgumentException("Enciphering key is not relatively prime to the modulus.");
      //Compute inverse of a
      BigInteger ainv=a.modInverse(modulus);
      byte[][] ba=block(msg,blockSize);
      //Begin the deciphering
      for (int i=0;i<ba.length;i++) ba[i]=getBytes(ainv.multiply(BigIntegerMath.lnr(new BigInteger(1,ba[i]).subtract(b),modulus)).mod(modulus));
      //Go from blocks to a 1D array, and remove padding; return this
      return unPad(unBlock(ba,blockSize),blockSize);
   }

   public static byte[] pohligHellmanEncipher(byte[] msg,BigInteger e,BigInteger p) {
      //Compute the plaintext block size
      int blockSize=(p.bitLength()-1)/8;
      //Check the enciphering exponent
      if (!(p.subtract(BigIntegerMath.ONE).gcd(e).equals(BigIntegerMath.ONE)))
         throw new IllegalArgumentException("Enciphering key is not relatively prime to (modulus minus one).");
      byte[][] ba=block(pad(msg,blockSize),blockSize);
      //Begin the enciphering
      for (int i=0;i<ba.length;i++) ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(e,p));
      //Return to a 1D array.  The ciphertext block size is one byte greater than plaintext block size.
      return unBlock(ba,blockSize+1);
   }

   public static byte[] pohligHellmanDecipher(byte[] msg,BigInteger d,BigInteger p) {
      //Compute the ciphertext block size
      int blockSize=(p.bitLength()-1)/8+1;
      //Check the deciphering exponent
      if (!(p.subtract(BigIntegerMath.ONE).gcd(d).equals(BigIntegerMath.ONE)))
         throw new IllegalArgumentException("Deciphering key is not relatively prime to (modulus minus one).");
      byte[][] ba=block(msg,blockSize);
      //Begin the deciphering
      for (int i=0;i<ba.length;i++) ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(d,p));
      //Go from blocks to a 1D array, and remove padding; return this
      return unPad(unBlock(ba,blockSize-1),blockSize-1);
   }

   public static byte[] pohligHellmanEncipherWSalt(byte[] msg,BigInteger e,BigInteger p,SecureRandom sr) {
      //Compute the plaintext block size
      int blockSize=(p.bitLength()-1)/8;
      if (blockSize<5) throw new IllegalArgumentException("Block size must be >= 5 bytes");
      //Check the enciphering exponent
      if (!(p.subtract(BigIntegerMath.ONE).gcd(e).equals(BigIntegerMath.ONE)))
         throw new IllegalArgumentException("Enciphering key is not relatively prime to (modulus minus one).");
      byte[][] ba=block(pad(msg,blockSize-4),blockSize-4);
      //Begin the enciphering
      for (int i=0;i<ba.length;i++) {
         ba[i]=addSalt(ba[i],sr);
         ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(e,p));
      }
      //Return to a 1D array.  The ciphertext block size is one byte greater than plaintext block size.
      return unBlock(ba,blockSize+1);
   }

   public static byte[] pohligHellmanDecipherWSalt(byte[] msg,BigInteger d,BigInteger p) {
      //Compute the ciphertext block size
      int blockSize=(p.bitLength()-1)/8+1;
      //Check the deciphering exponent
      if (!(p.subtract(BigIntegerMath.ONE).gcd(d).equals(BigIntegerMath.ONE)))
         throw new IllegalArgumentException("Deciphering key is not relatively prime to (modulus minus one).");
      byte[][] ba=block(msg,blockSize);
      //Begin the deciphering
      for (int i=0;i<ba.length;i++) {
         ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(d,p));
         ba[i]=removeSalt(ba[i]);
      }
      //Go from blocks to a 1D array, and remove padding; return this
      return unPad(unBlock(ba,blockSize-5),blockSize-5);
   }

   public static byte[] RSAEncipher(byte[] msg,BigInteger e,BigInteger n) {
      //Compute the plaintext block size
      int blockSize=(n.bitLength()-1)/8;
      byte[][] ba=block(pad(msg,blockSize),blockSize);
      //Begin the enciphering
      for (int i=0;i<ba.length;i++) ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(e,n));
      //Return to a 1D array.  The ciphertext block size is one byte greater than plaintext block size.
      return unBlock(ba,blockSize+1);
   }

   public static byte[] RSADecipher(byte[] msg,BigInteger d,BigInteger n) {
      //Compute the ciphertext block size
      int blockSize=(n.bitLength()-1)/8+1;
      byte[][] ba=block(msg,blockSize);
      //Begin the deciphering
      for (int i=0;i<ba.length;i++) ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(d,n));
      //Go from blocks to a 1D array, and remove padding; return this
      return unPad(unBlock(ba,blockSize-1),blockSize-1);
   }

   public static byte[] RSAEncipherWSalt(byte[] msg,BigInteger e,BigInteger n,SecureRandom sr) {
      //Compute the plaintext block size
      int blockSize=(n.bitLength()-1)/8;
      if (blockSize<5) throw new IllegalArgumentException("Block size must be >= 5 bytes");
      byte[][] ba=block(pad(msg,blockSize-4),blockSize-4);
      //Begin the enciphering
      for (int i=0;i<ba.length;i++) {
         ba[i]=addSalt(ba[i],sr);
         ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(e,n));
      }
      //Return to a 1D array.  The ciphertext block size is one byte greater than plaintext block size.
      return unBlock(ba,blockSize+1);
   }

   public static byte[] RSADecipherWSalt(byte[] msg,BigInteger d,BigInteger n) {
      //Compute the ciphertext block size
      int blockSize=(n.bitLength()-1)/8+1;
      byte[][] ba=block(msg,blockSize);
      //Begin the deciphering
      for (int i=0;i<ba.length;i++) {
         ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(d,n));
         ba[i]=removeSalt(ba[i]);
      }
      //Go from blocks to a 1D array, and remove padding; return this
      return unPad(unBlock(ba,blockSize-5),blockSize-5);
   }

   public static byte[] RSAEncipherSigned(byte[] msg,BigInteger dSender,BigInteger nSender,BigInteger eRecip,BigInteger nRecip,SecureRandom sr) {
      return RSAEncipherWSalt(RSAEncipherWSalt(msg,dSender,nSender,sr),eRecip,nRecip,sr);
   }

   public static byte[] RSADecipherSigned(byte[] msg,BigInteger dRecip,BigInteger nRecip,BigInteger eSender,BigInteger nSender) {
      return RSADecipherWSalt(RSADecipherWSalt(msg,dRecip,nRecip),eSender,nSender);
   }

   public static byte[] rabinEncipherWSalt(byte[] msg,BigInteger n,SecureRandom sr) {
      //Compute the plaintext block size-take 4 bytes salt and 4 bytes redundancy into account
      int blockSize=(n.bitLength()-1)/8;
      if (blockSize<12) throw new IllegalArgumentException("Block size must be >= 12 bytes");
      byte[][] ba=block(pad(msg,blockSize-8),blockSize-8);
      //Begin the enciphering
      for (int i=0;i<ba.length;i++) {
         ba[i]=addRedundancyAndSalt(ba[i],sr);
         ba[i]=getBytes(new BigInteger(1,ba[i]).modPow(BigIntegerMath.TWO,n));
      }
      //Return to a 1D array.  The ciphertext block size is one byte greater than plaintext block size.
      return unBlock(ba,blockSize+1);
   }

   public static byte[] rabinDecipherWSalt(byte[] msg,BigInteger p,BigInteger q) {
      //Compute inverse of p mod q, and of q mod p
      BigInteger n=p.multiply(q);
      BigInteger pinv=p.modInverse(q);
      BigInteger qinv=q.modInverse(p);
      BigInteger pexp=(p.add(BigIntegerMath.ONE)).divide(BigIntegerMath.FOUR);
      BigInteger qexp=(q.add(BigIntegerMath.ONE)).divide(BigIntegerMath.FOUR);
      //Compute the ciphertext block size
      int blockSize=(n.bitLength()-1)/8+1;
      byte[][] ba=block(msg,blockSize);
      //Begin the deciphering
      for (int i=0;i<ba.length;i++) {
         //Get the four roots
         BigInteger term1=new BigInteger(1,ba[i]).modPow(pexp,n).multiply(q).multiply(qinv);
         BigInteger term2=new BigInteger(1,ba[i]).modPow(qexp,n).multiply(p).multiply(pinv);
         byte[][] msgroot=new byte[4][0];
         BigInteger sum=term1.add(term2);
         BigInteger difference=term1.subtract(term2);
         msgroot[0]=getBytes(BigIntegerMath.lnr(sum,n));
         msgroot[1]=getBytes(BigIntegerMath.lnr(sum.negate(),n));
         msgroot[2]=getBytes(BigIntegerMath.lnr(difference,n));
         msgroot[3]=getBytes(BigIntegerMath.lnr(difference.negate(),n));
         boolean[] isCorrectRoot=new boolean[4];
         for (int k=0;k<4;k++) {
            //Assume this root is the correct one
            isCorrectRoot[k]=true;
            for (int j=4;j<8;j++) if (msgroot[k][j]!=msgroot[k][j+4]) {
               //Bytes don't match-not correct root
               isCorrectRoot[k]=false;
               break;
            }
         }
         //May be more than one root which satisfied redundancy requirement
         boolean correctFound=false;
         for (int k=0;k<4;k++) if (isCorrectRoot[k]) {
            if (!correctFound) {
               //A correct root exists
               correctFound=true;
               ba[i]=msgroot[k];
            } else {
               //A correct root was already found-this is a problem
               ba[i]=null;
               throw new IllegalArgumentException("Multiple messages satisfied redundancy requirement!");
            }
         }
         //If we get here, it means only one root satisfied redundancy requirement
         if (!correctFound) throw new NoSuchElementException("No message satisfied redundancy requirement!");
         ba[i]=removeRedundancyAndSalt(ba[i]);
      }
      //Go from blocks to a 1D array, and remove padding; return this
      return unPad(unBlock(ba,blockSize-9),blockSize-9);
   }

   //Method to add redundancy and salt to blocks using Rabin
   private static byte[] addRedundancyAndSalt(byte[] b,SecureRandom random) {
      byte[] answer=new byte[b.length+8];
      byte[] salt=new byte[4];
      random.nextBytes(salt);
      //Put salt in front
      System.arraycopy(salt,0,answer,0,4);
      //Follow with 1st 4 bytes of message-redundancy
      System.arraycopy(b,0,answer,4,4);
      //Copy the message over
      System.arraycopy(b,0,answer,8,b.length);
      return answer;
   }

   private static byte[] removeRedundancyAndSalt(byte[] b) {
      byte[] answer=new byte[b.length-8];
      //Copy the message over
      System.arraycopy(b,8,answer,0,answer.length);
      return answer;
   }

   //Method to add salt to blocks
   private static byte[] addSalt(byte[] b,SecureRandom random) {
      byte[] answer=new byte[b.length+4];
      byte[] salt=new byte[4];
      random.nextBytes(salt);
      //Put salt in front
      System.arraycopy(salt,0,answer,0,4);
      //Copy the message over
      System.arraycopy(b,0,answer,4,b.length);
      return answer;
   }

   private static byte[] removeSalt(byte[] b) {
      byte[] answer=new byte[b.length-4];
      //Copy the message over
      System.arraycopy(b,4,answer,0,answer.length);
      return answer;
   }

   //Method to rectify the extra bit problem of the toByteArray() method
   private static byte[] getBytes(BigInteger big) {
      byte[] bigBytes=big.toByteArray();
      if (big.bitLength()%8!=0) return bigBytes;
      else {
         byte[] smallerBytes=new byte[big.bitLength()/8];
         System.arraycopy(bigBytes,1,smallerBytes,0,smallerBytes.length);
         return smallerBytes;
      }
   }

   //Pads message using PKCS#5
   private static byte[] pad(byte[] msg,int blockSize) {
      //Check that block size is proper for PKCS#5 padding
      if (blockSize<1||blockSize>255) throw new IllegalArgumentException("Block size must be between 1 and 255.");
      //Pad the message
      int numberToPad=blockSize-msg.length%blockSize;
      byte[] paddedMsg=new byte[msg.length+numberToPad];
      System.arraycopy(msg,0,paddedMsg,0,msg.length);
      for (int i=msg.length;i<paddedMsg.length;i++) paddedMsg[i]=(byte)numberToPad;
      return paddedMsg;
   }

   //Turns the message into a 2D array of blocks
   private static byte[][] block(byte[] msg,int blockSize)  {
      //Create a 2D array of bytes corresponding to the message-all blocks should be full
      int numberOfBlocks=msg.length/blockSize;
      byte[][] ba=new byte[numberOfBlocks][blockSize];
      for (int i=0;i<numberOfBlocks;i++)
         for (int j=0;j<blockSize;j++)
            ba[i][j]=msg[i*blockSize+j];
      return ba;
   }

   //Takes the blocks and returns the message as a linear array
   private static byte[] unBlock(byte[][] ba,int blockSize) {
      //Create the 1D array in which to place the enciphered blocks
      byte[] m2=new byte[ba.length*blockSize];
      //Place the blocks in the 1D array
      for (int i=0;i<ba.length;i++)
         for (int j=blockSize-1,k=ba[i].length-1;k>=0;k--,j--) m2[i*blockSize+j]=ba[i][k];
      return m2;
   }

   private static byte[] unPad(byte[] msg,int blockSize) {
      //Determine the amount of padding-just look at last block
      int numberOfPads=(msg[msg.length-1]+256)%256;
      //Chop off the padding and return the array
      byte[] answer=new byte[msg.length-numberOfPads];
      System.arraycopy(msg,0,answer,0,answer.length);
      return answer;
   }

}

