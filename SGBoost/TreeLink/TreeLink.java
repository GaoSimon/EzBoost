package TreeLink;

import java.io.Serializable;
import java.math.BigInteger;

public class TreeLink implements Serializable {
   public int[] data;
   public TreeLink leftNode;
   public TreeLink rightNode;
   public String[] feature = new String[5];
   public double weight;
   public int coffe;
   public BigInteger nowInformation;
   public BigInteger rightcoffe;
   public BigInteger leftcoffe;

   public TreeLink(int[] data, TreeLink leftNode, TreeLink rightNode) {
      this.data = data;
      this.leftNode = leftNode;
      this.rightNode = rightNode;
   }

   public String toString() {
      return "TreeLink [data=" + this.data + "\n, leftNode=" + this.leftNode + "\n, rightNode=" + this.rightNode + "\n, feature=" + this.feature + "\n, weight=" + this.weight + "]";
   }
}
