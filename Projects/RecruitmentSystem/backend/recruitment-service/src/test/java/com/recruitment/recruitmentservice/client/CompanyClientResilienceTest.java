package com.recruitment.recruitmentservice.client;

import com.recruitment.recruitmentservice.exception.BusinessException;
import com.recruitment.recruitmentservice.exception.ErrorCode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyClientResilienceTest {
    private HttpServer server;
    @AfterEach void stop(){if(server!=null)server.stop(0);}

    @Test void normalNotFoundAndUnavailableAreDistinct() throws Exception {
        UUID id=UUID.randomUUID();
        start(200,"{\"id\":\""+id+"\",\"ownerId\":\""+id+"\"}");
        assertThat(client().getCompanyById(id)).isPresent(); stop();
        start(404,"{}"); assertThat(client().getCompanyById(id)).isEmpty(); stop();
        start(503,"{}"); assertThatThrownBy(()->client().getCompanyById(id)).isInstanceOfSatisfying(BusinessException.class,
                ex->assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DOWNSTREAM_UNAVAILABLE));
    }

    private CompanyClientImpl client(){return new CompanyClientImpl("http://127.0.0.1:"+server.getAddress().getPort(),200,200);}
    private void start(int status,String body)throws Exception{server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.createContext("/",exchange->{byte[] bytes=body.getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(status,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();});server.start();}
}
