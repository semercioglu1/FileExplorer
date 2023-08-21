import java.io.File;
import java.util.concurrent.*;

public class FileSearcherTP_WorkStealing extends FileSearcher {

    String fileName;
    LinkedBlockingDeque<File> queuePrev;
    LinkedBlockingDeque<File> queue; //Current Job of this Queue
    LinkedBlockingDeque<File> queueNext;
    ConcurrentMap<File,Boolean> visitedFiles;
    ConcurrentMap<File,Boolean> visitedFolders;

    public FileSearcherTP_WorkStealing(String fileName,LinkedBlockingDeque<File> queue,
                                       LinkedBlockingDeque<File> queuePrev, LinkedBlockingDeque<File> queueNext,
                                       ConcurrentMap<File,Boolean> visitedFiles,ConcurrentMap<File,Boolean> visitedFolders){
        this.fileName = fileName;
        this.queue = queue;
        this.queuePrev = queuePrev;
        this.queueNext = queueNext;
        this.visitedFiles = visitedFiles;
        this.visitedFolders = visitedFolders;
    }

    @Override
    public void run() {
        while (!Main.shutdown.get()) {
            System.out.println("Thread " + this.threadId() + " filename: "+ fileName); //just for Debug
            File file = null;
                file = queue.poll();
            try {
                if (file == null) {
                    file = queuePrev.pollLast();
                    if (file == null) {
                        file = queueNext.pollLast();
                    }
                }
            }catch (Exception e){
                break;
            }

            if (file == null){
                try {
                    Thread.sleep(10); //Avoiding Busy-Waiting maybe?
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }

            //up is just mess, needs to be cleaned

            File[] files = file.listFiles();

            if (files != null) {
                for (File foundFile : files) {
                    String name = foundFile.getName();
                    System.out.println("Check ! WORK STEALING"); // Just for Debug
                    if (foundFile.isFile() && visitedFiles.putIfAbsent(foundFile,Boolean.TRUE) == null) {
                        if (name.equals(fileName)) {
                            Main.solution = foundFile.getAbsolutePath();
                            Thread.currentThread().interrupt();
                            Main.shutdown.set(true);
                            visitedFolders =  new ConcurrentHashMap<>();
                            break;
                        }
                    } else if (foundFile.isDirectory() &&
                    visitedFolders.putIfAbsent(foundFile,Boolean.TRUE) == null) {
                        queue.add(foundFile);
                    }
                }
            }
        }
    }
}
