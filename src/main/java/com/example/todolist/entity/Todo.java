package com.example.todolist.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

//エンティティクラスと宣言
@Entity
//対応するテーブルを設定
@Table(name = "todo")
@Data
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

}
