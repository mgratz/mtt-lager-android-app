package de.mtt.lager.android.app;


import android.content.Context;
import de.mtt.lager.android.backend.Backend;
import de.mtt.lager.android.database.Database;


public class Module {

	private final Backend		backend;
	private final Context		context;
	private final Database		database;

	public Module(Context context) {
		this.context = context;
		this.database = new Database(context);
		this.backend = new Backend(context);
	}


	public Context getAppContext() {
		return context;
	}


	public Backend getBackend() {
		return backend;
	}


	public Database getDatabase() {
		return database;
	}

}
