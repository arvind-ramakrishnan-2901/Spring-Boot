package com.test.util;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClientUtil {
	HttpClient httpClient;
	RequestConfig requestConfig;
	private ObjectMapper mapper;
	private Class<?> clazz;	
	
	public HttpClientUtil(Class<?> clazz) {
		this.clazz = clazz;
	}

	public HttpClient createHttpClient() 
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext =
				SSLContextBuilder.create()
				.loadTrustMaterial(new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						return true; // brutal, do not do this in production!
					} })
				.build();
		SSLConnectionSocketFactory sslConnectionSocketFactory =
				new SSLConnectionSocketFactory(
						sslContext,
						new String[] { "TLSv1", "SSLv3" },
						null,
						new HostnameVerifier() {
							@Override
							public
							boolean verify(String hostname, SSLSession s) {
								return true;
							}
						});
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", sslConnectionSocketFactory)
				.build();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		requestConfig = RequestConfig.custom()
				.setConnectTimeout(20 * 1000)
				.setConnectionRequestTimeout(20 * 1000)
				.setSocketTimeout(80 * 1000)
				.setMaxRedirects(20)
				.build();

		if(httpClient == null) {
			httpClient = HttpClients.custom()
					.setSSLSocketFactory(sslConnectionSocketFactory)
					.setConnectionManager(cm)
					.disableRedirectHandling()  // important
					.setDefaultRequestConfig(requestConfig)
					.build();
		}
		return httpClient;
	}
	
	private ObjectMapper getObjectMapper() {
		if(mapper == null) {
			mapper = new ObjectMapper();
		}
		return mapper;
	}
	
	public Object getEntity(String json) 
			throws JsonParseException, JsonMappingException, IOException {
		return getObjectMapper().readValue(json, clazz);
	}
	
	public HttpResponse executeGetRequest(String url, String placeHolder) 
			throws ClientProtocolException, IOException, URISyntaxException, KeyManagementException, 
			NoSuchAlgorithmException, KeyStoreException {
		
		URIBuilder urib = null;
		if(placeHolder == null) {
			urib = new URIBuilder(url);
		}
		else {
			urib = new URIBuilder(url + placeHolder);
		}
		
		URI uri = urib.setPath(urib.getPath())
				.build()
				.normalize();
		httpClient = createHttpClient();
		HttpGet getRequest = new HttpGet(uri.toString());
		getRequest.setConfig(requestConfig);
		HttpResponse response = httpClient.execute(getRequest);
		return response;
	}
	
	public HttpResponse executeDeleteRequest(String url, String placeHolder) 
			throws ClientProtocolException, IOException, URISyntaxException, KeyManagementException, 
			NoSuchAlgorithmException, KeyStoreException {
		httpClient = createHttpClient();
		URIBuilder urib = new URIBuilder(url);
		URI uri = urib.setPath(urib.getPath() + placeHolder).build().normalize();
		HttpDelete deleteRequest = new HttpDelete(uri.toString());
		deleteRequest.setConfig(requestConfig);
		HttpResponse response = httpClient.execute(deleteRequest);
		deleteRequest.releaseConnection();
		return response;
	}
	
	private StringEntity getParams(StringWriter sw) {
		StringEntity params = new StringEntity(sw.toString(), "UTF-8");
		params.setContentType(ContentType.APPLICATION_JSON.getMimeType());
		return params;
	}
}
