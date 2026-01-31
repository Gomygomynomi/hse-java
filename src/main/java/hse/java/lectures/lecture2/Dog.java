package hse.java.lectures.lecture2;

public class Dog extends Animal implements Say {

    public Dog(String name) {
        super(name);
    }

    @Override
    public void say() {
        System.out.println("WOW");
    }
}
