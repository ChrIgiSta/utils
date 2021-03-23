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
    Date: 20.03.2021, Winterthur

    Description: Logging Tool.
*/

import java.util.Date;

public class Logger {
    public static final int LOGLEVEL_DEBUG   = 0;
    public static final int LOGLEVEL_INFO    = 1;
    public static final int LOGLEVEL_WARNING = 2;
    public static final int LOGLEVEL_ERROR   = 3;
    public static final int LOGLEVEL_NONE    = 4;

    public static final int LOGLEVEL = LOGLEVEL_DEBUG;


    private int    logLevel;
    private String logRegio;


    public Logger(int logLevel, String logRegio)
    {
        this.logLevel = logLevel;
        this.logRegio = logRegio;
        this.Debug("logger initialized");
    }

    public Logger(String logRegio)
    {
        this.logLevel = LOGLEVEL;
        this.logRegio = logRegio;
        this.Debug("logger initialized");
    }

    public void Debug(String message)
    {
        if(!MatchLoglevel(LOGLEVEL_DEBUG))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "DEBUG", message);
    }

    public void Debug(String message, Object ... arguments)
    {
        if(!MatchLoglevel(LOGLEVEL_DEBUG))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "DEBUG", String.format(message, arguments));
    }

    public void Info(String message)
    {
        if(!MatchLoglevel(LOGLEVEL_INFO))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "INFO", message);
    }

    public void Info(String message, Object ... arguments)
    {
        if(!MatchLoglevel(LOGLEVEL_INFO))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "INFO", String.format(message, arguments));
    }

    public void Warning(String message)
    {
        if(!MatchLoglevel(LOGLEVEL_WARNING))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "WARNING", message);
    }

    public void Warning(String message, Object ... arguments)
    {
        if(!MatchLoglevel(LOGLEVEL_WARNING))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "WARNING", String.format(message, arguments));
    }

    public void Error(String message)
    {
        if(!MatchLoglevel(LOGLEVEL_ERROR))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "ERROR", message);
    }

    public void Error(String message, Object ... arguments)
    {
        if(!MatchLoglevel(LOGLEVEL_ERROR))
        {
            return;
        }
        PrintLogMessage(this.logRegio, "ERROR", String.format(message, arguments));
    }

    private void PrintLogMessage(String logRegion, String logLevel, String logMessage)
    {
        Date date = new Date();
        String timeStamp = date.toString();
        String spaces = "";

        switch(logLevel)
        {
            case "DEBUG":
            case "ERROR":
                spaces = "  ";
                break;
            case "INFO":
                spaces = "   ";
                break;
            default:
                break;
        }

        System.out.println(timeStamp + " [" + logLevel + "] "+ spaces +"- " + logRegion + ": " + logMessage);
    }

    private boolean MatchLoglevel(int logLevel)
    {
        if(logLevel < this.logLevel)
        {
            return false;
        }
        return true;
    }
}
