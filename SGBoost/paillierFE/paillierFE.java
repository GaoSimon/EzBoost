package paillierFE;

import java.math.BigInteger;
import java.util.Random;

public class paillierFE {
   public int k0;
   public int k1;
   public BigInteger g;
   public BigInteger N;
   public BigInteger p;
   public BigInteger q;
   public BigInteger N2N;
   public BigInteger[] s;
   public BigInteger[] h;

   public void FEInitialization(int k0, int k1) {
      Random rnd = new Random();
      Random rnd1 = new Random();
      this.p = BigInteger.probablePrime(k0 + 1, rnd);
      this.q = BigInteger.probablePrime(k0 + 1, rnd1);
      this.N = this.p.multiply(this.q);
      this.N2N = this.N.multiply(this.N);
      this.g = this.N.add(BigInteger.ONE);
      this.s = new BigInteger[k1];
      this.h = new BigInteger[k1];

      int i;
      for(i = 0; i < k1; ++i) {
         this.s[i] = this.GenerateBigInteger(k0);
      }

      for(i = 0; i < k1; ++i) {
         this.h[i] = this.g.modPow(this.s[i], this.N2N);
      }

      this.k0 = k0;
      this.k1 = k1;
   }

   public BigInteger GenerateBigInteger(int lenth) {
      Random rnd = new Random();
      BigInteger a = new BigInteger(lenth, rnd);
      int b = Math.abs(a.intValue());
      BigInteger c = new BigInteger(String.valueOf(b));
      return c;
   }

   public BigInteger InnerProduct(BigInteger[] y) {
      BigInteger sky = new BigInteger("0");

      for(int i = 0; i < this.k1; ++i) {
         sky = sky.add(y[i].multiply(this.s[i]));
      }

      return sky;
   }

   public BigInteger[] FEEncryption(BigInteger[] plaintext) {
      BigInteger r = this.GenerateBigInteger(this.k0);
      BigInteger[] C = new BigInteger[this.k1 + 1];
      C[0] = this.g.modPow(r, this.N2N);

      for(int i = 1; i < this.k1 + 1; ++i) {
         C[i] = BigInteger.ONE.add(plaintext[i - 1].multiply(this.N)).multiply(this.h[i - 1].modPow(r, this.N2N)).mod(this.N2N);
      }

      return C;
   }

   public BigInteger FEDecryption(BigInteger[] ciphertext, BigInteger[] y, BigInteger sky) {
      BigInteger c0Inverse = ciphertext[0].modPow(this.N2N.subtract(sky), this.N2N);
      new CRTSet();
      new CRTSet();
      BigInteger plaintext = c0Inverse;

      for(int i = 1; i < this.k1 + 1; ++i) {
         plaintext = plaintext.multiply(ciphertext[i].modPow(y[i - 1], this.N2N)).mod(this.N2N);
      }

      CRTSet xa = new CRTSet();
      CRTSet ya = new CRTSet();
      inverse(this.N, this.N2N, xa, ya);
      plaintext = plaintext.subtract(BigInteger.ONE).mod(this.N2N).divide(this.N);
      return plaintext;
   }

   public static BigInteger inverse(BigInteger a, BigInteger b, CRTSet x, CRTSet y) {
      if (b.equals(new BigInteger("0"))) {
         x.v = new BigInteger("1");
         y.v = new BigInteger("0");
         return a;
      } else {
         inverse(b, a.mod(b), x, y);
         BigInteger c = y.v;
         y.v = x.v.subtract(a.divide(b).multiply(y.v));
         return x.v = c;
      }
   }

   public static void main(String[] args) {
      paillierFE FE = new paillierFE();
      FE.FEInitialization(1024, 2);
      BigInteger[] plaintext = new BigInteger[]{FE.GenerateBigInteger(1000), FE.GenerateBigInteger(1000)};
      BigInteger[] y = new BigInteger[]{FE.GenerateBigInteger(1000), FE.GenerateBigInteger(1000)};
      BigInteger sky = FE.InnerProduct(y);
      BigInteger[] ciphertext = FE.FEEncryption(plaintext);
      BigInteger sp = FE.FEDecryption(ciphertext, y, sky);
      System.out.println(sp);
      System.out.println(plaintext[0].multiply(y[0]).add(plaintext[1].multiply(y[1])));
   }
}
