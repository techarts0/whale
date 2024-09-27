package cn.techarts.whale.test;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import cn.techarts.whale.Valued;

@Singleton
public class Person {
	@Inject
	@Valued(key="user.id")
	private int id;
	private String name;
	
	private Provider<Mobile> mobile;
	
	@Inject
	private Provider<Office> office;
	
	@Inject
	private SomeInterface service;
	
	@Inject
	public Person(Provider<Mobile> mobile) {
		this.mobile = mobile;
	}
	
	public SomeInterface getService() {
		return this.service;
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

	public Mobile getMobile() {
		return mobile.get();
	}
	
	public Office getOffice() {
		return office.get();
	}
}
