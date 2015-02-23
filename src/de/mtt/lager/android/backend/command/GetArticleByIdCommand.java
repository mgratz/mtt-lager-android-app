package de.mtt.lager.android.backend.command;


import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.mtt.lager.android.backend.Backend;
import de.mtt.lager.android.backend.BackendResult;


public class GetArticleByIdCommand extends JsonCommand<BackendResult> {

	private static final String TAG = GetArticleByIdCommand.class.getCanonicalName();

	private final static String url = Backend.BASE_URL + "/article?id=";

	public GetArticleByIdCommand(Gson gson, String id) {
		super(gson, url+id);

	}

	@Override
	protected TypeToken<BackendResult> getTypeToken() {
		return new TypeToken<BackendResult>() {
		};
	}


	@Override
	public void onErrorResponse(VolleyError error) {
		Log.d(TAG, "error");
	}


	@Override
	public void onResponse(BackendResult response) {
		Log.d(TAG, response.toString());

	}


	@Override
	public void onResponseParsed(BackendResult parseResult) {
		// nothing to do here
		Log.d(TAG, parseResult.toString());
	}
}