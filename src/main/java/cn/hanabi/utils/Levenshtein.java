package cn.hanabi.utils;

public class Levenshtein {
   public Levenshtein() {
      super();
   }

   private int compare(String str, String target) {
      int n = str.length();
      int m = target.length();
      if (n == 0) {
         return m;
      } else if (m == 0) {
         return n;
      } else {
         int[][] d = new int[n + 1][m + 1];

         for(int i = 0; i <= n; d[i][0] = i++) {
            ;
         }

         for(int j = 0; j <= m; d[0][j] = j++) {
            ;
         }

         for(int var11 = 1; var11 <= n; ++var11) {
            char ch1 = str.charAt(var11 - 1);

            for(int var12 = 1; var12 <= m; ++var12) {
               char ch2 = target.charAt(var12 - 1);
               int temp;
               if (ch1 == ch2) {
                  temp = 0;
               } else {
                  temp = 1;
               }

               d[var11][var12] = this.min(d[var11 - 1][var12] + 1, d[var11][var12 - 1] + 1, d[var11 - 1][var12 - 1] + temp);
            }
         }

         return d[n][m];
      }
   }

   private int min(int one, int two, int three) {
      int var4;
      return (var4 = Math.min(one, two)) < three ? var4 : three;
   }

   public float getSimilarityRatio(String str, String target) {
      return 1.0F - (float)this.compare(str, target) / (float)Math.max(str.length(), target.length());
   }
}
