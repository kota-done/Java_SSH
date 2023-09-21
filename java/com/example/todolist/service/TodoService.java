package com.example.todolist.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.AttachedFile;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TaskData;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.AttachedFileRepository;
import com.example.todolist.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
//ALLArgsConstructorから変更、ATTACHED_FILE_PATHはString型のためインジェクションしようとするとエラーとなるため下記にすることで、finalのみに限定できる。
@RequiredArgsConstructor
public class TodoService {
	//エラーメッセージ
	private final MessageSource messageSource; //国際化に対応したメッセージの型
	private final AttachedFileRepository attachedFileRepository; //

	@Value("${attached.file.path}") //application.propaに設定した格納フォルダ名をATTACHED_FILE_PATHに設定（"C:/tempt/uploadFiles"）
	private String ATTACHED_FILE_PATH; //

	//Todo+Taskチェック
	public boolean isValid(TodoData todoData, BindingResult result, boolean isCreate, Locale locale) {
		boolean ans = true;
		//件名が全角のときはエラーで返す
		String title = todoData.getTitle();
		if (title != null && !title.equals("")) {
			boolean isAllDoubleSpace = true;
			for (int i = 0; i < title.length(); i++) {
				if (title.charAt(i) != '　') {
					isAllDoubleSpace = false;
					break;
				}
			}
			if (isAllDoubleSpace) {
				FieldError fieldError = new FieldError(result.getObjectName(), "title",
						messageSource.getMessage("DoubleSpace.todoData.title", null, locale));
				//Localeに表示地域の特徴を取得している。getMessageメソッドには（取得ファイル名、エラーメッセージのプレースフォルダー、どの言語か）
				result.addError(fieldError);
				ans = false;
			}
		}
		//Todoの期限が過去の日付ならエラー
		String deadline = todoData.getDeadline();
		if (!deadline.equals("")) {
			LocalDate tody = LocalDate.now();
			LocalDate deadlineDate = null;

			try {
				deadlineDate = LocalDate.parse(deadline);
				if (deadlineDate.isBefore(tody)) {
					FieldError fieldError = new FieldError(result.getObjectName(), "deadline",
							messageSource.getMessage("InvalidFormat.todoData.deadline", null, locale));
					result.addError(fieldError);
					ans = false;
				}
			} catch (DateTimeException e) {
				// TODO: handle exception
				FieldError fieldError = new FieldError(result.getObjectName(), "deadline",
						messageSource.getMessage("Past.todoData.deadline", null, locale));
				result.addError(fieldError);
				ans = false;
			}
		}
		//Taskチェック
		List<TaskData> taskList = todoData.getTaskList();
		if (taskList != null) {
			//タスクリスト内のすべてのタスクを取得　for文を使用
			for (int n = 0; n < taskList.size(); n++) {
				TaskData taskData = taskList.get(n);
				//タスクの件名が全角スペースのみならエラー
				if (!Utils.isBlank(taskData.getTitle())) {
					if (Utils.isAllDoubleSpace(taskData.getTitle())) {
						//
						FieldError fieldError = new FieldError(result.getObjectName(), "taskList[" + n + "].title",
								messageSource.getMessage("DoubleSpace.todoData.title", null, locale));
						result.addError(fieldError);
						ans = false;
					}
				}
				//タスクの期限のチェック
				String taskDeadline = taskData.getDeadline();
				if (!taskDeadline.equals("") && !Utils.isValidDateFormat(taskDeadline)) {
					FieldError fieldError = new FieldError(result.getObjectName(), "taskList[" + n + "].deadline",
							messageSource.getMessage("InvalidFormat.todoData.deadline", null, locale));
					result.addError(fieldError);
					ans = false;
				}
			}
		}

		return ans;
	}

	//検索条件の
	public boolean isValid(TodoQuery todoQuery, BindingResult result, Locale locale) {
		boolean ans = true;
		//期限・開始の形式をチェック
		String date = todoQuery.getDeadlineFrom();
		if (!date.equals("")) {
			try {
				LocalDate.parse(date);

			} catch (DateTimeException e) {
				// TODO: handle exception
				FieldError fieldError = new FieldError(result.getObjectName(), "deadlineFrom",
						messageSource.getMessage("InvalidFormat.todoQuery.deadlineFrom", null, locale));
				result.addError(fieldError);
				ans = false;
			}
		}
		//期限：終了の形式をチェック
		date = todoQuery.getDeadlineTo();
		if (!date.equals("")) {
			try {
				LocalDate.parse(date);
			} catch (DateTimeException e) {
				// TODO: handle exception
				FieldError fieldError = new FieldError(result.getObjectName(), "deadlineTo",
						messageSource.getMessage("InvalidFormat.todoQuery.deadlineTo", null, locale));
				result.addError(fieldError);
				ans = false;
			}
		}
		return ans;
	}

