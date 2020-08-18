package fi.neskola.kartta.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO: change to Executors.newFixedThreadPool(NUMBER_OF_THREADS)?
public class Executor {
    public static void execute(Runnable t) {
        ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();
        IO_EXECUTOR.execute(t);
    }
}
