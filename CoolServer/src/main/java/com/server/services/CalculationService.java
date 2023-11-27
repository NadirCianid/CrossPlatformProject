package com.server.services;

import com.test.grpc.CalculatorGrpc;

import com.test.grpc.CalculatorService;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalculationService extends CalculatorGrpc.CalculatorImplBase {

    @Override
    public void calculate(CalculatorService.Request request, StreamObserver<CalculatorService.ResponseArray> responseObserver) {
        for (long t = request.getStartT(); compare(t, request.getEndT()); t += 1) {

            List<CalculatorService.Response> responses = new ArrayList<>();

            for (long x = request.getStartX(); compare(x, request.getEndX()); x += 1) {
                for (long y = request.getStartY(); compare(y, request.getEndY()); y += 1) {

                    CalculatorService.Response response = buildResponse(x, y, t);

                    responses.add(response);
                }
            }

            responseObserver.onNext(buildResponseArray(responses));
        }

        responseObserver.onCompleted();
    }

    private CalculatorService.ResponseArray buildResponseArray(List<CalculatorService.Response> responses) {
        return CalculatorService.ResponseArray.
                newBuilder().
                addAllResponses(responses).
                build();
    }

    private CalculatorService.Response buildResponse(long x, long y, long t) {
        logParams(x, y, t);

        CalculatorService.Response result = CalculatorService.Response.
                newBuilder().
                setTime(t).
                setX(x).
                setY(y).
                setResult(calculate(x, y, t)).
                build();

        logResponse(result.getResult());

        return result;
    }

    private Double calculate(long x, long y, long t) {
        return Math.cos(x + t) + t * Math.sin(y + x);
    }

    private boolean compare(long first, long second) {
        return first <= second;
    }

    private void logParams(long x, long y, long t) {
        System.out.printf("X - %d, Y - %d, t - %d ", x, y, t);
    }

    private void logResponse(Double response) {
        System.out.printf("Response - %f \n", response);
    }
}
