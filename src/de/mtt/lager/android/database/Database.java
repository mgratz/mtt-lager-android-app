package de.mtt.lager.android.database;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import de.mtt.lager.android.database.DaoMaster.DevOpenHelper;


public class Database {

	private static final String DATABASE_NAME = "mtt_lager_app.db";

	private final DaoSession daoSession;

	private final SQLiteDatabase database;

	private final DevOpenHelper openHelper;

	public Database(Context context) {
		openHelper = new DaoMaster.DevOpenHelper(context, DATABASE_NAME, null);
		database = openHelper.getWritableDatabase();
		daoSession = new DaoMaster(database).newSession();
	}

	public void deleteArticle(Article item){
		daoSession.delete(item);
	}

	public long insertArticle(Article item){
		return daoSession.insert(item);
	}

	public List<Article> selectAllArticles(){
		return daoSession.loadAll(Article.class);
	}

	public void updateArticle(Article item){
		daoSession.update(item);
	}
}



