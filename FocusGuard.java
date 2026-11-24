import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FocusGuard {
    static final Color BG = new Color(18, 22, 38);
static final Color CARD = new Color(31, 36, 58);
static final Color PURPLE = new Color(139, 92, 246);
static final Color WHITE = new Color(245, 245, 255);
static final Color MUTED = new Color(165, 170, 
0);

    static final int FOCUS_TIME = 25 * 60;
    static final int BREAK_TIME = 5 * 60;
    static final int DAILY_GOAL = 4;
    static final String HISTORY_FILE = "study_history.txt";

    static int timeLeft = FOCUS_TIME;
    static int distractions = 0;
    static boolean isBreak = false;

    static Timer timer;
    static JLabel timerLabel, statusLabel;
    static CircularTimer circularTimer;
    static JLabel countLabel, scoreLabel, distractionLabel;
    static JProgressBar goalProgressBar;
static JLabel goalProgressLabel;

    public static void main(String[] args) {

        JFrame frame = new JFrame("FocusGuard - Study App");
        frame.getContentPane().setBackground(BG);
frame.setSize(600, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
panel.setBackground(BG);
        panel.setBorder(
        BorderFactory.createEmptyBorder(20, 25, 20, 25)
        );

        JLabel title = new JLabel("FOCUSGUARD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));

        statusLabel = new JLabel("Focus Session", SwingConstants.CENTER);

        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 45));

        countLabel = new JLabel(
            "Total Sessions: " + countSessions(),
            SwingConstants.CENTER
        );

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        updateScore();

        distractionLabel = new JLabel(
            "0", SwingConstants.CENTER
        );
        
        title.setForeground(PURPLE);
statusLabel.setForeground(MUTED);
timerLabel.setForeground(WHITE);
countLabel.setForeground(WHITE);
scoreLabel.setForeground(WHITE);
distractionLabel.setForeground(WHITE);

        JButton startButton = new RoundedButton("Start");
JButton pauseButton = new RoundedButton("Pause");
JButton resetButton = new RoundedButton("Reset");
JButton historyButton = new RoundedButton("View Study History");
JButton consistencyButton = new RoundedButton("View Focus Consistency");
JButton distractionButton = new RoundedButton("I Got Distracted");
        styleButton(startButton);
styleButton(pauseButton);
styleButton(resetButton);
styleButton(historyButton);
styleButton(distractionButton);
styleButton(consistencyButton);


JPanel buttonPanel = new JPanel();
buttonPanel.setOpaque(false);

buttonPanel.add(startButton);
buttonPanel.add(pauseButton);
buttonPanel.add(resetButton);

        timer = new Timer(1000, e -> {

            if (timeLeft > 0) {
                timeLeft--;
                updateTimer();
            }

            if (timeLeft == 0) {
                timer.stop();

                if (!isBreak) {
                    saveSession();

                    countLabel.setText("" + countSessions());

                    updateScore();

                    isBreak = true;
                    timeLeft = BREAK_TIME;
                    statusLabel.setText("Break Time");
                    updateTimer();

                    JOptionPane.showMessageDialog(
                        frame,
                        "Focus completed! Session saved.\n"
                        + "Distractions this session: " + distractions
                    );

                    distractions = 0;
                    distractionLabel.setText("Distractions: 0");

                    timer.start();

                } else {
                    isBreak = false;
                    timeLeft = FOCUS_TIME;
                    statusLabel.setText("Focus Session");
                    updateTimer();

                    JOptionPane.showMessageDialog(
                        frame,
                        "Break completed! Ready to study?"
                    );
                }
            }
        });

        startButton.addActionListener(e -> timer.start());
        pauseButton.addActionListener(e -> timer.stop());

        resetButton.addActionListener(e -> {
            timer.stop();
            isBreak = false;
            timeLeft = FOCUS_TIME;
            distractions = 0;
            statusLabel.setText("Focus Session");
            distractionLabel.setText("Distractions: 0");
            updateTimer();
        });

        distractionButton.addActionListener(e -> {
            if (timer.isRunning() && !isBreak) {
                distractions++;
                distractionLabel.setText("" + distractions);
            } else {
                JOptionPane.showMessageDialog(
                    frame, "Start a focus session first!"
                );
            }
        });

        historyButton.addActionListener(e -> showHistory(frame));
        consistencyButton.addActionListener(
    e -> showConsistency(frame)
);


JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
statsPanel.setOpaque(false);

RoundedPanel sessionsCard = new RoundedPanel(25);
sessionsCard.setBackground(CARD);
sessionsCard.setLayout(new BorderLayout());
sessionsCard.setBorder(
    BorderFactory.createEmptyBorder(20, 5, 20, 5)
);

RoundedPanel scoreCard = new RoundedPanel(25);
scoreCard.setBackground(CARD);
scoreCard.setLayout(new BorderLayout());
scoreCard.setBorder(
    BorderFactory.createEmptyBorder(20, 5, 20, 5)
);

RoundedPanel distractionsCard = new RoundedPanel(25);
distractionsCard.setBackground(CARD);
distractionsCard.setLayout(new BorderLayout());
distractionsCard.setBorder(
    BorderFactory.createEmptyBorder(20, 5, 20, 5)
);

// Card headings
JLabel sessionsTitle = new JLabel(
    "TOTAL SESSIONS", SwingConstants.CENTER
);
JLabel scoreTitle = new JLabel(
    "PRODUCTIVITY", SwingConstants.CENTER
);
JLabel distractionsTitle = new JLabel(
    "DISTRACTIONS", SwingConstants.CENTER
);

for (JLabel heading : new JLabel[]{
        sessionsTitle, scoreTitle, distractionsTitle
}) {
    heading.setForeground(MUTED);
    heading.setFont(new Font("Arial", Font.BOLD, 11));
}

// Show numbers instead of long text
countLabel.setText("" + countSessions());
scoreLabel.setText(
    Math.min(100, countTodaySessions() * 100 / DAILY_GOAL) + "%"
);
distractionLabel.setText("" + distractions);

for (JLabel value : new JLabel[]{
        countLabel, scoreLabel, distractionLabel
}) {
    value.setForeground(WHITE);
    value.setFont(new Font("Arial", Font.BOLD, 25));
}

sessionsCard.add(sessionsTitle, BorderLayout.NORTH);
sessionsCard.add(countLabel, BorderLayout.CENTER);

scoreCard.add(scoreTitle, BorderLayout.NORTH);
scoreCard.add(scoreLabel, BorderLayout.CENTER);

distractionsCard.add(distractionsTitle, BorderLayout.NORTH);
distractionsCard.add(distractionLabel, BorderLayout.CENTER);

statsPanel.add(sessionsCard);
statsPanel.add(scoreCard);
statsPanel.add(distractionsCard);

statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
statsPanel.setMaximumSize(new Dimension(500, 125));
statsPanel.setPreferredSize(new Dimension(500, 125));

 
RoundedPanel timerCard = new RoundedPanel(30);
timerCard.setLayout(new BorderLayout(10, 15));
timerCard.setBackground(CARD);

timerCard.setBorder(
    BorderFactory.createEmptyBorder(25, 20, 25, 20)
);

circularTimer = new CircularTimer();
timerCard.add(circularTimer, BorderLayout.CENTER);
timerCard.add(buttonPanel, BorderLayout.SOUTH);

timerCard.setAlignmentX(Component.CENTER_ALIGNMENT);
timerCard.setMaximumSize(new Dimension(450, 300));
timerCard.setPreferredSize(new Dimension(450, 300));
       
title.setAlignmentX(Component.CENTER_ALIGNMENT);
statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
distractionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

distractionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
historyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
consistencyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

panel.add(Box.createVerticalStrut(15));
panel.add(title);

panel.add(Box.createVerticalStrut(20));
panel.add(statusLabel);

panel.add(Box.createVerticalStrut(15));
panel.add(timerCard);

panel.add(Box.createVerticalStrut(25));
panel.add(statsPanel);
goalProgressLabel = new JLabel(
    "Daily Goal: " + countTodaySessions() + "/" + DAILY_GOAL + " Sessions",
    SwingConstants.CENTER
);

goalProgressLabel.setForeground(WHITE);
goalProgressLabel.setFont(
    new Font("Arial", Font.BOLD, 14)
);

goalProgressBar = new JProgressBar(0, DAILY_GOAL);
goalProgressBar.setValue(countTodaySessions());
goalProgressBar.setStringPainted(false);
goalProgressBar.setForeground(PURPLE);
goalProgressBar.setBackground(CARD);
goalProgressBar.setBorderPainted(false);
goalProgressBar.setPreferredSize(new Dimension(450, 14));
goalProgressBar.setMaximumSize(new Dimension(500, 14));
goalProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

panel.add(Box.createVerticalStrut(15));
panel.add(goalProgressLabel);

panel.add(Box.createVerticalStrut(8));
panel.add(goalProgressBar);
panel.add(Box.createVerticalStrut(25));
panel.add(distractionButton);


JPanel bottomButtonPanel = new JPanel(
    new GridLayout(1, 2, 12, 0)
);

bottomButtonPanel.setBackground(BG);
bottomButtonPanel.setOpaque(false);

bottomButtonPanel.add(historyButton);
bottomButtonPanel.add(consistencyButton);

bottomButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
bottomButtonPanel.setMaximumSize(new Dimension(500, 60));
bottomButtonPanel.setPreferredSize(new Dimension(500, 60));

panel.add(Box.createVerticalStrut(15));
panel.add(bottomButtonPanel);



        frame.add(panel);
        frame.setVisible(true);
    }

static void updateTimer() {
    timerLabel.setText(
        String.format("%02d:%02d",
            timeLeft / 60, timeLeft % 60)
    );

    if (circularTimer != null) {
        circularTimer.repaint();
    }
}

    static void updateScore() {
    int todaySessions = countTodaySessions();

    int score = Math.min(100,
        todaySessions * 100 / DAILY_GOAL
    );

    scoreLabel.setText(score + "%");

    if (goalProgressBar != null) {
        goalProgressBar.setValue(
            Math.min(todaySessions, DAILY_GOAL)
        );

        goalProgressLabel.setText(
            "Daily Goal: " + todaySessions
            + "/" + DAILY_GOAL + " Sessions"
        );
    }
}

    static void saveSession() {
        try (FileWriter writer = new FileWriter(HISTORY_FILE, true)) {
            String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
            );

            writer.write(
                date + " | Focus Duration: "
                + (FOCUS_TIME / 60) + " minutes"
                + " | Distractions: " + distractions + "\n"
            );

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null, "Error saving session!"
            );
        }
    }

    static int countSessions() {
        int count = 0;

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(HISTORY_FILE))) {

            while (reader.readLine() != null) {
                count++;
            }

        } catch (IOException e) {
            return 0;
        }

        return count;
    }

    static int countTodaySessions() {
        int count = 0;

        String today = LocalDate.now().format(
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(HISTORY_FILE))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(today)) {
                    count++;
                }
            }

        } catch (IOException e) {
            return 0;
        }

        return count;
    }

    static void showHistory(JFrame frame) {
        StringBuilder history = new StringBuilder();

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(HISTORY_FILE))) {

            String line;

            while ((line = reader.readLine()) != null) {
                history.append(line).append("\n");
            }

        } catch (IOException e) {
            history.append("No study sessions recorded yet.");
        }

        JTextArea textArea = new JTextArea(history.toString());
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 250));

        JOptionPane.showMessageDialog(
            frame, scrollPane,
            "Study Session History",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

static Map<LocalDate, Integer> getDailySessions() {
    Map<LocalDate, Integer> daily = new HashMap<>();

    DateTimeFormatter format =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    try (BufferedReader reader =
             new BufferedReader(new FileReader(HISTORY_FILE))) {

        String line;

        while ((line = reader.readLine()) != null) {
            try {
                String dateText = line.split(" ")[0];
                LocalDate date = LocalDate.parse(dateText, format);

                daily.put(date, daily.getOrDefault(date, 0) + 1);
            } catch (Exception e) {
                // Ignore invalid history entries
            }
        }

    } catch (IOException e) {
        // No history yet
    }

    return daily;
}

static void showConsistency(JFrame frame) {
    Map<LocalDate, Integer> daily = getDailySessions();

    LocalDate today = LocalDate.now();
    int successfulDays = 0;

    for (int i = 0; i < 7; i++) {
        LocalDate date = today.minusDays(i);

        if (daily.getOrDefault(date, 0) >= DAILY_GOAL) {
            successfulDays++;
        }
    }

    int consistency = successfulDays * 100 / 7;

    int streak = 0;
    LocalDate date = today;

    // Include an ongoing streak from yesterday
    if (daily.getOrDefault(date, 0) < DAILY_GOAL) {
        date = date.minusDays(1);
    }

    while (daily.getOrDefault(date, 0) >= DAILY_GOAL) {
        streak++;
        date = date.minusDays(1);
    }

    String message =
        "Last 7 Days Consistency: " + consistency + "%\n"
        + "Successful Days: " + successfulDays + "/7\n"
        + "Current Streak: " + streak + " days\n"
        + "Daily Goal: " + DAILY_GOAL + " sessions";

    JOptionPane.showMessageDialog(
        frame, message, "Focus Consistency",
        JOptionPane.INFORMATION_MESSAGE
    );
}

static void styleButton(JButton button) {
    button.setBackground(PURPLE);
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Arial", Font.BOLD, 14));
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));

    button.setBorder(
        BorderFactory.createEmptyBorder(12, 20, 12, 20)
    );

    button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

    button.setContentAreaFilled(false);

    button.setBorderPainted(false);

    button.setRolloverEnabled(true);
}


