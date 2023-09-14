package com.example.todolist.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

//エンティティクラスと宣言
@Entity
//対応するテーブルを設定
@Table(name = "todo")
@Data
//9/12追加
@ToString(exclude = "taskList") //@Dataの自動生成のtoStringから除外しないとtaskListは自分でオブジェクトを保持するのでStackOverFlowとなる。
public class Todo {
	//Todoのデータベースのカラム、IDは主キーであると表す。
	@Id
	//主キーに自動で番号を割り振ることを宣言。「GenerationType」の方式を採用している。
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	@Column(name = "id")

	private Integer id;

	@Column(name = "title")
	private String title;

	@Column(name = "importance")
	private Integer importance;

	@Column(name = "urgency")
	private Integer urgency;

	@Column(name = "deadline")
	private Date deadline;

	@Column(name = "done")
	private String done;

	//9/12taskテーブル追加による追加
	@OneToMany(mappedBy = "todo", cascade = CascadeType.ALL) //1側であることを表す。Task.javaのManyToOneのプロパティと同期している。 
	//cascadeは登録、削除、更新といった処理を両方ともに適用させる
	@OrderBy("id asc") //テーブル更新時に自動で並び順が変更されるため、idで昇順になるように設定。
	private List<Task> taskList = new ArrayList<>();

	//Todoへの参照設定
	public void addTask(Task task) {
		task.setTodo(this);
		taskList.add(task);
	}
}
