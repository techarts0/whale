package cn.techarts.whale.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class Party implements AutoCloseable{
	private int id;
	private String name;
	private int memebers;
	
	@Inject
	private Person chairman;
	
	public Party() {}

	public Person getChairman() {
		return chairman;
	}

	public void setChairman(Person chairman) {
		this.chairman = chairman;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
