package com.example.todolist.form;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.AttachedFile;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

	@Valid
	private List<TaskData> taskList; //タスク一覧のプロパティを追加

	private TaskData newTask; //新規タスク入力行用
	//入力データからEntityを生成してかえす

	//添付ファイルのList
	private List<AttachedFileData> attachedFileList; //

	public Todo toEntity() {
		//Todo部分
		Todo todo = new Todo();
		todo.setId(id);
		todo.setTitle(title);
		todo.setImportance(importance);
		todo.setUrgency(urgency);
		todo.setDone(done);

		//Task部分 バインドされたtaskListからTaskオブジェクトを生成してセットする。
		Date date;
		Task task;
		if (taskList != null) {
			for (TaskData taskData : taskList) {
				date = Utils.str2dateOrNull(taskData.getDeadline());
				task = new Task(taskData.getId(), null, taskData.getTitle(), date, taskData.getDone()); //第2引数にnullを渡すことで
				todo.addTask(task);
			}
		}

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

	//Todoの内容から入力画面へ渡すTdoDataを生成する＋添付ファイルAttachedFileも渡す（9/19)
	public TodoData(Todo todo, List<AttachedFile> attachedFiles) {
		//Todo部分
		this.id = todo.getId();
		this.title = todo.getTitle();
		this.importance = todo.getImportance();
		this.urgency = todo.getUrgency();
		this.deadline = Utils.date2str(todo.getDeadline());
		this.done = todo.getDone();

		//登録済みTask
		this.taskList = new ArrayList<>();
		String dt;
		for (Task task : todo.getTaskList()) {
			dt = Utils.date2str(task.getDeadline());
			this.taskList.add(new TaskData(task.getId(), task.getTitle(), dt, task.getDone()));
		}
		//Task追加用
		newTask = new TaskData();
		//添付ファイル用
		attachedFileList = new ArrayList<>();
		String fileName;
		String fext;
		String contentType;
		boolean isOpenNewWindow;
		for (AttachedFile af : attachedFiles) {
			//ファイル名
			fileName = af.getFileName();
			//拡張子
			fext = fileName.substring(fileName.lastIndexOf(".") + 1);
			//Content-Type
			contentType = Utils.ext2contentType(fext); //ファイル拡張子から、Content-Typeの対応するファイルタイプを求めるメソッド。
			//別Windowで表示するか？
			isOpenNewWindow = contentType.equals("") ? false : true;
			attachedFileList.add(new AttachedFileData(af.getId(), fileName, af.getNote(), isOpenNewWindow));
		}
	}

	//新規タスク入力画面からTaskオブジェクトを生成して返すメソッド
	public Task toTaskEntity() {
		Task task = new Task();
		task.setId(newTask.getId());
		task.setTitle(newTask.getTitle());
		task.setDone(newTask.getDone());
		task.setDeadline(Utils.str2date(newTask.getDeadline()));
		return task;
	}

}