	//新規タスクのチェック用
	public boolean isValid(TaskData taskData, BindingResult result, Locale locale) {
		boolean ans = true;
		//タスクの件名が半角スペースだけ、もしくは””ならエラー
		if (Utils.isBlank(taskData.getTitle())) {
			FieldError fieldError = new FieldError(result.getObjectName(), "newTask.title",
					messageSource.getMessage("NotBlank.taskData.title", null, locale));
			result.addError(fieldError);
			ans = false;
		} else {
			//タスクの件名が全角スペースだけで構成されていたらエラー
			if (Utils.isAllDoubleSpace(taskData.getTitle())) {
				FieldError fieldError = new FieldError(result.getObjectName(), "newTask.title",
						messageSource.getMessage("DoubleSpace.todoData.title", null, locale));
				result.addError(fieldError);
				ans = false;
			}
		}
		//期限が””ならチェックしない
		String deadline = taskData.getDeadline();
		if (deadline.equals("")) {
			return ans;
		}

		//期限のyyyy-mm-dd形式チェック
		if (!Utils.isValidDateFormat(deadline)) {
			FieldError fieldError = new FieldError(result.getObjectName(), "newTask.deadline",
					messageSource.getMessage("InvalidFormat.todoData.deadline", null, locale));
			result.addError(fieldError);
			ans = false;
		} else {
			//過去日付ならエラー
			if (!Utils.isTodayOrFurtureDate(deadline)) {
				FieldError fieldError = new FieldError(result.getObjectName(), "newTask.deadline",
						messageSource.getMessage("Past.todoData.deadline", null, locale));
				result.addError(fieldError);
				ans = false;
			}
		}
		return ans;
	}

	//ファイル登録用のバリデーション+メソッド
	public void saveAttachedFile(int todoId, String note, MultipartFile fileContents) {
		//アップロード元ファイル名
		String fileName = fileContents.getOriginalFilename();
		//格納ファイルの存在チェック
		File uploadDir = new File(ATTACHED_FILE_PATH);
		if (!uploadDir.exists()) {
			//ファイルが存在しない場合はディレクトリ作成
			uploadDir.mkdirs();
		}
		//格納ファイルの格納時刻を取得
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String createTime = sdf.format(new Date());

		//テーブルへ格納するインスタンス作成
		AttachedFile af = new AttachedFile();
		af.setTodoId(todoId);
		af.setFileName(fileName);
		af.setCreateTime(createTime);
		af.setNote(note);

		//アップロードファイルの内容を取得
		byte[] contents;
		try (BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(Utils.makeAttahcedFilePath(ATTACHED_FILE_PATH, af)))) {
			//書き込み
			contents = fileContents.getBytes();
			bos.write(contents);
			//テーブルへ記録
			attachedFileRepository.saveAndFlush(af);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	//添付ファイル削除処理(更新画面からの個別用)
	public void deleteAttachedFile(int afId) {
		AttachedFile af = attachedFileRepository.findById(afId).get();
		File file = new File(Utils.makeAttahcedFilePath(ATTACHED_FILE_PATH, af));
		file.delete();
	}

	//添付ファイル削除処理（Todo削除に伴う一括削除） 上記とほとんど同じ、リスト配列でIDを元にテーブルないの情報を受け取り、for文を使用して一つずつ削除を実行。
	public void deleteAttachedFiles(Integer todoId) {
		File file;
		List<AttachedFile> attachedFiles = attachedFileRepository.findByTodoIdOrderById(todoId);
		for (AttachedFile af : attachedFiles) {
			file = new File(Utils.makeAttahcedFilePath(ATTACHED_FILE_PATH, af));
			file.delete();
		}

	}

	//レポジトリで設定した検索処理の実行メソッドの設定
	private final TodoRepository todoRepository;

	public List<Todo> doQuery(TodoQuery todoQuery) {
		List<Todo> todoList = null;
		if (todoQuery.getTitle().length() > 0) {
			//タイトル検索
			todoList = todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");
		} else if (todoQuery.getImportance() != -1) {
			//重要度検索
			todoList = todoRepository.findByImportance(todoQuery.getImportance());
		} else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
			//緊急度検索
			todoList = todoRepository.findByUrgency(todoQuery.getUrgency());
		} else if (!todoQuery.getDeadlineFrom().equals("") && todoQuery.getDeadlineTo().equals("")) {
			//期限：開始〜
			todoList = todoRepository
					.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineFrom())); //Utilsは文字列をjava.sql.Date型に変換するヘルパーメソッド

		} else if (todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			//期限：~終了
			todoList = todoRepository
					.findByDeadlineLessThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadlineTo())); //)
		} else if (!todoQuery.getDeadlineFrom().equals("") && !todoQuery.getDeadlineTo().equals("")) {
			//期限　開始〜終了
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
					Utils.str2date(todoQuery.getDeadlineFrom()), Utils.str2date(todoQuery.getDeadlineTo()));
		} else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			//完了で検索
			todoList = todoRepository.findByDone("Y");
		} else {
			//入力条件がなければ全件検索
			todoList = todoRepository.findAll();
		}
		return todoList; //検索結果
	}

}
