package me.mrdev.test;

public class Example {

    public static void main(String[] args) {
        ServerStatus status = new ServerStatus.Builder()
                .hostname("mc.hypixel.net")
                .build();
        System.out.println(status.getStatus()); //returns a JSON formatted string
    }

}
