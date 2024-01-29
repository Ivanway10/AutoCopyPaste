
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class JavaIOUtils {

    private JFrame frame;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JFileChooser sourceFileChooser;
    private JFileChooser destinationFileChooser;
    private File sourceFolder;
    private File destinationFolder;
    private Thread autoCopyThread;

    public static void main(String[] args) {
        JavaIOUtils autoFileCopier = new JavaIOUtils();
        autoFileCopier.createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Auto File Copier");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        // Create the JFileChooser components for source and destination directories
        sourceFileChooser = new JFileChooser();
        sourceFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        destinationFileChooser = new JFileChooser();
        destinationFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Create the start button
        startButton = new JButton("Start");
        startButton.addActionListener(e -> startAutoCopy());

        // Create the stop button
        stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopAutoCopy());

        // Create the log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Create a JPanel to hold the JFileChooser components
        JPanel fileChooserPanel = new JPanel();
        fileChooserPanel.setLayout(new GridLayout(2, 2));

        // Buttons to select the source and destination directories
        JButton sourceButton = new JButton("Select Source Folder");
        sourceButton.addActionListener(e -> selectSourceFolder());
        fileChooserPanel.add(sourceButton);
        fileChooserPanel.add(sourceFileChooser);

        JButton destinationButton = new JButton("Select Destination Folder");
        destinationButton.addActionListener(e -> selectDestinationFolder());
        fileChooserPanel.add(destinationButton);
        fileChooserPanel.add(destinationFileChooser);

        // Add the fileChooserPanel to the frame
        frame.add(fileChooserPanel, BorderLayout.NORTH);

        // Add the components to the frame
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(logScrollPane, BorderLayout.CENTER);

        // Create a Logger and a FileHandler to handle logging
        Logger logger = Logger.getLogger(JavaIOUtils.class.getName());
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler("logs.log", true);
            logger.addHandler(fileHandler);
            LoggingUtil.setup(logArea); // Configura LoggingUtil para mostrar logs en el JTextArea
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Make the frame visible
        frame.setVisible(true);
    }

    private void startAutoCopy() {
        sourceFolder = sourceFileChooser.getSelectedFile();
        destinationFolder = destinationFileChooser.getSelectedFile();

        if (sourceFolder != null && destinationFolder != null) {
            autoCopyThread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    try {
                        copyNewFiles();
                        Thread.sleep(5000); // Verificar cada 5 segundos
                    } catch (InterruptedException | IOException e) {
                        LoggingUtil.log("Error: " + e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
            });
            autoCopyThread.start();
            LoggingUtil.log("Auto Copy Started");
        }
    }

    private void stopAutoCopy() {
        if (autoCopyThread != null) {
            autoCopyThread.interrupt();
            LoggingUtil.log("Auto Copy Stopped");
        }
    }

    private void selectSourceFolder() {
        int returnValue = sourceFileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            LoggingUtil.log("Selected Source Folder: " + sourceFileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void selectDestinationFolder() {
        int returnValue = destinationFileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            LoggingUtil.log("Selected Destination Folder: " + destinationFileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void copyNewFiles() throws IOException {
        File[] sourceFiles = sourceFolder.listFiles();
        for (File sourceFile : sourceFiles) {
            File destinationFile = new File(destinationFolder, sourceFile.getName());
            if (sourceFile.isFile() && !destinationFile.exists()) {
                copyFile(sourceFile, destinationFile);
                LoggingUtil.log("Copied file: " + sourceFile.getAbsolutePath() + " to " + destinationFile.getAbsolutePath());
            }
        }
    }

    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile); OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    static class LoggingUtil {

        private static JTextArea logArea;

        // Configura LoggingUtil con el JTextArea
        public static void setup(JTextArea area) {
            logArea = area;
        }

        // Log en el JTextArea
        public static void log(String message) {
            if (logArea != null) {
                logArea.append(getTimestamp() + " " + message + "\n");
            }
        }

        // Obtén una marca de tiempo formateada
        private static String getTimestamp() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "[" + dateFormat.format(new Date()) + "]";
        }
    }
}
