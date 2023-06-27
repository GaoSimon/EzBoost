package cloud;

import FE.FE;
import SHE.SHE;
import TreeLink.TreeLink;
import XGBoost.xgboost;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
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

public class CloudServiceProvider {
   private LinkedHashMap<String, BigInteger[]> EncBuckteSplictInformation;
   private LinkedHashMap<String, int[][]> BucketFromDOs;
   private LinkedHashMap<String, BigInteger[]> KeyDer;
   private FE FE;
   private xgboost XGBoost;
   private SHE SHE;
   private LinkedHashMap<String, BigDecimal[]> DataOwnerGH;
   private BigInteger[] Encg;
   private BigInteger[] Engh;
   private String[] GlobalGain;
   private String[] OrganizedGain;
   private BigDecimal[] GH;
   private int[] GlobalBit;
   private int maxGH = 100;
   private int flag;
   public int thread_num = 64;
   private int serverNumber;
   private ConcurrentHashMap<Integer, BigInteger> AllEncFeatureInformation;
   private BigInteger[] coffe;
   private LinkedHashMap<String, BigInteger[]> EncFeatureInformation;
   private Linkdict[] treedict;
   private LinkedHashMap<String, String> GlobalDataTags;
   private TreeLink[] treeLinks;
   private int paramN = 3;
   public static int corePoolSize = 4;
   public static int maximumPoolSize = 20;
   public static int keepAliveTime = 20;
   public static TimeUnit unit;
   public static BlockingQueue<Runnable> workQueue;
   public static ThreadFactory threadFactory;
   public static ThreadPoolExecutor threadPoolExecutor;
   public long commnication_cost = 0;
   public String json;
   private void systemInit() {
      this.KeyDer = new LinkedHashMap();
      this.BucketFromDOs = new LinkedHashMap();
      this.DataOwnerGH = new LinkedHashMap();
      this.EncBuckteSplictInformation = new LinkedHashMap();
      this.OrganizedGain = new String[5];
      this.GlobalGain = new String[5];
      this.XGBoost = new xgboost();
      System.out.println("CSP 初始化完成");
   }

   private void startServerAndRecieveParamsDataOwners(String URL, int port) throws IOException {
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(URL, port));
      System.out.println("Server:" + serverSocket.getLocalSocketAddress());

      for(int index = this.paramN; index != 0; --index) {
         Socket socket = serverSocket.accept();
         PrintStream var10000 = System.out;
         InetAddress var10001 = socket.getInetAddress();
         var10000.println("建立连接:" + var10001 + ":" + socket.getLocalPort());
         new ObjectOutputStream(socket.getOutputStream());
         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

         try {
            new LinkedHashMap();
            LinkedHashMap<String, int[][]> map = (LinkedHashMap)objectInputStream.readObject();
            new LinkedHashMap();
            LinkedHashMap<String, BigInteger[]> keyder = (LinkedHashMap)objectInputStream.readObject();
            new LinkedHashMap();
            LinkedHashMap<String, BigInteger[]> encBuckteSplictInformation = (LinkedHashMap)objectInputStream.readObject();
            map.entrySet().stream().forEach((bi) -> {
               this.BucketFromDOs.put((String)bi.getKey(), (int[][])bi.getValue());
            });
            keyder.entrySet().stream().forEach((bi) -> {
               this.KeyDer.put((String)bi.getKey(), (BigInteger[])bi.getValue());
            });
            encBuckteSplictInformation.entrySet().stream().forEach((bi) -> {
               this.EncBuckteSplictInformation.put((String)bi.getKey(), (BigInteger[])bi.getValue());
            });
            System.out.println("接受秘密分桶信息完成");
         } catch (ClassNotFoundException var11) {
            var11.printStackTrace();
         }

         utils.closeSocketServer(socket);
      }

