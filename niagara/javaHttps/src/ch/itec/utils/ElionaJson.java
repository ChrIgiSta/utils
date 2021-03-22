package ch.itec.utils;

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
    Date: 22.03.2021, Winterthur

    Description: Parse json body received from eliona into
                 single vars.
*/

/**
 * json-simple
 */
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;


public class ElionaJson {
    public String deviceId;
    public float  value;
    public String json;

    //private static final String [] elionaJsonKeys = {"deviceId", "setPoint"};


    public ElionaJson ()
    {
        this.json = "";
    }

    public ElionaJson (String json)
    {
        this.json = json;
    }

    public ElionaJson (String deviceId, float value)
    {
        this.deviceId = deviceId;
        this.value    = value;
    }

    public boolean parseJson()
    {
        boolean result    = true;
        JSONParser parser = new JSONParser();

        try
        {
            Object obj            = parser.parse(this.json);
            //JSONArray jsonArray = (JSONArray)obj;
            JSONObject jsonObject = (JSONObject)obj;

            Object setpoint = jsonObject.get("setPoint");
            Object assetId  = jsonObject.get("deviceId");

            this.value    = Float.parseFloat(setpoint.toString());
            this.deviceId = assetId.toString();
         }
         catch(ParseException pe)
         {
            result = false;
         }
         catch(NullPointerException np)
         {
             result = false;
         }

         return result;
    }

    public void setJson(String json)
    {
        this.json = json;
    }
}
