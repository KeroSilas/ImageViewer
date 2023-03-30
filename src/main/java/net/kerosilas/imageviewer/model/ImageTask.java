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
        ImageManager manager = ImageManager.getInstance();
        boolean success = true;
        int count = 0;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        // create a list of futures to store the subtask results
        List<Future<Boolean>> futures = new ArrayList<>();
        // divide the files list into sublists of equal size
        int sublistSize = (int) Math.ceil(files.size() / (double) numThreads);
        List<List<File>> sublists = new ArrayList<>();
        for (int i = 0; i < files.size(); i += sublistSize) {
            sublists.add(files.subList(i, Math.min(i + sublistSize, files.size())));
        }
        // submit a subtask for each sublist
        for (List<File> sublist : sublists) {
            futures.add(executor.submit(new SubTask(sublist, manager)));
        }
        // wait for all subtasks to finish and update progress
        for (Future<Boolean> future : futures) {
            success = success && future.get(); // get the result of the subtask
            count += sublistSize; // increment the count by the sublist size
            updateProgress(count, files.size()); // update progress
        }
        // shutdown the executor
        executor.shutdown();
        return success;
    }
}

// A subtask that handles a sublist of files and adds them to the image manager
class SubTask implements Callable<Boolean> {

    private final List<File> files;
    private final ImageManager imageManager;

    public SubTask(List<File> files, ImageManager imageManager) {
        this.files = files;
        this.imageManager = imageManager;
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
