package dataowner;

import FE.FE;
import SHE.SHE;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import utils.utils;

public class DataOwner {
   public static int PARAMS_N = 3;
   public int dataNumbers;
   public int dataDims;
   public FE FE;
   private SHE SHE;
   private BigInteger[] r;
   private String DosName;
   private LinkedHashMap<String, double[]> BuckteSplictInformation;
   private LinkedHashMap<String, BigInteger[]> EncBuckteSplictInformation;
   private LinkedHashMap<String, int[][]> BucketOrganized;
   private LinkedHashMap<String, int[][]> BucketCSP;
   private LinkedHashMap<String, BigInteger[]> KeyDer;
   private LinkedHashMap<String, String> LocalDataTags;
   private double[][] rawDatas;
   private String[] localDataTags;
   private int minSampleNumber;
   private int binNumbers = 32;
   private int maxGH = 100;

   private void readDataAndPreprocessing(String pathName) {
      System.out.println("预处理本地数据......");
      Map<String[], double[][]> map = utils.readDataFromFile(this.dataNumbers, this.dataDims, pathName);
      this.localDataTags = (String[])map.keySet().iterator().next();
      this.rawDatas = (double[][])((Entry)map.entrySet().iterator().next()).getValue();
      LinkedHashMap<String, int[][]> map1 = this.processingData();
      this.BucketSS(map1);
      System.out.println("本地数据信息秘密分桶处理完毕");
   }

   private BigInteger[] InttoBigInteger(int[] BucktInformation) {
      BigInteger[] Bucket = new BigInteger[BucktInformation.length];

      for(int i = 0; i < BucktInformation.length; ++i) {
         Bucket[i] = BigInteger.valueOf((long)BucktInformation[i]);
      }

      return Bucket;
   }

   private BigInteger[] InnerBY(int[][] BucketInformation) {
      BigInteger[] result = new BigInteger[BucketInformation.length];

      for(int i = 0; i < BucketInformation.length; ++i) {
         BigInteger[] Bucket = this.InttoBigInteger(BucketInformation[i]);
         result[i] = this.FE.InnerProduct(Bucket);
      }

      return result;
   }

   private void caculateKeyDer() {
      this.KeyDer = new LinkedHashMap();

      for(Integer i = 0; i < this.BucketCSP.size(); i = i + 1) {
         int len = ((int[][])this.BucketCSP.get(this.DosName + i)).length;
         int[][] var10000 = new int[len][this.dataNumbers];
         int[][] BucketInformation = (int[][])this.BucketCSP.get(this.DosName + i);
         BigInteger[] Bucket = this.InnerBY(BucketInformation);
         this.KeyDer.put(this.DosName + i, Bucket);
      }

      System.out.println("CSP分桶加密处理完毕");
   }

