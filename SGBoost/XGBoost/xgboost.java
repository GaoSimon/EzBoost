package XGBoost;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import utils.utils;

public class xgboost implements Serializable {
   public int trainDataNums;
   public int IterNums;
   public int MaxDeep;
   public int minBinNumber;
   public double LearningRate;
   public BigDecimal Lambda;
   public double[] TrueY;
   public double[] PredictY;
   public double[] weight;
   public BigDecimal[] gradient;
   public BigDecimal[] hessian;
   public BigInteger[] ELgradient;
   public BigInteger[] ELhessian;
   public double[][] trainData;
   public double[][] testData;
   public double[] testDataY;
   public double[] testDataPredictY;
   public int testDataNums;
   public int testDims;
   public double[] testWeight;
   public LinkedHashMap<String, Integer> testDataTags;
   int DataDims;

   public void modelInit(int dataNums, int DataDims, int IterNums, double LearningRate, BigDecimal lambda, int MaxDeep, int minBinNumber) {
      this.trainDataNums = dataNums;
      this.DataDims = DataDims;
      this.IterNums = IterNums;
      this.LearningRate = LearningRate;
      this.Lambda = lambda;
      this.MaxDeep = MaxDeep;
      this.minBinNumber = minBinNumber;
      this.PredictY = new double[this.trainDataNums];
      this.weight = new double[this.trainDataNums];
      this.gradient = new BigDecimal[this.trainDataNums];
      this.hessian = new BigDecimal[this.trainDataNums];
      this.ELgradient = new BigInteger[this.trainDataNums];
      this.ELhessian = new BigInteger[this.trainDataNums];

      for(int i = 0; i < this.trainDataNums; ++i) {
         this.PredictY[i] = 0.5D;
         this.weight[i] = 0.0D;
      }

   }

   public void readtestData(int nums, int dims, String pathName) throws IOException {
      this.testDataNums = nums;
      this.testDims = dims;
      this.testData = new double[this.testDataNums][this.testDims];
      this.testDataPredictY = new double[this.testDataNums];
      this.testDataY = new double[this.testDataNums];
      this.testWeight = new double[this.testDataNums];
      this.testDataTags = new LinkedHashMap();
      Map<String[], double[][]> map = utils.readDataFromFile(this.testDataNums, this.testDims, pathName);
      String[] localDataTags = (String[])map.keySet().iterator().next();

      for(Integer i = 0; i < localDataTags.length; i = i + 1) {
         this.testDataTags.put(localDataTags[i], i);
      }

      this.testData = (double[][])((Entry)map.entrySet().iterator().next()).getValue();

      for(int i = 0; i < this.testDataNums; ++i) {
         this.testDataY[i] = this.testData[i][this.testDims - 1];
      }

      System.out.println("数据预处理完成");
   }

   public double accuracyRate(double[] y, double[] PredictY) {
      double trueNumber = 0.0D;

      for(int i = 0; i < y.length; ++i) {
         if (y[i] == PredictY[i]) {
            ++trueNumber;
         }
      }

      double accuracy = trueNumber / (double)y.length;
      return accuracy;
   }

   public void sigmoid() {
      for(int i = 0; i < this.trainDataNums; ++i) {
         this.PredictY[i] = 1.0D / (1.0D + Math.exp(-this.weight[i]));
      }

   }

   public void sigmoidTestY() {
      for(int i = 0; i < this.testDataNums; ++i) {
         this.testDataPredictY[i] = (double)Math.round(1.0D / (1.0D + Math.exp(-this.testWeight[i])));
      }

   }

   public void caculategh() {
      for(int i = 0; i < this.trainDataNums; ++i) {
         this.gradient[i] = BigDecimal.valueOf(this.PredictY[i] - this.TrueY[i]);
         this.hessian[i] = BigDecimal.valueOf(this.PredictY[i] * (1.0D - this.PredictY[i]));
      }

   }

   public void EnLargegh() {
      for(int i = 0; i < this.trainDataNums; ++i) {
         this.ELgradient[i] = BigInteger.valueOf((long)(100.0D * (this.PredictY[i] - this.TrueY[i])));
         this.ELhessian[i] = BigInteger.valueOf((long)(100.0D * this.PredictY[i] * (1.0D - this.PredictY[i])));
      }

   }

   public BigDecimal[] caculateGH(int[] bit) {
      BigDecimal[] GH = new BigDecimal[2];
      BigDecimal G = new BigDecimal("0");
      BigDecimal H = new BigDecimal("0");

      for(int i = 0; i < this.trainDataNums; ++i) {
         if (bit[i] == 1) {
            G = G.add(this.gradient[i]);
            H = H.add(this.hessian[i]);
         }
      }

      GH[0] = G;
      GH[1] = H;
      return GH;
   }

