import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibraryManagementSystem {
    private JFrame frame;
    private JTextField usernameField, passwordField, bookNameField, categoryField, copiesField;
    private String currentUserRole; // to determine if the user is admin

    public LibraryManagementSystem() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new CardLayout());

        // Login Panel
        JPanel loginPanel = createLoginPanel();
        frame.add(loginPanel, "Login");

        // Dashboard Panel
        JPanel dashboardPanel = createDashboardPanel();
        frame.add(dashboardPanel, "Dashboard");

        frame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginAction());
        gbc.gridx = 1; gbc.gridy = 2;
        loginPanel.add(loginButton, gbc);

        return loginPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new FlowLayout());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> showPanel(createLoginPanel()));
        dashboardPanel.add(logoutButton);

        JButton manageBooksButton = new JButton("Book Management");
        manageBooksButton.addActionListener(e -> showBookManagementPanel());
        dashboardPanel.add(manageBooksButton);

        JButton issueReturnButton = new JButton("Issue/Return Book");
        issueReturnButton.addActionListener(e -> showIssueReturnPanel());
        dashboardPanel.add(issueReturnButton);

        return dashboardPanel;
    }

    private void showPanel(JPanel panel) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showBookManagementPanel() {
        if (!"admin".equals(currentUserRole)) {
            JOptionPane.showMessageDialog(frame, "Access Denied! Admin login required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel bookManagementPanel = createBookManagementPanel();
        showPanel(bookManagementPanel);
    }

    private JPanel createBookManagementPanel() {
        JPanel bookManagementPanel = new JPanel();
        bookManagementPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        bookManagementPanel.add(new JLabel("Book Name:"), gbc);
        bookNameField = new JTextField(15);
        gbc.gridx = 1;
        bookManagementPanel.add(bookNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        bookManagementPanel.add(new JLabel("Category:"), gbc);
        categoryField = new JTextField(15);
        gbc.gridx = 1;
        bookManagementPanel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        bookManagementPanel.add(new JLabel("Copies:"), gbc);
        copiesField = new JTextField(15);
        gbc.gridx = 1;
        bookManagementPanel.add(copiesField, gbc);

        JButton addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> addBookToDatabase());
        gbc.gridx = 0; gbc.gridy = 3;
        bookManagementPanel.add(addBookButton, gbc);

        JButton removeBookButton = new JButton("Remove Book");
        removeBookButton.addActionListener(e -> removeBookFromDatabase());
        gbc.gridx = 1;
        bookManagementPanel.add(removeBookButton, gbc);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> showPanel(createDashboardPanel()));
        gbc.gridx = 1; gbc.gridy = 4;
        bookManagementPanel.add(backButton, gbc);

        return bookManagementPanel;
    }

    private void showIssueReturnPanel() {
        JPanel issueReturnPanel = createIssueReturnPanel();
        showPanel(issueReturnPanel);
    }

    private JPanel createIssueReturnPanel() {
        JPanel issueReturnPanel = new JPanel();
        issueReturnPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        issueReturnPanel.add(new JLabel("Book Name:"), gbc);
        bookNameField = new JTextField(15);
        gbc.gridx = 1;
        issueReturnPanel.add(bookNameField, gbc);

        JButton issueButton = new JButton("Issue Book");
        issueButton.addActionListener(e -> issueBook());
        gbc.gridx = 0; gbc.gridy = 1;
        issueReturnPanel.add(issueButton, gbc);

        JButton returnButton = new JButton("Return Book");
        returnButton.addActionListener(e -> returnBook());
        gbc.gridx = 1;
        issueReturnPanel.add(returnButton, gbc);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> showPanel(createDashboardPanel()));
        gbc.gridx = 1; gbc.gridy = 2;
        issueReturnPanel.add(backButton, gbc);

        return issueReturnPanel;
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (adminLogin(username, password)) {
                currentUserRole = "admin";
                showPanel(createDashboardPanel());
            } else if (userLogin(username, password)) {
                currentUserRole = "user";
                showPanel(createDashboardPanel());
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean adminLogin(String username, String password) {
        String sql = "SELECT * FROM Admin WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error logging in: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean userLogin(String username, String password) {
        String sql = "SELECT * FROM User WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error logging in: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void addBookToDatabase() {
        String name = bookNameField.getText().trim();
        String category = categoryField.getText().trim();
        String copiesText = copiesField.getText().trim();

        if (name.isEmpty() || category.isEmpty() || copiesText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int copies = Integer.parseInt(copiesText);
            String sql = "INSERT INTO Book (name, category, copies) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, category);
                pstmt.setInt(3, copies);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Book added successfully!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Copies must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeBookFromDatabase() {
        String name = bookNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Book name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "DELETE FROM Book WHERE name = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(frame, "Book removed successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "No such book found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error removing book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void issueBook() {
        String name = bookNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Book name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE Book SET copies = copies - 1 WHERE name = ? AND copies > 0";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(frame, "Book issued successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Book not available for issuing.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error issuing book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void returnBook() {
        String name = bookNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Book name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE Book SET copies = copies + 1 WHERE name = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Book returned successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error returning book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryManagementSystem::new);
    }
}

class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library"; // Update with your database details
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }
}
