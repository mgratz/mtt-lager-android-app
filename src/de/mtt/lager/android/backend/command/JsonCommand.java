package de.mtt.lager.android.backend.command;


import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.mtt.lager.android.backend.BackendRequest;
import de.mtt.lager.android.backend.GsonRequest;
import de.mtt.lager.android.backend.GsonRequest.ParsingListener;

abstract class JsonCommand<T> implements Listener<T>, ErrorListener, ParsingListener<T> {

	private Request<T>	request;


	JsonCommand(Gson gson, int method, String url, String requestBody) {
		this(gson, url, method, requestBody);
	}

	JsonCommand(Gson gson, String url) {
		this(gson, url, Method.GET, null);
	}


	@SuppressWarnings("ucd")
	JsonCommand(Gson gson, String url, int method, String requestBody) {
		request = createRequest(gson, method, url, requestBody);
		request.setTag(new BackendRequest());
		request.setShouldCache(false);
	}



	private Request<T> createRequest(Gson gson, int method, String url, String requestBody) {
		return new GsonRequest<T>(gson, method, url, requestBody, getTypeToken(), this, this, this);
	}


	public Request<T> getRequest() {
		return request;
	}


	protected abstract TypeToken<T> getTypeToken();

}
