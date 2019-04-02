/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
