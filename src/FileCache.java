import javax.swing.*;

public class FileCache {
    public static void cache(){
        FileTraverser[] arr = new FileTraverser[Main.startDirs.length];
        for (int i = 0; i < arr.length; i++){
            arr[i] = new FileTraverser(Main.startDirs[i].toPath());
            arr[i].start();
        }

        for (int i = 0; i < arr.length; i++){
            try {
                arr[i].join();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(new JFrame(), "Error Encountered While Caching System Files", "Dialog",
                        JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
        JOptionPane.showMessageDialog(new JFrame(), "Files are cached successfully !", "Dialog",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
