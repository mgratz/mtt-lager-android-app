package de.mtt.lager.android.backend;


import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import de.mtt.lager.android.backend.command.GetArticleByIdCommand;
import de.mtt.lager.android.backend.command.PutArticleCommand;
import de.mtt.lager.android.database.Article;

public class Backend {

	public static final String BASE_URL = "http://89.200.174.33:8181";

	private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		// 2014-05-21T22:00:00+00:00P1D
		builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		Gson gson = builder.create();
		return gson;
	}
	private final Gson			gson;

	private final RequestQueue	requestQueue;

	public Backend(Context context) {
		gson = createGson();
		requestQueue = Volley.newRequestQueue(context);
	}


	public BackendRequest getArticleById(String id) {
		GetArticleByIdCommand command = new GetArticleByIdCommand(gson, id);
		requestQueue.add(command.getRequest());
		return (BackendRequest) command.getRequest().getTag();
	}

	public RequestQueue getRequestQueue() {
		return requestQueue;
	}


	public void killBackendRequest(BackendRequest backendRequest) {
		requestQueue.cancelAll(backendRequest);
	}


	public BackendRequest putArticle(Article article) {
		JsonObject json = new JsonObject();

		if(article.getOrderno()!=null){
			json.addProperty("id", article.getOrderno());
		}

		if(article.getTitle()!=null){
			json.addProperty("title", article.getTitle());
		}

		if(article.getDescription()!=null){
			json.addProperty("description", article.getDescription());
		}

		if(article.getPictureURL()!=null){
			json.addProperty("pictureURL", article.getPictureURL());
		}

		PutArticleCommand command = new PutArticleCommand(gson, json.toString());
		requestQueue.add(command.getRequest());
		return (BackendRequest) command.getRequest().getTag();
	}
}

