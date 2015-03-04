package de.mtt.lager.android.backend;


public class BusData<T>  {

	private final T data;

	public BusData(T data){
		this.data = data;
	}

	public T getData(){
		return this.data;
	}
}
