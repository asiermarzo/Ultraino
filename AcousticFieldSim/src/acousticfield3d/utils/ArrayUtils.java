package acousticfield3d.utils;

import java.util.List;

/**
 *
 * @author am14010
 */
public class ArrayUtils {
    public static int[] toArray(final List<Integer> list){
        final int n = list.size();
        final int[] array = new int[n];
        for(int i = 0; i < n; ++i){
            array[i] = list.get(i);
        }
        return array;
    }
}
