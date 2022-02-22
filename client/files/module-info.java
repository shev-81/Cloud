module client {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.apache.logging.log4j;

  opens client to javafx.fxml;
  exports client;
}