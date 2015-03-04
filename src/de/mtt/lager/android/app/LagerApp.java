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


	private Article createArticle(String title, String description, String pictureUrl, String articleBarcode){
		Article result = new Article();
		result.setTitle(title);
		result.setDescription(description);
		result.setPictureURL(pictureUrl);
		result.setOrderno(articleBarcode);
		return result;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			StrictMode.enableDefaults();
		}
		module = new Module(getApplicationContext());

		createArticle("blend-a-med complete plus 75ml", "1.Kariesschutz 2.Zahnsteinschutz 3.Zahnhalskariesschutz",
				"http://media.oralb-blendamed.de/LocaleData/de-DE/Assets/Images/products/5000174118452%20blend-a-med%20Complete%207%20Extra%20Fresh_main.png","5000174118452");

		//module.getBackend().putArticle(blend);

		//module.getBackend().getArticleById(String.valueOf(41005958));
	}

}
