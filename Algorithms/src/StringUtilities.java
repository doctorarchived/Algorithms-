import java.util.Arrays;
import java.util.stream.IntStream;

public class StringUtilities {
	
	private static boolean leq(int a1, int a2, int b1, int b2) { 
		return(a1 < b1 || a1 == b1 && a2 <= b2); 
	}
	
	private StringUtilities() { }
	
	private static boolean leq(int a1, int a2, int a3, int b1, int b2, int b3) { 
		return(a1 < b1 || a1 == b1 && leq(a2,a3, b2,b3)); 
	}
	
	public static int[] suffixArray(String s) {
		//s = s.toLowerCase();
		if (s.chars().allMatch(x -> Character.isLetterOrDigit(x))) {
			//return suffixArray(s.chars().map(c -> (int) (c - 'a') + 1).toArray());
			int[] ar = (s.chars().map(c -> Character.isDigit(c) ? (int) (c - '0' + 1)  :
				(Character.isUpperCase(c) ? (int) (c - 'A' + 11) : (int) (c - 'a' + 37))).toArray());
			
			return suffixArray(ar);
			
		} else {
			return null;
		}
	}
	
	public static int[] z_function(String s) {
	    int n = s.length();
	    int[] z = new int[n];
	    for (int i = 1, l = 0, r = 0; i < n; ++i) {
	        if (i <= r) {
	            z[i] = Math.min (r - i + 1, z[i - l]);
	        }
	        while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
	            ++z[i];
	        }
	        if (i + z[i] - 1 > r) {
	            l = i;
	        	r = i + z[i] - 1;
	        }
	    }
	    return z;
	}
	
	public static int[] lcp(String s, int[] sa) {
		int n = sa.length;
		int[] rank = new int[n], lcp = new int[n];

		for (int i = 0; i < rank.length; i++) {
			rank[sa[i]] = i;
		}
		
		for (int i = 0, k = 0; i < n; i++, k = (k > 0 ? --k : 0)) {
			if(rank[i]==n-1) {k=0; continue;}
	        int j=sa[rank[i]+1];
	        while(i+k<n && j+k<n && s.charAt(i+k)==s.charAt(j+k)) k++;
	        lcp[rank[i]]=k;
		}
		
		return lcp;
	}
	
	public static int[] suffixArray(int[] word) {
		
		final int alphabetSize = 10 + 26 + 26;
		
		//We need to iterate over the 1 and 2 mod3 indices later
		int[] pos12 = IntStream.range(0, word.length).filter(a -> a % 3 != 0).toArray(); 
		
		//Useful to keep track of the number of 0mod3, 1mod3, 2mod3 indices
		int n = word.length;
		int n0 = (n + 2) / 3, n1 = (n + 1) / 3, n2 = n / 3, n12 = n1 + n2; 

		//We append two 0s to easily compare triples that begin at length-1 and length-2.
		word = Arrays.copyOf(word, n + 2);
		int[] triples12 = lsdRadixSort(pos12, word, alphabetSize, 3);
		
		//Suffix array for full word, one for indices 1/2 mod 3 and one for 0 mod 3. r12 is inverse of sa12
		int[] sa = new int[n];
		int[] r12 = new int[n12 + 1], sa12 = new int[n12 + 1], sa0 = new int[n0];
		int name = 0, c0 = -1, c1 = -1, c2 = -1;
		
		for (int i = 0; i < n12; i++) {
			if (word[triples12[i]] != c0 || word[triples12[i]+1] != c1 || word[triples12[i]+2] != c2) { 
				name++; 
				c0 = word[triples12[i]]; 
				c1 = word[triples12[i]+1]; 
				c2 = word[triples12[i]+2]; 
			}
			if (triples12[i] % 3 == 1) { 
				r12[triples12[i]/3] = name; 
			} else { 
				r12[triples12[i]/3 + n1] = name; 
			}
		}
	
		if (name < triples12.length) {
			sa12 = suffixArray(Arrays.copyOf(r12, n12));
			// store unique names in R using the suffix array
			for (int i = 0; i < n12; i++) {
				r12[sa12[i]] = i + 1;
			}
		} else { // generate the suffix array of R directly
			for (int i = 0; i < n12; i++) {
				sa12[r12[i] - 1] = i;
			}
		}

		int[] w0r12 = new int[n0 * 2];
		for (int i = 0; i < n0; i++) {
			sa0[i] = 2*i;
			w0r12[2*i] = word[3*i];
			w0r12[2*i + 1] = r12[i];
		}
		
		sa0 = lsdRadixSort(sa0, w0r12, alphabetSize, 2);
		for (int i = 0; i < n0; i++) {
			sa0[i] = sa0[i] / 2;
		}

		boolean sa0sa12;
		for (int i = 0, j = 0, k = 0; i + j < n; ) {
			k = (sa12[j] % n0) * 3 + (sa12[j] / n0) + 1;
			if (j == n12 || i == n0) {
				sa0sa12 = (j == n12);
			} else {
				if (sa12[j] < n1) {
					sa0sa12 = (leq(word[3*sa0[i]], r12[sa0[i]], word[k], r12[sa12[j] + n1])) ;
				} else {
					sa0sa12 = (leq(word[3*sa0[i]], word[3*sa0[i] + 1], r12[sa0[i] + n1], word[k], word[k+1], r12[sa12[j] - n1 + 1]));
				}
			}
			if (sa0sa12) {
				sa[i+j] = 3*sa0[i];
				i++;
			} else {
				sa[i+j] = k;
				j++;
			}
		}
		return sa;
	}
	
	// and triples
	// stably sort a[0..n-1] to b[0..n-1] with keys in 0..K from r
	private static int[] lsdRadixSort(int[] in, int[] keys, int numKeys, int radix) { // count occurrences
		int[] out = null;
		for (int w = radix - 1; w >= 0; w--) {
			out = new int[in.length];
			int[] c = new int[numKeys + 1]; // counter array
			for (int i = 0; i < in.length; i++) {
				c[keys[in[i] + w]]++; // count occurrences
			}
			for (int i = 0, sum = 0; i <= numKeys; i++)  { // exclusive prefix sums
				int t = c[i]; 
				c[i] = sum; 
				sum += t; 
			}
			for (int i = 0; i < in.length; i++) { 
				out[c[keys[in[i] + w]]++] = in[i]; // sort
			}
			//System.out.println("PASS " + Arrays.toString(out));
			in = out;
		}
		return out;
	}

}
