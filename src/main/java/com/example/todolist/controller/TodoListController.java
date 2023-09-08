package com.example.todolist.controller;

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
import org.springframework.web.servlet.ModelAndView;

import com.example.todolist.dao.TodoDaoImpl;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.service.TodoService;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

//todoテーブルを検索し、結果をhtmlに渡す

@Controller
@RequiredArgsConstructor //

public class TodoListController {
	private final HttpSession session;
	private final TodoRepository todoRepository;
	private final TodoService todoService; //データベース操作用で追加

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
		return mv;
	}

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
			ModelAndView mv) {
		boolean isValid = todoService.isValid(todoData, result);
		//serviceクラスのバリデーションもクリアしたら登録処理に移る
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			//saveAndFlushのメソッド処理で「カラム毎の内容をデータベースに登録」までをスプリングで実行してくれる。レポジトリクラスで実装しているから可能。
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo/query";
			//			return showTodoList(mv);　アドレスがtodo/createのままで一覧表示のメソッドを動かそうとすると、再読み込みが発生し、
			//実行するとブラウザは直前動作も一緒に実行するため「登録・画面表示」の二つが実行されてしまう。そのためリダイレクトで直接URLを移動してからメソッドを実行するように設定する。
		} else {
			//			mv.setViewName("todoForm");
			//＠ModelAttributeがあることでmv.addObject("todoData",todoData)の記述が不要となる。自動的に発生したデータを遷移先でも表示可能。しかし、第一引数は使用するモデルクラス（頭文字は小文字である必要がある。
			//エラー発生時はModelAndViewの機能は不要のため、ビューに渡すデータの保持（Model）を使用するだけで良い。
			return "todoForm";
		}
	}

	@PostMapping("/todo/cancel")
	public String cancel() {
		return "redirect:/todo/query";
	}

	@GetMapping("/todo/{id}")
	public ModelAndView todoById(@PathVariable(name = "id") int id, ModelAndView mv) {
		mv.setViewName("todoForm");
		//レポジトリを利用したIDでの検索(SELECT * FORM ~~ WHERE id =引数のidの値)と同義　レポジトリクラスはエンティティクラスを参照してtododbを参照するようになっている。
		Todo todo = todoRepository.findById(id).get();
		mv.addObject("todoData", todo);
		session.setAttribute("mode", "update");
		return mv;

	}

	//更新
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData, BindingResult result, Model model) {
		//エラーチェック
		boolean isValid = todoService.isValid(todoData, result);
		if (!result.hasErrors() && isValid) {
			//エラーがない場合
			Todo todo = todoData.toEntity(); //エンティティオブジェクト
			todoRepository.saveAndFlush(todo);
			return "redirect:/todo/query"; //リスト一覧に戻す
		} else {
			//エラーがあった場合、
			return "todoForm";
		}
	}

	//削除
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute TodoData todoData) {
		//更新画面の表示内容からIDを取得して、それに該当するデータ内容を削除する。
		todoRepository.deleteById(todoData.getId());
		return "redirect:/todo";
	}

	//検索
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery, BindingResult result,
			@PageableDefault(page = 0, size = 5) Pageable pageable, ModelAndView mv) { //検索フォーム内容はバインドされている
		mv.setViewName("todoList");
		//入力内容を独自チェックに欠ける
		//		List<Todo> todoList = null;
		Page<Todo> todoPage = null;
		if (todoService.isValid(todoQuery, result)) {
			//			todoList = todoService.doQuery(todoQuery); 
			//			todoPage = todoDaoImpl.findByJPQL(todoQuery); //①エラーがなければ実施　②todoListのServiceのクエリを実行、　③JPQLによる検索を実行
			todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable); //Criteriaによる検索　検索条件は上記と同じ。使用する方をのこす。

			//		mv.addObject("todoList", todoList); //mv.addObject("todoQuery",todoQuery);
			session.setAttribute("todoQuery", todoQuery); //検索条件をセッションに保存しておく。Getメソッドのため、Modelを利用した保存はできない。

			mv.addObject("todoPage", todoPage); //ページ情報
			mv.addObject("todoList", todoPage.getContent());//検索結果
		} else {
			//エラーがあった場合の検索
			mv.addObject("todoPage", null);
			mv.addObject("todoList", null);
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
