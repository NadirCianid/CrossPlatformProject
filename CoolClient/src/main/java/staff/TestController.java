package staff;

import com.test.grpc.TestServiceGrpc;
import com.test.grpc.TestServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class TestController extends TestServiceGrpc.TestServiceImplBase {
    private double[] fValues = new double[11];

    @FXML
    private TextField a1TextField;

    @FXML
    private TextField a2TextField;

    @FXML
    private TextField a3TextField;

    @FXML
    private TextField a4TextField;

    @FXML
    private Slider bSlider;

    @FXML
    private StackedAreaChart<Integer, Double> chart;

    //Хранилище точек графика
    private XYChart.Series<Integer, Double> series = new XYChart.Series();

    public void chartInit() {
        //Инициализация графика
        chart.getData().add(series);
        series.setName("Some values");
    }

    @FXML
    void showChart(ActionEvent event) {
        //Создание объекта канала, по которому будет происходить общение с сервером
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8082") //адрес сервера
                .usePlaintext().build();

        //Создание объекта, через который будут вызываться методы сервера
        TestServiceGrpc.TestServiceBlockingStub stub = TestServiceGrpc.newBlockingStub(channel);


        double a1;
        double a2;
        double a3;
        double a4;
        double b;

        try {
            //Считывание параметров функции
            a1 = Double.parseDouble(a1TextField.getText());
            a2 = Double.parseDouble(a2TextField.getText());
            a3 = Double.parseDouble(a3TextField.getText());
            a4 = Double.parseDouble(a4TextField.getText());
            b = bSlider.getValue();

            //Очистка графика
            series.getData().clear();


            for(int i = 0; i <= 10; i++) {
                //Создание объекта Params для передачи установленных параметров на сервер
                TestServiceOuterClass.Params params = TestServiceOuterClass.Params
                        .newBuilder()
                        .setA1(a1)
                        .setA2(a2)
                        .setA3(a3)
                        .setA4(a4)
                        .setB(b)
                        .setX(i)
                        .build();

                //Получение ответа точки графика по установленным параметрам
                TestServiceOuterClass.Point point = stub.testFunc(params);

                //Добавление полученной точки на график
                series.getData().add(new XYChart.Data<>(i, point.getY()));
            }


        } catch (NumberFormatException e) {
            showParseError();
        }

        channel.shutdown();
    }


    private void showParseError() {
        a1TextField.clear();
        a1TextField.setText("Incorrect value. Enter double value.");

        a2TextField.clear();
        a2TextField.setText("Incorrect value. Enter double value.");

        a3TextField.clear();
        a3TextField.setText("Incorrect value. Enter double value.");

        a4TextField.clear();
        a4TextField.setText("Incorrect value. Enter double value.");
    }

}