      serverSocket.close();
   }

   private void createSocketAndRecieveParam(String URL, int port) throws IOException, ClassNotFoundException {
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(URL, port));
      System.out.println("Server:" + serverSocket.getLocalSocketAddress());
      Socket socket = serverSocket.accept();
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getInetAddress();
      var10000.println("连接建立：" + var10001 + ":" + socket.getLocalPort());
      new ObjectOutputStream(socket.getOutputStream());
      ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
      this.XGBoost = (xgboost)objectInputStream.readObject();
      this.FE = (FE)objectInputStream.readObject();
      new LinkedHashMap();
      LinkedHashMap<String, BigInteger[]> encBuckteSplictInformation = (LinkedHashMap)objectInputStream.readObject();
      encBuckteSplictInformation.entrySet().stream().forEach((bi) -> {
         this.EncBuckteSplictInformation.put((String)bi.getKey(), (BigInteger[])bi.getValue());
      });
      System.out.println("接受模型参数完成");
      utils.closeSocketServer(socket);
      serverSocket.close();
   }

   private int createSocketAndRecieveBucket(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
      this.flag = (Integer)objectInputStream.readObject();
      if (this.flag == 1) {
         this.OrganizedGain = (String[])objectInputStream.readObject();
         this.DataOwnerGH = (LinkedHashMap)objectInputStream.readObject();
         this.GH = new BigDecimal[2];
         this.GH = (BigDecimal[])objectInputStream.readObject();
         this.Encg = new BigInteger[this.XGBoost.trainDataNums];
         this.Encg = (BigInteger[])objectInputStream.readObject();
         this.Engh = new BigInteger[this.XGBoost.trainDataNums];
         this.Engh = (BigInteger[])objectInputStream.readObject();
         System.out.println("接受organized分裂参数完成");
      }

      return this.flag;
   }

   private void createSocketAndsendGain(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
      sizeOutputStream.writeObject(this.GlobalGain);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      objectOutputStream.writeObject(this.GlobalGain);
      objectOutputStream.flush();
      if (!this.GlobalGain[0].equals(this.OrganizedGain[0])) {
         this.GlobalBit = new int[this.XGBoost.trainDataNums];
         int[][] BucketBitfromDOs = (int[][])this.BucketFromDOs.get(this.GlobalGain[0]);

         for(int i = 0; i < this.XGBoost.trainDataNums; ++i) {
            for(int j = 0; j < Integer.valueOf(this.GlobalGain[1]) + 1; ++j) {
               int[] var10000 = this.GlobalBit;
               var10000[i] += BucketBitfromDOs[j][i];
            }
         }

         objectOutputStream.writeObject(this.GlobalBit);
         objectOutputStream.flush();


         sizeOutputStream.writeObject(this.GlobalBit);
         sizeOutputStream.flush();
         this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      }

      System.out.println("发送organized全局最大增益与分桶信息完成");
   }

   private BigInteger[] InttoBigInteger(int[] BucktInformation) {
      BigInteger[] Bucket = new BigInteger[BucktInformation.length];

      for(int i = 0; i < BucktInformation.length; ++i) {
         Bucket[i] = BigInteger.valueOf((long)BucktInformation[i]);
      }

      return Bucket;
   }

   private LinkedHashMap<String, BigDecimal[]> DecryptionBucket() {
      final LinkedHashMap<String, BigDecimal[]> result = new LinkedHashMap();
      int total_num = 0;

      int length;
      for(Iterator var3 = this.BucketFromDOs.keySet().iterator(); var3.hasNext(); total_num += length) {
         String key = (String)var3.next();
         length = ((int[][])this.BucketFromDOs.get(key)).length;
         BigDecimal[] dataGH = new BigDecimal[length * 2];
         result.put(key, dataGH);
      }

      final String[] index = new String[total_num * 2];
      int counter = 0;
      Iterator var14 = this.BucketFromDOs.keySet().iterator();

      int mid_id;
      while(var14.hasNext()) {
         String key = (String)var14.next();
         mid_id = ((int[][])this.BucketFromDOs.get(key)).length;

         for(int i = 0; i < mid_id; ++i) {
            index[counter] = key;
            ++counter;
            index[counter] = Integer.toString(i);
            ++counter;
         }
      }

      ExecutorService executorService = Executors.newFixedThreadPool(this.thread_num);
      final int block_size = total_num / this.thread_num;
      mid_id = this.thread_num - total_num + block_size * this.thread_num;
      final CountDownLatch latch = new CountDownLatch(this.thread_num);
      final int my_mid_id = mid_id;
      final int my_thread_num = this.thread_num;
         executorService.execute(new Runnable() {

            public void run() {
               for( int i = 0; i < my_thread_num; ++i) {
               int start_index = i * block_size;
               int end_index = start_index + block_size;
               if (i >= my_mid_id) {
                  start_index = my_mid_id * block_size + (i - my_mid_id) * (block_size + 1);
                  end_index = start_index + block_size + 1;
               }

               for(int j = start_index; j < end_index; ++j) {
                  String key = index[2 * j];
                  int id = Integer.parseInt(index[2 * j + 1]);
                  int[][] bucketbit = (int[][])CloudServiceProvider.this.BucketFromDOs.get(key);
                  BigInteger G = CloudServiceProvider.this.FE.FEDecryption(CloudServiceProvider.this.Encg, CloudServiceProvider.this.InttoBigInteger(bucketbit[id]), ((BigInteger[])CloudServiceProvider.this.KeyDer.get(key))[id]);
                  BigInteger H = CloudServiceProvider.this.FE.FEDecryption(CloudServiceProvider.this.Engh, CloudServiceProvider.this.InttoBigInteger(bucketbit[id]), ((BigInteger[])CloudServiceProvider.this.KeyDer.get(key))[id]);
                  G = (BigInteger)CloudServiceProvider.this.FE.Sites.get(G);
                  H = (BigInteger)CloudServiceProvider.this.FE.Sites.get(H);
                  BigDecimal GG = BigDecimal.valueOf(G.doubleValue() / (double)CloudServiceProvider.this.maxGH);
                  BigDecimal HH = BigDecimal.valueOf(H.doubleValue() / (double)CloudServiceProvider.this.maxGH);
                  BigDecimal[] tmp = (BigDecimal[])result.get(key);
                  tmp[2 * id] = GG;
                  tmp[2 * id + 1] = HH;
               }

               latch.countDown();
            }
         }});


      try {
         latch.await();
      } catch (InterruptedException var11) {
         var11.printStackTrace();
      }

      return result;
   }

   private LinkedHashMap<String, BigDecimal[]> caculateGandH(LinkedHashMap<String, BigDecimal[]> DecryResult) {
      LinkedHashMap<String, BigDecimal[]> GandHleft = new LinkedHashMap();
      Iterator var3 = this.BucketFromDOs.keySet().iterator();

      while(var3.hasNext()) {
         String key = (String)var3.next();
         BigDecimal[] GHfromOz = (BigDecimal[])this.DataOwnerGH.get(key);
         BigDecimal[] GHfromLocal = (BigDecimal[])DecryResult.get(key);
         BigDecimal[] GHleft = new BigDecimal[GHfromLocal.length];

         for(int i = 0; i < GHfromLocal.length / 2; ++i) {
            if (i == 0) {
               GHleft[2 * i] = this.GH[0].subtract(GHfromOz[2 * i]).subtract(GHfromLocal[2 * i]);
               GHleft[2 * i + 1] = this.GH[1].subtract(GHfromOz[2 * i + 1]).subtract(GHfromLocal[2 * i + 1]);
            } else {
               GHleft[2 * i] = GHleft[2 * (i - 1)].add(this.GH[0]).subtract(GHfromOz[2 * i]).subtract(GHfromLocal[2 * i]);
               GHleft[2 * i + 1] = GHleft[2 * (i - 1) + 1].add(this.GH[1]).subtract(GHfromOz[2 * i + 1]).subtract(GHfromLocal[2 * i + 1]);
            }
         }

         GandHleft.put(key, GHleft);
      }

      return GandHleft;
   }

   public String[] caculateDOsGain(LinkedHashMap<String, BigDecimal[]> GandHleft) {
      String[] MaxLocalGain = new String[]{this.OrganizedGain[0], this.OrganizedGain[1], this.OrganizedGain[2], this.OrganizedGain[3], this.OrganizedGain[4]};
      BigDecimal MaxGain = new BigDecimal(this.OrganizedGain[4]);
      Iterator var4 = GandHleft.keySet().iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         BigDecimal[] GHleft = (BigDecimal[])GandHleft.get(key);

         for(int i = 0; i < GHleft.length / 2; ++i) {
            BigDecimal Gain = this.XGBoost.caculateGain(GHleft[2 * i], GHleft[2 * i + 1], this.GH[0], this.GH[1]);
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

   private void splictNode(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, int depth, int nodeindex, Linkdict treedict) throws ClassNotFoundException, IOException {
      if (depth == this.XGBoost.MaxDeep) {
         System.out.println("达到最大深度");
      } else {
         ++depth;
         int flag = this.createSocketAndRecieveBucket(objectOutputStream, objectInputStream);
         if (flag == 1) {
            new LinkedHashMap();
            LinkedHashMap<String, BigDecimal[]> DecryResult = this.DecryptionBucket();
            new LinkedHashMap();
            LinkedHashMap<String, BigDecimal[]> GandHleft = this.caculateGandH(DecryResult);
            this.GlobalGain = this.caculateDOsGain(GandHleft);
            treedict.dict.put(nodeindex - 1, this.GlobalGain);
            this.createSocketAndsendGain(objectOutputStream, objectInputStream);
            this.splictNode(objectOutputStream, objectInputStream, depth, 2 * nodeindex + 1, treedict);
            this.splictNode(objectOutputStream, objectInputStream, depth, 2 * nodeindex, treedict);
         } else {
            System.out.println("不需下一次分裂");
         }
      }

   }

   private void trainTree(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
      this.FE.DiscreteTable(this.maxGH, this.XGBoost.trainDataNums);
      this.treedict = new Linkdict[this.XGBoost.IterNums];

      for(int i = 0; i < this.XGBoost.IterNums; ++i) {
         int depth = 1;
         int nodeindex = 1;
         this.treedict[i] = new Linkdict();
         this.splictNode(objectOutputStream, objectInputStream, depth, nodeindex, this.treedict[i]);
      }

   }

   private void createSocketAndTraintree(String URL, int port) throws IOException, ClassNotFoundException {
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
      this.trainTree(objectOutputStream, objectInputStream);
      this.treeLinks = new TreeLink[this.XGBoost.IterNums];
      this.treeLinks = (TreeLink[])objectInputStream.readObject();
      this.GlobalDataTags = (LinkedHashMap)objectInputStream.readObject();
      utils.closeSocketServer(socket);
      serverSocket.close();
   }

   private void createSocketAndUser(String URL, int port) throws IOException, ClassNotFoundException {
      ServerSocket serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(URL, port));
      System.out.println("Server:" + serverSocket.getLocalSocketAddress());
      Socket socket = serverSocket.accept();
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getInetAddress();
      var10000.println("连接建立：" + var10001 + ":" + socket.getLocalPort());
      new ObjectOutputStream(socket.getOutputStream());
      ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
      this.EncFeatureInformation = (LinkedHashMap)objectInputStream.readObject();
      this.SHE = (SHE)objectInputStream.readObject();
      this.serverNumber = (Integer)objectInputStream.readObject();
      utils.closeSocketServer(socket);
      serverSocket.close();
   }

   public BigInteger GenerateBigInteger(int lenth) {
      Random rnd = new Random();
      BigInteger a = new BigInteger(lenth, rnd);
      int b = Math.abs(a.intValue());
      BigInteger c = new BigInteger(String.valueOf(b));
      return c;
   }

   public int GenerateCoffe() {
      Random rnd = new Random();
      int r = rnd.nextInt(10);
      byte c;
      if (r < 5) {
         c = 1;
      } else {
         c = 0;
      }

      return c;
   }

   public void transientNode(TreeLink treenode, TreeLink localTreeNode, int index) {
      if (localTreeNode.rightNode == null && localTreeNode.leftNode == null) {
         treenode.weight = localTreeNode.weight;
      } else {
         BigInteger r0 = this.GenerateBigInteger(8);
         BigInteger r1 = this.GenerateBigInteger(6);
         treenode.coffe = this.GenerateCoffe();
         int fetureindex = Integer.valueOf(localTreeNode.feature[1]);
         String fetureName = localTreeNode.feature[0];
         BigInteger a = ((BigInteger[])this.EncFeatureInformation.get(this.GlobalDataTags.get(fetureName)))[index].add(((BigInteger[])this.EncBuckteSplictInformation.get(fetureName))[fetureindex]);
         if (treenode.coffe == 0) {
            treenode.nowInformation = a.multiply(r0).add(r1);
         } else {
            treenode.nowInformation = a.multiply(r0).multiply(this.coffe[1]).add(r1);
         }

         TreeLink leftTreeNode;
         if (localTreeNode.rightNode != null) {
            leftTreeNode = new TreeLink((int[])null, (TreeLink)null, (TreeLink)null);
            treenode.rightNode = leftTreeNode;
            this.transientNode(treenode.rightNode, localTreeNode.rightNode, index);
         }

         if (localTreeNode.leftNode != null) {
            leftTreeNode = new TreeLink((int[])null, (TreeLink)null, (TreeLink)null);
            treenode.leftNode = leftTreeNode;
            this.transientNode(treenode.leftNode, localTreeNode.leftNode, index);
         }
      }

   }

   private TreeLink[] pallEncSHE(int index) {
      TreeLink[] treeLinks = new TreeLink[this.XGBoost.IterNums];

      for(int i = 0; i < this.XGBoost.IterNums; ++i) {
         treeLinks[i] = new TreeLink((int[])null, (TreeLink)null, (TreeLink)null);
         this.transientNode(treeLinks[i], this.treeLinks[i], index);
      }

      return treeLinks;
   }

   private ConcurrentHashMap<Integer, TreeLink[]> SecureServer() {
      int nodenum = (int)Math.pow(2.0D, (double)(this.XGBoost.MaxDeep - 1)) - 1;

      for(int i = 0; i < this.XGBoost.IterNums; ++i) {
         this.treedict[i].coffeNumber(this.serverNumber, nodenum);
      }

      ConcurrentHashMap<Integer, TreeLink[]> AllServerInformation = new ConcurrentHashMap();

      for(int j = 0; j < this.serverNumber; ++j) {
         TreeLink[] treeLinks = this.pallEncSHE(j);
         AllServerInformation.put(j, treeLinks);
      }

      return AllServerInformation;
   }

   public void calculateTreeNodeInformation(TreeLink treeLink, BigInteger EncWeight, int j) {
      BigInteger ive;
      if (treeLink.rightNode == null && treeLink.leftNode == null) {
         if (treeLink.weight < 0.0D) {
            EncWeight = BigInteger.valueOf((new Double(-treeLink.weight * (double)this.maxGH)).longValue()).multiply(EncWeight).multiply(this.coffe[3]);
            ive = (BigInteger)this.AllEncFeatureInformation.get(j);
            this.AllEncFeatureInformation.replace(j, ive, ive.add(EncWeight));
         } else {
            EncWeight = BigInteger.valueOf((new Double(treeLink.weight * (double)this.maxGH)).longValue()).multiply(EncWeight);
            ive = (BigInteger)this.AllEncFeatureInformation.get(j);
            this.AllEncFeatureInformation.replace(j, ive, ive.add(EncWeight));
         }
      } else {
         if (treeLink.coffe == 1) {
            ive = treeLink.rightcoffe;
            treeLink.rightcoffe = treeLink.leftcoffe;
            treeLink.leftcoffe = ive;
         }

         if (treeLink.rightNode != null) {
            this.calculateTreeNodeInformation(treeLink.rightNode, EncWeight.multiply(treeLink.rightcoffe), j);
         }

         if (treeLink.leftNode != null) {
            this.calculateTreeNodeInformation(treeLink.leftNode, EncWeight.multiply(treeLink.leftcoffe), j);
         }
      }

   }

   private void calculateTreearray(TreeLink[] treeLinks, int j) {
      for(int i = 0; i < treeLinks.length; ++i) {
         this.calculateTreeNodeInformation(treeLinks[i], BigInteger.ONE, j);
      }

   }

   private void palcalculateTreearray(final ConcurrentHashMap<Integer, TreeLink[]> AllresposeInformation) {
      final int block_size = this.serverNumber / (this.thread_num - 1);
      ExecutorService executorService = Executors.newFixedThreadPool(this.thread_num);
      final CountDownLatch latch = new CountDownLatch(this.thread_num);
      this.AllEncFeatureInformation = new ConcurrentHashMap();

      final int my_thread_num = this.thread_num;
         executorService.execute(new Runnable() {
            public void run() {
               for( int i = 0; i < my_thread_num; ++i) {
               int start_id = i * block_size;
               int end_id = (i + 1) * block_size;
               if (i == CloudServiceProvider.this.thread_num - 1) {
                  end_id = CloudServiceProvider.this.serverNumber;
               }

               for(int j = start_id; j < end_id; ++j) {
                  CloudServiceProvider.this.AllEncFeatureInformation.put(j, BigInteger.ZERO);
                  TreeLink[] treeLinks = (TreeLink[])AllresposeInformation.get(j);
                  CloudServiceProvider.this.calculateTreearray(treeLinks, j);
               }

               latch.countDown();
            }
         }});


      try {
         latch.await();
      } catch (InterruptedException var7) {
         var7.printStackTrace();
      }

      executorService.shutdown();
   }

   private void sendANDrecieveServer(String URL, int port) throws IOException, ClassNotFoundException {
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
      this.coffe = new BigInteger[4];
      this.coffe = (BigInteger[])objectInputStream.readObject();
      ConcurrentHashMap<Integer, TreeLink[]> AllServerInformation = this.SecureServer();
      objectOutputStream.writeObject(AllServerInformation);
      new ConcurrentHashMap();
      ConcurrentHashMap<Integer, TreeLink[]> AllresposeInformation = (ConcurrentHashMap)objectInputStream.readObject();
      this.palcalculateTreearray(AllresposeInformation);
      utils.closeSocketClient(socket);
   }

   private void sendtoUser(String URL, int port) throws IOException, ClassNotFoundException {
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
      objectOutputStream.writeObject(this.AllEncFeatureInformation);
      objectOutputStream.flush();

      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
      sizeOutputStream.writeObject(this.AllEncFeatureInformation);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();

      utils.closeSocketClient(socket);
   }

   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
      CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
      cloudServiceProvider.systemInit();
      cloudServiceProvider.startServerAndRecieveParamsDataOwners("127.0.0.1", 2020);
      cloudServiceProvider.createSocketAndRecieveParam("localhost", 2021);
      cloudServiceProvider.createSocketAndTraintree("127.0.0.1", 2023);
      System.out.println("服务器训练通信开销：" + cloudServiceProvider.commnication_cost );
      cloudServiceProvider.commnication_cost = 0;
      cloudServiceProvider.createSocketAndUser("127.0.0.1", 2025);
      cloudServiceProvider.sendANDrecieveServer("127.0.0.1", 8084);
      cloudServiceProvider.sendtoUser("127.0.0.1", 2026);
      System.out.println("服务器查询通信开销：" + cloudServiceProvider.commnication_cost );
      cloudServiceProvider.commnication_cost = 0;
   }

   static {
      unit = TimeUnit.MILLISECONDS;
      workQueue = new LinkedBlockingQueue();
      threadFactory = Thread::new;
      threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, (long)keepAliveTime, unit, workQueue, threadFactory);
   }
}
