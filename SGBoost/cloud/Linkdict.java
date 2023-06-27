package cloud;

import java.util.LinkedHashMap;

class Linkdict {
   LinkedHashMap<Integer, String[]> dict = new LinkedHashMap();
   int[][] coffe;

   public Linkdict() {
   }

   public void coffeNumber(int serverNumber, int nodenum) {
      this.coffe = new int[serverNumber][nodenum];
   }
}
