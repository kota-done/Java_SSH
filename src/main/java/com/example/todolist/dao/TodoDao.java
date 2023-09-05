package com.example.todolist.dao;

import java.util.List;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {
	//JPQLによる検索
	List<Todo> findByJPQL(TodoQuery todoQuery);

	//Criteria APIによる検索 今回使用する方。　TodoQueryオブジェクトを受け取り、リスト配列に返す抽象メソッドを定義
	List<Todo> findByCriteria(TodoQuery todoaQuery);
}
