import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class Main {

    static String solution = "The file does not exists"; //if no file was found
    private BlockingQueue<File> jobs;
    private LinkedBlockingDeque<File>[] workSteal;
    private ExecutorService exc;
    private ForkJoinPool fjp;
    static private ConcurrentHashMap<File,Boolean> memoizationVisitedFiles = new ConcurrentHashMap<>();
    static private ConcurrentHashMap<File,Boolean> memoizationVisitedFolders = new ConcurrentHashMap<>();
    static AtomicBoolean shutdown = new AtomicBoolean(false);
    private FileSearcher[] workers;
    private JTextField fileNameTextField;
    private JComboBox<String> modeComboBox;
    private JSpinner coresSpinner;
    private JLabel elapsedTimeLabel;
    private JLabel foundInLabel;
    static Queue<File> indexFiles = new LinkedBlockingQueue<>();
    static ConcurrentMap<String,String> files = new ConcurrentHashMap<>();;

    static String osName = System.getProperty("os.name"); //Get the OS Info. in Compile time

    static File[] startDirs;
    //static CyclicBarrier barrier = new CyclicBarrier(3+1);

    static{
        if (osName.startsWith("Windows")){
            startDirs = new File[]{new File("C:/"), new File("D:/"),
                    new File("E:/")};
        }
        else startDirs = new File[]{new File("/")}; //I hope this works for Mac and Linux, not tested
    }

    /*
    static{
        FileTraverser[] arr = new FileTraverser[startDirs.length];
        for (int i = 0; i < arr.length; i++){
            arr[i] = new FileTraverser(startDirs[i].toPath());
            arr[i].start();
        }

    }*/

    public Main(JTextField fileNameTextField, JComboBox<String> modeComboBox, JSpinner coresSpinner,
                JLabel elapsedTimeLabel, JLabel foundInLabel) {
        this.fileNameTextField = fileNameTextField;
        this.modeComboBox = modeComboBox;
        this.coresSpinner = coresSpinner;
        this.elapsedTimeLabel = elapsedTimeLabel;
        this.foundInLabel = foundInLabel;
    }

    public void execute() throws InterruptedException, ExecutionException, IOException, BrokenBarrierException {
        String fileName = fileNameTextField.getText();
        int mode = modeComboBox.getSelectedIndex();
        int cores = (int) coresSpinner.getValue();

        workers = new FileSearcher[cores];
        long start = 0;

        long elapsedTime;

        switch (mode){
            case 0:
                if (!GUI.isCached){
                    JOptionPane.showMessageDialog(new JFrame(), "Error ! You need to " +
                                    "cache the files first for Indexing", "Dialog",
                            JOptionPane.ERROR_MESSAGE);
                }
                else{
                    start = System.currentTimeMillis();
                    solution = files.get(fileName) != null ? files.get(fileName) : solution;
                    elapsedTime = System.currentTimeMillis() - start;

                    foundInLabel.setText("<html>File found in: <a href=\"\">" + solution + "</a></html> "+"["+modeComboBox.getItemAt(mode)+"]");
                    elapsedTimeLabel.setText("Elapsed Time: " + elapsedTime + " ms ["+modeComboBox.getItemAt(mode)+"]");
                }
                break;
            case 1:
                jobs = new LinkedBlockingQueue<>();
                start = System.currentTimeMillis();
                for (int i = 0; i < cores; i++){
                    jobs.add(startDirs[i % startDirs.length]);
                    workers[i] = new FileSearcherTP_BlockingQueue(fileName,jobs,memoizationVisitedFiles,memoizationVisitedFolders);
                    workers[i].start();
                }
                while (!shutdown.get()){ }
                elapsedTime = System.currentTimeMillis() - start;

                foundInLabel.setText("<html>File found in: <a href=\"\">" + solution + "</a></html> "+"["+modeComboBox.getItemAt(mode)+"]");
                elapsedTimeLabel.setText("Elapsed Time: " + elapsedTime + " ms ["+modeComboBox.getItemAt(mode)+"]");
                memoizationVisitedFiles = new ConcurrentHashMap<>();
                memoizationVisitedFolders = new ConcurrentHashMap<>();
                jobs.clear();
                break;

            case 2:
                workSteal = new LinkedBlockingDeque[cores];
                for (int i = 0; i < cores; i++){
                    workSteal[i] = new LinkedBlockingDeque<>();
                }
                for (int i = 0; i<cores; i++){
                    workSteal[i].add(startDirs[i % startDirs.length]);
                    workers[i] = new FileSearcherTP_WorkStealing(fileName,i == 0 ? workSteal[cores-1] : workSteal[i - 1],
                            workSteal[i],i+1 == cores ? workSteal[0] : workSteal[i+1],memoizationVisitedFiles,memoizationVisitedFolders);
                    workers[i].start();
                }
                start = System.currentTimeMillis();
                while (!shutdown.get()){ }
                elapsedTime = System.currentTimeMillis() - start;

                foundInLabel.setText("<html>File found in: <a href=\"\">" + solution + "</a></html> "+"["+modeComboBox.getItemAt(mode)+"]");
                elapsedTimeLabel.setText("Elapsed Time: " + elapsedTime + " ms ["+modeComboBox.getItemAt(mode)+"]");
                memoizationVisitedFiles = new ConcurrentHashMap<>();
                memoizationVisitedFolders = new ConcurrentHashMap<>();
                break;
            default:
                //TODO More Modes, More test
                System.out.println("TODO : MORE MODES TO IMPLEMENT");
                break;

        }

        for (int i = 0; i< cores; i++){
            if (workers[i] == null) continue;
            workers[i].join();
        }
        shutdown.set(false);
        solution = "The file does not exists";
    }
}