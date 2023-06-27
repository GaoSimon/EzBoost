package SHE;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class SHE implements Serializable {
   public int k0;
   public int k1;
   public int k2;
   private BigInteger p;
   private BigInteger q;
   private BigInteger l;
   public BigInteger N;

   public void SHEInitialization(int k0, int k1, int k2) {
      Random rnd = new Random();
      this.p = BigInteger.probablePrime(k0, rnd);
      this.q = BigInteger.probablePrime(k0, rnd);
      this.l = new BigInteger(k2, rnd);
      this.N = this.p.multiply(this.q);
      this.k0 = k0;
      this.k1 = k1;
      this.k2 = k2;
   }

   public BigInteger SHEEncryption(BigInteger plaintext) {
      Random rnd = new Random();
      BigInteger r0 = new BigInteger(this.k2, rnd);
      BigInteger r1 = new BigInteger(this.k0, rnd);
      BigInteger tmp1 = r0.multiply(this.l).mod(this.N).add(plaintext).mod(this.N);
      BigInteger tmp2 = r1.multiply(this.p).mod(this.N).add(BigInteger.ONE).mod(this.N);
      return tmp1.multiply(tmp2).mod(this.N);
   }

   public BigInteger SHEDecryption(BigInteger ciphertext) {
      BigInteger plaintext = ciphertext.mod(this.p).mod(this.l);
      if (plaintext.compareTo(this.l.divide(new BigInteger("2"))) >= 0) {
         plaintext = plaintext.subtract(this.l);
      }

      return plaintext;
   }

   public static BigInteger SHEAdd(BigInteger ctxt1, BigInteger ctxt2) {
      return ctxt1.add(ctxt2);
   }

   public static BigInteger SHEMul(BigInteger ctxt1, BigInteger ctxt2) {
      return ctxt1.multiply(ctxt2);
   }

   public static void main(String[] args) {
   }
}
