package com.example.chatapp;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Integrated ChatApp1: contains User/Login/Register (from Part 1)
 * plus QuickChat menu and Message class for Part 2.
 *
 * NOTE: Put this file under package com.example.chatapp and compile/run.
 */
public class ChatApp1 {

    // --------- User Class ---------
    static class User {
        private String fullname, gender, username, password, phone, profileImagePath;

        public User(String fullname, String gender, String username,
                    String password, String phone, String profileImagePath) {
            this.fullname = fullname;
            this.gender = gender;
            this.username = username;
            this.password = password;
            this.phone = phone;
            this.profileImagePath = profileImagePath;
        }

        public String getFullname() { return fullname; }
        public String getGender() { return gender; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getPhone() { return phone; }
        public String getProfileImagePath() { return profileImagePath; }
    }

    // --------- Login Logic (same validation behaviour as previously) ---------
    static class Login {
        protected static HashMap<String, User> users = new HashMap<>();

        public boolean checkUsername(String username) {
            return username.contains("_") && username.length() <= 5;
        }

        public boolean checkPasswordComplexity(String password) {
            return password != null && password.length() > 8;
        }

        public boolean checkCellphone(String phone) {
            // Practical rule: must start with + and commonly international length between 10-13 characters
            return phone != null && phone.startsWith("+") && phone.length() >= 10 && phone.length() <= 13;
        }

        public String registerUser(String fullname, String gender, String username,
                                   String password, String confirmPassword,
                                   String phone, String imagePath) {

            // Collect missing fields
            StringBuilder missingFields = new StringBuilder();

            if (fullname == null || fullname.isEmpty()) missingFields.append("Full Name, ");
            if (username == null || username.isEmpty()) missingFields.append("Username, ");
            if (phone == null || phone.isEmpty()) missingFields.append("Phone Number, ");
            if (password == null || password.isEmpty()) missingFields.append("Password, ");
            if (confirmPassword == null || confirmPassword.isEmpty()) missingFields.append("Confirm Password, ");
            if (gender == null || gender.isEmpty()) missingFields.append("Gender, ");

            // If any fields are missing
            if (missingFields.length() > 0) {
                String fields = missingFields.substring(0, missingFields.length() - 2);
                if (fields.contains(",")) {
                    return "Please fill in the following fields: " + fields + ".";
                } else {
                    return "Please fill in the " + fields + ".";
                }
            }

            // Username validation
            if (!checkUsername(username)) {
                return "Username must contain an underscore and be no longer than 5 characters.";
            }

            // Password validation (still just length > 8)
            if (!checkPasswordComplexity(password)) {
                return "Password must be at least 8 characters long and include letters.";
            }

            // Cellphone validation
            if (!checkCellphone(phone)) {
                return "Phone number is incorrectly formatted. Please include an international code (example: +27831234567).";
            }

            // Password match check
            if (!password.equals(confirmPassword)) {
                return "Passwords do not match. Please re-enter your password.";
            }

            // Duplicate username check
            if (users.containsKey(username)) {
                return "This username is already taken. Please choose a different one.";
            }

            // ✅ Success
            User user = new User(fullname, gender, username, password, phone, imagePath);
            users.put(username, user);
            return "Registration successful!";
        }

        public boolean loginUser(String username, String password) {
            User user = users.get(username);
            return user != null && user.getPassword().equals(password);
        }

        public String returnLoginStatus(boolean loginSuccess, String username) {
            if (loginSuccess) {
                return "Welcome " + username + ", it is great to see you again.";
            }
            return "Username or password incorrect, please try again.";
        }
    }

    // ------------------ Message class for Part 2 ------------------
    public static class Message {
        private String messageID;
        private String recipientCell;
        private String messageText;
        private int messageNumber; // 0-based index for this message
        private String messageHash;

        private static int sentCounter = 0; // counts messages that have been *sent*
        private static List<Message> sentMessages = new ArrayList<>();

        public Message(String messageID, String recipientCell, String messageText, int messageNumber) {
            this.messageID = messageID;
            this.recipientCell = recipientCell;
            this.messageText = messageText;
            this.messageNumber = messageNumber;
            this.messageHash = createMessageHash();
        }

        // Boolean: checkMessageID() - ensure no more than 10 characters
        public boolean checkMessageID() {
            return messageID != null && messageID.length() <= 10;
        }

