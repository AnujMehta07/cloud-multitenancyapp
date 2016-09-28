package com.sap.hana.cloud.samples.mfplantapp.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyStore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONTokener;

import com.sap.cloud.crypto.keystore.api.KeyStoreService;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

public class SSLExampleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// get Keystore Service
		KeyStoreService keystoreService;
		try {
			Context context = new InitialContext();
			keystoreService = (KeyStoreService) context.lookup("java:comp/env/KeyStoreService");
		} catch (NamingException e) {
			response.getWriter().println("Error:<br><pre>");
			e.printStackTrace(response.getWriter());
			response.getWriter().println("</pre>");
			throw new ServletException(e);
		}

		String host = request.getParameter("host");
		if (host == null || (host = host.trim()).isEmpty()) {
			response.getWriter().println("Host is not specified");
			return;
		}
		String port = request.getParameter("port");
		if (port == null || (port = port.trim()).isEmpty()) {
			port = "443";
		}
		String path = request.getParameter("path");
		if (path == null || (path = path.trim()).isEmpty()) {
			path = "/";
		}
		String clientKeystoreName = "jssecacerts1";
		String clientKeystorePassword = request.getParameter("client.keystore.password");
		if (clientKeystorePassword == null || (clientKeystorePassword = clientKeystorePassword.trim()).isEmpty()) {
			// response.getWriter().println("Password for client keystore is not
			// specified");
		}
		String trustedCAKeystoreName = "cacerts";

		// get a named keystores with password for integrity check
		KeyStore clientKeystore;
		try {
			clientKeystore = keystoreService.getKeyStore(clientKeystoreName, null);
		} catch (Exception e) {
			response.getWriter().println("Client keystore is not available: " + e);
			return;
		}

		// get a named keystore without integrity check
		KeyStore trustedCAKeystore;
		try {
			trustedCAKeystore = keystoreService.getKeyStore(trustedCAKeystoreName, null);
		} catch (Exception e) {
			response.getWriter().println("Trusted CAs keystore is not available" + e);
			return;
		}

		callHTTPSServer(response, host, port, path, null, clientKeystore, trustedCAKeystore);
	}

	private void callHTTPSServer(HttpServletResponse response, String host, String port, String path,
			String clientKeystorePassword, KeyStore clientKeystore, KeyStore trustedCAKeystore) throws IOException {
		SSLSocket socket = null;
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(clientKeystore, null);

			KeyManager[] keyManagers = kmf.getKeyManagers();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustedCAKeystore);
			TrustManager[] trustManagers = tmf.getTrustManagers();
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagers, trustManagers, null);
			SSLSocketFactory factory = sslContext.getSocketFactory();
			socket = (SSLSocket) factory.createSocket(host, Integer.parseInt(port));
			socket.startHandshake();
			URL url = new URL("https://api.openaq.org/v1/locations");
			HttpsURLConnection urlConnection =
				    (HttpsURLConnection)url.openConnection();
			urlConnection.setSSLSocketFactory(factory);
			InputStream inputStream = urlConnection.getInputStream();
			ServletOutputStream outputStream = response.getOutputStream();
			copyStream(inputStream, outputStream);
			String responseBodyasString = getResponseBodyasString(inputStream);
			
//			javax.naming.Context ctx = new InitialContext();
//			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
//					.lookup("java:comp/env/connectivityConfiguration");

			// Get destination configuration for "destinationName"
//			DestinationConfiguration destConfiguration = configuration.getConfiguration("plantopenaq");

//			// Get the destination URL
//			String value = destConfiguration.getProperty("URL");
//
//			final String baseURL = value + "?parameter=o3&location=" + "SAINT JOHN WEST" + "&date_from=" + "2016-08-07"
//					+ "&date_to=" + "2016-09-07";
//			URL url = new URL(baseURL.replaceAll(" ", "%20"));
//			//String proxyType = destConfiguration.getProperty("ProxyType");
//			//Proxy proxy = getProxy(proxyType);
//			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
//			// Copy content from the incoming response to the outgoing response
//			InputStream instream = urlConnection.getInputStream();
//			String responseBodyasString = getResponseBodyasString(instream);
			
			socket.close();
		} catch (Exception e) {
			response.getWriter().println("Error:<br><pre>");
			e.printStackTrace(response.getWriter());
			response.getWriter().println("</pre>");
		} finally {
			response.getWriter();
			socket.close();
		}
	}

	private Proxy getProxy(String proxyType) {
		String proxyHost = null;
		int proxyPort;
		// Get proxy for internet destinations
		proxyHost = System.getProperty("https.proxyHost");
		proxyPort = Integer.parseInt(System.getProperty("https.proxyPort"));

		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
	}

	
	
	 private void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
	        byte[] buffer = new byte[1024];
	        int len;
	        while ((len = inStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, len);
	        }
	    }
	
	
	static String getResponseBodyasString(InputStream instream) throws Exception {
		String retVal = null;
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = instream.read(buffer)) != -1) {
				outstream.write(buffer, 0, len);
			}
		} catch (IOException e) {
			// In case of an IOException the connection will be released
			// back to the connection manager automatically
			throw e;
		} finally {
			// Closing the input stream will trigger connection release
			try {
				instream.close();
			} catch (Exception e) {
				// Ignore
			}
		}
		retVal = outstream.toString("UTF-8");

		return retVal;

	}

}