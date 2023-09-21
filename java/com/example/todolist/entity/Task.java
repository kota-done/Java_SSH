package com.example.todolist.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task")
@Data
//連携したtodoを使用時に、バインドされた全てのプロパティを対象とするためにコンストラクタを生成する9/13
@AllArgsConstructor
@NoArgsConstructor
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@ManyToOne //taskテーブルが多重関係におけるn側である。
	@JoinColumn(name = "todo_id") //関連する主要キーの指定。
	private Todo todo; //

	@Column(name = "title")
	private String title;

	@Column(name = "deadline")
	private Date deadline;

	@Column(name = "done")
	private String done;

}
