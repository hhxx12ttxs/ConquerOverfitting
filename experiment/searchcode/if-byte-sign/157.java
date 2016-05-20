package com.selex.mikeysakke.crypto.eccsi;

import com.selex.mikeysakke.crypto.ECCSI;
import com.selex.mikeysakke.crypto.SigningParameterSet;
import com.selex.mikeysakke.crypto.CryptoUtils;

import com.selex.mikeysakke.user.KeyStorage;
import com.selex.util.OctetString;
import com.selex.mikeysakke.crypto.hash.SHA256Digest;
import com.selex.mikeysakke.crypto.ecc.PrimeCurveAffineFp;

import com.selex.math.ConstantBigInt;
import com.selex.math.ModIntValue;
import com.selex.math.InplaceModInt;
import com.selex.math.ScratchContext;
import com.selex.util.RandomGenerator;

import com.selex.log.DefaultLogger;


public class LocalECCSI implements ECCSI
{
   public boolean ValidateSigningKeysAndCacheHS(String identifier,
                                                String community,
                                                KeyStorage keys)
   {
      OctetString PVT = keys.GetPublicKey(identifier, "PVT");
      OctetString KPAK = keys.GetPublicKey(community, "KPAK");

      PrimeCurveAffineFp E = SigningParameterSet.ECCSI_6509_PARAM_SET.curve;

      ConstantBigInt p = E.field_order();
      ScratchContext P = ScratchContext.forField(p);

      InplaceModInt lx = P.getInField(), ly = P.getInField(),
                    rx = P.getInField(), ry = P.getInField();

      try { // use scratch and catch EC verification point failure

      // RFC6507 5.1.2
      //
      // 1) Validate that the PVT lies on the curve E
      //
      if (!E.decode_point(PVT, rx, ry))
         throw new RuntimeException("PVT does not lie on curve");

      // 2) Compute HS = hash( G || KPAK || ID || PVT )
      //
      InplaceModInt HS = P.getInField();
      {
         SHA256Digest d = new SHA256Digest();
         d.digest(E.base_point_octets());
         d.digest(KPAK);
         d.digest(identifier);
         d.digest(PVT);
         OctetString HS_str = new OctetString(32);
         d.sync(HS_str.raw());
         HS.assign(HS_str.raw());

         // [2.1] Cache HS for later use by Sign().
         keys.StorePublicKey(identifier, "HS", HS_str);
      }


      // 3) Validate that KPAK = [SSK]G - [HS]PVT
      //
      // [3.1] Specifically, to save inversion,
      //       validate that KPAK + [HS]PVT = [SSK]G
      //
      if (!E.decode_point(KPAK, lx, ly))
         throw new RuntimeException("KPAK does not lie on curve");
      
      E.mul_into_trusted(rx, ry, rx, ry, HS);
      E.add_point_trusted_distinct(lx, ly, rx, ry);

      ModIntValue SSK = P.getInField().assign(keys.GetPrivateKey(identifier, "SSK").clampedArray());

      E.mul_base_into(rx, ry, SSK);

      if (lx.equals(rx) && ly.equals(ry))
         return true;
      }
      catch (RuntimeException e) 
      {
         DefaultLogger.err.println("Exception verifying ECCSI signing keys: "+e);
      }
      finally { P.recycle(); }

      DefaultLogger.err.println("Failed to verify ECCSI signing keys.  Revoking keys for '" + identifier + "'");
      keys.RevokeKeys(identifier);
      return false;
   }

   public OctetString Sign(byte[] msg, int offset, int len,
                           String identifier,
                           String community,
                           RandomGenerator random,
                           KeyStorage keys)
   {
      // RFC6507 5.2.1
      //
      OctetString PVT = keys.GetPublicKey(identifier, "PVT");

      PrimeCurveAffineFp E = SigningParameterSet.ECCSI_6509_PARAM_SET.curve;

      ConstantBigInt p = E.field_order();
      ConstantBigInt q = E.point_order();

      ScratchContext P = ScratchContext.forField(p);
      ScratchContext Q = ScratchContext.forField(q);

      // XXX: Note that s below is used as scratch value for HE + r * SSK
      // XXX: within the [1-4] loop; it is updated to s' in [5] and the
      // XXX: true s in [6].

      InplaceModInt j = Q.getInField(),
                    r = P.getInField(),
                    s = Q.getInField();

      try { // use scratch

      // [1-4]: Loop until security criteria met:
      //
      for (;;)
      {
         // 1) Choose a random (ephemeral) non-zero value j in F_q
         //
         j.randomize(random);

         if (j.isZero())
            continue;
         
         // 2) Compute J = (Jx,Jy) = [j]G and assign Jx to r
         //
         // G is loaded into curve.
         //
         E.mul_base_into(r, E.ignore, j);

         // 3) Compute HE = hash( HS || r || M )
         //
         InplaceModInt HE = Q.getInField();
         {
            SHA256Digest d = new SHA256Digest();
            d.digest(keys.GetPublicKey(identifier, "HS"));
            d.digest(r.peekFieldByteRange());
            d.digest(msg, offset, len);
            byte[] bi = new byte[32];
            d.sync(bi);
            HE.assign(bi);
         }

         // 4) Verify that HE + r * SSK is non-zero (mod q)
         //
         s.assign(keys.GetPrivateKey(identifier, "SSK").clampedArray()).mul(r).add(HE);

         Q.recycle(HE);

         if (!s.isZero())
            break;
      }

      // 5) Compute s' = ( (( HE + r * SSK )^-1) * j ) (mod q)
      //    and erase ephemeral j
      //
      s.inv().mul(j);
      CryptoUtils.Erase(Q.takeOwnership(j).moveToByteArray());

      // 6) Set s = q - s' if octet_count(s) > N
      //
      if (s.bytes() > SigningParameterSet.ECCSI_6509_PARAM_SET.hash.octets)
         InplaceModInt.sub_into(s, q, s);

      // 7) Output the signature SIG = ( r || s || PVT )
      //
      return new OctetString()
         .reserve(p.bytesInField() + q.bytesInField() + 1 + 2 * p.bytesInField())
         .concat(r.peekFieldByteRange())
         .concat(s.peekFieldByteRange())
         .concat(PVT)
         ;

      } finally { Q.recycle(); P.recycle(); }
   }

