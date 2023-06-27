package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class utils {
   public static final BigDecimal SCALE = new BigDecimal("1000");

   public static BigInteger doubleToScaledInteger(double d, BigDecimal scale) {
      return new BigInteger(String.valueOf((int)(d * (double)scale.intValue())));
   }

   public static double scaledIntegerToDouble(BigInteger bi, BigDecimal scale) {
      return (new BigDecimal(bi)).divide(scale).doubleValue();
   }

   public static void closeSocketClient(Socket socket) throws IOException {
      socket.shutdownInput();
      socket.shutdownOutput();
      socket.close();
   }

   public static void closeSocketServer(Socket socket) throws IOException {
      InputStream inputStream = socket.getInputStream();
      byte[] buf = new byte[1024];

      while(inputStream.read(buf) != -1) {
         System.out.println(new String(buf));
      }

      socket.shutdownInput();
      socket.shutdownOutput();
      socket.close();
      System.out.println("关闭连接:" + socket.getPort());
   }

   public static List<Double> countNumber(double[] data) {
      Set<Double> set = new TreeSet();

      for(int i = 0; i < data.length; ++i) {
         set.add(data[i]);
      }

      List<Double> list = new ArrayList(set);
      return list;
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

   public static Map<String[], double[][]> readDataFromFile(int dataNums, int dataDims, String pathName) {
      double[][] dataSets = new double[dataNums][dataDims];
      String[] dataTags = new String[dataDims];
      Map<String[], double[][]> map = new HashMap();
      File csv = new File(pathName);

      try {
         BufferedReader textFile = new BufferedReader(new FileReader(csv));
         String lineDta = "";
         lineDta = textFile.readLine();
         dataTags = lineDta.split(",");

         for(int index = 0; index < dataNums; ++index) {
            lineDta = textFile.readLine();
            String[] arr = lineDta.split(",");

            for(int j = 0; j < dataDims; ++j) {
               dataSets[index][j] = Double.parseDouble(arr[j]);
            }
         }
      } catch (FileNotFoundException var12) {
         System.out.println("没有找到指定文件");
      } catch (IOException var13) {
         System.out.println("文件读写出错");
      }

      map.put(dataTags, dataSets);
      return map;
   }
}
