package none.sixdicecoloryahtzee;

/**
 *
 * @author samikump
 * 
 * Yahtzee, but with six colored dice!
 * Now with Multiplayer support and Las Vegas style!
 * 
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class SixDiceColorYahtzee extends JFrame {

    private static final String[] COLORS = {"purple", "red", "orange", "yellow", "green", "blue"};
    private static final Color[] AWT_COLORS = {
        new Color(102, 0, 102), // purple
        new Color(153, 0, 0),   // red
        new Color(204, 82, 0),  // orange
        new Color(184, 134, 11), // dark goldenrod (better contrast with white)
        new Color(0, 102, 0),   // green
        new Color(0, 51, 153)   // blue
    };

    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int rollsLeft = 4;
    private int totalTurnsPerPlayer;

    private Die[] dice = new Die[6];

    private JPanel dicePanel;
    private JPanel scorePanel;
    private JButton rollButton;
    private JLabel statusLabel;
    private JLabel rollsLabel;

    public SixDiceColorYahtzee() {
        setTitle("Six Dice Color Yahtzee - Las Vegas Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        if (!setupPlayers()) System.exit(0);

        initDice();
        initUI();

        pack();
        
        Dimension minSize = new Dimension(780, 860);
        setMinimumSize(minSize);
        setSize(minSize);
        setLocationRelativeTo(null);
        setVisible(true);
    } // constructor SixDiceColorYahtzee ends here

    private boolean setupPlayers() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBackground(new Color(0, 80, 40));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 165, 32), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("SIX DICE COLOR YAHTZEE", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(new Color(218, 165, 32));
        
        JLabel subtitle = new JLabel("LAS VEGAS EDITION", SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.ITALIC, 18));
        subtitle.setForeground(Color.WHITE);
        
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 0, 10, 0);
        
        JLabel prompt = new JLabel("How many players are joining the table?");
        prompt.setFont(new Font("SansSerif", Font.BOLD, 16));
        prompt.setForeground(Color.WHITE);
        center.add(prompt, gbc);

        String[] options = {"1 Player", "2 Players", "3 Players", "4 Players"};
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = 1;
        center.add(combo, gbc);

        JPanel namesPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        namesPanel.setOpaque(false);
        JTextField[] nameFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            JPanel pRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pRow.setOpaque(false);
            JLabel l = new JLabel("Player " + (i + 1) + ": ");
            l.setForeground(Color.WHITE);
            l.setPreferredSize(new Dimension(70, 20));
            nameFields[i] = new JTextField(15);
            nameFields[i].setVisible(i == 0);
            pRow.add(l);
            pRow.add(nameFields[i]);
            namesPanel.add(pRow);
        }
        
        combo.addActionListener(e -> {
            int count = combo.getSelectedIndex() + 1;
            for (int i = 0; i < 4; i++) {
                nameFields[i].setVisible(i < count);
            }
            mainPanel.revalidate();
        });

        gbc.gridy = 2;
        center.add(namesPanel, gbc);
        mainPanel.add(center, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, mainPanel, "Six Dice Color Yahtzee Registration", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) return false;

        int numPlayers = combo.getSelectedIndex() + 1;
        for (int i = 0; i < numPlayers; i++) {
            String name = nameFields[i].getText().trim();
            if (name.isEmpty()) name = "Player " + (i + 1);
            if (name.length() > 20) name = name.substring(0, 20);
            players.add(new Player(name));
        }
        
        totalTurnsPerPlayer = players.get(0).scores.size() - 5; 
        return true;
    } // setupPlayers method ends here

    private void initDice() {
        for (int i = 0; i < 6; i++) {
            dice[i] = new Die(i);
        }
    } // initDice method ends here

    private void initUI() {
        dicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        dicePanel.setBackground(new Color(0, 80, 40)); 
        dicePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(218, 165, 32))); 
        for (Die d : dice) {
            dicePanel.add(d.getComponent());
        }

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        controlPanel.setBackground(new Color(220, 220, 220));
        
        rollButton = new JButton("ROLL DICE");
        rollButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        rollButton.setBackground(new Color(180, 0, 0)); 
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.setPreferredSize(new Dimension(150, 35));
        rollButton.addActionListener(e -> rollDice());
        
        rollsLabel = new JLabel("Rolls left: " + rollsLeft);
        rollsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        controlPanel.add(rollButton);
        controlPanel.add(rollsLabel);

        scorePanel = new JPanel(new GridBagLayout());
        updateScorePanel();

        statusLabel = new JLabel(players.get(0).name + "'s turn! Roll to start.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(218, 165, 32));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(dicePanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(scorePanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // Force no scrollbar
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    } // initUI method ends here

    private void updateScorePanel() {
        scorePanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 6, 1, 6); // Tighter insets to save space
        gbc.weightx = 1.0;

        Player cp = players.get(currentPlayerIndex);
        List<String> colorCats = Arrays.asList("Purples", "Reds", "Oranges", "Yellows", "Greens", "Blues");
        
        int row = 0;
        
        // Current Player Header
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel nameLabel = new JLabel("PLAYER: " + cp.name.toUpperCase(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 18));
        nameLabel.setForeground(new Color(150, 0, 0));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        scorePanel.add(nameLabel, gbc);

        int col = 0;
        for (String category : cp.scores.keySet()) {
            boolean fullWidth = category.equals("Upper Subtotal") || category.equals("Upper Bonus") || 
                               category.equals("Color Subtotal") || category.equals("Color Bonus") || 
                               category.equals("Total Score");
            
            if (fullWidth) {
                if (col == 1) { row++; col = 0; }
                gbc.gridx = 0;
                gbc.gridy = row++;
                gbc.gridwidth = 2;
                
                JLabel label = new JLabel(category.toUpperCase() + ": " + cp.scores.get(category), SwingConstants.CENTER);
                label.setFont(new Font("SansSerif", Font.BOLD, 13));
                label.setOpaque(true);
                if (category.contains("Total Score")) {
                    label.setBackground(new Color(218, 165, 32)); 
                    label.setForeground(Color.BLACK);
                    label.setFont(new Font("SansSerif", Font.BOLD, 15));
                } else {
                    label.setBackground(new Color(215, 215, 215));
                    label.setForeground(new Color(30, 30, 30));
                }
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(3, 10, 3, 10)));
                scorePanel.add(label, gbc);
            } else {
                gbc.gridx = col;
                gbc.gridy = row;
                gbc.gridwidth = 1;
                
                JButton btn = new JButton();
                btn.setFont(new Font("SansSerif", Font.BOLD, 11));
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(218, 165, 32), 1));
                
                if (colorCats.contains(category)) {
                    int colorIdx = colorCats.indexOf(category);
                    btn.setIcon(new ColorBarIcon(AWT_COLORS[colorIdx]));
                } else {
                    btn.setText(category);
                }

                Integer score = cp.scores.get(category);
                if (cp.filledCells.contains(category)) {
                    if (colorCats.contains(category)) {
                        btn.setText(": " + score);
                    } else {
                        btn.setText(category + ": " + score);
                    }
                    btn.setEnabled(false);
                    btn.setBackground(new Color(240, 240, 240));
                    btn.setForeground(Color.DARK_GRAY);
                } else {
                    int potential = calculateScore(category);
                    if (colorCats.contains(category)) {
                        btn.setText(" (" + potential + ")");
                    } else {
                        btn.setText(category + " (" + potential + ")");
                    }
                    btn.addActionListener(e -> assignScore(category, potential));
                    if (rollsLeft == 4) btn.setEnabled(false);
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(new Color(0, 102, 51)); 
                }
                scorePanel.add(btn, gbc);
                
                col++;
                if (col == 2) { col = 0; row++; }
            }
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
            } else {
                statusLabel.setText("Assign your score or roll again.");
            }
            updateScorePanel();
        }
    } // rollDice method ends here

    private void assignScore(String category, int score) {
        Player cp = players.get(currentPlayerIndex);
        if (isYahtzee() && cp.filledCells.contains("Yahtzee")) {
            cp.scores.put("Yahtzee", cp.scores.get("Yahtzee") + 100);
        }

        cp.scores.put(category, score);
        cp.filledCells.add(category);
        
        updateTotals();
        
        boolean allFinished = players.stream().allMatch(p -> p.filledCells.size() == totalTurnsPerPlayer);
        
        if (allFinished) {
            endGame();
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            while (players.get(currentPlayerIndex).filledCells.size() == totalTurnsPerPlayer) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
            
            resetTurn();
            statusLabel.setText(players.get(currentPlayerIndex).name + "'s turn! Roll to start.");
            updateScorePanel();
        }
    } // assignScore method ends here

    private void updateTotals() {
        Player cp = players.get(currentPlayerIndex);
        int upperSum = 0;
        String[] upperCats = {"Ones", "Twos", "Threes", "Fours", "Fives", "Sixes"};
        for (String c : upperCats) {
            upperSum += (cp.scores.get(c) != null ? cp.scores.get(c) : 0);
        }
        cp.scores.put("Upper Subtotal", upperSum);
        if (upperSum >= 84) cp.scores.put("Upper Bonus", 50);
        else cp.scores.put("Upper Bonus", 0);

        int colorSum = 0;
        String[] colorCats = {"Purples", "Reds", "Oranges", "Yellows", "Greens", "Blues"};
        for (String c : colorCats) {
            colorSum += (cp.scores.get(c) != null ? cp.scores.get(c) : 0);
        }
        cp.scores.put("Color Subtotal", colorSum);
        if (colorSum >= 84) cp.scores.put("Color Bonus", 50);
        else cp.scores.put("Color Bonus", 0);

        int total = upperSum + cp.scores.get("Upper Bonus") + colorSum + cp.scores.get("Color Bonus");
        String[] otherCats = {
            "Pair", "2 Pairs", "3 Pairs", "3 of a Kind", "2 * 3 of a Kind", "4 of a Kind", "5 of a Kind",
            "Small Straight", "Large Straight", "Huge Straight", "Full House", "Extended Full House",
            "3 of a Color", "2 * 3 of a Color", "4 of a Color", "5 of a Color", "Painted House",
            "Extended Painted House", "Rainbow", "Flush", "Yahtzee", "Chance"
        };
        for (String c : otherCats) {
            total += (cp.scores.get(c) != null ? cp.scores.get(c) : 0);
        }
        
        cp.totalScore = total;
        cp.scores.put("Total Score", total);
    } // updateTotals method ends here

    private void resetTurn() {
        rollsLeft = 4;
        rollsLabel.setText("Rolls left: " + rollsLeft);
        rollButton.setEnabled(true);
        for (Die d : dice) {
            d.setLocked(false);
            d.reset();
        }
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
            case "2 * 3 of a Kind": return getTwoThreeOfAKinds(valCounts);
            
            case "4 of a Kind": return getXOfAKind(valCounts, 4) * 4;
            case "5 of a Kind": return getXOfAKind(valCounts, 5) * 5;
            
            case "Small Straight": return isStraight(values, 4) ? 15 : 0;
            case "Large Straight": return isStraight(values, 5) ? 20 : 0;
            case "Huge Straight": return isStraight(values, 6) ? 35 : 0;
            
            case "Full House": return isFullHouse(valCounts, 3, 2) ? 25 : 0;
            case "Extended Full House": return isFullHouse(valCounts, 4, 2) ? 35 : 0;

            case "3 of a Color": return sumOfXOfAColor(colCounts, 3, dice);
            case "2 * 3 of a Color": return getTwoThreeOfAColor(colCounts, dice);
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
    } //sumOfXOfAColor method ends here

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
        statusLabel.setText("Game Over!");

        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.totalScore, p1.totalScore));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBackground(new Color(0, 80, 40));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 165, 32), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("FINAL RESULTS", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(new Color(218, 165, 32));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel(new GridBagLayout());
        resultsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;

        int currentRank = 1;
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            if (i > 0 && p.totalScore < sortedPlayers.get(i-1).totalScore) {
                currentRank = i + 1;
            }
            
            final int score = p.totalScore;
            List<String> tiedPlayers = sortedPlayers.stream()
                    .filter(pl -> pl.totalScore == score)
                    .map(pl -> pl.name)
                    .collect(Collectors.toList());
            
            if (i > 0 && p.totalScore == sortedPlayers.get(i-1).totalScore) continue;

            String rankStr = getRankString(currentRank);
            String names = String.join(", ", tiedPlayers.subList(0, tiedPlayers.size() - 1));
            if (tiedPlayers.size() > 1) {
                names = (names.isEmpty() ? "" : names + " and ") + tiedPlayers.get(tiedPlayers.size() - 1);
            } else {
                names = tiedPlayers.get(0);
            }
            
            JLabel rankLabel = new JLabel(rankStr + " PLACE: " + names.toUpperCase(), SwingConstants.LEFT);
            rankLabel.setFont(new Font("Serif", Font.BOLD, 20));
            
            // Rank-specific colors
            if (currentRank == 1) rankLabel.setForeground(new Color(218, 165, 32)); // Gold
            else if (currentRank == 2) rankLabel.setForeground(new Color(192, 192, 192)); // Silver
            else if (currentRank == 3) rankLabel.setForeground(new Color(205, 127, 50)); // Bronze
            else rankLabel.setForeground(Color.WHITE);

            JLabel scoreLabel = new JLabel(String.valueOf(score), SwingConstants.RIGHT);
            scoreLabel.setFont(new Font("Serif", Font.BOLD, 20));
            scoreLabel.setForeground(rankLabel.getForeground());

            gbc.gridy = i;
            gbc.weightx = 1.0;
            resultsPanel.add(rankLabel, gbc);
            gbc.weightx = 0.0;
            resultsPanel.add(scoreLabel, gbc);
        }

        int topScore = sortedPlayers.get(0).totalScore;
        List<String> winners = sortedPlayers.stream()
                .filter(p -> p.totalScore == topScore)
                .map(p -> p.name)
                .collect(Collectors.toList());
        
        JLabel congrats = new JLabel("Congratulations " + String.join(" and ", winners) + "!", SwingConstants.CENTER);
        congrats.setFont(new Font("Serif", Font.ITALIC, 22));
        congrats.setForeground(Color.WHITE);
        congrats.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(resultsPanel, BorderLayout.CENTER);
        centerPanel.add(congrats, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, mainPanel, "Game Results", JOptionPane.PLAIN_MESSAGE);

        Properties props = new Properties();
        String password = "dummy"; // set as dummy for default

        try (FileInputStream in = new FileInputStream("dbconfig.properties")) {
            props.load(in);
            password = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Could not load config file, using default.");
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Yahtzee", "root", password)) {
            for (Player p : players) {
                handleHighScore(p, conn);
            }
            showHighScores(conn);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Database error. High scores could not be processed.");
        }
        
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to play another game?", "Play Again", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    } // endGame method ends here

    private String getRankString(int rank) {
        if (rank == 1) return "1st";
        if (rank == 2) return "2nd";
        if (rank == 3) return "3rd";
        return rank + "th";
    } // getRankString method ends here

    private void handleHighScore(Player p, Connection conn) throws SQLException {
        String insertSql = "INSERT INTO hiscore (playerName, score, date) VALUES (?, ?, ?)";
        PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
        insertPstmt.setString(1, p.name);
        insertPstmt.setInt(2, p.totalScore);
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        insertPstmt.setTimestamp(3, currentTimestamp);
        insertPstmt.executeUpdate();

        String checkSql = "SELECT COUNT(*) FROM hiscore WHERE score > ? OR (score = ? AND date < ?)";
        PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
        checkPstmt.setInt(1, p.totalScore);
        checkPstmt.setInt(2, p.totalScore);
        checkPstmt.setTimestamp(3, currentTimestamp);
        ResultSet rs = checkPstmt.executeQuery();
        rs.next();
        int rank = rs.getInt(1) + 1;

        if (rank <= 10) {
            JOptionPane.showMessageDialog(this, "Congratulations, " + p.name + "! Your score of " + p.totalScore + " is rank #" + rank + " in the all-time high scores!");
        }

        String deleteSql = "DELETE FROM hiscore WHERE date NOT IN (SELECT date FROM (SELECT date FROM hiscore ORDER BY score DESC, date ASC LIMIT 10) as t)";
        conn.createStatement().executeUpdate(deleteSql);
    } // handleHighScore method ends here

    private void showHighScores(Connection conn) throws SQLException {
        String sql = "SELECT playerName, score, date FROM hiscore ORDER BY score DESC, date ASC LIMIT 10";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Player", "Score", "Date"}, 0);
        while (rs.next()) {
            model.addRow(new Object[]{rs.getString("playerName"), rs.getInt("score"), rs.getTimestamp("date")});
        }

        JTable table = new JTable(model);
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "All-Time High Scores", JOptionPane.INFORMATION_MESSAGE);
    } // showHighScores method ends here

    private void resetGame() {
        players.clear();
        currentPlayerIndex = 0;
        if (!setupPlayers()) System.exit(0);
        rollsLeft = 4;
        for (Die d : dice) d.reset();
        rollButton.setEnabled(true);
        statusLabel.setText(players.get(0).name + "'s turn! Roll to start.");
        updateScorePanel();
    } // resetGame method ends here

    private class Player {
        String name;
        Map<String, Integer> scores = new LinkedHashMap<>();
        Set<String> filledCells = new HashSet<>();
        int totalScore = 0;

        Player(String name) {
            this.name = name;
            initPlayerScores();
        }

        private void initPlayerScores() {
            scores.put("Ones", null);
            scores.put("Twos", null);
            scores.put("Threes", null);
            scores.put("Fours", null);
            scores.put("Fives", null);
            scores.put("Sixes", null);
            scores.put("Upper Subtotal", 0);
            scores.put("Upper Bonus", 0);

            scores.put("Purples", null);
            scores.put("Reds", null);
            scores.put("Oranges", null);
            scores.put("Yellows", null);
            scores.put("Greens", null);
            scores.put("Blues", null);
            scores.put("Color Subtotal", 0);
            scores.put("Color Bonus", 0);

            scores.put("Pair", null);
            scores.put("2 Pairs", null);
            scores.put("3 Pairs", null);
            scores.put("3 of a Kind", null);
            scores.put("2 * 3 of a Kind", null);
            scores.put("4 of a Kind", null);
            scores.put("5 of a Kind", null);
            scores.put("Small Straight", null);
            scores.put("Large Straight", null);
            scores.put("Huge Straight", null);
            scores.put("Full House", null);
            scores.put("Extended Full House", null);

            scores.put("3 of a Color", null);
            scores.put("2 * 3 of a Color", null);
            scores.put("4 of a Color", null);
            scores.put("5 of a Color", null);
            scores.put("Painted House", null);
            scores.put("Extended Painted House", null);
            scores.put("Rainbow", null);
            scores.put("Flush", null);

            scores.put("Yahtzee", null);
            scores.put("Chance", null);
            scores.put("Total Score", 0);
        } // initPlayerScores method ends here
    } // private class Player ends here

    private class Die {
        private int index;
        private int value = 0;
        private boolean locked = false;
        private JPanel panel;
        private JPanel dieGraphic;
        private JToggleButton holdButton;

        public Die(int index) {
            this.index = index;
            panel = new JPanel(new BorderLayout(0, 5));
            panel.setOpaque(false);
            
            dieGraphic = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    
                    Color baseColor;
                    if (value == 0) {
                        baseColor = Color.WHITE;
                    } else {
                        baseColor = AWT_COLORS[(index + value - 1) % 6];
                    }

                    // Glossy Las Vegas style with gradient
                    GradientPaint gp = new GradientPaint(0, 0, baseColor.brighter(), 0, h, baseColor.darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 14, 14);

                    // Border
                    g2.setColor(new Color(50, 50, 50));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1, 1, w-2, h-2, 14, 14);

                    if (value == 0) {
                        g2.setColor(Color.DARK_GRAY);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString("?", (w - fm.stringWidth("?")) / 2, (h + fm.getAscent()) / 2 - 4);
                    } else {
                        g2.setColor(Color.WHITE);
                        int d = w / 5; 
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
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.drawOval(x - d/2, y - d/2, d, d);
                    g2.setColor(Color.WHITE);
                }
            };
            dieGraphic.setPreferredSize(new Dimension(70, 70));
            dieGraphic.setOpaque(false);

            holdButton = new JToggleButton("HOLD");
            holdButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            holdButton.setFocusPainted(false);
            holdButton.setBackground(new Color(150, 0, 0)); 
            holdButton.setForeground(Color.WHITE);
            holdButton.setBorder(BorderFactory.createRaisedBevelBorder());
            // Override UI to prevent default blue selection color
            holdButton.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            holdButton.addActionListener(e -> {
                locked = holdButton.isSelected();
                updateHoldButtonState();
            });
            
            panel.add(dieGraphic, BorderLayout.CENTER);
            panel.add(holdButton, BorderLayout.SOUTH);
        } // Die method ends here

        private void updateHoldButtonState() {
            if (locked) {
                holdButton.setBackground(new Color(255, 40, 40)); // Semi-bright red
                holdButton.setText("HELD");
                holdButton.setBorder(BorderFactory.createLoweredBevelBorder());
            } else {
                holdButton.setBackground(new Color(150, 0, 0)); 
                holdButton.setText("HOLD");
                holdButton.setBorder(BorderFactory.createRaisedBevelBorder());
            }
        } // updateHoldButtonState ends here

        public void roll() {
            if (!locked) {
                value = new Random().nextInt(6) + 1;
                updateUI();
            }
        } // roll method ends here

        public void minReset() {
            value = 0;
            locked = false;
            holdButton.setSelected(false);
            updateHoldButtonState();
            updateUI();
        } // reset method ends here

        public void reset() {
            value = 0;
            locked = false;
            holdButton.setSelected(false);
            updateHoldButtonState();
            updateUI();
        } // reset method ends here

        public void setLocked(boolean locked) {
            this.locked = locked;
            holdButton.setSelected(locked);
            updateHoldButtonState();
        } // setLocked method ends here

        public int getValue() { return value; }
        public String getColor() {
            if (value == 0) return "";
            return COLORS[(index + value - 1) % 6];
        } // getValue method ends here

        private void updateUI() {
            dieGraphic.repaint();
        } // updateUI method ends here

        public Component getComponent() { return panel; }
    } // private class Die ends here

    private static class ColorBarIcon implements Icon {
        private final Color color;
        public ColorBarIcon(Color color) { this.color = color; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x + 2, y + 2, 70, 18);
            g.setColor(Color.BLACK);
            g.drawRect(x + 2, y + 2, 70, 18);
        }
        @Override public int getIconWidth() { return 74; }
        @Override public int getIconHeight() { return 22; }
    } // ColorBarIcon method ends here

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SixDiceColorYahtzee::new);
    } // main method ends here
} // class SixDiceColorYahtzee ends here
