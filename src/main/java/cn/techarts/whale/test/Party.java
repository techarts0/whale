package cn.techarts.whale.test;

import javax.inject.Singleton;
//import jakarta.inject.Singleton;

@Singleton
public class Party {
	private int id;
	private String name;
	private int members;
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

	public int getMembers() {
		return members;
	}

	public void setMembers(int memebers) {
		this.members = memebers;
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
