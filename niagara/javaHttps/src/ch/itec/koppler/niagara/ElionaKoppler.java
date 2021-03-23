package ch.itec.koppler.niagara;

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

    Description: Eliona to Niagara Interface over HTTP
                 with json formated Payload.

    You maid test it with curl:
        curl --request POST --header 'Content-Type: application/json' \
        --header 'token: yourVerySecretGenWith./create_api_key.sh' \
        --data-raw '{"deviceId":"myDevId","setPoint":123.456}' \
        https://localhost:1234/command -k
*/

import ch.itec.utils.Utils;
import ch.itec.utils.Logger;
import ch.itec.utils.ElionaJson;
import ch.itec.connection.http.ItecHttpServer;

import java.util.Queue;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class ElionaKoppler 
{
    // public data from last received frame
    public String  lastJson;
    public String  lastId;
    public float   lastValue;
    public boolean enabled;

    // server configuration
    static int     SERVER_PORT            = 13443;
    static boolean HTTPS                  = true;
    static int     QUEUE_POLL_INTERVAL_MS = 500;
    static String  KEY_STORE_PATH         = "ch/itec/keystore/niagara.ks";
    static String  KEY_STORE_PASSWORD     = "N1agArA.Eli0na";
    static String  API_KEY_MD5            = "51d53c2b5466e1917fec70727ba8c529";

    static boolean RESTART_BY_ERROR = true;
    static int     RESTART_INTERVAL = 5;

    // private vars
    private Logger                      logger;
    private Queue<String>               bodys;
    private ItecHttpServer.serverConfig config;
    private ItecHttpServer              server;
    private boolean                     interrupted;

    public ElionaKoppler()
    {
        this.logger = new Logger("KOPPLER");
        this.bodys  = new LinkedList<>();

        this.enabled     = true;
        this.interrupted = false;

        this.config = new ItecHttpServer.serverConfig();

        this.config.localPort        = SERVER_PORT;
        this.config.listenIp         = ItecHttpServer.LISTEN_TO_ALL_IP;
        this.config.useSsl           = HTTPS;
        this.config.keyStore         = KEY_STORE_PATH;
        this.config.keyStorePassword = KEY_STORE_PASSWORD;
        this.config.md5SumApiKey     = API_KEY_MD5;

        this.server = new ItecHttpServer(this.config, this.bodys);

        this.logger.Debug("init koppler");
    }

    public void stop()
    {
        this.interrupted = true;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    private void serveForever()
    {
        try
        {
            this.server.start();
        }
        catch (ItecHttpServer.ServerShutdownException shEx)
        {
            this.logger.Error(shEx.toString());
            this.interrupted = true;
        }

        ElionaJson device = new ElionaJson();

        while(this.enabled && !this.interrupted)
        {
            try
            {
                String newJsonInput = bodys.remove();
                this.logger.Info("a new message received: %s", newJsonInput);

                device.setJson(newJsonInput);
                boolean parseResult = device.parseJson();

                if(parseResult)
                {
                    this.logger.Debug("json parsed > device: %s, value: %f", device.deviceId, device.value);
                    this.lastId    = device.deviceId;
                    this.lastValue = device.value;
                }
                else
                {
                    this.logger.Error("error parsing device from json");
                }
                this.lastJson = newJsonInput;
            }
            catch (NoSuchElementException e)
            {
                // do nothing, queue is empty
                try
                {
                    Thread.sleep(QUEUE_POLL_INTERVAL_MS);
                }
                catch(InterruptedException ir)
                {
                    // wakeup
                }
            }
        }
        this.server.stop();
        this.logger.Info("serve stopped");
    }

    /**
     * Entry point
     */
    public static void main(String[] args)
    {
        Logger        logger         = new Logger("MAIN");
        int           waitForRestart = 0;

        if(RESTART_BY_ERROR)
        {
            waitForRestart = RESTART_INTERVAL;
        }

        do
        {
            ElionaKoppler koppler = new ElionaKoppler();
            koppler.serveForever();
            koppler.stop();
            logger.Warning("server has stopped!");

            if(RESTART_BY_ERROR)
            {
                logger.Info("try to restart in ");
            }

            for(int i = waitForRestart; i > 0; i--)
            {
                System.out.print(i + " seconds\r");
                // do nothing, queue is empty
                try
                {
                    Thread.sleep(1000);
                }
                catch(InterruptedException ir)
                {
                    // wakeup
                }
            }

            if(!RESTART_BY_ERROR || !koppler.isEnabled())
            {
                break;
            }
        } while(true);
    }
}