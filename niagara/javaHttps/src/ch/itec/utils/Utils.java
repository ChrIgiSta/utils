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

    Description: Logger Tools.
*/

import java.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils {

    public static byte[] decodeBase64(String input) throws NullPointerException
    {
        return Base64.getDecoder().decode(input);
    }

    public static String getMd5Hash(byte[] input) throws NoSuchAlgorithmException, NullPointerException
    {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(input);
        byte[] md5Hash = messageDigest.digest();

        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : md5Hash) 
        {
            stringBuilder.append(String.format("%02X", b));
        }

        return stringBuilder.toString();
    }
}