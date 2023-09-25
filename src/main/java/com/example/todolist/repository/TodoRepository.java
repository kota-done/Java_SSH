package com.example.todolist.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todolist.entity.Todo;

//エンティティで操作するテーブルを指定し、リポジトリで具体的な操作を記述する。

//第一引数：対象のエンティティ　第二引数：＠Idが指定されているクラス（id=Integer）を継承することでCRUD処理が一通り抽象メソッドとして実装した。
@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {

	//検索処理で追記（9/1）　findByで検索、orderByで並び替え
	List<Todo> findByTitleLike(String title); //select * from todo where title like '引数'と同様

	List<Todo> findByImportance(Integer importance); //Select *From  todo where importane 引数　と同様

	List<Todo> findByUrgency(Integer urgency);

	List<Todo> findByDeadlineBetweenOrderByDeadlineAsc(Date from, Date to); //引数二つの間にdeadlineがあるかどうか検索

	List<Todo> findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Date from);

	List<Todo> findByDeadlineLessThanEqualOrderByDeadlineAsc(Date to);

	List<Todo> findByDone(String done);

	@Override
	List<Todo> findAll();

	//PDF化のために追加（9/25）
	List<Todo> findAllByOrderById();

}
