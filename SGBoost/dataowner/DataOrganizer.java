package dataowner;

import FE.FE;
import SHE.SHE;
import TreeLink.TreeLink;
import XGBoost.xgboost;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import utils.utils;

public class DataOrganizer {
   public int securityParam;
   public int dataNumber;
   public int binNumber = 32;
   public int paramN;
   public int DataDims;
   public int IterNums;
   public double LearningRate;
   public BigDecimal lambda;
   public int testDataNums;
   public int MaxDeep;
   public int minBinNumber;
   private int maxGH = 100;
   private FE FE;
   private xgboost XGBoost;
   private SHE SHE;
   private SHE SHE2;
   private TreeLink[] treeLinks;
   private BigInteger[] r;
   private LinkedHashMap<String, double[]> BuckteSplictInformation;
   private LinkedHashMap<String, BigInteger[]> EncBuckteSplictInformation;
   private LinkedHashMap<String, String> GlobalDataTags;
   private String[] localDataTags;
   private String organizeName = "organize";
   private double[][] rawData;
   private double[] localDataY;
   private LinkedHashMap<String, int[][]> BucketFromDOs;
   private LinkedHashMap<String, int[][]> BucketFromLocal;
   private BigInteger[] Encg;
   private BigInteger[] Ench;
   private int[] bitFromCSP;
   private String[] Globalgain = new String[5];
   private String[] localgain = new String[5];
   private double[] accuracy;
   public int thread_num = 64;
   public static int corePoolSize = 4;
   public static int maximumPoolSize = 20;
   public static int keepAliveTime = 20;
   public static TimeUnit unit;
   public static BlockingQueue<Runnable> workQueue;
   public static ThreadFactory threadFactory;
   public static ThreadPoolExecutor threadPoolExecutor;

   public long commnication_cost = 0;
   public String json;

