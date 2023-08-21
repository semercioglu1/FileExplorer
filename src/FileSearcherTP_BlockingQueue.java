import java.io.File;
import java.util.HashSet;
import java.util.concurrent.*;

public class FileSearcherTP_BlockingQueue extends FileSearcher{

    BlockingQueue<File> jobs;
    ConcurrentMap<File,Boolean> visitedFiles;
    ConcurrentMap<File,Boolean> visitedFolders;
    String fileName;

    FileSearcherTP_BlockingQueue(String fileName, BlockingQueue<File> jobs ,
                                 ConcurrentMap<File,Boolean> visitedFiles, ConcurrentMap<File,Boolean> visitedFolders){
        this.fileName = fileName;
        this.jobs = jobs;
        this.visitedFiles = visitedFiles;
        this.visitedFolders = visitedFolders;
    }

    @Override
    public void run() {

        while (!Main.shutdown.get()){
            System.out.println(this.threadId() +" filename: "+ fileName);
            File file = jobs.poll();
            if (file == null){
                continue;
            }
            File[] files = file.listFiles();
            if (files != null) {
                for (File foundFile : files) {
                    String name = foundFile.getName();
                    System.out.println("!Check BLOCKING QUEUE"); //Console debug
                    if (foundFile.isFile() && visitedFiles.putIfAbsent(foundFile,Boolean.TRUE) == null) {
                        if (name.equals(fileName)){
                            Main.solution = foundFile.getAbsolutePath();
                            Main.shutdown.set(true);
                            return;
                        }
                    } else if (foundFile.isDirectory() && visitedFolders.putIfAbsent(foundFile,Boolean.TRUE) == null) {
                        jobs.add(foundFile);
                    }
                }
            }
        }
    }
}