        // Int: checkRecipientCell() - ensures recipient cell is properly formatted
        // returns 1 for success, 0 for failure (POE asked method to be int)
        public int checkRecipientCell() {
            if (recipientCell == null) return 0;
            // Practical validation:
            if (!recipientCell.startsWith("+")) return 0;
            int len = recipientCell.length();
            if (len < 10 || len > 13) return 0; // accepts typical international lengths (adjustable)
            // ensure remaining chars are digits
            String rest = recipientCell.substring(1);
            return rest.matches("\\d+") ? 1 : 0;
        }

        // String: createMessageHash()
        // first two digits of messageID (pad with 0s if needed) : messageNumber : first+last words of message in caps
        public String createMessageHash() {
            String idPart = messageID != null && messageID.length() >= 2 ? messageID.substring(0, 2) : String.format("%02d", 0);
            String text = messageText == null ? "" : messageText.trim();
            String[] words = text.split("\\s+");
            String first = words.length >= 1 ? words[0].replaceAll("[^a-zA-Z0-9]", "") : "";
            String last = words.length >= 2 ? words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "") : first;
            String combined = (first + last).toUpperCase();
            return (idPart + ":" + messageNumber + ":" + combined).toUpperCase();
        }

        // String: sentMessage()
        // Interactive: asks user via JOptionPane; but we provide an overloaded method for tests
        public String sentMessageInteractive() {
            String[] options = {"Send Message", "Store Message", "Disregard Message"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Choose action for this message:",
                    "Message Action",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            return sentMessage(choice + 1); // our switch uses 1-based mapping
        }

        // Overloaded version for tests / programmatic calls: 1=Send,2=Store,3=Disregard
        public String sentMessage(int choice) {
            switch (choice) {
                case 1:
                    // Send
                    sentCounter++;
                    sentMessages.add(this);
                    // after sending we persist current sentMessages to JSON
                    storeAllSentMessagesToJSON();
                    return "Message successfully sent.";
                case 2:
                    // Store (store to JSON without incrementing sent counter)
                    storeMessageAsDraft();
                    return "Message successfully stored.";
                case 3:
                    // Disregard
                    return "Press 0 to delete message.";
                default:
                    return "Invalid choice.";
            }
        }

        // String: printMessages() - returns list of sent messages
        public static String printMessages() {
            if (sentMessages.isEmpty()) return "No messages sent yet.";
            StringBuilder sb = new StringBuilder();
            for (Message m : sentMessages) {
                sb.append("MessageID: ").append(m.getMessageID()).append("\n")
                  .append("MessageHash: ").append(m.getMessageHash()).append("\n")
                  .append("Recipient: ").append(m.getRecipientCell()).append("\n")
                  .append("Message: ").append(m.getMessageText()).append("\n\n");
            }
            return sb.toString();
        }

        // Int: returnTotalMessagess() - total sent
        public static int returnTotalMessagess() {
            return sentCounter;
        }

        // storeMessage(): append this message as a draft to JSON (for stored messages)
        public void storeMessageAsDraft() {
            // store this single message into a separate "stored_messages.json"
            List<Map<String, String>> drafts = readJsonListFromFile("stored_messages.json");
            Map<String, String> entry = new HashMap<>();
            entry.put("MessageID", messageID);
            entry.put("MessageHash", messageHash);
            entry.put("Recipient", recipientCell);
            entry.put("Message", messageText);
            drafts.add(entry);
            writeJsonListToFile("stored_messages.json", drafts);
        }

        // Helper: store all sent messages (sentMessages list) to messages.json
        private static void storeAllSentMessagesToJSON() {
            List<Map<String, String>> list = new ArrayList<>();
            for (Message m : sentMessages) {
                Map<String, String> obj = new HashMap<>();
                obj.put("MessageID", m.getMessageID());
                obj.put("MessageHash", m.getMessageHash());
                obj.put("Recipient", m.getRecipientCell());
                obj.put("Message", m.getMessageText());
                list.add(obj);
            }
            writeJsonListToFile("messages.json", list);
        }

        // Generic JSON helpers (simple manual JSON list writer)
        private static List<Map<String, String>> readJsonListFromFile(String filename) {
            try {
                Path p = Paths.get(filename);
                if (!Files.exists(p)) return new ArrayList<>();
                String content = new String(Files.readAllBytes(p));
                // Very small lightweight parser: assume file contains JSON array of objects each with string values.
                // For robustness in production use a JSON library. For this assignment this is sufficient.
                // If empty or not parseable, return empty list.
                if (content.trim().isEmpty()) return new ArrayList<>();
                // Very crude parse: not robust for nested objects — but OK for our simple schema
                List<Map<String, String>> out = new ArrayList<>();
                String trimmed = content.trim();
                if (!trimmed.startsWith("[")) return out;
                // Remove leading/trailing square brackets
                String inner = trimmed.substring(1, trimmed.length() - 1).trim();
                if (inner.isEmpty()) return out;
                // Split on "}," boundaries
                String[] items = inner.split("\\},\\s*\\{");
                for (String item : items) {
                    String it = item;
                    if (!it.startsWith("{")) it = "{" + it;
                    if (!it.endsWith("}")) it = it + "}";
                    Map<String, String> map = new HashMap<>();
                    // find "key":"value" pairs
                    Scanner s = new Scanner(it);
                    s.useDelimiter("\",\\s*\"");
                    while (s.hasNext()) {
                        String part = s.next();
                        // remove braces/quotes
                        part = part.replaceAll("[\\{\\}\"]", "");
                        String[] kv = part.split(":", 2);
                        if (kv.length == 2) {
                            String k = kv[0].trim().replaceAll("^\"|\"$", "");
                            String v = kv[1].trim().replaceAll("^\"|\"$", "");
                            map.put(k, v);
                        }
                    }
                    out.add(map);
                    s.close();
                }
                return out;
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        }

        private static void writeJsonListToFile(String filename, List<Map<String, String>> list) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
                writer.write("[\n");
                for (int i = 0; i < list.size(); i++) {
                    Map<String, String> map = list.get(i);
                    writer.write("  {\n");
                    int j = 0;
                    for (Map.Entry<String, String> e : map.entrySet()) {
                        writer.write("    \"" + escapeJson(e.getKey()) + "\": \"" + escapeJson(e.getValue()) + "\"");
                        j++;
                        if (j < map.size()) writer.write(",");
                        writer.write("\n");
                    }
                    writer.write("  }");
                    if (i < list.size() - 1) writer.write(",");
                    writer.write("\n");
                }
                writer.write("]\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }

        // getters
        public String getMessageID() { return messageID; }
        public String getRecipientCell() { return recipientCell; }
        public String getMessageText() { return messageText; }
        public String getMessageHash() { return messageHash; }
    }

    // --------- Registration Form (same as before, unchanged) ---------
    static class RegisterForm {
        private JFrame frame;
        private JTextField fullnameField, usernameField, phoneField;
        private JPasswordField passwordField, confirmpasswordField;
        private JRadioButton maleRadioButton, femaleRadioButton;
        private ButtonGroup genderGroup;
        private JLabel profilePictureImage;
        private JButton browseButton, buttonRegister, returnToLoginButton;
        private String selectedImagePath;

        public RegisterForm() {
            frame = new JFrame("Register Form");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 750);
            frame.setLocationRelativeTo(null);

            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBackground(Color.BLACK);
            contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Full Name
            gbc.gridx = 0; gbc.gridy = 0;
            contentPanel.add(new JLabel("Full Name") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            fullnameField = new JTextField(15);
            contentPanel.add(fullnameField, gbc);

            // Username
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Username") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            usernameField = new JTextField(15);
            contentPanel.add(usernameField, gbc);

            // Phone
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Phone") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            phoneField = new JTextField(15);
            contentPanel.add(phoneField, gbc);

            // Password
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Password") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            contentPanel.add(passwordField, gbc);

            // Show password checkbox
            gbc.gridx = 1; gbc.gridy++;
            JCheckBox showPasswordCheck = new JCheckBox("Show");
            showPasswordCheck.setForeground(Color.BLUE);
            showPasswordCheck.setBackground(Color.BLACK);
            contentPanel.add(showPasswordCheck, gbc);
            showPasswordCheck.addActionListener(e ->
                    passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char)0 : '•')
            );

            // Confirm Password
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Confirm Password") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            confirmpasswordField = new JPasswordField(15);
            contentPanel.add(confirmpasswordField, gbc);

            // Show confirm password checkbox
            gbc.gridx = 1; gbc.gridy++;
            JCheckBox showConfirmPasswordCheck = new JCheckBox("Show");
            showConfirmPasswordCheck.setForeground(Color.BLUE);
            showConfirmPasswordCheck.setBackground(Color.BLACK);
            contentPanel.add(showConfirmPasswordCheck, gbc);
            showConfirmPasswordCheck.addActionListener(e ->
                    confirmpasswordField.setEchoChar(showConfirmPasswordCheck.isSelected() ? (char)0 : '•')
            );

            // Gender
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Gender") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            JPanel genderPanel = new JPanel();
            genderPanel.setBackground(Color.BLACK);
            maleRadioButton = new JRadioButton("Male"); maleRadioButton.setForeground(Color.BLUE); maleRadioButton.setBackground(Color.BLACK);
            femaleRadioButton = new JRadioButton("Female"); femaleRadioButton.setForeground(Color.BLUE); femaleRadioButton.setBackground(Color.BLACK);
            genderGroup = new ButtonGroup();
            genderGroup.add(maleRadioButton); genderGroup.add(femaleRadioButton);
            genderPanel.add(maleRadioButton); genderPanel.add(femaleRadioButton);
            contentPanel.add(genderPanel, gbc);

            // Profile Picture
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Profile Picture") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            profilePictureImage = new JLabel();
            profilePictureImage.setPreferredSize(new Dimension(120, 120));
            Border border = BorderFactory.createLineBorder(Color.BLUE, 2);
            profilePictureImage.setBorder(border);
            contentPanel.add(profilePictureImage, gbc);

            gbc.gridy++;
            browseButton = new JButton("Browse");
            contentPanel.add(browseButton, gbc);
            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    selectedImagePath = file.getAbsolutePath();
                    ImageIcon icon = new ImageIcon(new ImageIcon(selectedImagePath).getImage()
                            .getScaledInstance(profilePictureImage.getWidth(), profilePictureImage.getHeight(), Image.SCALE_SMOOTH));
                    profilePictureImage.setIcon(icon);
                }
            });