   public int countBinNumber(int[] bit) {
      int count = 0;

      for(int i = 0; i < bit.length; ++i) {
         if (bit[i] == 1) {
            ++count;
         }
      }

      return count;
   }

   public double generateWeight(int[] bit) {
      BigDecimal G = BigDecimal.ZERO;
      BigDecimal H = BigDecimal.ZERO;

      for(int i = 0; i < bit.length; ++i) {
         if (bit[i] == 1) {
            G = G.add(this.gradient[i]);
            H = H.add(this.hessian[i]);
         }
      }

      BigDecimal weightNow = G.divide(H.add(this.Lambda), 10, 3);

      for(int i = 0; i < bit.length; ++i) {
         if (bit[i] == 1) {
            double[] var10000 = this.weight;
            var10000[i] += 0.0D - weightNow.doubleValue() * this.LearningRate;
         }
      }

      return 0.0D - weightNow.doubleValue() * this.LearningRate;
   }

   public LinkedHashMap<String, BigDecimal[]> caculateDataownerGH(LinkedHashMap<String, int[][]> Bucket, int[] bit) {
      LinkedHashMap<String, BigDecimal[]> bucketGH = new LinkedHashMap();
      Iterator var4 = Bucket.keySet().iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         int[][] bucketbit = (int[][])Bucket.get(key);
         BigDecimal[] GHnumpy = new BigDecimal[2 * bucketbit.length];

         for(int i = 0; i < bucketbit.length; ++i) {
            BigDecimal G = new BigDecimal("0");
            BigDecimal H = new BigDecimal("0");

            for(int j = 0; j < bucketbit[0].length; ++j) {
               if ((bucketbit[i][j] & bit[j]) == 1) {
                  G = G.add(this.gradient[j]);
                  H = H.add(this.hessian[j]);
               }
            }

            GHnumpy[2 * i] = G;
            GHnumpy[2 * i + 1] = H;
         }

         bucketGH.put(key, GHnumpy);
      }

      return bucketGH;
   }

   public BigInteger[] ANDoperate(BigInteger[] gh, int[] bit) {
      BigInteger[] result = new BigInteger[gh.length];

      for(int i = 0; i < gh.length; ++i) {
         if (bit[i] == 1) {
            result[i] = gh[i];
         } else {
            if (bit[i] != 0) {
               System.out.println("bit信息有误！！！");
               return null;
            }

            result[i] = BigInteger.ZERO;
         }
      }

      return result;
   }

   public int[][] ANDoperation(int[][] bucket, int[] bit) {
      int[][] Bucket = new int[bucket.length][bucket[0].length];

      for(int i = 0; i < Bucket.length; ++i) {
         for(int j = 0; j < Bucket[0].length; ++j) {
            Bucket[i][j] = bucket[i][j] & bit[j];
         }
      }

      return Bucket;
   }

   public String[] caculateLocalGain(LinkedHashMap<String, int[][]> Bucket, int[] bit) {
      String[] MaxLocalGain = new String[5];
      BigDecimal[] GH = this.caculateGH(bit);
      BigDecimal MaxGain = new BigDecimal("-1000");
      Iterator var6 = Bucket.keySet().iterator();

      while(var6.hasNext()) {
         String key = (String)var6.next();
         int[][] bucketbit = (int[][])Bucket.get(key);
         int[][] bucket = this.ANDoperation(bucketbit, bit);

         for(int i = 0; i < bucket.length; ++i) {
            BigDecimal[] GHleft = this.caculateGH(bucket[i]);
            BigDecimal Gain = this.caculateGain(GHleft[0], GHleft[1], GH[0], GH[1]);
            if (Gain.compareTo(MaxGain) == 1) {
               MaxLocalGain[0] = key;
               MaxLocalGain[1] = String.valueOf(i);
               MaxLocalGain[2] = String.valueOf(GHleft[0]);
               MaxLocalGain[3] = String.valueOf(GHleft[1]);
               MaxLocalGain[4] = String.valueOf(Gain);
               MaxGain = Gain;
            }
         }
      }

      return MaxLocalGain;
   }

   public BigDecimal caculateGain(BigDecimal Gleft, BigDecimal Hleft, BigDecimal G, BigDecimal H) {
      BigDecimal a = Hleft.add(this.Lambda);
      BigDecimal b = Gleft.pow(2);
      BigDecimal Gainleft = b.divide(a, 10, 3);
      BigDecimal a1 = H.subtract(Hleft).add(this.Lambda);
      BigDecimal b1 = G.subtract(Gleft).pow(2);
      BigDecimal Gainright = b1.divide(a1, 10, 3);
      BigDecimal Gain = Gainleft.add(Gainright);
      return Gain;
   }

   public static void main(String[] args) {
      xgboost XGBoost = new xgboost();
      XGBoost.Lambda = new BigDecimal("1");
      System.out.println(XGBoost.caculateGain(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("5"), new BigDecimal("7")));
   }
}
