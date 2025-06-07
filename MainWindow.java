import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainWindow {
    public static void main(String[] args) {
        // Column names
        String[] columns = {"Start Time", "End Time", "Running Timer", "Duration", "Start", "Pause/Resume", "Done"};

        // Object Data (empty initially)
        Object[][] data = {
                {null, null, "00:00:00", "00:00:00", "Start", "Pause/Resume", "Done"}
        };

        // Create the table model
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only button columns editable
                return column >= 4 && column <= 6;
            }
        };

        // Create the table with the model
        JTable table = new JTable(model);

        // Set custom button renderer and editor
        table.getColumn("Start").setCellRenderer(new ButtonRenderer());
        table.getColumn("Pause/Resume").setCellRenderer(new ButtonRenderer());
        table.getColumn("Done").setCellRenderer(new ButtonRenderer());

        table.getColumn("Start").setCellEditor(new ButtonEditor(new JButton("Start"), model, table, "Start"));
        table.getColumn("Pause/Resume").setCellEditor(new ButtonEditor(new JButton("Pause/Resume"), model, table, "Pause/Resume"));
        table.getColumn("Done").setCellEditor(new ButtonEditor(new JButton("Done"), model, table, "Done"));

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Create the frame
        JFrame frame = new JFrame("Default Table Model Example with Buttons in Row");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add components to frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Button renderer for the table cells
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button editor for handling button clicks in the table cells
    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private DefaultTableModel model;
        private JTable table;
        private String label;
        private boolean isRunning = false;
        private boolean isPaused = false;
        private Timer timer;
        private long startTime;
        private long pausedStart;
        private long pausedDuration = 0;
        private long elapsedTime = 0;

        public ButtonEditor(JButton button, DefaultTableModel model, JTable table, String label) {
            super(new JCheckBox());
            this.button = button;
            this.model = model;
            this.table = table;
            this.label = label;

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int row = table.getSelectedRow();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

                    if (label.equals("Start")) {
                        if (!isRunning) {
                            startTime = System.currentTimeMillis();
                            isRunning = true;
                            isPaused = false;
                            pausedDuration = 0;

                            // Set start time in the Start Time cell
                            model.setValueAt(timeFormat.format(new Date(startTime)), row, 0);

                            // Start the timer to update Running Timer cell
                            timer = new Timer(1000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent evt) {
                                    if (!isPaused) {
                                        elapsedTime = System.currentTimeMillis() - startTime - pausedDuration;
                                        model.setValueAt(formatDuration(elapsedTime), row, 2);
                                    }
                                }
                            });
                            timer.start();
                        }
                    } else if (label.equals("Pause/Resume")) {
                        if (isRunning) {
                            if (!isPaused) {
                                // Pause the timer
                                timer = new Timer(1000, new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent evt) {
                                        isPaused = true;
                                        pausedStart = System.currentTimeMillis(); // Record the pause start time

                                    }
                                });
                                timer.stop();
                            } else {
                                // Resume the timer
                                isPaused = false;
                                pausedDuration += System.currentTimeMillis() - pausedStart; // Accumulate paused duration
                                timer.start();
                            }
                        }
                    } else if (label.equals("Done")) {
                        if (isRunning) {
                            // Stop the timer
                            timer.stop();
                            isRunning = false;

                            // Set end time in the End Time cell
                            long endTime = System.currentTimeMillis();
                            model.setValueAt(timeFormat.format(new Date(endTime)), row, 1);

                            // Calculate total duration in milliseconds
                            long totalDuration = endTime - startTime - pausedDuration;
                            model.setValueAt(formatDuration(totalDuration), row, 3);
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText((value == null) ? "" : value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        // Helper method to format duration (milliseconds) as HH:mm:ss
        private String formatDuration(long millis) {
            long hours = millis / (1000 * 60 * 60);
            long minutes = (millis / (1000 * 60)) % 60;
            long seconds = (millis / 1000) % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
