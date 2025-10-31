package edu.una.lab612.consumirapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.http.*;
import java.net.URI;
import java.net.URL;
import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class API extends javax.swing.JFrame {

    public API() {
        initComponents();
        setTitle("GitHub API Client");
    setLocationRelativeTo(null);

    reposTable.setModel(new DefaultTableModel(
        new Object[][] {},
        new String[] {"Nombre", "Descripción", "Lenguaje", "⭐", "Forks", "Actualizado"}
    ));

    statusLabel.setText("Listo.");
    btnSearch.addActionListener(e -> buscarUsuario());

    }
private void buscarUsuario() {
    String username = usernameField.getText().trim();
    if (username.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor escribe un nombre de usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    statusLabel.setText("Buscando usuario: " + username + " ...");

    new Thread(() -> {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();


            HttpRequest requestUser = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + username))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "java-swing-client")
                    .build();

            HttpResponse<String> responseUser = client.send(requestUser, HttpResponse.BodyHandlers.ofString());

            if (responseUser.statusCode() == 404) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Usuario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE));
                statusLabel.setText("Usuario no encontrado.");
                return;
            }

            Map<String, Object> userData = mapper.readValue(responseUser.body(), new TypeReference<Map<String, Object>>() {});

            HttpRequest requestRepos = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + username + "/repos?per_page=100&sort=updated"))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "java-swing-client")
                    .build();

            HttpResponse<String> responseRepos = client.send(requestRepos, HttpResponse.BodyHandlers.ofString());
            List<Map<String, Object>> repos = mapper.readValue(responseRepos.body(), new TypeReference<List<Map<String, Object>>>() {});

            SwingUtilities.invokeLater(() -> {
                mostrarUsuario(userData);
                mostrarRepositorios(repos);
                mostrarLenguajes(repos);
                statusLabel.setText("Datos de " + username + " mostrados.");
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error al obtener datos.");
            });
        }
    }).start();
}
private void mostrarUsuario(Map<String, Object> user) {
    nameLabel.setText(user.get("name") + " (" + user.get("login") + ")");
    bioLabel.setText("<html>" + (user.get("bio") == null ? "" : user.get("bio")) + "</html>");
    statsLabel.setText(String.format(
            "<html>Repos: %s<br>Followers: %s - Following: %s<br>Ubicación: %s<br>Blog: %s</html>",
            user.get("public_repos"), user.get("followers"), user.get("following"),
            user.get("location") == null ? "-" : user.get("location"),
            user.get("blog") == null ? "-" : user.get("blog")
    ));

    // Cargar el avatar
    try {
        String avatarUrl = (String) user.get("avatar_url");
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            URL url = new URL(avatarUrl);
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
            avatarLabel.setText("");
        }
    } catch (Exception e) {
        avatarLabel.setIcon(null);
    }
}
private void mostrarRepositorios(List<Map<String, Object>> repos) {
    DefaultTableModel model = (DefaultTableModel) reposTable.getModel();
    model.setRowCount(0); // limpiar tabla

    for (Map<String, Object> repo : repos) {
        model.addRow(new Object[]{
            repo.get("name"),
            repo.get("description"),
            repo.get("language"),
            repo.get("stargazers_count"),
            repo.get("forks_count"),
            repo.get("updated_at")
        });
    }
}
private void mostrarLenguajes(List<Map<String, Object>> repos) {
    languagesPanel.removeAll();

    Map<String, Long> counts = repos.stream()
            .map(r -> (String) r.get("language"))
            .filter(l -> l != null && !l.isBlank())
            .collect(Collectors.groupingBy(l -> l, Collectors.counting()));

    if (counts.isEmpty()) {
        languagesPanel.add(new JLabel("No hay datos de lenguajes."));
    } else {
        languagesPanel.add(new JLabel("Lenguajes más usados:"));
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            String lang = entry.getKey();
            Long count = entry.getValue();
            languagesPanel.add(new JLabel(lang + " (" + count + " repos)"));
        }
    }

    languagesPanel.revalidate();
    languagesPanel.repaint();
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();
        usernameField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        avatarLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        bioLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        statsLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        languagesPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        reposTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSearch.setText("Buscar");
        jPanel1.add(btnSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 40, -1, -1));
        jPanel1.add(usernameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 160, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 0, 18)); // NOI18N
        jLabel1.setText("USER NAME:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 140, -1));

        jLabel2.setText("AVATAR:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, -1, -1));
        jPanel1.add(avatarLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 90, 70));

        jLabel3.setText("NOMBRE:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));
        jPanel1.add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 180, -1, -1));

        jLabel4.setText("BIO:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 440, -1, -1));
        jPanel1.add(bioLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 220, -1, -1));

        jLabel5.setText("REPOS/SEGUIDORES/UBICACION:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 220, -1));
        jPanel1.add(statsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 260, -1, -1));

        jLabel6.setText("ESTATUS:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 400, -1, -1));

        javax.swing.GroupLayout languagesPanelLayout = new javax.swing.GroupLayout(languagesPanel);
        languagesPanel.setLayout(languagesPanelLayout);
        languagesPanelLayout.setHorizontalGroup(
            languagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );
        languagesPanelLayout.setVerticalGroup(
            languagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        jPanel1.add(languagesPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 70, 70));
        jPanel1.add(statusLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 400, -1, -1));

        reposTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(reposTable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new API().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avatarLabel;
    private javax.swing.JLabel bioLabel;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel languagesPanel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTable reposTable;
    private javax.swing.JLabel statsLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
