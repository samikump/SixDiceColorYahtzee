package none.sixdicecoloryahtzee;

/**
 *
 * @author samikump
 * copyright © samikump 2026
 * Yahtzee, but with six colored dice!
 * check out the README for details
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SixDiceColorYahtzee extends JFrame {

    private static final String[] COLORS = {"purple", "red", "orange", "yellow", "green", "blue"};
    private static final Color[] AWT_COLORS = {
        new Color(128, 0, 128), // purple
        Color.RED,
        Color.ORANGE,
        Color.YELLOW,
        Color.GREEN,
        Color.BLUE
    };

    private String playerName;
    private int rollsLeft = 4;

    private Die[] dice = new Die[6];
    private Map<String, Integer> scores = new LinkedHashMap<>();
    private Set<String> filledCells = new HashSet<>();

    private JPanel dicePanel;
    private JPanel scorePanel;
    private JButton rollButton;
    private JLabel statusLabel;
    private JLabel rollsLabel;

    public SixDiceColorYahtzee() {
        setTitle("Six Dice Color Yahtzee");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        askPlayerName();

        initDice();
        initScores();
        initUI();

        pack();
        // Ensure the window is wide enough for the longest category names
        Dimension minSize = new Dimension(Math.max(600, getWidth()), Math.max(750, getHeight()));
        setMinimumSize(minSize);
        setSize(minSize);
        setLocationRelativeTo(null);
        setVisible(true);
    } // constructor SixDiceColorYahtzee ends here

    private void askPlayerName() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:");
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        } else if (playerName.length() > 20) {
            playerName = playerName.substring(0, 20);
        }
    } // askPlayerName method ends here

    private void initDice() {
        for (int i = 0; i < 6; i++) {
            dice[i] = new Die(i);
        }
    } // initDice method ends here

    private void initScores() {
        scores.clear();
        filledCells.clear();
        // Upper Part Numbers
        scores.put("Ones", null);
        scores.put("Twos", null);
        scores.put("Threes", null);
        scores.put("Fours", null);
        scores.put("Fives", null);
        scores.put("Sixes", null);
        scores.put("Upper Bonus", 0);

        // Upper Part Colors
        scores.put("Purples", null);
        scores.put("Reds", null);
        scores.put("Oranges", null);
        scores.put("Yellows", null);
        scores.put("Greens", null);
        scores.put("Blues", null);
        scores.put("Color Bonus", 0);

        // Traditional with a few additions
        scores.put("Pair", null);
        scores.put("2 Pairs", null);
        scores.put("3 Pairs", null);
        scores.put("3 of a Kind", null);
        scores.put("2x 3 of a Kind", null);
        scores.put("4 of a Kind", null);
        scores.put("5 of a Kind", null);
        scores.put("Small Straight", null);
        scores.put("Large Straight", null);
        scores.put("Huge Straight", null);
        scores.put("Full House", null);
        scores.put("Extended Full House", null);

        // Color Based
        scores.put("3 of a Color", null);
        scores.put("2x 3 of a Color", null);
        scores.put("4 of a Color", null);
        scores.put("5 of a Color", null);
        scores.put("Painted House", null);
        scores.put("Extended Painted House", null);
        scores.put("Rainbow", null);
        scores.put("Flush", null);

        // Yahtzee
        scores.put("Yahtzee", null);
        
        // Chance
        scores.put("Chance", null);
        
        scores.put("Total Score", 0);
    } // initScores method ends here

    private void initUI() {
        dicePanel = new JPanel(new FlowLayout());
        for (Die d : dice) {
            dicePanel.add(d.getComponent());
        }

        JPanel controlPanel = new JPanel(new FlowLayout());
        rollButton = new JButton("Roll Dice");
        rollButton.addActionListener(e -> rollDice());
        rollsLabel = new JLabel("Rolls left: " + rollsLeft);
        controlPanel.add(rollButton);
        controlPanel.add(rollsLabel);

        scorePanel = new JPanel(new GridLayout(0, 2));
        updateScorePanel();

        statusLabel = new JLabel("Welcome, " + playerName + "! Roll to start.", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(dicePanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(scorePanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    } // initUI method ends here

    private void updateScorePanel() {
        scorePanel.removeAll();
        List<String> colorCats = Arrays.asList("Purples", "Reds", "Oranges", "Yellows", "Greens", "Blues");
        
        for (String category : scores.keySet()) {
            if (category.equals("Upper Bonus") || category.equals("Color Bonus") || category.equals("Total Score")) {
                scorePanel.add(new JLabel(category + ":"));
                scorePanel.add(new JLabel(String.valueOf(scores.get(category))));
                continue;
            }

            JButton btn = new JButton();
            if (colorCats.contains(category)) {
                int colorIdx = colorCats.indexOf(category);
                btn.setIcon(new ColorBarIcon(AWT_COLORS[colorIdx]));
            } else {
                btn.setText(category);
            }

            Integer score = scores.get(category);
            if (filledCells.contains(category)) {
                if (colorCats.contains(category)) {
                    btn.setText(": " + score);
                } else {
                    btn.setText(category + ": " + score);
                }
                btn.setEnabled(false);
            } else {
                int potential = calculateScore(category);
                if (colorCats.contains(category)) {
                    btn.setText(" (" + potential + ")");
                } else {
                    btn.setText(category + " (" + potential + ")");
                }
                btn.addActionListener(e -> assignScore(category, potential));
                if (rollsLeft == 4) btn.setEnabled(false);
            }
            scorePanel.add(btn);
        }
        scorePanel.revalidate();
        scorePanel.repaint();
    } // updateScorePanel method ends here

    private void rollDice() {
        if (rollsLeft > 0) {
            for (Die d : dice) {
                d.roll();
            }
            rollsLeft--;
            rollsLabel.setText("Rolls left: " + rollsLeft);
            if (rollsLeft == 0) {
                rollButton.setEnabled(false);
                statusLabel.setText("Assign your score");
            }
            updateScorePanel();
            if (rollsLeft < 4 && rollsLeft > 0) {
            statusLabel.setText("Assign your score or roll again.");
            }
        }
    } // rollDice method ends here

    private void assignScore(String category, int score) {
        if (isYahtzee() && filledCells.contains("Yahtzee")) {
            scores.put("Yahtzee", scores.get("Yahtzee") + 100);
        }

        scores.put(category, score);
        filledCells.add(category);
        
        updateTotals();
        resetTurn();
        
        if (filledCells.size() == scores.size() - 3) { // Excluding totals/bonuses
            endGame();
        } else {
            updateScorePanel();
        }
    } // assingScore method ends here

    private void updateTotals() {
        int upperSum = 0;
        String[] upperCats = {"Ones", "Twos", "Threes", "Fours", "Fives", "Sixes"};
        for (String c : upperCats) {
            upperSum += (scores.get(c) != null ? scores.get(c) : 0);
        }
        if (upperSum >= 84) scores.put("Upper Bonus", 50);
        else scores.put("Upper Bonus", 0);

        int colorSum = 0;
        String[] colorCats = {"Purples", "Reds", "Oranges", "Yellows", "Greens", "Blues"};
        for (String c : colorCats) {
            colorSum += (scores.get(c) != null ? scores.get(c) : 0);
        }
        if (colorSum >= 84) scores.put("Color Bonus", 50);
        else scores.put("Color Bonus", 0);

        int total = upperSum + scores.get("Upper Bonus") + colorSum + scores.get("Color Bonus");
        for (String c : scores.keySet()) {
            if (!Arrays.asList(upperCats).contains(c) && !Arrays.asList(colorCats).contains(c) 
                && !c.contains("Bonus") && !c.equals("Total Score") && !c.equals("Yahtzee")) {
                total += (scores.get(c) != null ? scores.get(c) : 0);
            }
        }
        total += (scores.get("Yahtzee") != null ? scores.get("Yahtzee") : 0);
        
        scores.put("Total Score", total);
    } // updateTotals method ends here

    private void resetTurn() {
        rollsLeft = 4;
        rollsLabel.setText("Rolls left: " + rollsLeft);
        rollButton.setEnabled(true);
        for (Die d : dice) {
            d.setLocked(false);
            d.reset();
        }
        statusLabel.setText("Turn ended. Roll to start next turn.");
    } // resetTurn method ends here

    private int calculateScore(String category) {
        int[] values = Arrays.stream(dice).mapToInt(Die::getValue).toArray();
        String[] colors = Arrays.stream(dice).map(Die::getColor).toArray(String[]::new);
        
        Map<Integer, Long> valCounts = Arrays.stream(values).boxed()
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));
        Map<String, Long> colCounts = Arrays.stream(colors)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        switch (category) {
            case "Ones": return (int) (valCounts.getOrDefault(1, 0L) * 1);
            case "Twos": return (int) (valCounts.getOrDefault(2, 0L) * 2);
            case "Threes": return (int) (valCounts.getOrDefault(3, 0L) * 3);
            case "Fours": return (int) (valCounts.getOrDefault(4, 0L) * 4);
            case "Fives": return (int) (valCounts.getOrDefault(5, 0L) * 5);
            case "Sixes": return (int) (valCounts.getOrDefault(6, 0L) * 6);

            case "Purples": return sumByColor("purple");
            case "Reds": return sumByColor("red");
            case "Oranges": return sumByColor("orange");
            case "Yellows": return sumByColor("yellow");
            case "Greens": return sumByColor("green");
            case "Blues": return sumByColor("blue");

            case "Pair": return getXOfAKind(valCounts, 2) * 2;
            case "2 Pairs": return getTwoPairs(valCounts);
            case "3 Pairs": return getThreePairs(valCounts);
            case "3 of a Kind": return getXOfAKind(valCounts, 3) * 3;
            case "2x 3 of a Kind": return getTwoThreeOfAKinds(valCounts);
            
            case "4 of a Kind": return getXOfAKind(valCounts, 4) * 4;
            case "5 of a Kind": return getXOfAKind(valCounts, 5) * 5;
            
            case "Small Straight": return isStraight(values, 4) ? 15 : 0;
            case "Large Straight": return isStraight(values, 5) ? 20 : 0;
            case "Huge Straight": return isStraight(values, 6) ? 35 : 0;
            
            case "Full House": return isFullHouse(valCounts, 3, 2) ? 25 : 0;
            case "Extended Full House": return isFullHouse(valCounts, 4, 2) ? 35 : 0;

            case "3 of a Color": return sumOfXOfAColor(colCounts, 3, dice);
            case "2x 3 of a Color": return getTwoThreeOfAColor(colCounts, dice);
            case "4 of a Color": return sumOfXOfAColor(colCounts, 4, dice);
            case "5 of a Color": return sumOfXOfAColor(colCounts, 5, dice);
            
            case "Painted House": return isPaintedHouse(colCounts, 3, 2) ? 25 : 0;
            case "Extended Painted House": return isPaintedHouse(colCounts, 4, 2) ? 35 : 0;
            case "Rainbow": return colCounts.size() == 6 ? 40 : 0;
            case "Flush": return colCounts.size() == 1 ? 50 : 0;
            
            case "Yahtzee": return isYahtzee() ? 50 : 0;
            case "Chance": return Arrays.stream(values).sum();
            
            default: return 0;
        }
    } // calculateScore method ends here

    private int sumByColor(String color) {
        int sum = 0;
        for (Die d : dice) {
            if (d.getColor().equals(color)) sum += d.getValue();
        }
        return sum;
    } // sumByColor method ends here

    private int getXOfAKind(Map<Integer, Long> counts, int x) {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() >= x)
                .mapToInt(Map.Entry::getKey)
                .max().orElse(0);
    } // getXOfAKind method ends here

    private int getTwoPairs(Map<Integer, Long> counts) {
        List<Integer> pairs = counts.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .map(Map.Entry::getKey)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        if (pairs.size() >= 2) return pairs.get(0) * 2 + pairs.get(1) * 2;
        return 0;
    } // getTwoPairs method ends here

    private int getThreePairs(Map<Integer, Long> counts) {
        List<Integer> pairs = counts.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // Only positive if all 3 pairs are different
        if (pairs.size() == 3) {
            return pairs.stream().mapToInt(i -> i * 2).sum();
        }
        return 0;
    } // getThreePairs method ends here

    private int getTwoThreeOfAKinds(Map<Integer, Long> counts) {
        List<Integer> triplets = counts.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (triplets.size() >= 2) return triplets.get(0) * 3 + triplets.get(1) * 3;
        return 0;
    } // getTwoThreeOfAKinds method ends here
    
    private int getTwoThreeOfAColor(Map<String, Long> counts, Die[] dice) {
        List<String> colors = counts.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (colors.size() >= 2) {
            return Arrays.stream(dice)
                    .filter(d -> colors.contains(d.getColor()))
                    .mapToInt(Die::getValue)
                    .sum();
        }
        return 0;
    } // getTwoThreeOfAColor method ends here

    private int sumOfXOfAColor(Map<String, Long> counts, int x, Die[] dice) {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() >= x)
                .mapToInt(e -> {
                    String color = e.getKey();
                    return Arrays.stream(dice)
                            .filter(d -> d.getColor().equals(color))
                            .mapToInt(Die::getValue)
                            .boxed()
                            .sorted(Comparator.reverseOrder())
                            .limit(x)
                            .mapToInt(i -> i)
                            .sum();
                })
                .max().orElse(0);
    } // sumOfXOfAColor method ends here

    private boolean isStraight(int[] values, int length) {
        Set<Integer> set = Arrays.stream(values).boxed().collect(Collectors.toSet());
        for (int i = 1; i <= 7 - length; i++) {
            boolean match = true;
            for (int j = 0; j < length; j++) {
                if (!set.contains(i + j)) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    } // isStraight method ends here

    private boolean isFullHouse(Map<Integer, Long> counts, int x, int y) {
        List<Long> cList = new ArrayList<>(counts.values());
        Collections.sort(cList, Collections.reverseOrder());
        return cList.size() >= 2 && cList.get(0) >= x && cList.get(1) >= y;
    } // isFullHouse method ends here

    private boolean isPaintedHouse(Map<String, Long> counts, int x, int y) {
        List<Long> cList = new ArrayList<>(counts.values());
        Collections.sort(cList, Collections.reverseOrder());
        return cList.size() >= 2 && cList.get(0) >= x && cList.get(1) >= y;
    } // isPaintedHouse method ends here

    private boolean isYahtzee() {
        if (dice[0].getValue() == 0) return false;
        int first = dice[0].getValue();
        return Arrays.stream(dice).allMatch(d -> d.getValue() == first);
    } // isYahtzee method ends here

    private void endGame() {
        rollButton.setEnabled(false);
        int finalScore = scores.get("Total Score");
        statusLabel.setText("Game Over! Final Score: " + finalScore);
        handleHighScore(finalScore);
    } // endGame method ends here

    private void handleHighScore(int score) {
        // to get this working fully, edit the third argument in next line and put your MySQL root password in place of yourRootPassword 
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Yahtzee", "root", "yourRootPassword")) {
            // Always insert the player's score first
            String insertSql = "INSERT INTO hiscore (playerName, score, date) VALUES (?, ?, ?)";
            PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
            insertPstmt.setString(1, playerName);
            insertPstmt.setInt(2, score);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            insertPstmt.setTimestamp(3, currentTimestamp);
            insertPstmt.executeUpdate();

            // Check if the score made it to the top 10 to show the appropriate message
            String checkSql = "SELECT COUNT(*) FROM hiscore WHERE score > ? OR (score = ? AND date < ?)";
            PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, score);
            checkPstmt.setInt(2, score);
            checkPstmt.setTimestamp(3, currentTimestamp);
            ResultSet rs = checkPstmt.executeQuery();
            rs.next();
            int rank = rs.getInt(1) + 1;

            if (rank <= 10) {
                JOptionPane.showMessageDialog(this, "Congratulations, " + playerName + "! Your score of " + score + " is rank #" + rank + "!");
            } else {
                JOptionPane.showMessageDialog(this, "Game Over! Your final score is: " + score);
            }

            // Maintain 10 rows limit by deleting scores outside the top 10
            // Using the unique 'date' column and the MySQL alias trick for subqueries on the same table
            String deleteSql = "DELETE FROM hiscore WHERE date NOT IN (SELECT date FROM (SELECT date FROM hiscore ORDER BY score DESC, date ASC LIMIT 10) as t)";
            conn.createStatement().executeUpdate(deleteSql);
            
            showHighScores(conn);
            
            int choice = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Play Again", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Database error. High scores could not be processed.");
        }
    } // handleHighScores method ends here

    private void showHighScores(Connection conn) throws SQLException {
        String sql = "SELECT playerName, score, date FROM hiscore ORDER BY score DESC, date ASC LIMIT 10";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Player", "Score", "Date"}, 0);
        while (rs.next()) {
            model.addRow(new Object[]{rs.getString("playerName"), rs.getInt("score"), rs.getTimestamp("date")});
        }

        JTable table = new JTable(model);
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "High Scores", JOptionPane.INFORMATION_MESSAGE);
    } // showHighScores method ends here

    private void resetGame() {
        rollsLeft = 4;
        initScores();
        for (Die d : dice) d.reset();
        rollButton.setEnabled(true);
        statusLabel.setText("New game started! Roll to start.");
        updateScorePanel();
    } // resetGame method ends here

    private class Die {
        private int index;
        private int value = 0;
        private boolean locked = false;
        private JPanel panel;
        private JPanel dieGraphic;
        private JCheckBox lockBox;

        public Die(int index) {
            this.index = index;
            panel = new JPanel(new BorderLayout());
            dieGraphic = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    if (value == 0) {
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, w, h, 15, 15);
                        g2.setColor(Color.BLACK);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString("?", (w - fm.stringWidth("?")) / 2, (h + fm.getAscent()) / 2 - 5);
                    } else {
                        g2.setColor(AWT_COLORS[(index + value - 1) % 6]);
                        g2.fillRoundRect(0, 0, w, h, 15, 15);
                        g2.setColor(Color.WHITE);
                        int d = w / 6; // pip diameter
                        int margin = w / 4;
                        switch (value) {
                            case 1: drawPip(g2, w/2, h/2, d); break;
                            case 2: drawPip(g2, margin, margin, d); drawPip(g2, w-margin, h-margin, d); break;
                            case 3: drawPip(g2, margin, margin, d); drawPip(g2, w/2, h/2, d); drawPip(g2, w-margin, h-margin, d); break;
                            case 4: drawPip(g2, margin, margin, d); drawPip(g2, w-margin, margin, d); drawPip(g2, margin, h-margin, d); drawPip(g2, w-margin, h-margin, d); break;
                            case 5: drawPip(g2, margin, margin, d); drawPip(g2, w-margin, margin, d); drawPip(g2, w/2, h/2, d); drawPip(g2, margin, h-margin, d); drawPip(g2, w-margin, h-margin, d); break;
                            case 6: drawPip(g2, margin, margin, d); drawPip(g2, w-margin, margin, d); drawPip(g2, margin, h/2, d); drawPip(g2, w-margin, h/2, d); drawPip(g2, margin, h-margin, d); drawPip(g2, w-margin, h-margin, d); break;
                        }
                    }
                }
                private void drawPip(Graphics2D g2, int x, int y, int d) {
                    g2.fillOval(x - d/2, y - d/2, d, d);
                }
            };
            dieGraphic.setPreferredSize(new Dimension(60, 60));
            // Removed hard border to emphasize rounded corners
            lockBox = new JCheckBox("Lock");
            lockBox.addActionListener(e -> locked = lockBox.isSelected());
            panel.add(dieGraphic, BorderLayout.CENTER);
            panel.add(lockBox, BorderLayout.SOUTH);
        }

        public void roll() {
            if (!locked) {
                value = new Random().nextInt(6) + 1;
                updateUI();
            }
        }

        public void reset() {
            value = 0;
            lockBox.setSelected(false);
            locked = false;
            updateUI();
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
            lockBox.setSelected(locked);
        }

        public int getValue() { return value; }
        public String getColor() {
            if (value == 0) return "";
            return COLORS[(index + value - 1) % 6];
        }

        private void updateUI() {
            dieGraphic.repaint();
        }

        public Component getComponent() { return panel; }
    } // class Die ends here

    private static class ColorBarIcon implements Icon {
        private final Color color;
        public ColorBarIcon(Color color) { this.color = color; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x + 2, y + 2, 70, 20);
        }
        @Override public int getIconWidth() { return 74; }
        @Override public int getIconHeight() { return 24; }
    } // class ColorBarIcon ends here

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SixDiceColorYahtzee::new);
    } // main method ends here
} // class SixDiceColorYahtzee ends here