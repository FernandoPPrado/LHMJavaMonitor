package com.fernandoprado.lhmagent;

import com.fernandoprado.lhmagent.Controller.service.MainService;

import java.io.IOException;


public class Main {

    static MainService mainService = new MainService();

    public static void main(String[] args) throws IOException {
        mainService.initProgram();
    }


}
