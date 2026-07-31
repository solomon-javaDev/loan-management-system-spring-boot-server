package io.sol.loanmanagementsystemspringbootserver.utilities;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

/**
 * The StageManager class is a shared service approach to configure the FXMLLoader to let
 * Spring handle controller creation, allowing the controllers to use Dependency Injection
 * JavaFX needs to know that Spring is managing the lifecycle of the Controller classes
 *
 */
@Component
public class StageManager {
        private final ApplicationContext springContext;

        public StageManager(ApplicationContext springContext) {
            this.springContext = springContext;
        }

        public Parent loadView(String fxmlPath) throws IOException {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            loader.setControllerFactory(springContext::getBean);

            return loader.load();
        }
}
