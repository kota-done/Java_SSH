package com.example.todolist.entity;

import java.sql.Date;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Todo.class)
public abstract class Todo_ {

	public static volatile SingularAttribute<Todo, Integer> urgency;
	public static volatile SingularAttribute<Todo, Integer> importance;
	public static volatile SingularAttribute<Todo, Integer> id;
	public static volatile SingularAttribute<Todo, String> title;
	public static volatile SingularAttribute<Todo, Date> deadline;
	public static volatile SingularAttribute<Todo, String> done;

	public static final String URGENCY = "urgency";
	public static final String IMPORTANCE = "importance";
	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String DEADLINE = "deadline";
	public static final String DONE = "done";
}