            // Register button
            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            JButton buttonRegister = new JButton("Register");
            contentPanel.add(buttonRegister, gbc);

            // Return to Login button
            returnToLoginButton = new JButton("Return");
            returnToLoginButton.addActionListener(e -> {
                frame.setVisible(false);
                new LoginForm(); // Show login form
            });
            contentPanel.add(returnToLoginButton, gbc);

            Login loginLogic = new Login();
            buttonRegister.addActionListener(e -> {
                String fullname = fullnameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmpasswordField.getPassword());
                String phone = phoneField.getText().trim();
                String gender = maleRadioButton.isSelected() ? "Male" : femaleRadioButton.isSelected() ? "Female" : "";

                String registrationMessage = loginLogic.registerUser(
                        fullname, gender, username, password, confirmPassword, phone, selectedImagePath
                );

                JOptionPane.showMessageDialog(frame, registrationMessage);
                if (registrationMessage.equals("Registration successful!")) {
                    frame.setVisible(false); // Close the registration form
                    new LoginForm(); // Open login page
                }
            });

            frame.add(contentPanel);
            frame.setVisible(true);
        }
    }

    // --------- Login Form (styled) ---------
    static class LoginForm {
        private JFrame frame;
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton, registerButton;

        public LoginForm() {
            frame = new JFrame("Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 400);
            frame.setLocationRelativeTo(null);

            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBackground(Color.BLACK);
            contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Username
            gbc.gridx = 0; gbc.gridy = 0;
            contentPanel.add(new JLabel("Username") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            usernameField = new JTextField(15);
            contentPanel.add(usernameField, gbc);

            // Password
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Password") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            contentPanel.add(passwordField, gbc);

            // Show password checkbox
            gbc.gridx = 1; gbc.gridy++;
            JCheckBox showPasswordCheck = new JCheckBox("Show");
            showPasswordCheck.setForeground(Color.BLUE);
            showPasswordCheck.setBackground(Color.BLACK);
            contentPanel.add(showPasswordCheck, gbc);
            showPasswordCheck.addActionListener(e ->
                    passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char)0 : '•')
            );

            // Login button
            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            loginButton = new JButton("Login");
            contentPanel.add(loginButton, gbc);

            // Register button (link to registration)
            gbc.gridy++;
            registerButton = new JButton("Register");
            registerButton.setForeground(Color.BLUE);
            registerButton.setBackground(Color.WHITE);
            contentPanel.add(registerButton, gbc);

            Login loginLogic = new Login();
            loginButton.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                boolean loginSuccess = loginLogic.loginUser(username, password);
                String message = loginLogic.returnLoginStatus(loginSuccess, username);
                JOptionPane.showMessageDialog(frame, message);
                if (loginSuccess) {
                    frame.setVisible(false);
                    runQuickChatLoop(username); // launch quickchat for logged-in user
                }
            });

            registerButton.addActionListener(e -> {
                frame.setVisible(false);
                new RegisterForm(); // Open register form
            });

            frame.add(contentPanel);
            frame.setVisible(true);
        }
    }

    // --------- QuickChat loop (part 2) ---------
    private static void runQuickChatLoop(String loggedInUsername) {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.");

        // Ask user how many messages they want to enter
        String numStr = JOptionPane.showInputDialog("How many messages will you enter?");
        int maxMessages = 0;
        try {
            maxMessages = Integer.parseInt(numStr);
            if (maxMessages < 1) {
                JOptionPane.showMessageDialog(null, "Number of messages must be at least 1. Exiting QuickChat.");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Invalid number entered. Exiting QuickChat.");
            return;
        }

        int entered = 0;
        while (true) {
            String[] options = {"Send Messages", "Show recently sent messages", "Quit"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Choose an option",
                    "QuickChat Menu",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) { // Send Messages
                if (entered >= maxMessages) {
                    JOptionPane.showMessageDialog(null, "You reached the number of messages you declared (" + maxMessages + ").");
                    continue;
                }

                // Generate message details
                String messageID = generateRandomDigitString(10); // up to 10 digits
                String recipient = JOptionPane.showInputDialog("Enter Recipient (include international code, e.g. +27831234567):");
                String messageText = JOptionPane.showInputDialog("Enter message (250 chars max):");

                if (messageText == null) messageText = "";
                if (messageText.length() > 250) {
                    int over = messageText.length() - 250;
                    JOptionPane.showMessageDialog(null, "Message exceeds 250 characters by " + over + ", please reduce size.");
                    continue;
                }

                // Create messageNumber equal to current total messages (0-based)
                int messageNumber = Message.returnTotalMessagess(); // this is 0 for the first sent message
                Message m = new Message(messageID, recipient, messageText, messageNumber);

                // Validate recipient and message length: show suitable messages
                if (m.checkRecipientCell() == 0) {
                    JOptionPane.showMessageDialog(null, "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
                    continue;
                }

                // After validation ask what to do
                String[] actions = {"Send Message", "Store Message", "Disregard Message"};
                int action = JOptionPane.showOptionDialog(
                        null,
                        "Choose action for this message",
                        "Message Action",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        actions,
                        actions[0]);

                String result = m.sentMessage(action + 1); // 1-based
                // Show full details via JOptionPane if action was Send or Store
                String details = "Message Details:\n" +
                        "MessageID: " + m.getMessageID() + "\n" +
                        "MessageHash: " + m.getMessageHash() + "\n" +
                        "Recipient: " + m.getRecipientCell() + "\n" +
                        "Message: " + m.getMessageText();

                JOptionPane.showMessageDialog(null, result + "\n\n" + details);

                if (action + 1 == 1) {
                    // increment entered count only if user actually "sent" the message
                    entered++;
                } else if (action + 1 == 2) {
                    // storing does not count as sent (per POE interpretation)
                    entered++;
                } else {
                    // disregarded - count it as entered (POE wasn't explicit; we will count it as "entered")
                    entered++;
                }

            } else if (choice == 1) { // Coming soon
                JOptionPane.showMessageDialog(null, "Coming Soon.");
            } else { // Quit or closed dialog
                JOptionPane.showMessageDialog(null, "Total messages sent: " + Message.returnTotalMessagess());
                break;
            }
        }
    }

    // helper to generate random n-digit numeric string (leading zeros allowed)
    private static String generateRandomDigitString(int n) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // For quick testing: add a sample user so you can log in immediately:
        Login.users.put("test_user", new User("Test User", "Male", "test_user", "Password123", "+27831234567", ""));

        // Launch login form
        SwingUtilities.invokeLater(() -> new LoginForm());
    }
}


    
