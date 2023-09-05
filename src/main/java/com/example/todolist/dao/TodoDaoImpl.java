package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {

	private final EntityManager entityManager;

	//JPQLの検索
	@Override
	public List<Todo> findByJPQL(TodoQuery todoQuery) {
		StringBuilder sb = new StringBuilder("select t from Todo t where 1 =1");
		List<Object> params = new ArrayList<>();
		int pos = 0;

		//実行JPQLの詳細 「select t from Todo t where 1 =1 ando TodoQueryオブジェクトから作成した検索条件　order by id」　という文字列
		//posで検索すべき項目のカウントをしている。
		//件名
		if (todoQuery.getTitle().length() > 0) {
			sb.append("and t.title like?" + (++pos)); //
			params.add("%" + todoQuery.getTitle() + "%"); //検索に使うパラメータをArrayList武ジェクトに保存
		}

		//重要度
		if (todoQuery.getImportance() != -1) {
			sb.append("and t.importance =?" + (++pos));
			params.add(todoQuery.getImportance());
		}

		//緊急度
		if (todoQuery.getUrgency() != -1) {
			sb.append("and t.urgency =?" + (++pos));
			params.add(todoQuery.getUrgency());
		}
		//期限：開始〜
		if (!todoQuery.getDeadlineFrom().equals("")) {
			sb.append("and t.deadline >=?" + (++pos)); //
			params.add(Utils.str2date(todoQuery.getDeadlineFrom()));
		}

		//〜期限：終了で検索
		if (!todoQuery.getDeadlineTo().equals("")) {
			sb.append("and t.deadline <=?" + (++pos)); //
			params.add(Utils.str2date(todoQuery.getDeadlineTo()));
		}

		//完了
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			sb.append("and t.done =?" + (++pos)); //
			params.add(todoQuery.getDone());
		}
		//order
		sb.append("order by id");

		Query query = entityManager.createQuery(sb.toString()); //作成したJPQLを引数にしたQueryオブジェを作成＝SQLのSELECT文　entityManagerは各種メソッドを内包している
		for (int i = 0; i < params.size(); ++i) {
			query = query.setParameter(i + 1, params.get(i));//
		}
		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList(); //取り出した検索を実行。　０〜n件
		return list;
	}

	//	//Criteria APIによる検索
	@Override
	public List<Todo> findByCriteria(TodoQuery todoaQuery) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
