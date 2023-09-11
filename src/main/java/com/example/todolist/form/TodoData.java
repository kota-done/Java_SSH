package com.example.todolist.form;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.example.todolist.entity.Todo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoData {
	private Integer id;
	@NotBlank //バリデーションファイルに詳細を記載したため削除　i18nファイル
	private String title;

	@NotNull
	private Integer importance;

	@Min(value = 0)
	private Integer urgency;
	private String deadline;
	private String done;

	//入力データからEntityを生成してかえす
	public Todo toEntity() {
		Todo todo = new Todo();
		todo.setId(id);
		todo.setTitle(title);
		todo.setImportance(importance);
		todo.setUrgency(urgency);
		todo.setDone(done);

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		long ms;
		try {
			ms = sdFormat.parse(deadline).getTime();
			todo.setDeadline(new Date(ms));
		} catch (ParseException e) {
			// TODO: handle exception
			todo.setDeadline(null);
		}
		return todo;
	}

}
