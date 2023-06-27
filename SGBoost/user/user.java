package user;

import SHE.SHE;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import utils.utils;
import com.google.gson.Gson;

public class user {
   private SHE SHE;
   private BigInteger[] r;
   private LinkedHashMap<String, BigInteger[]> EncFeatureInformation;
   private BigInteger[][] resposething;
   private double[][] treeweight;
   private String[] localDataTags;
   private double[][] rawData;
   private double[] localDataY;
   private int dataNumber;
   private int DataDims;
   private int maxGH = 100;
   public int thread_num = 8;
   public long commnication_cost = 0;
   public String json;
   private void RecieveSHE(String URL, int port) throws IOException, ClassNotFoundException {
      Socket socket = new Socket(URL, port);
      ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
      new ObjectOutputStream(socket.getOutputStream());
      System.out.println("已发送服务器连接");
      PrintStream var10000 = System.out;
      InetAddress var10001 = socket.getLocalAddress();
      var10000.println("客户端:" + var10001 + " Port:" + socket.getLocalPort());
      var10000 = System.out;
      var10001 = socket.getInetAddress();
      var10000.println("服务器:" + var10001 + " Port:" + socket.getLocalPort());
      this.SHE = (SHE)objectInputStream.readObject();
      this.r = new BigInteger[2];
      this.r = (BigInteger[])objectInputStream.readObject();
      utils.closeSocketClient(socket);
   }

   private void SendEncInformation(String URL, int port) throws IOException, ClassNotFoundException {
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
      objectOutputStream.writeObject(this.EncFeatureInformation);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.SHE);
      objectOutputStream.flush();
      objectOutputStream.writeObject(this.dataNumber);
      objectOutputStream.flush();

      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream sizeOutputStream = new ObjectOutputStream(byteOutputStream);
      sizeOutputStream.writeObject(this.EncFeatureInformation);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      sizeOutputStream.writeObject(this.SHE);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();
      sizeOutputStream.writeObject(this.dataNumber);
      sizeOutputStream.flush();
      this.commnication_cost = this.commnication_cost + byteOutputStream.size();

      utils.closeSocketClient(socket);
   }

   private ConcurrentHashMap<Integer, BigInteger> RecieveEncInformation(String URL, int port) throws IOException, ClassNotFoundException {
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
      ConcurrentHashMap<Integer, BigInteger> AllEncFeatureInformation = (ConcurrentHashMap)objectInputStream.readObject();
      utils.closeSocketClient(socket);
      serverSocket.close();
      return AllEncFeatureInformation;
   }

   public BigInteger GenerateBigInteger(int lenth) {
      Random rnd = new Random();
      BigInteger a = new BigInteger(lenth, rnd);
      int b = Math.abs(a.intValue());
      BigInteger c = new BigInteger(String.valueOf(b));
      return c;
   }

   private void SHEEncryption() {
      this.EncFeatureInformation = new LinkedHashMap();

      for(int i = 0; i < this.DataDims - 1; ++i) {
         BigInteger[] a = new BigInteger[this.dataNumber];

         for(int j = 0; j < this.dataNumber; ++j) {
            BigInteger r0 = this.GenerateBigInteger(1024);
            BigInteger r1 = this.GenerateBigInteger(1024);
            a[j] = BigInteger.valueOf((new Double(this.rawData[j][i] * (double)this.maxGH)).longValue()).add(r0.multiply(this.r[0])).add(r1.multiply(this.r[1]));
         }

         this.EncFeatureInformation.put(this.localDataTags[i], a);
      }

   }

   private void readData(String pathName, int dataNumber, int DataDims) throws IOException {
      this.dataNumber = dataNumber;
      this.DataDims = DataDims;
      Map<String[], double[][]> map = utils.readDataFromFile(dataNumber, DataDims, pathName);
      this.localDataTags = (String[])map.keySet().iterator().next();
      this.rawData = (double[][])((Entry)map.entrySet().iterator().next()).getValue();
      this.localDataY = new double[dataNumber];

      for(int i = 0; i < this.dataNumber; ++i) {
         this.localDataY[i] = this.rawData[i][this.DataDims - 1];
      }

   }

   public static void main(String[] args) throws ClassNotFoundException, IOException {
      user user = new user();
      user.RecieveSHE("127.0.0.1", 2024);
      user.readData("/home/cinwa/SGBoost-main/sources/Credit-4/test", 1000, 24);
      long startTime = System.currentTimeMillis();
      user.SHEEncryption();
      user.SendEncInformation("127.0.0.1", 2025);
      ConcurrentHashMap<Integer, BigInteger> AllEncFeatureInformation = user.RecieveEncInformation("127.0.0.1", 2026);
      long endTime = System.currentTimeMillis();
      System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
      System.out.println("用户查询通信开销：" + user.commnication_cost  );
      user.commnication_cost = 0;
      double accuracy = 0.0D;

      for(int i = 0; i < AllEncFeatureInformation.size(); ++i) {
         double weight = user.SHE.SHEDecryption((BigInteger)AllEncFeatureInformation.get(i)).doubleValue();
         int y_pred = (int)Math.round(1.0D / (1.0D + Math.exp(-weight)));
         if ((int)user.localDataY[i] == y_pred) {
            ++accuracy;
         }
      }

      double var10001 = accuracy / (double)user.dataNumber;
      System.out.println("准确度:" + var10001);
   }
}
