package todotool;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import todotool.controller.EditorController;
import todotool.ui.EditorView;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        EditorView editorView = new EditorView();
        EditorController editorController = new EditorController(editorView);

        editorView.setController(editorController);

        Scene scene = new Scene(editorView.getRoot(), 480, 500);

        editorView.setupGlobalKeyBindings(scene);

        stage.setTitle("Todotool");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
