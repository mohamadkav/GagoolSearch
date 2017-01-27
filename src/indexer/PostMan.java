package indexer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class PostMan {
	
	
	
	public static int sendPut(String data, String url) {
		
	    int responseCode = -1;
	    HttpClient httpClient = new DefaultHttpClient();
	    try {
	        HttpPut request = new HttpPut(url);
	        StringEntity params =new StringEntity(data,"UTF-8");
	        params.setContentType("application/json");
	        request.addHeader("content-type", "application/json");
//	        request.addHeader("Accept", "*/*");
//	        request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
//	        request.addHeader("Accept-Language", "en-US,en;q=0.8");
	        request.setEntity(params);
	        HttpResponse response = httpClient.execute(request);
	        responseCode = response.getStatusLine().getStatusCode();
	        if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 204) {

	            BufferedReader br = new BufferedReader(
	                    new InputStreamReader((response.getEntity().getContent())));

	            String output;
	           // System.out.println("Output from Server ...." + response.getStatusLine().getStatusCode() + "\n");
	            while ((output = br.readLine()) != null) {
	               // System.out.println(output);
	            }
	        }
	        else{
//	            logger.error(response.getStatusLine().getStatusCode());

	            throw new RuntimeException("Failed : HTTP error code : "
	                    + response.getStatusLine().getStatusCode());
	        }

	    }catch (Exception ex) {
//	        logger.error("ex Code sendPut: " + ex);
//	        logger.error("url:" + url);
//	        logger.error("data:" + data);
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }

	    return responseCode;

	}
	
//	private static HttpDelete getDELETE(String fullURI, int timeout) {
//		HttpDelete delete = new HttpDelete(fullURI);
//		delete.setHeader("Accept-Charset", UTF_8); //$NON-NLS-1$
//		delete.addHeader("Accept", TEXT_JSON); //$NON-NLS-1$
//		delete.setConfig(getRequestConfig(timeout));
//		return delete;
//	}
	 
}
