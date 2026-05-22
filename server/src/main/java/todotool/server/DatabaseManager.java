package todotool.server;

import todotool.shared.Todo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:database.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private static void initDatabase() {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "text TEXT, " +
                    "completed INTEGER" +
                    ");";
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Todo> getAllTasks() {
        List<Todo> todos = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String text = resultSet.getString("text");
                boolean completed = resultSet.getInt("completed") == 1;
                todos.add(new Todo(uuid, text, completed));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todos;
    }

    public static void insertTask(Todo todo) {
        String sql = "INSERT INTO tasks(uuid, text, completed) VALUES(?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, todo.uuid.toString());
            preparedStatement.setString(2, todo.text);
            preparedStatement.setInt(3, todo.completed ? 1 : 0);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTask(Todo todo) {
        String sql = "UPDATE tasks SET text = ?, completed = ? WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, todo.text);
            preparedStatement.setInt(2, todo.completed ? 1 : 0);
            preparedStatement.setString(3, todo.uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTask(Todo todo) {
        String sql = "DELETE FROM tasks WHERE uuid = ?";
        try (Connection connection = DriverManager.getConnection(URL);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, todo.uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}