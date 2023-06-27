package FE;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FE implements Serializable {
   public int k0;
   public int k1;
   public int thread_num = 64;
   public BigInteger p;
   public BigInteger g;
   public BigInteger[] s;
   public BigInteger[] mpk;
   public LinkedHashMap<BigInteger, BigInteger> Sites;

   public void FEInitialization(int k0, int k1) {
      Random rnd = new Random();
      this.p = BigInteger.probablePrime(k0, rnd);
      this.g = new BigInteger("2");
      this.s = new BigInteger[k1];
      this.mpk = new BigInteger[k1];

      int i;
      for(i = 0; i < k1; ++i) {
         this.s[i] = this.GenerateBigInteger(k0);
      }

      for(i = 0; i < k1; ++i) {
         this.mpk[i] = this.g.modPow(this.s[i], this.p);
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

   public BigInteger[] FEEncryption(final BigInteger[] plaintext) {
      final BigInteger r = this.GenerateBigInteger(this.k0);
      final BigInteger[] Ct = new BigInteger[this.k1 + 1];
      Ct[0] = this.g.modPow(r, this.p);
      final int block_size = this.k1 / (this.thread_num - 1);
      ExecutorService executorService = Executors.newFixedThreadPool(this.thread_num);
      final CountDownLatch latch = new CountDownLatch(this.thread_num);

      final int my_thread_num = this.thread_num;
         executorService.execute(new Runnable() {
            public void run() {
               for(int i = 0; i < my_thread_num; ++i) {
               int start_id = i * block_size + 1;
               int end_id = (i + 1) * block_size + 1;
               if (i == FE.this.thread_num - 1) {
                  end_id = FE.this.k1 + 1;
               }

               for(int j = start_id; j < end_id; ++j) {
                  Ct[j] = FE.this.mpk[j - 1].modPow(r, FE.this.p).multiply(FE.this.g.modPow(plaintext[j - 1], FE.this.p)).mod(FE.this.p);
               }

               latch.countDown();
            }
         }});


      try {
         latch.await();
      } catch (InterruptedException var9) {
         var9.printStackTrace();
      }

      executorService.shutdown();
      return Ct;
   }

   public BigInteger InnerProduct(BigInteger[] y) {
      BigInteger sky = new BigInteger("0");

      for(int i = 0; i < this.k1; ++i) {
         sky = sky.add(y[i].multiply(this.s[i]));
      }

      return sky;
   }

   public BigInteger FEDecryption(BigInteger[] ciphertext, BigInteger[] y, BigInteger sky) {
      BigInteger plaintext = new BigInteger("1");

      for(int i = 1; i < this.k1 + 1; ++i) {
         plaintext = plaintext.multiply(ciphertext[i].modPow(y[i - 1], this.p)).mod(this.p);
      }

      CRTSet x1 = new CRTSet();
      CRTSet y1 = new CRTSet();
      BigInteger inve = inverse(ciphertext[0].modPow(sky, this.p), this.p, x1, y1);
      plaintext = plaintext.multiply(inve).mod(this.p);
      return plaintext;
   }

   public void DiscreteTable(int maxGH, int lenth) {
      this.Sites = new LinkedHashMap();
      int maxlenth = maxGH * lenth / 2;
      BigInteger a = new BigInteger("1");
      this.Sites.put(a, new BigInteger("0"));

      int i;
      for(i = 1; i < maxlenth; ++i) {
         a = a.multiply(this.g).mod(this.p);
         this.Sites.put(a, BigInteger.valueOf((long)i));
      }

      a = this.g.modPow(this.p.subtract(BigInteger.valueOf((long)maxlenth)), this.p);
      this.Sites.put(a, BigInteger.ONE.subtract(BigInteger.valueOf((long)maxlenth)));

      for(i = 1; i < maxlenth; ++i) {
         a = a.multiply(this.g).mod(this.p);
         this.Sites.put(a, BigInteger.ONE.subtract(BigInteger.valueOf((long)maxlenth)).add(BigInteger.valueOf((long)i)));
      }

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
      FE she = new FE();
      she.FEInitialization(1024, 10000);
      BigInteger[] plaintext = new BigInteger[10000];

      for(int i = 0; i < 10000; ++i) {
         plaintext[i] = BigInteger.valueOf((long)(1000.0D * Math.random()));
      }

      long startTime = System.currentTimeMillis();
      she.FEEncryption(plaintext);
      long endTime = System.currentTimeMillis();
      System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
   }

   public int getK0() {
      return this.k0;
   }

   public void setK0(int k0) {
      this.k0 = k0;
   }

   public int getK1() {
      return this.k1;
   }

   public void setK1(int k1) {
      this.k1 = k1;
   }

   public BigInteger getP() {
      return this.p;
   }

   public void setP(BigInteger p) {
      this.p = p;
   }

   public BigInteger getG() {
      return this.g;
   }

   public void setG(BigInteger g) {
      this.g = g;
   }

   public BigInteger[] getS() {
      return this.s;
   }

   public void setS(BigInteger[] s) {
      this.s = s;
   }

   public BigInteger[] getMpk() {
      return this.mpk;
   }

   public void setMpk(BigInteger[] mpk) {
      this.mpk = mpk;
   }
}
