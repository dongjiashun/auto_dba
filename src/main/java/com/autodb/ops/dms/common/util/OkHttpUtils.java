package com.autodb.ops.dms.common.util;

import com.squareup.okhttp.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * OkHttp Utils
 *
 * @author dongjs
 * @since 16/4/21
 */
public final class OkHttpUtils {
    private OkHttpUtils() {
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier((hostname, session) -> true);

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
