package de.mtt.lager.android.backend;


import java.io.UnsupportedEncodingException;

import org.apache.http.protocol.HTTP;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import de.mtt.lager.android.BuildConfig;


public class GsonRequest<T> extends JsonRequest<T> {

	/**
	 * Callback interface for delivering parsed responses on the isAbleToDraw thread.
	 */
	public interface ParsingListener<T> {

		/**
		 * Called when a response is received and parsed, but compared with Listener<T>.onResponse this method is called
		 * on the isAbleToDraw thread.
		 */
		void onResponseParsed(T parseResult);
	}

	private final Gson			gson;
	private final ParsingListener<T>	parsingListener;
	private final TypeToken<T>	token;


	public GsonRequest(Gson gson, int method, String url, String requestBody, TypeToken<T> token, Listener<T> listener,
			ErrorListener errorListener, ParsingListener<T> parsingListener) {
		super(method, url, requestBody, listener, errorListener);
		this.gson = gson;
		this.token = token;
		this.parsingListener = parsingListener;
	}

	private T parse(String json) {
		T parseResult = gson.fromJson(json, token.getType());
		if (parsingListener != null) {
			parsingListener.onResponseParsed(parseResult);
		}
		return parseResult;
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		if (isCanceled()) {
			return null;
		}
		try {
			String charset = HttpHeaderParser.parseCharset(response.headers);
			if (TextUtils.isEmpty(charset) || charset.equalsIgnoreCase(HTTP.DEFAULT_CONTENT_CHARSET)) {
				charset = "UTF-8";
			}
			String json = new String(response.data, charset);
			if (BuildConfig.DEBUG) {
				Log.i("GsonRequest", "received Json \n" + json);
			}
			return Response.success(parse(json), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JsonParseException e) {
			return Response.error(new ParseError(e));
		}
	}

}
