package com.smatiukaite.notetakingapp.JsonArrayRequestSim;

import androidx.annotation.Nullable;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JsonArrayRequestSim extends JsonRequest<JSONArray> {
    public JsonArrayRequestSim(
            String url, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener) {
        super(Request.Method.GET, url, null, listener, errorListener);
    }

    /**
     * Creates a new request.
     *
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param jsonRequest A {@link JSONArray} to post with the request. Null indicates no parameters
     *     will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequestSim(
            int method,
            String url,
            @Nullable JSONObject jsonRequest,
            Response.Listener<JSONArray> listener,
            @Nullable Response.ErrorListener errorListener) {
        super(
                method,
                url,
                jsonRequest != null ? jsonRequest.toString() : null,
                listener,
                errorListener);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(
                    new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}