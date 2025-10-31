package edu.una.lab612.consumirapi.ui;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;

import edu.una.lab612.consumirapi.domain.Repo;
import edu.una.lab612.consumirapi.domain.User;
import edu.una.lab612.consumirapi.service.GitHubApi;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainSwingApp {
    private final GitHubApi api = new GitHubApi();

    private JFrame frame;
    private JTextField usernameField;
    private JButton btnSearch;
    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel bioLabel;
    private JLabel statsLabel;
    private JTable reposTable;
    private DefaultTableModel reposModel;
    private JPanel languagesPanel;
    private JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new MainSwingApp().createAndShowGUI(); }
            catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("GitHub Swing Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernameField = new JTextField(20);
        btnSearch = new JButton("Buscar");
        top.add(new JLabel("Usuario:"));
        top.add(usernameField);
        top.add(btnSearch);
        frame.add(top, BorderLayout.NORTH);

        // Center split: left profile, right repos
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.3);
        split.setLeftComponent(buildProfilePanel());
        split.setRightComponent(buildReposPanel());
        frame.add(split, BorderLayout.CENTER);

        statusLabel = new JLabel("Listo.");
        frame.add(statusLabel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> onSearch());

        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildProfilePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout(8,8));
        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(avatarLabel, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 16f));
        bioLabel = new JLabel();
        bioLabel.setVerticalAlignment(SwingConstants.TOP);
        statsLabel = new JLabel();
        info.add(nameLabel);
        info.add(Box.createVerticalStrut(6));
        info.add(bioLabel);
        info.add(Box.createVerticalStrut(10));
        info.add(statsLabel);
        p.add(info, BorderLayout.CENTER);

        languagesPanel = new JPanel();
        languagesPanel.setLayout(new BoxLayout(languagesPanel, BoxLayout.Y_AXIS));
        p.add(new JScrollPane(languagesPanel), BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildReposPanel() {
        JPanel p = new JPanel(new BorderLayout());
        reposModel = new DefaultTableModel(new Object[]{"Nombre","Descripción","Lenguaje","⭐","Forks","Actualizado"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        reposTable = new JTable(reposModel);
        p.add(new JScrollPane(reposTable), BorderLayout.CENTER);
        return p;
    }

    private void onSearch() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Escribe un username.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setStatus("Buscando " + username + " ...");
        // Ejecutar en hilo separado para no bloquear UI
        new Thread(() -> {
            try {
                User user = api.getUser(username);
                List<Repo> repos = api.getRepos(username);
                SwingUtilities.invokeLater(() -> {
                    showUser(user);
                    showRepos(repos);
                    showLanguagesSummary(repos);
                    setStatus("Mostrados datos de " + username);
                });
            } catch (GitHubApi.ApiException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error API", JOptionPane.ERROR_MESSAGE);
                    setStatus("Error: " + ex.getMessage());
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Error al conectarse: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    setStatus("Error: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void showUser(User user) {
        nameLabel.setText(user.name != null ? user.name + " (" + user.login + ")" : user.login);
        bioLabel.setText("<html>" + (user.bio != null ? user.bio : "") + "</html>");
        statsLabel.setText(String.format("<html>Repos: %d<br/>Followers: %d - Following: %d<br/>Ubicación: %s<br/>Blog: %s</html>",
                user.publicRepos, user.followers, user.following,
                user.location == null ? "-" : user.location,
                user.blog == null ? "-" : user.blog));
        // Cargar avatar
        if (user.avatarUrl != null && !user.avatarUrl.isBlank()) {
            try {
                URL url = new URL(user.avatarUrl);
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                avatarLabel.setIcon(null);
            }
        } else {
            avatarLabel.setIcon(null);
        }
    }

    private void showRepos(List<Repo> repos) {
        reposModel.setRowCount(0);
        repos.forEach(r -> reposModel.addRow(new Object[]{
                r.name,
                r.description == null ? "" : r.description,
                r.language == null ? "" : r.language,
                r.stargazersCount,
                r.forksCount,
                r.updatedAt
        }));
    }

    private void showLanguagesSummary(List<Repo> repos) {
        languagesPanel.removeAll();
        Map<String, Long> counts = repos.stream()
                .map(r -> r.language == null ? "Unknown" : r.language)
                .collect(Collectors.groupingBy(l -> l, Collectors.counting()));
        // Orden descendente
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a,b) -> Long.compare(b.getValue(), a.getValue()));
        int total = repos.size() == 0 ? 1 : repos.size();
        languagesPanel.add(new JLabel("Resumen de lenguajes (por cantidad de repos):"));
        for (Map.Entry<String, Long> e : sorted) {
            double pct = (100.0 * e.getValue()) / total;
            JPanel row = new JPanel(new BorderLayout());
            row.add(new JLabel(e.getKey() + " (" + e.getValue() + ") - " + String.format("%.1f%%", pct)), BorderLayout.WEST);
            // una barra simple con JLabel
            JProgressBar bar = new JProgressBar(0,100);
            bar.setValue((int) Math.round(pct));
            bar.setStringPainted(true);
            row.add(bar, BorderLayout.CENTER);
            languagesPanel.add(row);
        }
        languagesPanel.revalidate();
        languagesPanel.repaint();
    }

    private void setStatus(String s) {
        statusLabel.setText(s);
    }
}