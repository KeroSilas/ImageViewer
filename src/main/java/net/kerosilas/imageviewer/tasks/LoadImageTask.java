package net.kerosilas.imageviewer.tasks;

import javafx.concurrent.Task;
import net.kerosilas.imageviewer.model.ImageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadImageTask extends Task<Boolean> {

    private final List<File> files;
    private final int numThreads;
    private final ImageManager imageManager;

    public LoadImageTask(List<File> files, int numThreads) {
        this.files = files;
        this.numThreads = numThreads;
        imageManager = ImageManager.getInstance();
    }

    @Override
    public Boolean call() {
        boolean success = true;

        int sublistSize = (int) Math.ceil(files.size() / (double) numThreads);
        List<List<File>> sublists = new ArrayList<>();
        for (int i = 0; i < files.size(); i += sublistSize) {
            sublists.add(files.subList(i, Math.min(i + sublistSize, files.size())));
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(sublists.size());

        for (List<File> sublist : sublists) {
            executor.submit(() -> {
                for (File file : sublist) {
                    imageManager.addImage(file);
                }
                latch.countDown();
            });
        }
        executor.shutdown();

        try {
            latch.await();
        } catch (InterruptedException e) {
            success = false;
        }

        return success;
    }
}
