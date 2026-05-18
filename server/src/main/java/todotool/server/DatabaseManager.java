package todotool.server;

import todotool.shared.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    // Plik bazy database.db utworzy się sam w głównym katalogu projektu
    private static final String URL = "jdbc:sqlite:database.db";

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Tworzymy tabelę na zadania, jeśli jeszcze jej nie ma
            String sql = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "text TEXT, " +
                    "completed INTEGER" +
                    ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String text = rs.getString("text");
                boolean completed = rs.getInt("completed") == 1;
                tasks.add(new Task(uuid, text, completed));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public static void insertTask(Task task) {
        String sql = "INSERT INTO tasks(uuid, text, completed) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.uuid.toString());
            pstmt.setString(2, task.text);
            pstmt.setInt(3, task.completed ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTask(Task task) {
        String sql = "UPDATE tasks SET text = ?, completed = ? WHERE uuid = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.text);
            pstmt.setInt(2, task.completed ? 1 : 0);
            pstmt.setString(3, task.uuid.toString());
            int rows = pstmt.executeUpdate();
            if (rows == 0) System.out.println("[DB] updateTask: nie znaleziono uuid " + task.uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTask(Task task) {
        String sql = "DELETE FROM tasks WHERE uuid = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}