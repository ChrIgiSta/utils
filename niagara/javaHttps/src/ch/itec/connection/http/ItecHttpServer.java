package ch.itec.connection.http;

/**
Copyright (c) 2021 LEICOM iTEC AG. All Rights Reserved.

         ----   _                   eliona
         |- |  |_|          Leicom ITEC AG
     ___  | |  ___  _____   _____   ___ _
    / o \ | |  | | /  _  \ /  _  \ /  _\ |
    | \-- | |  | | | |_| | | | | | | |_  |
    \___/ | |_ |_| \_____/ |_| |_| \___/_|
          \___|

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF
    ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
    TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
    PARTICULAR PURPOSE AND NONINFRINGEMENT.
    IN NO EVENT SHALL LEICOM BE LIABLE FOR ANY
    CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
    IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.

    Authors: Christian Stauffer
    Date: 20.03.2021, Winterthur

    Description: HTTP Sever to Handle API Requests on Niagara
                 form Eliona.
*/

import ch.itec.utils.Utils;
import ch.itec.utils.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Queue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

// for https
import java.util.Properties;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLEngine;

import java.security.KeyStore;


public class ItecHttpServer {

    public static final String API_TOKEN_HEADER_KEY = "token";

    public static final String LISTEN_TO_ALL_IP = "*";

    public static final int HTTP_OK                    = 200;
    public static final int HTTP_NOT_AUTHORIZED        = 401;
    public static final int HTTP_BAD_REQUEST           = 400;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    public static final int HTTP_NOT_IMPLEMENTED       = 501;

    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public static final String HTTP_REQUEST_TYPE = "POST";

    public static class serverConfig {
        public int     localPort;
        public String  listenIp;
        public boolean useSsl;
        public String  keyStore;
        public String  keyStorePassword;
        public String  md5SumApiKey;
    }

    private String upstreamPath = "/command";

    private serverConfig config;

    private Logger        logger;
    private Queue<String> queue;

    private InetSocketAddress sockAddr;
    private HttpServer        server;
    private HttpsServer       secServer;

    private boolean initialized = false;

    public ItecHttpServer(serverConfig config, Queue<String> messageQueue)
    {
        this.config                  = new serverConfig();
        this.config.localPort        = config.localPort;
        this.config.listenIp         = config.listenIp;
        this.config.useSsl           = config.useSsl;
        this.config.keyStore         = config.keyStore;
        this.config.keyStorePassword = config.keyStorePassword;
        this.config.md5SumApiKey     = config.md5SumApiKey;

        this.queue = messageQueue;

        this.logger = new Logger("HTTP");

        try
        {
            this.sockAddr = new InetSocketAddress(this.config.localPort);

            this.initialized = true;

            if(!this.config.useSsl)
            {
                this.server   = HttpServer.create(sockAddr, 0);
            }
            else
            {
                this.secServer = HttpsServer.create(sockAddr, 0);

                HttpsConfigurator httpsConf = this.createHttpsConfig(this.config.keyStorePassword, this.config.keyStore);
                if(httpsConf == null)
                {
                    logger.Error("couldn't create https server config.");
                    this.initialized = false;
                }
                else
                {
                    this.secServer.setHttpsConfigurator(httpsConf);
                }
            }
        }
        catch(IOException ioEx)
        {
            this.logger.Error("Couldn't create server. Is assigned port already in use? > " + ioEx.toString());
            this.initialized = false;
        }
    }

    public void start() throws ServerShutdownException
    {
        try
        {
            if(!this.initialized)
            {
                throw new NullPointerException("Server not or not correctly initialized.");
            }

            if(!this.config.useSsl)
            {
                this.server.createContext(this.upstreamPath, new ItecNiagaraHandler(this.queue, this.config.md5SumApiKey));
                this.server.setExecutor(null);
                this.server.start();
            }
            else
            {
                this.secServer.createContext(this.upstreamPath, new ItecNiagaraHandler(this.queue, this.config.md5SumApiKey));
                this.secServer.setExecutor(null);
                this.secServer.start();
            }

            this.logger.Info("server started and listening on port %s", this.config.localPort);
        }
        catch(NullPointerException npEx)
        {
            this.logger.Error("couldn't start server because it isn't initialized.");
            throw new ServerShutdownException("couldn't start server because it isn't initialized.");
        }
    }