   private void sendBuckettoCSP(String URL, int port) throws IOException {
      Socket socket = new Socket(URL, port);
      new ObjectInputStream(socket.getInputStream());
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      System.out.println("已发送服务器连接");
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getLocalAddress();
      var10000.println("客户端:" + var10001 + " Port:" + socket.getLocalPort());
      var10000 = System.out;
      var10001 = socket.getInetAddress();
      var10000.println("服务器:" + var10001 + " Port:" + socket.getLocalPort());
      objectOutputStream.writeObject(this.BucketCSP);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.KeyDer);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.EncBuckteSplictInformation);
      objectOutputStream.flush();
      System.out.println("向CSP发送秘密桶信息完成");
      utils.closeSocketClient(socket);
   }

   private double[][] NumpyT(double[][] Datas) {
      double[][] DatasT = new double[Datas[0].length][Datas.length];

      for(int i = 0; i < Datas[0].length; ++i) {
         for(int j = 0; j < Datas.length; ++j) {
            DatasT[i][j] = Datas[j][i];
         }
      }

      return DatasT;
   }

   public static Integer[] sort(final double[] arr) {
      Integer[] index = new Integer[arr.length];

      for(int i = 0; i < arr.length; ++i) {
         index[i] = i;
      }

      Arrays.sort(index, new Comparator<Integer>() {
         public int compare(Integer o1, Integer o2) {
            if (arr[o1] > arr[o2]) {
               return 1;
            } else {
               return arr[o1] < arr[o2] ? -1 : 0;
            }
         }
      });
      Integer[] a = new Integer[index.length];

      for(int i = 0; i < index.length; ++i) {
         a[index[i]] = i;
      }

      return a;
   }

   private int[][] constructBins(Integer[] index) {
      int[][] BinIndex = new int[this.binNumbers][this.dataNumbers];

      for(int i = 0; i < this.binNumbers; ++i) {
         for(int j = 0; j < this.dataNumbers; ++j) {
            if (i * this.dataNumbers / this.binNumbers <= index[j] && index[j] < (i + 1) * this.dataNumbers / this.binNumbers) {
               BinIndex[i][j] = 1;
            } else {
               BinIndex[i][j] = 0;
            }
         }
      }

      return BinIndex;
   }

   private int[][] constructOnehotBins(double[] rawdata, List<Double> IndexNumber) {
      int[][] BinIndex = new int[IndexNumber.size() - 1][this.dataNumbers];

      for(int i = 0; i < IndexNumber.size() - 1; ++i) {
         for(int j = 0; j < this.dataNumbers; ++j) {
            if ((Double)IndexNumber.get(i) <= rawdata[j] && rawdata[j] < (Double)IndexNumber.get(i + 1)) {
               BinIndex[i][j] = 1;
            } else {
               BinIndex[i][j] = 0;
            }
         }
      }

      return BinIndex;
   }

   private LinkedHashMap<String, int[][]> processingData() {
      LinkedHashMap<String, int[][]> map = new LinkedHashMap();
      this.BuckteSplictInformation = new LinkedHashMap();
      double[][] rawDatasT = this.NumpyT(this.rawDatas);

      for(Integer i = 0; i < rawDatasT.length; i = i + 1) {
         List<Double> indexNumber = utils.countNumber(rawDatasT[i]);
         if (indexNumber.size() < this.binNumbers) {
            double[] splictfeture = new double[indexNumber.size()];

            for(Integer j = 1; j < indexNumber.size() + 1; j = j + 1) {
               splictfeture[j - 1] = (Double)indexNumber.get(j - 1);
            }

            this.BuckteSplictInformation.put(this.DosName + i, splictfeture);
            int[][] BinIndex = this.constructOnehotBins(rawDatasT[i], indexNumber);
            map.put(this.DosName + i, BinIndex);
         } else {
            Integer[] index = sort(rawDatasT[i]);
            double[] splictfeture = new double[this.binNumbers];
            LinkedHashMap<Integer, Integer> a = new LinkedHashMap();

            int k;
            for(k = 0; k < index.length; ++k) {
               a.put(index[k], k);
            }

            for(k = 0; k < this.binNumbers; ++k) {
               double intVals = rawDatasT[i][(Integer)a.get((k + 1) * this.dataNumbers / this.binNumbers - 1)];
               splictfeture[k] = intVals;
            }

            this.BuckteSplictInformation.put(this.DosName + i, splictfeture);
            int[][] BinIndex = this.constructBins(index);
            map.put(this.DosName + i, BinIndex);
         }
      }

      return map;
   }

   private void BucketSS(LinkedHashMap<String, int[][]> map) {
      this.BucketOrganized = new LinkedHashMap();
      this.BucketCSP = new LinkedHashMap();

      for(Integer i = 0; i < map.size(); i = i + 1) {
         String Name = this.DosName + i;
         int[][] data = (int[][])map.get(Name);
         int[][] BinIndexOg = new int[data.length][this.dataNumbers];
         int[][] BinIndexCSP = new int[data.length][this.dataNumbers];

         for(int j = 0; j < data.length; ++j) {
            for(int k = 0; k < this.dataNumbers; ++k) {
               if (data[j][k] == 1) {
                  BinIndexOg[j][k] = 0;
                  BinIndexCSP[j][k] = 0;
               } else if (Math.random() < 0.5D) {
                  BinIndexOg[j][k] = 1;
                  BinIndexCSP[j][k] = 0;
               } else {
                  BinIndexOg[j][k] = 0;
                  BinIndexCSP[j][k] = 1;
               }
            }
         }

         this.BucketOrganized.put(Name, BinIndexOg);
         this.BucketCSP.put(Name, BinIndexCSP);
      }

   }

   public BigInteger GenerateBigInteger(int lenth) {
      Random rnd = new Random();
      BigInteger a = new BigInteger(lenth, rnd);
      int b = Math.abs(a.intValue());
      BigInteger c = new BigInteger(String.valueOf(b));
      return c;
   }

   private void EncFeture() {
      this.EncBuckteSplictInformation = new LinkedHashMap();
      Iterator var1 = this.BuckteSplictInformation.keySet().iterator();

      while(var1.hasNext()) {
         String i = (String)var1.next();
         double[] splictfeture = (double[])this.BuckteSplictInformation.get(i);
         BigInteger[] Encsplictfeture = new BigInteger[splictfeture.length];

         for(int j = 0; j < splictfeture.length; ++j) {
            BigInteger r0 = this.GenerateBigInteger(10);
            BigInteger r1 = this.GenerateBigInteger(10);
            Encsplictfeture[j] = BigInteger.valueOf((new Double(-splictfeture[j] * (double)this.maxGH)).longValue()).add(r0.multiply(this.r[0])).add(r1.multiply(this.r[1]));
         }

         this.EncBuckteSplictInformation.put(i, Encsplictfeture);
      }

   }

   private void createSocketAndReceiveParams(String URL, int port) throws IOException, ClassNotFoundException {
      Socket socket = new Socket(URL, port);
      ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      System.out.println("已发送服务器连接");
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getLocalAddress();
      var10000.println("客户端:" + var10001 + " Port:" + socket.getLocalPort());
      var10000 = System.out;
      var10001 = socket.getInetAddress();
      var10000.println("服务器:" + var10001 + " Port:" + socket.getLocalPort());
      this.FE = (FE)objectInputStream.readObject();
      System.out.println("从Organizer接收参数完成");
      this.SHE = (SHE)objectInputStream.readObject();
      this.r = new BigInteger[2];
      this.r = (BigInteger[])objectInputStream.readObject();
      System.out.println("从Organizer接收安全服务密钥完成");
      objectOutputStream.writeObject(this.BucketOrganized);
      objectOutputStream.flush();
      System.out.println("向Organizer发送秘密桶信息完成");
      objectOutputStream.writeObject(this.BuckteSplictInformation);
      objectOutputStream.flush();
      System.out.println("向Organizer发送分裂具体信息完成");
      this.LocalDataTags = new LinkedHashMap();

      for(int i = 0; i < this.localDataTags.length; ++i) {
         this.LocalDataTags.put(this.DosName + i, this.localDataTags[i]);
      }

      objectOutputStream.writeObject(this.LocalDataTags);
      objectOutputStream.flush();
      System.out.println("向Organizer发送分裂具体信息完成");
      utils.closeSocketClient(socket);
   }

   public static void main(String[] args) throws InterruptedException {
      int[] dims = new int[]{6, 6, 6};

      for(int i = 0; i < PARAMS_N; ++i) {
         String pathName = "/home/cinwa/SGBoost-main/sources/Credit-4/party" + (i + 1);
         DataOwner dataOwner = new DataOwner();
         dataOwner.DosName = "Dos" + i;
         dataOwner.dataNumbers = 24000;
         dataOwner.dataDims = dims[i];
         dataOwner.readDataAndPreprocessing(pathName);

         try {
            dataOwner.createSocketAndReceiveParams("127.0.0.1", 8123);
         } catch (ClassNotFoundException | IOException var7) {
            var7.printStackTrace();
         }

         dataOwner.EncFeture();
         dataOwner.caculateKeyDer();

         try {
            dataOwner.sendBuckettoCSP("127.0.0.1", 2020);
         } catch (IOException var6) {
            var6.printStackTrace();
         }
      }

      Thread.sleep(300000L);
   }
}
