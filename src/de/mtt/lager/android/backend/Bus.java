package de.mtt.lager.android.backend;

import de.mtt.lager.android.database.Article;


public class Bus {

	public static class Articles{

		public static class Failure{
			public Failure(){

			}
		}

		public static class Success extends BusData<Article>{
			public Success(Article article){
				super(article);
			}
		}

	}
}