    private HttpsConfigurator createHttpsConfig(String keyStorePw, String keyStorePath)
    {
        HttpsConfigurator httpsConfig;
        try
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            char[] keyStorePwC    = keyStorePw.toCharArray();
            KeyStore keyStore     = KeyStore.getInstance("JKS");

            try
            {
                // try to read keystore from host path
                FileInputStream keyStoreInputStream = new FileInputStream(keyStorePath);
                keyStore.load(keyStoreInputStream, keyStorePwC);
            } 
            catch (FileNotFoundException e)
            {
                this.logger.Info("try load keystore from jar package");
                // try to read keystore from package path
                // Properties properties = new Properties();
                InputStream keyStoreInputStream = getClass().getClassLoader().getResourceAsStream(keyStorePath);
                if(keyStoreInputStream == null)
                {
                    throw new FileNotFoundException("keystore neigher found in host's path nor jar package @ " + keyStorePath);
                }
                keyStore.load(keyStoreInputStream, keyStorePwC);
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePwC);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            httpsConfig = new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters HttpsParams) {
                    try
                    {
                        SSLContext sslContext = SSLContext.getDefault();
                        SSLEngine sslEngine   = sslContext.createSSLEngine();

                        HttpsParams.setNeedClientAuth(false);

                        HttpsParams.setCipherSuites(sslEngine.getEnabledCipherSuites());
                        HttpsParams.setProtocols(sslEngine.getEnabledProtocols());

                        SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                        HttpsParams.setSSLParameters(defaultSSLParameters);
                    }
                    catch (Exception ex)
                    {}
                }
            };
        }
        catch(Exception ex)
        {
            httpsConfig = null;
            this.logger.Error("while creating https configuration: %s", ex.toString());
        }

        return httpsConfig;
    }

    static class ItecNiagaraHandler implements HttpHandler 
    {
        Logger logger;
        Queue<String> queue;
        String md5ApiKey;

        public ItecNiagaraHandler(Queue<String> queue, String md5ApiKey)
        {
            this.logger    = new Logger("HTTP HANDLER");
            this.queue     = queue;
            this.md5ApiKey = md5ApiKey;
        }

        private boolean checkApiKey(String apiKeyReceived)
        {
            boolean authorized = false;

            try
            {

                byte[] key = Utils.decodeBase64(apiKeyReceived);

                String md5String = Utils.getMd5Hash(key);

                this.logger.Debug(md5String);

                authorized = md5String.equalsIgnoreCase(this.md5ApiKey);
            }
            catch (NullPointerException nEx)
            {
                this.logger.Error("key seems not to be present");
            }
            catch (Exception ex)
            {
                this.logger.Error("cannot process key check. %s", ex.toString());
            }

            return authorized;
        }

        private boolean checkHeader(Headers headers)
        {
            boolean headerOk = true;

            if(!headers.getFirst("Content-Type").equals(CONTENT_TYPE_APPLICATION_JSON))
            {
                headerOk = false;
            }
            return headerOk;
        }

        private boolean postHandle(HttpExchange exchange) 
        {
            boolean result = true;

            try 
            {
                // request
                InputStream is     = exchange.getRequestBody();
                byte[] requestBody = is.readAllBytes();
                String rb          = new String(requestBody, "utf-8");
                this.logger.Debug(rb);

                this.queue.add(rb);
                is.close();
            }
            catch (IOException io)
            {
                this.logger.Error("Cannot process body.");
                result = false;
            }
            return result;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException 
        {
            this.logger.Debug("handle call");

            String response     = "{\"result\":\"ok\"}";
            int    responseCode = HTTP_OK;

            Headers headers = exchange.getRequestHeaders();

            if(!checkHeader(headers))
            {
                this.logger.Warning("wrong header content.");
                response     = "{\"result\":\"content type mismatch\"}";
                responseCode = HTTP_BAD_REQUEST;
            }

            if(this.md5ApiKey != null && responseCode == HTTP_OK)
            {
                boolean authorized = this.checkApiKey(headers.getFirst("Token"));
                if(!authorized)
                {
                    this.logger.Warning("Connection with wrong API Key. Access denied.");
                    response     = "{\"result\":\"not authorized\"}";
                    responseCode = HTTP_NOT_AUTHORIZED;
                }
            }

            if(responseCode == HTTP_OK)
            {
                switch (exchange.getRequestMethod())
                {
                    case "POST":
                        boolean result = postHandle(exchange);
                        if(!result)
                        {
                            responseCode = HTTP_INTERNAL_SERVER_ERROR;
                            response = "{\"result\":\"error while handling post request.\"}";
                        }
                        break;
                    case "PUT":
                    case "GET":
                    default:
                        this.logger.Info("not implemented request method");
                        responseCode = HTTP_NOT_IMPLEMENTED;
                        break;
                }
            }

            // response
            exchange.sendResponseHeaders(responseCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            headers.clear();
            exchange.close();
        }
    }

    public void stop()
    {
        try
        {
            this.server.stop(0);
        }
        catch (NullPointerException np)
        {
            // ok
        }
        try 
        {
            this.secServer.stop(0);
        }
        catch (NullPointerException np)
        {
            // ok
        }
        this.logger.Info("http server stopped");
    }

    public static class ServerShutdownException extends Exception
    {
        static final long serialVersionUID = 1;

        public ServerShutdownException(String errorMessage)
        {
            super(errorMessage);
        }
    }
}
