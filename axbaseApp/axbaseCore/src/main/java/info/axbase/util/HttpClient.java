/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpClient {
	public Map<String, String> headers;
	public int timeout = 10000;
	public boolean disconnect = true;

	public static interface HttpHandler {
		void writeReq(OutputStream out) throws IOException;

		void readRes(InputStream in, int length) throws IOException;

		void onError(int code, Object msg);
	}

	public static class HttpException extends Exception {
		private static final long serialVersionUID = 3369739421484457943L;

		public int responseCode = 0;
		public Object error = null;

		public HttpException(int responseCode) {
			this.responseCode = responseCode;
		}

		public HttpException(int responseCode, String error) {
			super(error);
			this.responseCode = responseCode;
			this.error = error;
		}

		public HttpException(int responseCode, Throwable error) {
			super(error);
			this.responseCode = responseCode;
			this.error = error;
		}

		public static HttpException make(int code, Object error) {
			if (error == null) {
				return new HttpException(code);
			} else if (error instanceof Throwable) {
				return new HttpException(code, (Throwable) error);
			} else {
				return new HttpException(code, error.toString());
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////
	// cookie

	public void initCookie() {
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}

	public String getCookie(String uri, String name) {
		CookieManager cookieManager = (CookieManager) CookieHandler
				.getDefault();
		try {
			List<HttpCookie> list = cookieManager.getCookieStore().get(
					new URI(uri));
			for (HttpCookie c : list) {
				if (c.getName().equals(name)) {
					return c.getValue();
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addCookie(String uri, String name, String value) {
		addCookie(uri, name, value, "/");
	}

	public void addCookie(String uri, String name, String value, String path) {
		CookieManager cookieManager = (CookieManager) CookieHandler
				.getDefault();
		try {
			URI u = new URI(uri);
			HttpCookie c = new HttpCookie(name, value);
			c.setPath(path);
			c.setDomain(u.getHost());
			cookieManager.getCookieStore().add(new URI(uri), c);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void clearCookie() {
		CookieManager cookieManager = (CookieManager) CookieHandler
				.getDefault();
		cookieManager.getCookieStore().removeAll();
	}

	// ////////////////////////////////////////////////////////////////////
	// util

	public static String streamToString(InputStream is) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();

		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public static class StringHttpHandler implements HttpHandler {
		public String response = null;
		public String request = null;
		public int responseCode = 0;
		public Object error = null;

		@Override
		public void writeReq(OutputStream out) {
			if (request == null)
				return;
			BufferedOutputStream os = new BufferedOutputStream(out);
			try {
				os.write(request.getBytes("UTF-8"));
				os.flush();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void readRes(InputStream in, int length) {
			try {
				response = streamToString(in);
			} catch (IOException e) {
			}
		}

		@Override
		public void onError(int code, Object msg) {
			responseCode = code;
			error = msg;
			System.err.println("response code: " + code + ", message:" + msg);
		}
	};

	public String get(String urlStr) throws HttpException {
		StringHttpHandler handler = new StringHttpHandler();
		handler.request = null;
		doRequest(urlStr, "GET", handler);
		if (handler.responseCode != 0) {
			System.err.println("url: " + urlStr);
			throw HttpException.make(handler.responseCode, handler.error);
		}
		return handler.response;
	}

	public String post(String urlStr, String content) throws HttpException {
		StringHttpHandler handler = new StringHttpHandler();
		handler.request = content;
		doRequest(urlStr, "POST", handler);
		if (handler.responseCode != 0) {
			throw HttpException.make(handler.responseCode, handler.error);
		}
		return handler.response;
	}

	private void setConnection(HttpURLConnection connection) {
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////
	// callback mode

	public void doRequest(String urlStr, String method, HttpHandler handler) {
		URL url = null;
		try {
			url = new URL(urlStr);

			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) url.openConnection();
				setConnection(connection);
				if (!"GET".equalsIgnoreCase(method)) {
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection.setRequestMethod("POST");
					connection.setUseCaches(false);
					connection.setInstanceFollowRedirects(false);
					connection.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					connection.connect();
					OutputStream out = connection.getOutputStream();

					try {
						handler.writeReq(out);
						out.flush();
					} finally {
						out.close();
					}
				}

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					String s = null;
					try {
						InputStream is = connection.getInputStream();
						s = streamToString(is);
						is.close();
					} catch (Exception e) {
					}
					handler.onError(connection.getResponseCode(), s);
				} else {
					InputStream is = connection.getInputStream();
					try {
						int length = connection.getContentLength();
						handler.readRes(is, length);
					} finally {
						is.close();
					}
				}
			} finally {
				if (connection != null && disconnect) {
					connection.disconnect();
				}
			}
		} catch (IOException e) {
			handler.onError(-1, e);
			e.printStackTrace();
		}
	}
}
