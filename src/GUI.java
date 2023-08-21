import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;

/*
    THIS IS FULL OF COPY PASTA BUT ANYWAY
 */
public class GUI extends JFrame {

    private JTextField fileNameTextField;
    private JComboBox<String> modeComboBox;
    private JSpinner coresSpinner;
    private static JButton searchButton = new JButton("Search");
    //private JButton cacheButton;
    private JLabel elapsedTimeLabel;
    private JLabel foundInLabel;
    static Main ref;
    static boolean shut = false;

    static boolean isCached = false;


    public GUI() {
        super("File Searcher");

        // Create components
        fileNameTextField = new JTextField(20);
        modeComboBox = new JComboBox<>(new String[]{
                "Indexing",
                "Task Parallelism - BlockingQueue [Stable]",
                "Task Parallelism - Work-Stealing [Not Stable]"
        });

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        SpinnerModel coresSpinnerModel = new SpinnerNumberModel(availableProcessors, 1, availableProcessors, 1);
        coresSpinner = new JSpinner(coresSpinnerModel);

        searchButton = new JButton("Search");
        elapsedTimeLabel = new JLabel("Elapsed Time: ");
        foundInLabel = new JLabel("File found in: ");
        JLabel coresHintLabel = new JLabel("<html> Hint: The program will automatically detect and set the number <br> of cores based on your CPU's core  count. Please note that <br> using all available cores on certain CPUs " +
                "might introduce <br> overheads and potentially result in suboptimal runtimes</html>");
        Font hintFont = coresHintLabel.getFont().deriveFont(Font.ITALIC);
        coresHintLabel.setFont(hintFont);
        coresHintLabel.setForeground(Color.GRAY);
        Font hintFont1 = coresHintLabel.getFont().deriveFont(Font.ITALIC);


        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("Enter the file name:"));
        inputPanel.add(fileNameTextField);
        inputPanel.add(new JLabel("Select the parallelism mode:"));
        inputPanel.add(modeComboBox);
        inputPanel.add(new JLabel("Select the number of cores:"));
        inputPanel.add(coresSpinner);
        inputPanel.add(new JLabel());
        inputPanel.add(coresHintLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(searchButton);

        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        resultPanel.add(elapsedTimeLabel);
        resultPanel.add(foundInLabel);

        JButton cacheButton = new JButton("Cache the files");
        JPanel cachePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cachePanel.add(cacheButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);
        contentPanel.add(resultPanel, BorderLayout.SOUTH);
        contentPanel.add(cacheButton,BorderLayout.WEST);

        JButton exitButton = new JButton("Exit");

        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exitPanel.add(exitButton);
        contentPanel.add(exitPanel, BorderLayout.LINE_END);

        setContentPane(contentPanel);

        ref = new Main(fileNameTextField,modeComboBox,coresSpinner,elapsedTimeLabel,
                foundInLabel);

        cacheButton.addActionListener(e -> {
            FileCache.cache();
            isCached = true;
        });

        searchButton.addActionListener(e -> {
            try {
                ref.execute();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException | IOException | BrokenBarrierException ex) {
                throw new RuntimeException(ex);
            }
        });
        exitButton.addActionListener(e -> {
            System.exit(0);
        });
        foundInLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        foundInLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        System.out.println(Main.solution);
                        Desktop.getDesktop().open(new File(Main.solution).getParentFile());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }

}
