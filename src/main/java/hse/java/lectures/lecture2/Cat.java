package hse.java.lectures.lecture2;

public class Cat implements Say {

    private final String name;

    public Cat(String name) {
        this.name = name;
    }
    // modificators private protected public default (package private)
    // private only in class
    // protected = in class + extends + package
    // public - open


    public String getName() {
        return name;
    }

    @Override
    public void say() {
        System.out.println("MEOW");
    }

}
