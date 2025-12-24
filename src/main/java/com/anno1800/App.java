package com.anno1800;

public class App {
    private final String name;

    public App(String name) {
        this.name = name;
    }

    public String greet() {
        return "Hello from Java, " + name + "!";
    }

    public static void main(String[] args) {
        App example = new App("Anno 1800");
        System.out.println(example.greet());
    }
}
