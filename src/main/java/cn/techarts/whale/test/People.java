package cn.techarts.whale.test;

import javax.inject.Inject;

import cn.techarts.whale.Valued;

@Student
public class People {
	private int id;
	private String name;
	
	@Inject
	public People(@Valued(val="3")int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