static class CircularTimer extends JPanel {

    CircularTimer() {
        setOpaque(false);
        setPreferredSize(new Dimension(190, 190));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int size = Math.min(getWidth(), getHeight()) - 24;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setStroke(new BasicStroke(
            10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
        ));

        g2.setColor(new Color(65, 55, 95));
        g2.drawOval(x, y, size, size);

        int total = isBreak ? BREAK_TIME : FOCUS_TIME;
        double progress = (double) timeLeft / total;
        int angle = (int) Math.round(progress * 360);

        g2.setColor(PURPLE);
        g2.drawArc(x, y, size, size, 90, -angle);

        g2.setColor(WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 35));

        String time = String.format(
            "%02d:%02d", timeLeft / 60, timeLeft % 60
        );

        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(time)) / 2;
        int textY = (getHeight() + fm.getAscent()
                     - fm.getDescent()) / 2;

        g2.drawString(time, textX, textY);

        g2.dispose();
    }
}

static class RoundedButton extends JButton {

    RoundedButton(String text) {
        super(text);
        setBackground(PURPLE);
        setForeground(WHITE);
        setFont(new Font("Arial", Font.BOLD, 14));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(12, 20, 12, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (getModel().isPressed()) {
            g2.setColor(new Color(109, 62, 216));
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(155, 110, 255));
        } else {
            g2.setColor(PURPLE);
        }

        g2.fillRoundRect(
            0, 0, getWidth(), getHeight(), 20, 20
        );

        g2.dispose();
        super.paintComponent(g);
    }
}

static class RoundedPanel extends JPanel {

    private final int radius;

    RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(getBackground());
        g2.fillRoundRect(
            0, 0,
            getWidth(), getHeight(),
            radius, radius
        );

        g2.dispose();
        super.paintComponent(g);
    }
}
}
