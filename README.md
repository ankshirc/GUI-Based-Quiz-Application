The goal of QuizApp is to create an engaging, user-friendly GUI based quiz application that allows users to answer multiple-choice questions. 
The primary aim of this application is to make learning more engaging and accessible by incorporating real-time feedback, score tracking, time management and a user-friendly interface.
2.	Design Phase
•	System Architecture: A modular, object-oriented structure was planned to separate different aspect of the application, like question management and other UI elements.
•	Class Structure
	QuizApp Class:  Manages the overall quiz flow, with a login panel, an admin panel where the admin can manage the questions and a quiz panel where the user attempts the timed multiple-choice questions.
	Question Class: Represents each question with its text, options and the correct answer, making it easier to add or modify questions.
•	File Handling with users.txt and questions.txt
	users.txt: Stores user login information, such as usernames and passwords.
	questions.txt: Contains quiz questions, options and correct answers. By using this file, admins can update the question bank externally without modifying the core code, and the app can dynamically loas questions each time a quiz is launched.
•	Swing Libraries for User Interface(UI)
	JFrame: Used as the main window for the app, serving as the container for all UI elements, including the quiz display, question navigation, and score summary.
	JLabel: Displays static text on the screen, such as instructions, question prompts, and feedback messages.
	JButton: Provides clickable buttons for user actions
	JTextField and JPasswordField: Used for user input fields in the login and registration screens, with JPasswordField providing hidden text for secure password entry.
	JOptionPane: Used for displaying pop-up dialogs to show alerts, confirmations, and score summaries.
	JRadioButton: Used for multiple-choice answer options, allowing users to select a single answer per question.
	JPanel: Serves as a container to organize other UI components.
	JList and JScrollPane: Together, they allow for a scrollable list view, used in admin sections for managing or selecting questions in a list format.
•	User Interface (UI) Design: A straightforward, user-friendly interface is created to guide users seamlessly through the quiz, displaying questions, feedback and scores clearly.
