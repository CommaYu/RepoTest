package com.tencent.wechat.http;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MySSLSocketFactory extends SSLSocketFactory {

    /**
     * HttpClient 的使用 SSLSocketFactory 创建 SSL 连接，SSLSocketFactory 允许高度定制，它可以采取
     * javax.net.ssl.SSLContext 实例作为一个参数，并使用它来创建自定义配置 SSL 连接。
     */
//	static{
//		//通过sslcontext
////		myySSLSocketFactory = new MyySSLSocketFactory(createSContext());
//		myySSLSocketFactory = MyySSLSocketFactory.getInstance();
//	}

    private static MySSLSocketFactory mySSLSocketFactory = null;

    /**
     * ���� SSLContext ʵ��
     *
     * @return
     */
//	private static SSLContext createSContext(){
//		SSLContext sslcontext = null;
//		try {
//			sslcontext = SSLContext.getInstance("TLS");
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//		try {
//			sslcontext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, null);
//		} catch (KeyManagementException e) {
//			e.printStackTrace();
//			return null;
//		}
//		return sslcontext;
//	}
    public static MySSLSocketFactory getInstance() {
        if (mySSLSocketFactory != null) {
            return mySSLSocketFactory;
        } else {
            //这里要获取KeyStore
            KeyStore keyStore;
            try {
                //InputStream keyStoreInput = MainActivity.mainActivity.getAssets().open("test.bks");
                String keyStoreType = KeyStore.getDefaultType();
                keyStore = KeyStore.getInstance(keyStoreType);
                //keyStore.load(keyStoreInput, "123456".toCharArray());
                keyStore.load(null, "".toCharArray());

                mySSLSocketFactory = new MySSLSocketFactory(keyStore);

                X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }

                    public void verify(String arg0, SSLSocket arg1) throws IOException {
                    }

                    public void verify(String arg0, String[] arg1, String[] arg2) throws SSLException {
                    }

                    public void verify(String arg0, X509Certificate arg1) throws SSLException {
                    }
                };

                mySSLSocketFactory.setHostnameVerifier(hostnameVerifier);

            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            return mySSLSocketFactory;
        }
    }

    SSLContext sslContext = SSLContext.getInstance("TLS");

    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[]{tm}, null);
    }

    public MySSLSocketFactory(KeyManager[] keys, KeyStore truststore) throws NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(keys, new TrustManager[]{tm}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}