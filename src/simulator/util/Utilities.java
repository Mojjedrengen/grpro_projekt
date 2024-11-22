package simulator.util;

import java.util.ArrayList;

public class Utilities {

    public static <T> T getLast(ArrayList<T> array) {
        return array.get(array.size() - 1);
    }

}
