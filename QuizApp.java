package QuizApp;

import javax.swing.*;
import javax.swing.Timer;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Question {
    private String questionText;
    private String[] options;
    private int correctAnswerIndex;

    public Question(String questionText, String[] options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    // Convert question to a file string
    public String toFileString() {
        return questionText + ";" + String.join(",", options) + ";" + correctAnswerIndex;
    }

    // Parse question from a file string
    public static Question fromFileString(String line) {
        String[] parts = line.split(";");
        String[] options = parts[1].split(",");
        return new Question(parts[0], options, Integer.parseInt(parts[2]));
    }
}
public class QuizApp {
    private JFrame frame;
    private JLabel questionLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton nextButton;
    private JLabel timerLabel;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    
    private int timeLeft = 30; // Time left for the quiz
    private boolean timerRunning = false; // Timer state
    private Timer timer; // Timer object
    private JButton startStopButton;
    private String currentUserRole = ""; // To store if current user is admin or user


    // Path to store user credentials
    private final String USER_FILE_PATH = "users.txt";  //file to store login details
    private final String QUESTION_FILE_PATH = "questions.txt"; // File to store questions

    public QuizApp() {
        // Start with the login screen
        createLoginPanel();
    }

    private void createLoginPanel() {
    	
        frame = new JFrame("Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4, 2));
        frame.setLocationRelativeTo(null); // Center the frame
        frame.getContentPane().setBackground(new Color(240, 240, 240)); // Light gray background
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Frame padding

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));

        JTextField userField = new JTextField();
        userField.setBorder(BorderFactory.createCompoundBorder(
        	    BorderFactory.createLineBorder(Color.GRAY, 1),
        	    BorderFactory.createEmptyBorder(5, 5, 5, 5)
        	));
        JPasswordField passField = new JPasswordField();
        passField.setBorder(BorderFactory.createCompoundBorder(
        	    BorderFactory.createLineBorder(Color.GRAY, 1),
        	    BorderFactory.createEmptyBorder(5, 5, 5, 5)
        	));

        JButton loginButton = new JButton("Login");
        frame.getContentPane().setBackground(new Color(240, 240, 240)); // Light gray background
        loginButton.setBackground(new Color(0, 150, 136)); // Teal colour
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1)); // Border for button
        userField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        passField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Action on login button click
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                if (authenticateUser(username, password)) {
                    JOptionPane.showMessageDialog(frame, "Login successful as " + currentUserRole + "!");
                    frame.dispose(); // Close login frame
                    if (currentUserRole.equals("admin")) {
                        createAdminPanel();
                    } else {
                        createQuizPanel();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login credentials. Try again.");
                }
            }
        });

        frame.add(userLabel);
        frame.add(userField);
        frame.add(passLabel);
        frame.add(passField);
        frame.add(new JLabel("")); // Spacer
        frame.add(loginButton);

        frame.setVisible(true);
    }

    // Authenticate user by checking username and password from file
    private boolean authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userDetails = line.split(";");
                if (userDetails[0].equals(username) && userDetails[1].equals(password)) {
                    currentUserRole = userDetails[2]; // "admin" or "user"
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
        return false;
    }

    // Create the Quiz Panel for regular users
    private void createQuizPanel() {
        frame = new JFrame("Quiz Application");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null); // Center the frame

        // Add top panel with logout button
        frame.add(createTopPanelWithLogout(), BorderLayout.NORTH);

        // Set background color
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(240, 240, 240)); // Light gray background
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Main padding

        // Timer Label
        timerLabel = new JLabel("Time left: " + timeLeft);
        timerLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        timerLabel.setForeground(new Color(0, 150, 136)); // Teal text

        // Timer Button
        startStopButton = new JButton("Start Timer");
        startStopButton.setFont(new Font("Consolas",Font.PLAIN, 12));
        startStopButton.setBackground(new Color(0,150,136)); //teal background
        startStopButton.setForeground(Color.WHITE); //White Text Color
        startStopButton.setBorder(BorderFactory.createEmptyBorder(5,10,5,10)); //padding for smaller button
        
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timerRunning) {
                    stopTimer();
                } else {
                    startTimer();
                }
            }
        });

        // Timer that updates every second
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    timeLeft--;
                    updateTimerLabel();
                } else {
                    timer.stop();
                    timerRunning = false; 
                    startStopButton.setText("Start Timer");
                    JOptionPane.showMessageDialog(frame, "Time's up! Choice recorded.");
                    checkAnswer(); // Automatically check the answer when time runs out
                    currentQuestionIndex++;
                    if (currentQuestionIndex < questions.size()) {
                        displayQuestion(currentQuestionIndex); // Display the next question
                    } else {
                        endQuiz(); // End the quiz if there are no more questions
                    }
                }
            }
        });

        // Create a panel for the timer and Start/Stop button
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new FlowLayout()); // Flow layout for horizontal alignment
        timerPanel.setBackground(new Color(240, 240, 240)); // Match panel color
        timerPanel.add(timerLabel);
        timerPanel.add(startStopButton); // Add the start/stop button to the timer panel

        // Question Label
        questionLabel = new JLabel("Question will appear here");
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the question
        questionLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        questionLabel.setBackground(new Color(0, 150, 136)); // Teal background
        questionLabel.setForeground(Color.WHITE); // White text
        questionLabel.setOpaque(true); // Make background visible
        mainPanel.add(questionLabel, BorderLayout.NORTH);

        // Option Buttons
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new GridLayout(4, 1));
        optionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Option padding
        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Consolas", Font.PLAIN, 16));
            optionButtons[i].setBackground(new Color(240, 240, 240)); // Match panel color
            optionGroup.add(optionButtons[i]);
            optionPanel.add(optionButtons[i]);
        }
        mainPanel.add(optionPanel, BorderLayout.CENTER);

        // Next Button
        nextButton = new JButton("Next");
        nextButton.setFont(new Font("Times New Roman", Font.BOLD, 16));
        nextButton.setBackground(new Color(0, 153, 51)); // Green background
        nextButton.setForeground(Color.WHITE); // White text 

        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
    	    public void mouseEntered(java.awt.event.MouseEvent evt) {
    	        nextButton.setBackground(new Color(0, 180, 60)); // Lighter green on hover
    	    }

    	    public void mouseExited(java.awt.event.MouseEvent evt) {
    	        nextButton.setBackground(new Color(0, 153, 51)); // Original green
    	    }
    	});
        
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
                currentQuestionIndex++;
                if (currentQuestionIndex < questions.size()) {
                    displayQuestion(currentQuestionIndex);
                } else {
                    endQuiz();
                }
            }
        });

        // Create a bottom panel to hold the timer panel and next button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 240, 240)); // Match panel color
        bottomPanel.add(timerPanel, BorderLayout.WEST); // Timer and button on the left
        bottomPanel.add(nextButton, BorderLayout.EAST); // Next button on the right

        mainPanel.add(bottomPanel, BorderLayout.SOUTH); // Add the bottom panel to the south

        //Load Questions from file
        loadQuestions();
        
        //display the first question
        displayQuestion(currentQuestionIndex);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // Method to stop the timer
    private void stopTimer() {
        timerRunning = false;
        startStopButton.setText("Start Timer");
        timer.stop();
    }

    // Method to start the timer
    private void startTimer() {
        timerRunning = true;
        startStopButton.setText("Stop Timer");
        timer.start();
    }

    // Method to update the timer label
    private void updateTimerLabel() {
        timerLabel.setText("Time left: " + timeLeft);
    }

    // Create the Admin Panel for managing questions
    private void createAdminPanel() {
    	frame = new JFrame("Admin Panel");
    	frame.setSize(600, 400);
    	frame.setLayout(new GridLayout(0,2,10,10)); // 0 rows, 2 columns
    	frame.getContentPane().setBackground(new Color(240, 240, 240)); // Light gray background
    	frame.setLocationRelativeTo(null); // Center the frame
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close application on exit
    	frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Initialize questions if null
        if (questions == null) {
            questions = new ArrayList<>();
            loadQuestions();
        }

        JTextField questionText = new JTextField();
        JTextField option1 = new JTextField();
        JTextField option2 = new JTextField();
        JTextField option3 = new JTextField();
        JTextField option4 = new JTextField();
        JComboBox<String> correctAnswerDropdown = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});

        // create buttons
        JButton addButton = createButton("Add Question", new Color(0, 153, 51));
        JButton editButton = createButton("Edit Question", new Color(0, 102, 204));
        JButton deleteButton = createButton("Delete Question", new Color(204, 0, 0));

        // List to display existing questions
        DefaultListModel<Question> listModel = new DefaultListModel<>();
        JList<Question> questionList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(questionList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Existing Questions"));
        
        // Adding components to frame
        frame.add(new JLabel("Question:"));
        frame.add(questionText);
        frame.add(new JLabel("Option 1:"));
        frame.add(option1);
        frame.add(new JLabel("Option 2:"));
        frame.add(option2);
        frame.add(new JLabel("Option 3:"));
        frame.add(option3);
        frame.add(new JLabel("Option 4:"));
        frame.add(option4);
        frame.add(new JLabel("Correct Answer:"));
        frame.add(correctAnswerDropdown);
       
        //adding scroll pane for the question list
        frame.add(scrollPane);
        
        //Create a panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        //add button panel to the frame
        frame.add(buttonPanel);
        
        // Action listener for adding questions
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] options = {option1.getText(), option2.getText(), option3.getText(), option4.getText()};
                int correctIndex = correctAnswerDropdown.getSelectedIndex();
                questions.add(new Question(questionText.getText(), options, correctIndex));
                saveQuestions(); // Save updated questions to the file
                JOptionPane.showMessageDialog(frame, "Question Added!");

                // Clear input fields after adding
                clearInputFields(questionText, option1, option2, option3, option4, correctAnswerDropdown);
                loadQuestionsIntoListModel(listModel); // Refresh the list
            }
        });
        
        //Action listener for editing questions
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = questionList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    // Update selected question
                    String[] options = {option1.getText(), option2.getText(), option3.getText(), option4.getText()};
                    int correctIndex = correctAnswerDropdown.getSelectedIndex();
                    Question updatedQuestion = new Question(questionText.getText(), options, correctIndex);
                    questions.set(selectedIndex, updatedQuestion); // Update the list
                    listModel.set(selectedIndex, updatedQuestion); // Update the list model
                    saveQuestions(); // Save updated questions to the file
                    JOptionPane.showMessageDialog(frame, "Question Edited!");
                    clearInputFields(questionText, option1, option2, option3, option4, correctAnswerDropdown);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a question to edit.");
                }
            }
        });
        
        // Action listener for deleting questions
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = questionList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    questions.remove(selectedIndex); // Remove from the list
                    listModel.remove(selectedIndex); // Remove from the list model
                    saveQuestions(); // Save updated questions to the file
                    JOptionPane.showMessageDialog(frame, "Question Deleted!");
                    clearInputFields(questionText, option1, option2, option3, option4, correctAnswerDropdown);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a question to delete.");
                }
            }
        });
        
        // add mouse listener to the question list to populate fields when a question is selected
        questionList.addListSelectionListener(e -> {
               if (!e.getValueIsAdjusting()) {
                   Question selectedQuestion = questionList.getSelectedValue();
                   if (selectedQuestion != null) {
                       questionText.setText(selectedQuestion.getQuestionText());
                       String[] options = selectedQuestion.getOptions();
                       option1.setText(options[0]);
                       option2.setText(options[1]);
                       option3.setText(options[2]);
                       option4.setText(options[3]);
                       correctAnswerDropdown.setSelectedIndex(selectedQuestion.getCorrectAnswerIndex());
                   }
               }
           });
        // Add the top panel with logout button
        frame.add(createTopPanelWithLogout(), BorderLayout.SOUTH);
        frame.setVisible(true);
        }
        
    
 // Helper method to create buttons with common styles
    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background); // Set button background
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Times New Roman", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding
        return button;
    }
    
    private JPanel createTopPanelWithLogout() {
        // Create the top panel for the logout button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(240, 240, 240)); // Light gray background to match main panels

        // Logout button with non-intrusive styling
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Times New Roman", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69)); // Red color to signify logout
        logoutButton.setForeground(Color.WHITE); // White text color
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding for smaller button
        logoutButton.setFocusPainted(false); // Remove focus border on click
        logoutButton.addActionListener(e -> logout());

        // Add the logout button to the top panel
        topPanel.add(logoutButton);

        return topPanel;
    }
    
 // Load existing questions into the list model
    private void loadQuestionsIntoListModel(DefaultListModel<Question> listModel) {
        listModel.clear();
        for (Question question : questions) {
            listModel.addElement(question);
        }
    }
    
 // Clear input fields
    private void clearInputFields(JTextField questionText, JTextField option1, JTextField option2, JTextField option3, JTextField option4, JComboBox<String> correctAnswerDropdown) {
        questionText.setText("");
        option1.setText("");
        option2.setText("");
        option3.setText("");
        option4.setText("");
        correctAnswerDropdown.setSelectedIndex(0);
    }

    // Logout method to handle switching between modes
    private void logout() {
        frame.dispose(); // Close current frame
        createLoginPanel(); // Redirect to login panel
    }


    // Load questions from the file
    private void loadQuestions() {
        questions = new ArrayList<>();
        File questionFile = new File(QUESTION_FILE_PATH);
        
        if (!questionFile.exists()) {
            JOptionPane.showMessageDialog(frame, "Questions file not found. Please check the path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(QUESTION_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                questions.add(Question.fromFileString(line));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading questions from file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
 // Save questions to the file, appending instead of overwriting
    private void saveQuestions() {
        // Save only the last added question to avoid duplicates
        if (!questions.isEmpty()) {
            Question lastQuestion = questions.get(questions.size() - 1);
            String lastQuestionString = lastQuestion.toFileString();
            
            // Check if the question already exists in the file
            try (BufferedReader reader = new BufferedReader(new FileReader(QUESTION_FILE_PATH))) {
                String line;
                boolean exists = false;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(lastQuestionString)) {
                        exists = true; // Question already exists
                        break;
                    }
                }

                // Append only if it doesn't exist
                if (!exists) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(QUESTION_FILE_PATH, true))) {
                        writer.write(lastQuestionString);
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error saving questions to file: " + e.getMessage());
            }
        }
    }


    // Display the question on the quiz panel
    private void displayQuestion(int questionIndex) {
        Question question = questions.get(questionIndex);
        questionLabel.setText(question.getQuestionText());

        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            optionButtons[i].setText(options[i]);
        }

        optionGroup.clearSelection(); // Clear previous selection
        timeLeft = 30; // Reset the timer for the new question
        updateTimerLabel(); // update the timer display
        startTimer(); //start the timer for the new question
    }

    // Check the answer for the current question
    private void checkAnswer() {
        Question question = questions.get(currentQuestionIndex);
        int correctAnswerIndex = question.getCorrectAnswerIndex();

        // Check which radio button is selected
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected() && i == correctAnswerIndex) {
                score++;
                break;
            }
        }
    }


    // End the quiz and show the final score
    private void endQuiz() {
        timer.stop();
        
        // Calculate the number of correct and incorrect answers
        int totalQuestions = questions.size();
        int correctAnswers = score;
        int incorrectAnswers = totalQuestions - correctAnswers;

        // Create feedback message
        String feedbackMessage = "Quiz finished! Your score is: " + score + " out of " + totalQuestions + "\n" +
                                 "Correct Answers: " + correctAnswers + "\n" +
                                 "Incorrect Answers: " + incorrectAnswers + "\n";

        // Provide comments based on performance
        if (correctAnswers == totalQuestions) {
            feedbackMessage += "Excellent work! You got all the answers correct!";
        } else if (correctAnswers >= totalQuestions / 2) {
            feedbackMessage += "Good job! You passed the quiz.";
        } else {
            feedbackMessage += "Don't worry! Review the material and try again.";
        }

        // Show the feedback message in a dialog
        JOptionPane.showMessageDialog(frame, feedbackMessage, "Quiz Results", JOptionPane.INFORMATION_MESSAGE);
        frame.dispose(); // Close the quiz frame
    }


    public static void main(String[] args) {
        new QuizApp();
    }
}
//end of program