   private void sendParamsToDataOwners(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
      objectOutputStream.writeObject(this.FE);
      objectOutputStream.flush();
      System.out.println("发送加密参数完成");
      objectOutputStream.writeObject(this.SHE);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.r);
      objectOutputStream.flush();
      System.out.println("发送安全服务密钥");
      new LinkedHashMap();
      LinkedHashMap<String, int[][]> Bucketdos = (LinkedHashMap)objectInputStream.readObject();
      Bucketdos.entrySet().stream().forEach((bi) -> {
         this.BucketFromDOs.put((String)bi.getKey(), (int[][])bi.getValue());
      });
      System.out.println("接收桶信息完成");
      new LinkedHashMap();
      LinkedHashMap<String, double[]> SplictInformation = (LinkedHashMap)objectInputStream.readObject();
      SplictInformation.entrySet().stream().forEach((bi) -> {
         this.BuckteSplictInformation.put((String)bi.getKey(), (double[])bi.getValue());
      });
      System.out.println("接收分裂信息完成");
      new LinkedHashMap();
      LinkedHashMap<String, String> GlobalTags = (LinkedHashMap)objectInputStream.readObject();
      GlobalTags.entrySet().stream().forEach((bi) -> {
         this.GlobalDataTags.put((String)bi.getKey(), (String)bi.getValue());
      });
      System.out.println("接收全球标签完成");
   }

   private void startServerAndDistributeParamsDataOwners(String URL, int port) throws IOException {
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(URL, port));
      System.out.println("Server:" + serverSocket.getLocalSocketAddress());
      CountDownLatch countDownLatch = new CountDownLatch(this.paramN);
      synchronized(countDownLatch) {
         for(int index = this.paramN; index != 0; --index) {
            threadPoolExecutor.execute(() -> {
               try {
                  Socket socket = serverSocket.accept();
                  PrintStream var10000 = System.out;
                  InetAddress var10001 = socket.getInetAddress();
                  var10000.println("建立连接:" + var10001 + ":" + socket.getLocalPort());
                  ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                  ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                  this.sendParamsToDataOwners(objectOutputStream, objectInputStream);
                  utils.closeSocketServer(socket);
               } catch (IOException var10) {
                  var10.printStackTrace();
               } catch (ClassNotFoundException var11) {
                  var11.printStackTrace();
               } finally {
                  countDownLatch.countDown();
               }

            });
         }

         try {
            countDownLatch.await();
         } catch (InterruptedException var9) {
            var9.printStackTrace();
         }

         threadPoolExecutor.shutdown();
      }

      serverSocket.close();
   }

   private void sendParamtoCSP(String URL, int port) throws IOException, ClassNotFoundException {
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
      objectOutputStream.writeObject(this.XGBoost);
      objectOutputStream.flush();
      FE FEtoCSP = new FE();
      FEtoCSP.g = this.FE.g;
      FEtoCSP.p = this.FE.p;
      FEtoCSP.k1 = this.FE.k1;
      objectOutputStream.writeObject(FEtoCSP);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.EncBuckteSplictInformation);
      objectOutputStream.flush();
      System.out.println("向CSP发送参数完成");
      utils.closeSocketClient(socket);
   }

   private void systemInit(int securityParam, int dataNumber, String URL, int port, int paramN) throws ClassNotFoundException {
      this.FE = new FE();
      this.SHE = new SHE();
      this.SHE2 = new SHE();
      this.SHE.SHEInitialization(1024, 50, 200);
      this.SHE2.SHEInitialization(2560, 20, 160);
      this.r = new BigInteger[2];
      this.r[0] = this.SHE.SHEEncryption(BigInteger.ZERO);
      this.r[1] = this.SHE.SHEEncryption(BigInteger.ZERO);
      this.securityParam = securityParam;
      this.dataNumber = dataNumber;
      this.paramN = paramN;
      this.FE.FEInitialization(this.securityParam, this.dataNumber);
      this.BucketFromDOs = new LinkedHashMap();
      this.BucketFromLocal = new LinkedHashMap();
      this.BuckteSplictInformation = new LinkedHashMap();
      this.EncBuckteSplictInformation = new LinkedHashMap();
      this.GlobalDataTags = new LinkedHashMap();

      try {
         this.startServerAndDistributeParamsDataOwners(URL, port);
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      System.out.println("Organizer初始化完成");
   }

   private void modelinit(int DataDims, int IterNums, double LearningRate, BigDecimal lambda, int MaxDeep, int minBinNumber) {
      this.XGBoost = new xgboost();
      this.DataDims = DataDims;
      this.IterNums = IterNums;
      this.LearningRate = LearningRate;
      this.lambda = lambda;
      this.MaxDeep = MaxDeep;
      this.minBinNumber = minBinNumber;
      this.accuracy = new double[this.IterNums];
      this.XGBoost.modelInit(this.dataNumber, this.DataDims, this.IterNums, this.LearningRate, this.lambda, this.MaxDeep, this.minBinNumber);
      System.out.println("模型初始化完成");
   }

   private void readData(String pathName) throws IOException {
      Map<String[], double[][]> map = utils.readDataFromFile(this.dataNumber, this.DataDims, pathName);
      this.localDataTags = (String[])map.keySet().iterator().next();

      int i;
      for(i = 0; i < this.localDataTags.length; ++i) {
         this.GlobalDataTags.put(this.organizeName + i, this.localDataTags[i]);
      }

      this.rawData = (double[][])((Entry)map.entrySet().iterator().next()).getValue();
      this.localDataY = new double[this.dataNumber];

      for(i = 0; i < this.dataNumber; ++i) {
         this.localDataY[i] = this.rawData[i][this.DataDims - 1];
      }

      this.processingData();
      System.out.println("数据预处理完成");
   }

   private void processingData() {
      LinkedHashMap<String, int[][]> map = new LinkedHashMap();
      double[][] rawDatasT = this.NumpyT(this.rawData);

      for(Integer i = 0; i < rawDatasT.length - 1; i = i + 1) {
         List<Double> indexNumber = utils.countNumber(rawDatasT[i]);
         if (indexNumber.size() < this.binNumber) {
            double[] splictfeture = new double[indexNumber.size()];
            BigInteger[] Encsplictfeture = new BigInteger[indexNumber.size()];

            for(Integer j = 1; j < indexNumber.size() + 1; j = j + 1) {
               splictfeture[j - 1] = (Double)indexNumber.get(j - 1);
               Encsplictfeture[j - 1] = this.SHE.SHEEncryption(BigInteger.ZERO.subtract(BigInteger.valueOf((new Double(splictfeture[j - 1] * 100.0D)).longValue())));
            }

            this.BuckteSplictInformation.put(this.organizeName + i, splictfeture);
            this.EncBuckteSplictInformation.put(this.organizeName + i, Encsplictfeture);
            int[][] BinIndex = this.constructOnehotBins(rawDatasT[i], indexNumber);
            map.put(this.organizeName + i, BinIndex);
         } else {
            Integer[] index = sort(rawDatasT[i]);
            double[] splictfeture = new double[this.binNumber];
            BigInteger[] Encsplictfeture = new BigInteger[this.binNumber];
            LinkedHashMap<Integer, Integer> a = new LinkedHashMap();

            int k;
            for(k = 0; k < index.length; ++k) {
               a.put(index[k], k);
            }

            for(k = 0; k < this.binNumber; ++k) {
               splictfeture[k] = rawDatasT[i][(Integer)a.get((k + 1) * this.dataNumber / this.binNumber - 1)];
               Encsplictfeture[k] = this.SHE.SHEEncryption(BigInteger.ZERO.subtract(BigInteger.valueOf((long)splictfeture[k] * 100L)));
            }

            this.BuckteSplictInformation.put(this.organizeName + i, splictfeture);
            this.EncBuckteSplictInformation.put(this.organizeName + i, Encsplictfeture);
            int[][] BinIndex = this.constructBins(index);
            map.put(this.organizeName + i, BinIndex);
         }
      }

      this.BucketFromLocal = map;
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

   private int[][] constructOnehotBins(double[] rawdata, List<Double> IndexNumber) {
      int[][] BinIndex = new int[IndexNumber.size() - 1][this.dataNumber];

      for(int i = 0; i < IndexNumber.size() - 1; ++i) {
         for(int j = 0; j < this.dataNumber; ++j) {
            if (rawdata[j] < (Double)IndexNumber.get(i + 1)) {
               BinIndex[i][j] = 1;
            } else {
               BinIndex[i][j] = 0;
            }
         }
      }

      return BinIndex;
   }

   private int[][] constructBins(Integer[] index) {
      int[][] BinIndex = new int[this.binNumber][this.dataNumber];

      for(int i = 0; i < this.binNumber; ++i) {
         for(int j = 0; j < this.dataNumber; ++j) {
            if (index[j] < (i + 1) * this.dataNumber / this.binNumber) {
               BinIndex[i][j] = 1;
            } else {
               BinIndex[i][j] = 0;
            }
         }
      }

      return BinIndex;
   }

   private void sendBucketGHtoCSP(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, int[] bit, int flag) throws IOException, ClassNotFoundException {
      objectOutputStream.writeObject(flag);
      objectOutputStream.flush();
      if (flag == 1) {
         this.localgain = this.XGBoost.caculateLocalGain(this.BucketFromLocal, bit);
         objectOutputStream.writeObject(this.localgain);
         objectOutputStream.flush();
         LinkedHashMap<String, BigDecimal[]> DataownerGH = this.XGBoost.caculateDataownerGH(this.BucketFromDOs, bit);
         objectOutputStream.writeObject(DataownerGH);
         objectOutputStream.flush();
         BigDecimal[] GH = new BigDecimal[2];
         GH = this.XGBoost.caculateGH(bit);
         objectOutputStream.writeObject(GH);
         objectOutputStream.flush();
         this.Encg = this.FE.FEEncryption(this.XGBoost.ANDoperate(this.XGBoost.ELgradient, bit));
         this.Ench = this.FE.FEEncryption(this.XGBoost.ANDoperate(this.XGBoost.ELhessian, bit));
         objectOutputStream.writeObject(this.Encg);
         objectOutputStream.flush();
         objectOutputStream.writeObject(this.Ench);
         objectOutputStream.flush();

         ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
         ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
         sizeOutputStream.writeObject(this.localgain);
         sizeOutputStream.flush();
         this.commnication_cost = this.commnication_cost + byteOutputStream.size();
         byteOutputStream.reset();
         sizeOutputStream.writeObject(GH);
         sizeOutputStream.flush();
         this.commnication_cost = this.commnication_cost + byteOutputStream.size();
         sizeOutputStream.writeObject(this.Encg);
         sizeOutputStream.flush();
         this.commnication_cost = this.commnication_cost + byteOutputStream.size();
         byteOutputStream.reset();
         sizeOutputStream.writeObject(this.Ench);
         sizeOutputStream.flush();
         this.commnication_cost = this.commnication_cost + byteOutputStream.size();

         System.out.println("向CSP发送本地最大增益、DOsGH、加密gh完成");
      }

   }

   private void RecieveGainFromCSP(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
      this.Globalgain = (String[])objectInputStream.readObject();
      this.bitFromCSP = new int[this.dataNumber];
      if (!this.Globalgain[0].equals(this.localgain[0])) {
         this.bitFromCSP = (int[])objectInputStream.readObject();
      }

      System.out.println("接收全球增益与分桶信息完成");
   }

   private void splictNode(TreeLink treeNode, int depth, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, int[] bit, int flag, int TreeIndex, int nodeIndex) throws ClassNotFoundException, IOException {
      if (depth == this.MaxDeep) {
         System.out.println("达到最大深度");
      } else {
         ++depth;
         long startTime = System.currentTimeMillis();

         this.sendBucketGHtoCSP(objectOutputStream, objectInputStream, bit, flag);
         if (flag == 0) {
            System.out.println("达到最小分桶数目，结束分裂");
         } else {
            this.RecieveGainFromCSP(objectOutputStream, objectInputStream);
            long endTime = System.currentTimeMillis();
            System.out.println("计算BucketGH的运行时间：" + (endTime - startTime) + "ms");
            int[] leftBit = new int[this.dataNumber];
            int[] rightBit = new int[this.dataNumber];
            int index;
            int len;
            int[][] localBucket;
            int[][] var10000;
            if (this.Globalgain[0].equals(this.localgain[0])) {
               index = Integer.valueOf(this.Globalgain[1]);
               len = ((int[][])this.BucketFromLocal.get(this.Globalgain[0])).length;
               var10000 = new int[len][this.dataNumber];
               localBucket = (int[][])this.BucketFromLocal.get(this.Globalgain[0]);

               for(int i = 0; i < this.dataNumber; ++i) {
                  leftBit[i] = bit[i] & localBucket[index][i];
                  if (bit[i] == 1 && leftBit[i] == 0) {
                     rightBit[i] = 1;
                  } else {
                     rightBit[i] = 0;
                  }
               }
            } else {
               index = Integer.valueOf(this.Globalgain[1]);
               len = ((int[][])this.BucketFromDOs.get(this.Globalgain[0])).length;
               var10000 = new int[len][this.dataNumber];
               localBucket = (int[][])this.BucketFromDOs.get(this.Globalgain[0]);
               int[] dosBucket = new int[this.dataNumber];
               int i = 0;

               label77:
               while(true) {
                  int a;
                  if (i >= this.XGBoost.trainDataNums) {
                     i = 0;

                     while(true) {
                        if (i >= this.dataNumber) {
                           break label77;
                        }

                        a = index + 1 - dosBucket[i] - this.bitFromCSP[i];
                        leftBit[i] = bit[i] & a;
                        if (bit[i] == 1 && leftBit[i] == 0) {
                           rightBit[i] = 1;
                        } else {
                           rightBit[i] = 0;
                        }

                        ++i;
                     }
                  }

                  for(a = 0; a < Integer.valueOf(index) + 1; ++a) {
                     dosBucket[i] += localBucket[a][i];
                  }

                  ++i;
               }
            }

            treeNode.feature = this.Globalgain;
            TreeLink NodeRight = new TreeLink(rightBit, (TreeLink)null, (TreeLink)null);
            TreeLink NodeLeft = new TreeLink(leftBit, (TreeLink)null, (TreeLink)null);
            treeNode.rightNode = NodeRight;
            treeNode.leftNode = NodeLeft;
            int flagleft = 1;
            int flagright = 1;
            if (depth == this.MaxDeep || this.XGBoost.countBinNumber(leftBit) <= this.XGBoost.minBinNumber) {
               NodeLeft.weight = this.XGBoost.generateWeight(leftBit);
               flagleft = 0;
            }

            if (depth == this.MaxDeep || this.XGBoost.countBinNumber(rightBit) <= this.XGBoost.minBinNumber) {
               NodeRight.weight = this.XGBoost.generateWeight(rightBit);
               flagright = 0;
            }

            this.splictNode(NodeRight, depth, objectOutputStream, objectInputStream, rightBit, flagright, TreeIndex, nodeIndex * 2 + 1);
            this.splictNode(NodeLeft, depth, objectOutputStream, objectInputStream, leftBit, flagleft, TreeIndex, nodeIndex * 2);
         }
      }

   }

   private TreeLink[] TrainTree(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
      TreeLink[] treelink = new TreeLink[this.IterNums];
      this.XGBoost.TrueY = this.localDataY;

      for(int i = 0; i < this.IterNums; ++i) {
         System.out.println("第" + i + "次迭代:");
         int depth = 1;
         this.XGBoost.caculategh();
         this.XGBoost.EnLargegh();
         int[] bit = new int[this.dataNumber];

         for(int j = 0; j < this.dataNumber; ++j) {
            bit[j] = 1;
         }

         int flag = 1;
         treelink[i] = new TreeLink(bit, (TreeLink)null, (TreeLink)null);
         this.splictNode(treelink[i], depth, objectOutputStream, objectInputStream, bit, flag, i, 1);
         this.XGBoost.sigmoid();
         this.Predict(treelink[i], i);
      }

      return treelink;
   }

   private void Predict(TreeLink treenode, int treeIndex) {
      for(int i = 0; i < this.XGBoost.testDataNums; ++i) {
         TreeLink TreeNode = new TreeLink(treenode.data, treenode.leftNode, treenode.rightNode);
         TreeNode.feature = treenode.feature;
         TreeNode.weight = treenode.weight;

         for(int j = 0; j < this.XGBoost.MaxDeep; ++j) {
            String[] feature = new String[5];
            feature = TreeNode.feature;
            if (TreeNode.rightNode == null && TreeNode.leftNode == null) {
               double[] var10000 = this.XGBoost.testWeight;
               var10000[i] += TreeNode.weight;
            } else {
               int featureIndex = Integer.valueOf(feature[1]);
               if (featureIndex == ((double[])this.BuckteSplictInformation.get(feature[0])).length) {
                  TreeNode = TreeNode.rightNode;
               } else {
                  String splictfeature = (String)this.GlobalDataTags.get(feature[0]);
                  int index = (Integer)this.XGBoost.testDataTags.get(splictfeature);
                  if (this.XGBoost.testData[i][index] < ((double[])this.BuckteSplictInformation.get(feature[0]))[featureIndex]) {
                     TreeNode = TreeNode.leftNode;
                  } else {
                     TreeNode = TreeNode.rightNode;
                  }
               }
            }
         }
      }

      this.XGBoost.sigmoidTestY();
      this.accuracy[treeIndex] = this.XGBoost.accuracyRate(this.XGBoost.testDataY, this.XGBoost.testDataPredictY);
      double var10001 = this.accuracy[treeIndex];
      System.out.println("Accuracy: " + var10001);
      System.out.println("weight1:=====" + this.XGBoost.testWeight[0]);
   }

   private void startTrainTreeAndCSP(String URL, int port) throws IOException, ClassNotFoundException {
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
      this.treeLinks = new TreeLink[this.XGBoost.IterNums];
      this.treeLinks = this.TrainTree(objectOutputStream, objectInputStream);
      objectOutputStream.writeObject(this.treeLinks);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.GlobalDataTags);
      objectOutputStream.flush();

      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
      sizeOutputStream.writeObject(this.treeLinks);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      byteOutputStream.reset();
      sizeOutputStream.writeObject(this.GlobalDataTags);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();

      utils.closeSocketClient(socket);
   }

   private void createSocketAndSendToUserParam(String URL, int port) throws IOException, ClassNotFoundException {
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(URL, port));
      System.out.println("Server:" + serverSocket.getLocalSocketAddress());
      Socket socket = serverSocket.accept();
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getInetAddress();
      var10000.println("连接建立：" + var10001 + ":" + socket.getLocalPort());
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      new ObjectInputStream(socket.getInputStream());
      objectOutputStream.writeObject(this.SHE2);
      objectOutputStream.flush();
      System.out.println("向user发送模型参数完成");
      objectOutputStream.writeObject(this.r);
      objectOutputStream.flush();

      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
      sizeOutputStream.writeObject(this.SHE2);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      byteOutputStream.reset();
      sizeOutputStream.writeObject(this.r);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();

      System.out.println("发送delta完成");
      utils.closeSocketServer(socket);
      serverSocket.close();
   }

   private void treeTraverse(TreeLink treenode) {
      if (treenode.rightNode != null || treenode.leftNode != null) {
         if (this.SHE.SHEDecryption(treenode.nowInformation).compareTo(BigInteger.ZERO) == -1) {
            treenode.leftcoffe = this.SHE2.SHEEncryption(BigInteger.ONE);
            treenode.rightcoffe = this.SHE2.SHEEncryption(BigInteger.ZERO);
         } else {
            treenode.leftcoffe = this.SHE2.SHEEncryption(BigInteger.ZERO);
            treenode.rightcoffe = this.SHE2.SHEEncryption(BigInteger.ONE);
         }

         if (treenode.rightNode != null) {
            this.treeTraverse(treenode.rightNode);
         }

         if (treenode.leftNode != null) {
            this.treeTraverse(treenode.leftNode);
         }
      }

   }

   private ConcurrentHashMap<Integer, TreeLink[]> palDncSHE(final ConcurrentHashMap<Integer, TreeLink[]> AllServerInformation) {
      final int number = AllServerInformation.size();
      final int block_size = number / (this.thread_num - 1);
      ExecutorService executorService = Executors.newFixedThreadPool(this.thread_num);
      final CountDownLatch latch = new CountDownLatch(this.thread_num);

      final int my_thread_num = this.thread_num;
         executorService.execute(new Runnable() {
            public void run() {
               for( int i = 0; i < my_thread_num; ++i) {
               int start_id = i * block_size;
               int end_id = (i + 1) * block_size;
               if (i == DataOrganizer.this.thread_num - 1) {
                  end_id = number;
               }

               for(int j = start_id; j < end_id; ++j) {
                  TreeLink[] treeLink_j = (TreeLink[])AllServerInformation.get(j);

                  for(int k = 0; k < treeLink_j.length; ++k) {
                     DataOrganizer.this.treeTraverse(treeLink_j[k]);
                  }
               }

               latch.countDown();
            }
         }});

      try {
         latch.await();
      } catch (InterruptedException var8) {
         var8.printStackTrace();
      }

      executorService.shutdown();
      return AllServerInformation;
   }

   private void createSocketAndDecANDCSP(String URL, int port) throws IOException, ClassNotFoundException {
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(URL, port));
      System.out.println("Server:" + serverSocket.getLocalSocketAddress());
      Socket socket = serverSocket.accept();
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getInetAddress();
      var10000.println("连接建立：" + var10001 + ":" + socket.getLocalPort());
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
      BigInteger[] coffe = new BigInteger[]{this.SHE.SHEEncryption(BigInteger.ONE), this.SHE.SHEEncryption(BigInteger.ZERO.subtract(BigInteger.ONE)), this.SHE2.SHEEncryption(BigInteger.ONE), this.SHE2.SHEEncryption(BigInteger.ZERO.subtract(BigInteger.ONE))};
      objectOutputStream.writeObject(coffe);
      objectOutputStream.flush();
      new ConcurrentHashMap();
      ConcurrentHashMap<Integer, TreeLink[]> AllServerInformation = (ConcurrentHashMap)objectInputStream.readObject();
      new ConcurrentHashMap();
      ConcurrentHashMap<Integer, TreeLink[]> AllresposeInformation = this.palDncSHE(AllServerInformation);
      objectOutputStream.writeObject(AllresposeInformation);
      objectOutputStream.flush();

      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
      sizeOutputStream.writeObject(coffe);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      byteOutputStream.reset();
      sizeOutputStream.writeObject(AllresposeInformation);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();

      utils.closeSocketServer(socket);
      serverSocket.close();
   }

   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
      DataOrganizer Dataorganizer = new DataOrganizer();
      String pathName = "/home/cinwa/SGBoost-main/sources/Credit-4/organizeData";
      Dataorganizer.systemInit(512, 24000, "127.0.0.1", 8123, 3);
      Dataorganizer.modelinit(6, 6, 0.3D, new BigDecimal(1), 3, 0);
      Dataorganizer.readData(pathName);
      Thread.sleep(3000L);
      Dataorganizer.sendParamtoCSP("127.0.0.1", 2021);
      String testpathName = "/home/cinwa/SGBoost-main/sources/Credit-4/test";
      Dataorganizer.XGBoost.readtestData(6000, 24, testpathName);

      Thread.sleep(1000L);
      long startTime = System.currentTimeMillis();
      Dataorganizer.startTrainTreeAndCSP("127.0.0.1", 2023);

      for(int i = 0; i < Dataorganizer.accuracy.length; ++i) {
         double var10001 = Dataorganizer.accuracy[i];
         System.out.print(var10001 + " ");
      }
      long endTime = System.currentTimeMillis();
      System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
      System.out.println("主动方训练通信开销：" + Dataorganizer.commnication_cost);
      Dataorganizer.commnication_cost = 0;
      System.out.println("主动方训练通信开销：" + Dataorganizer.commnication_cost);
      Dataorganizer.createSocketAndSendToUserParam("127.0.0.1", 2024);
      Dataorganizer.createSocketAndDecANDCSP("127.0.0.1", 8084);
      System.out.println("主动方查询通信开销：" + Dataorganizer.commnication_cost);
      Dataorganizer.commnication_cost = 0;
   }

   static {
      unit = TimeUnit.MILLISECONDS;
      workQueue = new LinkedBlockingQueue();
      threadFactory = Thread::new;
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, (long)keepAliveTime, unit, workQueue, threadFactory);
   }
}
