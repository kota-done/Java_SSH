package com.example.todolist.form;

import lombok.Data;

//検索処理用のモデルフォーム
@Data
public class TodoQuery {

	private String title;
	private Integer importance;
	private Integer urgency;
	private String deadlineFrom;
	private String deadlineTo;
	private String done;

	//入力用のTodoDataと違い必須条件が不要。
	public TodoQuery() {
		title = "";
		importance = -1;
		urgency = -1;
		deadlineFrom = "";
		deadlineTo = "";
		done = "";
	}
}
