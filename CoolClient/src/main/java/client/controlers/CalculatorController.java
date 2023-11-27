package client.controlers;

import client.dto.Point;
import client.network.NetworkManager;
import client.plot.PlotMaker;
import com.test.grpc.CalculatorGrpc;
import com.test.grpc.CalculatorService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.util.*;


public class CalculatorController extends CalculatorGrpc.CalculatorImplBase {

    @FXML
    private TextField startTimeTextField;
    @FXML
    private TextField endTimeTextField;
    @FXML
    private TextField startXTextField;
    @FXML
    private TextField endXTextField;
    @FXML
    private TextField startYTextField;
    @FXML
    private TextField endYTextField;

    @FXML
    private Slider timeSlider;

    @FXML
    private Button startButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Button showChartButton;


    private Map<Long, List<Point>> allSeries;

    private static final String RED_COLOR = "-fx-background-color: red;";

    private static final String WHITE_COLOR = "-fx-background-color: white;";

    public void initialize() {
        allSeries = new HashMap<>();
        startButton.setDisable(true);

        errorLabel.setVisible(false);
        showChartButton.setDisable(true);

        addListeners();
    }

    private void addListeners() {

        addTextFieldValidator(startTimeTextField);
        addTextFieldValidator(endTimeTextField);
        addTextFieldValidator(startXTextField);
        addTextFieldValidator(endXTextField);
        addTextFieldValidator(startYTextField);
        addTextFieldValidator(endYTextField);
    }

    private void addTextFieldValidator(TextField textField) {
        textField.textProperty().addListener((observable) -> validateTextField(textField));
    }

    @FXML
    private void showChartByTime() {
        Long time = timeSlider.valueProperty().longValue();
        System.out.println(time);
        PlotMaker.showChart(allSeries.get(time), time);
    }

    @FXML
    void sendRequest() throws NumberFormatException { //never thrown
        errorLabel.setVisible(false);

        Thread thread = new Thread(() -> {
            try {
                CalculatorGrpc.CalculatorBlockingStub stub = NetworkManager.buildStub();

                CalculatorService.Request request = buildRequest();


                Iterator<CalculatorService.ResponseArray> results = stub.calculate(request);

                Platform.runLater(() -> rebuildSliderByRequest(request));

                while (results.hasNext()) {
                    CalculatorService.ResponseArray responses = results.next();

                    List<Point> points = new ArrayList<>();
                    long key = 0;

                    for (CalculatorService.Response resp : responses.getResponsesList()) {

                        logResponse(resp);

                        points.add(new Point(resp.getX(), resp.getY(), resp.getResult()));
                        key = resp.getTime();

                        Platform.runLater(() -> timeSlider.setMax(resp.getTime()));
                    }

                    long finalKey = key;
                    Platform.runLater(() -> allSeries.put(finalKey, points));
                    Platform.runLater(() -> showChartButton.setDisable(false));
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Ошибка соединения с сервером");
                    errorLabel.setVisible(true);
                });
            } finally {
                Platform.runLater(() -> startButton.setDisable(false));
            }
        });

        thread.start();
    }


    private void validateTextField(TextField textField) {
        if (isTextFieldOk(textField)) {
            startButton.setDisable(false);
        }
    }

    private void logResponse(CalculatorService.Response response) {
        System.out.printf("X - %d, Y - %d, T - %d, Result = %f\n",
                response.getX(),
                response.getY(),
                response.getTime(),
                response.getResult());
    }

    private boolean isTextFieldOk(TextField textField) {
        startButton.setDisable(true);
        textField.setStyle(WHITE_COLOR);
        errorLabel.setVisible(false);

        if (textField.getText().isEmpty()) {
            textField.setStyle(RED_COLOR);
            errorLabel.setText("Поле не можем быть пустым!");
            errorLabel.setVisible(true);

            return false;
        }

        try {
            Double.parseDouble(textField.getText());
        } catch (NumberFormatException e) {
            textField.setStyle(RED_COLOR);
            errorLabel.setText("Некорректные данные!");
            errorLabel.setVisible(true);

            return false;
        }

        return areRequestParamsOk();
    }

    private boolean areRequestParamsOk() {

        if (!allFieldsNotEmpty()) {
            return false;
        }

        long startTime = Long.parseLong(startTimeTextField.getText());
        long endTime = Long.parseLong(endTimeTextField.getText());

        long startX = Long.parseLong(startXTextField.getText());
        long endX = Long.parseLong(endXTextField.getText());

        long startY = Long.parseLong(startYTextField.getText());
        long endY = Long.parseLong(endYTextField.getText());

        if (startTime >= endTime) {
            errorLabel.setText("Начальное время должно быть меньше конечного!");
            errorLabel.setVisible(true);
            return false;
        }

        if (startX >= endX || startY >= endY) {
            errorLabel.setText("Начальное значение должно быть меньше конечного!");
            errorLabel.setVisible(true);
            return false;
        }

        return true;
    }

    private boolean allFieldsNotEmpty() {
        return !startTimeTextField.getText().isEmpty() &&
                !endTimeTextField.getText().isEmpty() &&
                !startXTextField.getText().isEmpty() &&
                !endXTextField.getText().isEmpty() &&
                !startYTextField.getText().isEmpty() &&
                !endYTextField.getText().isEmpty();
    }

    private CalculatorService.Request buildRequest() throws NumberFormatException {
        long startTime = Long.parseLong(startTimeTextField.getText());
        long endTime = Long.parseLong(endTimeTextField.getText());
        long startX = Long.parseLong(startXTextField.getText());
        long endX = Long.parseLong(endXTextField.getText());
        long startY = Long.parseLong(startYTextField.getText());
        long endY = Long.parseLong(endYTextField.getText());

        return CalculatorService.Request.newBuilder().
                setStartT(startTime).
                setEndT(endTime).
                setStartX(startX).
                setEndX(endX).
                setStartY(startY).
                setEndY(endY).
                build();
    }


    private void rebuildSliderByRequest(CalculatorService.Request request) {
        timeSlider.setMin(request.getStartT());
        timeSlider.setBlockIncrement(1.0);
        timeSlider.setMajorTickUnit(1.0);
        timeSlider.setMinorTickCount(0);
        timeSlider.setShowTickLabels(true);
        timeSlider.setSnapToTicks(true);
    }

}