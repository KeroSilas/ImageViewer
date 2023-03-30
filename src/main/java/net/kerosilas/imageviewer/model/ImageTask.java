package net.kerosilas.imageviewer.model;

import javafx.concurrent.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageTask extends Task<Boolean> {

    private final List<File> files;
    private final int numThreads;

    public ImageTask(List<File> files, int numThreads) {
        this.files = files;
        this.numThreads = numThreads;
    }

    @Override
    public Boolean call() throws Exception {
        boolean success = true;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Boolean>> futures = new ArrayList<>();

        int sublistSize = (int) Math.ceil(files.size() / (double) numThreads);
        List<List<File>> sublists = new ArrayList<>();
        for (int i = 0; i < files.size(); i += sublistSize) {
            sublists.add(files.subList(i, Math.min(i + sublistSize, files.size())));
        }

        for (List<File> sublist : sublists) {
            futures.add(executor.submit(new SubTask(sublist)));
        }

        for (Future<Boolean> future : futures) {
            success = success && future.get();
        }
        executor.shutdown();
        return success;
    }
}

// A subtask that handles a sublist of files and adds them to the ImageManager
class SubTask implements Callable<Boolean> {

    private final List<File> files;
    private final ImageManager imageManager;

    public SubTask(List<File> files) {
        this.files = files;
        imageManager = ImageManager.getInstance();
    }

    @Override
    public Boolean call() {
        boolean success = true;
        for (File file : files) {
            success = success && imageManager.addImage(file);
        }
        return success;
    }
}
