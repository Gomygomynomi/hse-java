package hse.java.lectures.lecture2;

public abstract class Animal {

    private final String name;

    public Animal(String name) {
        this.name = name;
        System.out.println("Animal" + this);
    }

    public String getName() {
        return name;
    }

    public abstract void say();
}
