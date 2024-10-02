package cn.techarts.whale.test;

import javax.inject.Singleton;
//import jakarta.inject.Singleton;

@Singleton
public class Party {
	private int id;
	private String name;
	private int memebers;
	private Chairman chairman;
	
	public Party() {}
	
	public Party(int id) {
		this.setId(id);
	}

	public Chairman getChairman() {
		return chairman;
	}

	public void setChairman(Chairman chairman) {
		this.chairman = chairman;
	}

	public int getMemebers() {
		return memebers;
	}

	public void setMemebers(int memebers) {
		this.memebers = memebers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