   public boolean Verify(byte[] msg, int msgOffset, int msgLen,
                         byte[] sign, int signOffset, int signLen,
                         String identifier,
                         String community,
                         KeyStorage keys)
   {
      int hashLen = SigningParameterSet.ECCSI_6509_PARAM_SET.hash.octets;

      // No value in continuing if signature is not the correct size; two
      // N-octet integers r and s, plus an elliptical curve point PVT
      // over E expressed in uncompressed form with length 2N -- See
      // RFC6507 3.3)
      //
      if (signLen != hashLen * 4 + 1)
      {
         DefaultLogger.err.println("Unexpected ECCSI signature length.");
         return false;
      }

      // RFC6507 5.2.2
      //
      OctetString KPAK = keys.GetPublicKey(community, "KPAK");

      PrimeCurveAffineFp E = SigningParameterSet.ECCSI_6509_PARAM_SET.curve;

      ConstantBigInt p = E.field_order();
      ScratchContext P = ScratchContext.forField(p);

      try { // use scratch and catch verification errors

         int r_len = hashLen;
         int s_len = hashLen;
         int PVT_len = 2*hashLen + 1;

         int r_off = signOffset;
         int s_off = r_off + r_len;
         int PVT_off = s_off + s_len;

         // 1) Check that PVT lies on the elliptical curve E
         //
         InplaceModInt px = P.getInField(), py = P.getInField();
         if (!E.decode_point(sign, PVT_off, PVT_len, px, py))
            throw new RuntimeException("PVT does not lie on curve");

         // 2) Compute HS = hash( G || KPAK || ID || PVT )
         //
         InplaceModInt HS = P.getInField();
         OctetString HS_str = new OctetString(32);
         {
            SHA256Digest d = new SHA256Digest();
            d.digest(E.base_point_octets());
            d.digest(KPAK);
            d.digest(identifier);
            d.digest(sign, PVT_off, PVT_len);
            d.sync(HS_str.raw());
            HS.assign(HS_str.raw());
         }

         // 3) Compute HE = hash( HS || r || M )
         //
         InplaceModInt HE = P.getInField();
         {
            SHA256Digest d = new SHA256Digest();
            d.digest(HS_str);
            d.digest(sign, r_off, r_len);
            d.digest(msg, msgOffset, msgLen);
            byte[] bi = new byte[32]; // HE.peekFieldByteRange() ?
            d.sync(bi);
            HE.assign(bi); // HE.pokeFieldByteRange() ?
         }

         // TODO: Consider vector multiplication as per C++ impl.  For
         // TODO: now though, just do it the slow way.
         //
         // [4-5]: Use OpenSSL EC_POINTs_mul to combine steps [4] and
         //        [5] after pre-multiplication of scalars.
         //
         // 4) Y = [HS]PVT + KPAK
         // 
         // 5) Compute J = [s]( [HE]G + [r]Y )
         //
         // Expanded expression: (XXX: not done here yet)
         //
         // 5') Compute J = [s][HE]G + [s][r][HS]PVT + [s][r]KPAK
         //
         // Note: reusing PVT (px,py) above as basis for J to save
         // unnecessary allocation and point-on-curve check.  PVT is
         // not needed again.
         //
         InplaceModInt kx = P.getInField(), ky = P.getInField();

         if (!E.decode_point(KPAK, kx, ky)) // XXX: could cache this with community keys
            throw new RuntimeException("KPAK does not lie on curve");

         InplaceModInt r = P.getInField().assign(sign, r_off, r_len);
         InplaceModInt s = P.getInField().assign(sign, s_off, s_len);

         // 4) Y = [HS]PVT + KPAK
         // 
         E.mul_into_trusted(px, py, px, py, HS);
         E.add_point_trusted_distinct(px, py, kx, ky);

         // 5) Compute J = [s]( [HE]G + [r]Y )
         //
         //   (kx,ky) = [HE]G
         //   (px,py) = [r]Y
         //   (kx,ky) = [HE]G + [r]Y
         //   (px,py) = [s]( [HE]G + [r]Y )
         //
         E.mul_base_into(kx, ky, HE);
         E.mul_into_trusted(px, py, px, py, r);
         E.add_point_trusted_distinct(kx, ky, px, py);
         E.mul_into_trusted(px, py, kx, ky, s);

         // 6) Viewing J in affine coordinates (Jx,Jy), check that
         //    Jx = r mod p, and that Jx mod p != 0.
         //
         // Note: If Jx = r mod p and Jx != 0, then Jx mod p != 0.
         //
         if (r.mod(p).equals(px) && !px.isZero())
            return true;
      }
      catch (RuntimeException e)
      {
         DefaultLogger.err.println("Exception verifying ECCSI signature: " + e);
      }
      finally { P.recycle(); }
      DefaultLogger.err.println("Failed to verify ECCSI signature from '" + identifier + "'");
      return false;
   }
}


