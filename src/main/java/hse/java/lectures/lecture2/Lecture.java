package hse.java.lectures.lecture2;

import java.math.BigInteger;
import java.util.Arrays;

public class Lecture {
    public static void main(String[] args) {
        // ints
        // back cmd + [ forward cmd + ]
        // cmd + e
        byte b = 127; // 1b
        b++;
        System.out.println(b);
        short s = (1 << 15) - 1; // 2b
        int x = 1; // 4b
        long l = (1L << 63) - 1;
        BigInteger bi = new BigInteger("1");
        System.out.println(Integer.toBinaryString(127));
        // System.out.printf("%", 127);
        // literals
        // dec 10, 15, 1000
        // oct 017
        // bin 0b001010101
        // hex 0xff
        // floats
        float f = 0.1f;
        double d = 0.3;
        char c = 'a';
        System.out.println((int)c);
        boolean boo = true;

        // if
//        if (cond) {
//
//        } else if(cond1) {
//
//        } else {
//
//        }
//        int a = 3, aa = 5;
//        System.out.println(a > aa ? a : aa);
        //cycles
        // fori, while, foreach
        for (int i = 0; i < 10; i++) {

        }

        int[] a = new int[] {1,2,3};
        for (int value : a) {

        }

        // arrays
        int[][] array = new int[10][];
        // Arrays.fill(array, 0);
        for (int i = 0; i < array.length; i++) {
            // array[i] = new int[]
        }

//        Animal cat = new Cat("Vaska");
//        cat.say();
//
//        Animal dog = new Dog("Spike");
//        dog.say();
        say(new Cat("Vaska"));
        say(new Dog("Spike"));
    }

    static void say(Say say) {
        say.say();
    }
}
