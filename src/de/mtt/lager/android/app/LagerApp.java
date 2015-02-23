package de.mtt.lager.android.app;


import android.app.Application;
import android.os.StrictMode;
import de.mtt.lager.android.BuildConfig;
import de.mtt.lager.android.database.Article;


public class LagerApp extends Application {

	private static Module	module;


	public static Module getModule() {
		return module;
	}


	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			StrictMode.enableDefaults();
		}
		module = new Module(getApplicationContext());

		module.getBackend().putArticle(new Article((long)123456, null,null, null, null, "Test Title", "Test Description", "http://upload.wikimedia.org/wikipedia/commons/8/87/QRCode.png",null));//getArticleById("111");
	}

}
