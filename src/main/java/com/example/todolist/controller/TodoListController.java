package com.example.todolist.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.todolist.common.OpMsg;
import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor //

public class TodoListController {
	private final HttpSession session;
	private final TodoRepository todoRepository;
	private final TodoService todoService; //データベース操作用で追加
	private final MessageSource messageSource;
	private final TaskRepository taskRepository; //タスク処理用

	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv,
			@PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable) {
		//一覧表示
		mv.setViewName("todoList");
		//ページング設定のため変更（9/7）
		//		List<Todo> todoList = todoRepository.findAll();//「/todo」が実行されたら、メソッド実行。

		//追加機能：登録・更新・削除後もページングや検索情報を保持して表示のため、todoQueryを最初に宣言して内容を追加するように記述。
		Page<Todo> todoPage = todoRepository.findAll(pageable); //ページネーションの結果をわたす
		mv.addObject("todoList", todoPage.getContent()); //Pageオブジェクトのページ情報に加えて、次に表示するページ単位のデータをコンテンツとして持っている。
		mv.addObject("todoPage", todoPage);
		mv.addObject("todoQuery", new TodoQuery()); //
		session.setAttribute("todoQuery", new TodoQuery()); //ページリングに現在の検索条件をセットして確保しておく
		//9/12　テーブル連携のため追加
		List<Todo> todoList = todoRepository.findAll();
		List<Task> taskList;
		for (Todo todo : todoList) {
			System.out.println(todo);
			taskList = todo.getTaskList();
			if (taskList.size() == 0) {
				System.out.println("\tTask not found.");
			} else {
				for (Task task : taskList) {
					System.out.println("\t" + task);
				}
			}
		}
		return mv;
	}

	//新規登録
	//Todoデータ登録・登録と削除ボタンをわけるために追記（9/1）
	@GetMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv) {
		mv.setViewName("todoForm");
		mv.addObject("todoData", new TodoData()); //初期状態のハンドラーTodoDataオブジェクトを渡す
		session.setAttribute("mode", "create");
		return mv;
	}

	@PostMapping("/todo/create/do")
	public String createTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, //TodoDataオブジェクトにバインドし、この時点でidはnull
			Model model, RedirectAttributes redirectAttributes, Locale locale) {
		boolean isValid = todoService.isValid(todoData, result, true, locale);
		//serviceクラスのバリデーションもクリアしたら登録処理に移る
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			//saveAndFlushのメソッド処理で「カラム毎の内容をデータベースに登録」までをスプリングで実行してくれる。レポジトリクラスで実装しているから可能。
			todoRepository.saveAndFlush(todo);
			String msg = messageSource.getMessage("msg.i.todo_created", null, locale);
			//９/11 リダイレクトの場合、Model経由はデータ保持のスコープ外。redirectAttributeを使用する
			redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
			return "redirect:/todo/" + todo.getId();//検索結果も再表示するために、redirect:/todo/queryとしていたが、新規登録後、そのままタスクも登録できるように再度入力画面に遷移するように変更（9/14）
			//return showTodoList(mv);　アドレスがtodo/createのままで一覧表示のメソッドを動かそうとすると、再読み込みが発生し、
			//実行するとブラウザは直前動作も一緒に実行するため「登録・画面表示」の二つが実行されてしまう。そのためリダイレクトで直接URLを移動してからメソッドを実行するように設定する。

		} else {
			//			mv.setViewName("todoForm");
			//＠ModelAttributeがあることでmv.addObject("todoData",todoData)の記述が不要となる。自動的に発生したデータを遷移先でも表示可能。しかし、第一引数は使用するモデルクラス（頭文字は小文字である必要がある。
			//エラー発生時はModelAndViewの機能は不要のため、ビューに渡すデータの保持（Model）を使用するだけで良い。

			//9/11追加　操作メッセージ用
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			model.addAttribute("msg", new OpMsg("E", msg)); //"msg"という名称のOpMsgオブジェクトを生成、メッセージ種別はErrorのため"E"を引数にしている。
			return "todoForm";
		}
	}

	//Task新規登録
	@PostMapping("/task/create")
	public String createTask(@ModelAttribute TodoData todoData, BindingResult result, Model model,
			RedirectAttributes redirectAttributes, Locale locale) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData.getNewTask(), result, locale);
		if (isValid) {
			//エラーなし
			Todo todo = todoData.toEntity();
			Task task = todoData.toTaskEntity(); //
			task.setTodo(todo);//
			taskRepository.saveAndFlush(task);
			//追加完了メッセージとリダイレクト
			String msg = messageSource.getMessage("msg.i.task_created", null, locale);
			redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
			return "redirect:/todo/" + todo.getId();
		} else {
			//エラーありの場合
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			model.addAttribute("msg", new OpMsg("E", msg));
			return "todoForm";
		}
	}

	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo/query";
	}

	//更新　　IDで2つのテーブル連携している。Todo todoに両方とも入る。（9/13）
	@GetMapping("/todo/{id}")
	public ModelAndView todoById(@PathVariable(name = "id") int id, ModelAndView mv) {
		mv.setViewName("todoForm");
		//レポジトリを利用したIDでの検索(SELECT * FORM ~~ WHERE id =引数のidの値)と同義　レポジトリクラスはエンティティクラスを経由してtododbを参照するようになっている。
		Todo todo = todoRepository.findById(id).get();
		//		mv.addObject("todoData", todo);
		mv.addObject("todoData", new TodoData(todo)); //TodoData型のオブジェクトで渡す必要があるため変更（9/	13)
		session.setAttribute("mode", "update");
		return mv;

	}

	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model,
			RedirectAttributes redirectAttributes, Locale locale) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result, false, locale);
		if (!result.hasErrors() && isValid) {
			//エラーがない場合
			Todo todo = todoData.toEntity(); //エンティティオブジェクト
			todoRepository.saveAndFlush(todo);
			String msg = messageSource.getMessage("msg.i.todo_updated", null, locale);
			//９/11 リダイレクトの場合、Model経由はデータ保持のスコープ外。redirectAttributeを使用する
			//	model.addAttribute("msg", new OpMsg("I", msg));
			redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
			return "redirect:/todo/" + todo.getId(); //リスト一覧に戻す　//更新後は一覧に戻す＋検索内容の保持のために/todo/queryとしていたが、更新確認のために再度入力画面に戻すために変更している。
		} else {
			//エラーがあった場合、メッセージ添付
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			model.addAttribute("msg", new OpMsg("E", msg));
			return "todoForm";
		}
	}

	//削除
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData, RedirectAttributes redirectAttributes, Locale locale) {
		//更新画面の表示内容からIDを取得して、それに該当するデータ内容を削除する。
		String msg = messageSource.getMessage("msg.i.todo_deleted", null, locale);
		//９/11 リダイレクトの場合、Model経由はデータ保持のスコープ外。redirectAttributeを使用する
		redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}

	//Taskのみ削除
	@GetMapping("/task/delete")
	public String deleteTask(@RequestParam(name = "task_id") int taskId, @RequestParam(name = "todo_id") int todoId,
			RedirectAttributes redirectAttributes, Locale locale) {
		taskRepository.deleteById(taskId);

		String msg = messageSource.getMessage("msg.i.task_deleted", null, locale);
		redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
		return "redirect:/todo/" + todoId;
	}

	//検索
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery, BindingResult result,
			@PageableDefault(page = 0, size = 5) Pageable pageable, ModelAndView mv, Locale locale) { //検索フォーム内容はバインドされている
		mv.setViewName("todoList");
		//入力内容を独自チェックに欠ける
		//		List<Todo> todoList = null;
		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result, locale)) {
			//			todoList = todoService.doQuery(todoQuery); 
			//			todoPage = todoDaoImpl.findByJPQL(todoQuery); //①エラーがなければ実施　②todoListのServiceのクエリを実行、　③JPQLによる検索を実行
			todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable); //Criteriaによる検索　検索条件は上記と同じ。使用する方をのこす。
			session.setAttribute("todoQuery", todoQuery); //検索条件をセッションに保存しておく。Getメソッドのため、Modelを利用した保存はできない。
			mv.addObject("todoPage", todoPage); //ページ情報
			mv.addObject("todoList", todoPage.getContent());//検索結果
			//		mv.addObject("todoList", todoList); //mv.addObject("todoQuery",todoQuery);

			if (todoPage.getContent().size() == 0) { //該当がなかったらメッセージを表示する
				String msg = messageSource.getMessage("msg.w.todo_not_found", null, locale);
				mv.addObject("msg", new OpMsg("W", msg));
			}
		} else {
			//エラーがあった場合の検索
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			mv.addObject("todoPage", null);
			mv.addObject("todoList", null);
			mv.addObject("msg", new OpMsg("E", msg));
		}
		return mv;
	}

	//ページネーション用の表示メソッド
	@GetMapping("/todo/query")
	public ModelAndView queryTodo(@PageableDefault(page = 0, size = 5) Pageable pageable, ModelAndView mv) {
		mv.setViewName("todoList");
		//sessionに保存されている条件で検索
		TodoQuery todoQuery = (TodoQuery) session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
		mv.addObject("todoQuery", todoQuery); //検索条件表示用
		mv.addObject("todoPage", todoPage); //ページ情報
		mv.addObject("todoList", todoPage.getContent()); //検索結果
		return mv;
	}

	@PersistenceContext //EnitytyManagerのインスタンスを取得するアノテーション　@Autowiredとは作成するタイミングが異なるため記述必要
	private EntityManager entityManager;
	TodoDaoImpl todoDaoImpl;

	@PostConstruct //実行タイミングをコンストラクタや@PersistenceContextによる初期化終了後に設定。
	//TodoDaoImplを作成し、それを経由でEntityManagerを渡せるようになる
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}
}
