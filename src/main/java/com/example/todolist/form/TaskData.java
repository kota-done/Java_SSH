package com.example.todolist.form;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//空白チェックなどの基本はアノテーションを使用するが、それ以外はもともとTodoDataに追加することで、再利用。あくまでフィールド作成するのみ。

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskData {
	private Integer id;

	@NotBlank
	private String title;

	private String deadline;
	private String done;
}
