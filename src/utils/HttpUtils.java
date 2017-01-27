package utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Created by mohammad on 1/28/17.
 */
public class HttpUtils {
    public static String requestWithEntity(HttpEntityEnclosingRequestBase httpRequest, String requestBody) throws IOException {
        StringEntity entity = new StringEntity(requestBody, "UTF-8");
        httpRequest.setEntity(entity);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        try {
            String responseString = new BasicResponseHandler().handleResponse(response);
            httpclient.close();
            return responseString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
