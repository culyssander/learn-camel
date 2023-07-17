package com.dxc.learncamelspring;

import org.apache.camel.Exchange;

import java.util.function.Predicate;

public class ValidacaoDoValorDeEntrada {

    public static boolean test(Exchange e) {
        MyBean bean = e.getMessage().getBody(MyBean.class);
        String nome = bean.getName();

        boolean b = nome == null || nome.isBlank() || !nome.matches("[0-9]+");
        System.out.println(b);
        return b;
    }
}
