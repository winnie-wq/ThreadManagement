package util;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {

    private static AtomicInteger id=
            new AtomicInteger(1);

    public static int next(){

        return id.getAndIncrement();

    }

}