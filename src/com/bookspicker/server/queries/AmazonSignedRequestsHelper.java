/**********************************************************************************************
 * Copyright 2009 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file 
 * except in compliance with the License. A copy of the License is located at
 *
 *       http://aws.amazon.com/apache2.0/
 *
 * or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License. 
 *
 * ********************************************************************************************
 *
 *  Amazon Product Advertising API
 *  Signed Requests Sample Code
 *
 *  API Version: 2009-03-31
 *
 */

package com.bookspicker.server.queries;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// TODO(rodrigo): format correctly

/**
 * This class contains all the logic for signing requests
 * to the Amazon Product Advertising API.
 * 
 * Took it from the forums + modified it a bit.
 * @author Jonathan 
 */
public class AmazonSignedRequestsHelper {
    private static XmlParsingTools xmlParsingTools = new XmlParsingTools();

    /**
     * All strings are handled as UTF-8
     */
    static final String UTF8_CHARSET = "UTF-8";
    
    /**
     * The HMAC algorithm required by Amazon
     */
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    
    private static final String REQUEST_URI = "/onca/xml";
    
    /**
     * The sample uses HTTP GET to fetch the response. If you changed the sample
     * to use HTTP POST instead, change the value below to POST. 
     */
    private static final String REQUEST_METHOD = "GET";

    private String endpoint = null;
    private String awsAccessKeyId = null;
    private String awsSecretKey = null;

    private SecretKeySpec secretKeySpec = null;
    private Mac mac = null;

    /**
     * You must provide the three values below to initialize the helper.
     *  
     * @param endpoint          Destination for the requests.
     * @param awsAccessKeyId    Your AWS Access Key ID
     * @param awsSecretKey      Your AWS Secret Key
     */
    public static AmazonSignedRequestsHelper getInstance(
            String endpoint, 
            String awsAccessKeyId, 
            String awsSecretKey
    ) throws IllegalArgumentException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
    {
        if (null == endpoint || endpoint.length() == 0)
            { throw new IllegalArgumentException("endpoint is null or empty"); }
        if (null == awsAccessKeyId || awsAccessKeyId.length() == 0) 
            { throw new IllegalArgumentException("awsAccessKeyId is null or empty"); }
        if (null == awsSecretKey || awsSecretKey.length() == 0)   
            { throw new IllegalArgumentException("awsSecretKey is null or empty"); }
        
        AmazonSignedRequestsHelper instance = new AmazonSignedRequestsHelper();
        instance.endpoint = endpoint.toLowerCase();
        instance.awsAccessKeyId = awsAccessKeyId;
        instance.awsSecretKey = awsSecretKey;

        byte[] secretyKeyBytes = instance.awsSecretKey.getBytes(UTF8_CHARSET);
        instance.secretKeySpec = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
        instance.mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        instance.mac.init(instance.secretKeySpec);

        return instance;
    }
    
    /**
     * The construct is private since we'd rather use getInstance()
     */
    private AmazonSignedRequestsHelper() {}

    /**
     * This method signs requests in hashmap form. It returns a URL that should
     * be used to fetch the response. The URL returned should not be modified in
     * any way, doing so will invalidate the signature and Amazon will reject
     * the request.
     */
    public String sign(Map<String, String> params) {
        // Let's add the AWSAccessKeyId and Timestamp parameters to the request.
        params.put("AWSAccessKeyId", this.awsAccessKeyId);
        params.put("Timestamp", this.timestamp());

        // The parameters need to be processed in lexicographical order, so we'll
        // use a TreeMap implementation for that.
        SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(params);
        
        // get the canonical form the query string
        String canonicalQS = xmlParsingTools.canonicalize(sortedParamMap);
        
        // create the string upon which the signature is calculated 
        String toSign = 
            REQUEST_METHOD + "\n" 
            + this.endpoint + "\n"
            + REQUEST_URI + "\n"
            + canonicalQS;

        // get the signature
        String hmac = this.hmac(toSign);
        String sig = xmlParsingTools.percentEncodeRfc3986(hmac);

        // construct the URL
        String url = 
            "http://" + this.endpoint + REQUEST_URI + "?" + canonicalQS + "&Signature=" + sig;

        return url;
    }

    /**
     * Compute the HMAC.
     *  
     * @param stringToSign  String to compute the HMAC over.
     * @return              base64-encoded hmac value.
     */
    private String hmac(String stringToSign) {
        String signature = null;
        byte[] data;
        byte[] rawHmac;
        try {
            data = stringToSign.getBytes(UTF8_CHARSET);
            rawHmac = mac.doFinal(data);
            signature = new String(Base64.encodeBytes(rawHmac));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
        }
        return signature;
    }

    /**
     * Generate a ISO-8601 format timestamp as required by Amazon.
     *  
     * @return  ISO-8601 format timestamp.
     */
    private String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    } 
}